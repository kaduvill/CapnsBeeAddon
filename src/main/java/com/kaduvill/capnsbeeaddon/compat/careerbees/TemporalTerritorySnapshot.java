package com.kaduvill.capnsbeeaddon.compat.careerbees;

import com.kaduvill.capnsbeeaddon.compat.gendustry.TemporalFocusUpgradeHelper;
import com.kaduvill.capnsbeeaddon.compat.gendustry.TemporalFocusUpgradeHelper.TemporalFocusMode;
import com.kaduvill.capnsbeeaddon.mixin.careerbees.EffectAccelerationAccessor;
import com.rwtema.careerbees.effects.EffectAcceleration;
import com.rwtema.careerbees.effects.EffectBase;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.core.IErrorState;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class TemporalTerritorySnapshot {

    public static final int QUALIFIED_TILE_RENDER_CAP = 256;
    public static final int EXCLUDED_TILE_RENDER_CAP = 256;
    public static final int QUALIFIED_RANDOM_RENDER_CAP = 2048;
    public static final int EXCLUDED_RANDOM_RENDER_CAP = 512;
    private static final int ERROR_CAP = 8;

    private final int dimension;
    private final long source;
    private final Status status;
    private final TemporalFocusMode focusMode;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final int loadedChunks;
    private final int totalChunks;
    private final long inspectedTileEntries;
    private final long inspectedBlockPositions;
    private final long qualifiedTileCount;
    private final long excludedTileCount;
    private final long qualifiedRandomCount;
    private final long excludedRandomCount;
    private final long globallyRegisteredQualifiedTiles;
    private final boolean sourceRegisteredGlobally;
    private final long[] qualifiedTiles;
    private final long[] excludedTiles;
    private final long[] qualifiedRandomBlocks;
    private final long[] excludedRandomBlocks;
    private final String[] errors;

    private TemporalTerritorySnapshot(
            int dimension,
            long source,
            Status status,
            TemporalFocusMode focusMode,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ,
            int loadedChunks,
            int totalChunks,
            long inspectedTileEntries,
            long inspectedBlockPositions,
            long qualifiedTileCount,
            long excludedTileCount,
            long qualifiedRandomCount,
            long excludedRandomCount,
            long globallyRegisteredQualifiedTiles,
            boolean sourceRegisteredGlobally,
            long[] qualifiedTiles,
            long[] excludedTiles,
            long[] qualifiedRandomBlocks,
            long[] excludedRandomBlocks,
            String[] errors
    ) {
        this.dimension = dimension;
        this.source = source;
        this.status = status;
        this.focusMode = focusMode;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.loadedChunks = loadedChunks;
        this.totalChunks = totalChunks;
        this.inspectedTileEntries = inspectedTileEntries;
        this.inspectedBlockPositions = inspectedBlockPositions;
        this.qualifiedTileCount = qualifiedTileCount;
        this.excludedTileCount = excludedTileCount;
        this.qualifiedRandomCount = qualifiedRandomCount;
        this.excludedRandomCount = excludedRandomCount;
        this.globallyRegisteredQualifiedTiles =
                globallyRegisteredQualifiedTiles;
        this.sourceRegisteredGlobally = sourceRegisteredGlobally;
        this.qualifiedTiles = qualifiedTiles;
        this.excludedTiles = excludedTiles;
        this.qualifiedRandomBlocks = qualifiedRandomBlocks;
        this.excludedRandomBlocks = excludedRandomBlocks;
        this.errors = errors;
    }

    public static TemporalTerritorySnapshot unavailable(
            int dimension,
            long source
    ) {
        return empty(
                dimension,
                source,
                Status.SOURCE_UNAVAILABLE,
                TemporalFocusMode.NONE,
                0,
                0,
                0,
                0,
                0,
                0,
                new String[0]
        );
    }

    @Nonnull
    public static TemporalTerritorySnapshot create(
            @Nonnull WorldServer world,
            @Nonnull BlockPos source,
            @Nonnull IBeeHousing housing
    ) {
        int dimension = world.provider.getDimension();
        long packedSource = source.toLong();
        TemporalFocusMode focusMode =
                TemporalFocusUpgradeHelper.getFocusMode(housing);
        IBeeHousingInventory inventory = housing.getBeeInventory();

        if (inventory == null) {
            return emptySource(
                    dimension,
                    packedSource,
                    Status.NO_TEMPORAL_QUEEN,
                    focusMode
            );
        }

        ItemStack queenStack = inventory.getQueen();
        if (queenStack.isEmpty()) {
            return emptySource(
                    dimension,
                    packedSource,
                    Status.NO_TEMPORAL_QUEEN,
                    focusMode
            );
        }
        IBee bee = BeeManager.beeRoot.getMember(queenStack);
        if (bee == null
                || BeeManager.beeRoot.getType(queenStack)
                != EnumBeeType.QUEEN
                || bee.getHealth() <= 0) {
            return emptySource(
                    dimension,
                    packedSource,
                    Status.NO_TEMPORAL_QUEEN,
                    focusMode
            );
        }

        IBeeGenome genome = bee.getGenome();
        IAlleleBeeEffect effect = genome.getEffect();
        if (!(effect instanceof EffectAcceleration)) {
            return emptySource(
                    dimension,
                    packedSource,
                    Status.NOT_TEMPORAL,
                    focusMode
            );
        }

        if (!((EffectBase) effect).isValidSpecies(genome)) {
            return emptySource(
                    dimension,
                    packedSource,
                    Status.INVALID_TEMPORAL_SPECIES,
                    focusMode
            );
        }

        Vec3d territory = EffectBase.getTerritory(genome, housing);
        int radiusX = MathHelper.floor(territory.x);
        int radiusY = MathHelper.floor(territory.y);
        int radiusZ = MathHelper.floor(territory.z);
        int minX = source.getX() - radiusX;
        int minY = source.getY() - radiusY;
        int minZ = source.getZ() - radiusZ;
        int maxX = source.getX() + radiusX;
        int maxY = source.getY() + radiusY;
        int maxZ = source.getZ() + radiusZ;

        if (focusMode == TemporalFocusMode.GROWTH) {
            minY = Math.max(0, minY);
            maxY = Math.min(255, maxY);
        }

        Set<IErrorState> errorStates =
                housing.getErrorLogic().getErrorStates();
        if (!errorStates.isEmpty()) {
            String[] errors = new String[Math.min(
                    errorStates.size(),
                    ERROR_CAP
            )];
            int index = 0;
            for (IErrorState error : errorStates) {
                if (index == errors.length) {
                    break;
                }
                errors[index++] = error.getUnlocalizedDescription();
            }
            Arrays.sort(errors);
            return empty(
                    dimension,
                    packedSource,
                    Status.INACTIVE,
                    focusMode,
                    minX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    maxZ,
                    errors
            );
        }

        return scan(
                world,
                source,
                focusMode,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

    private static TemporalTerritorySnapshot scan(
            WorldServer world,
            BlockPos source,
            TemporalFocusMode focusMode,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        TLongArrayList qualifiedTiles =
                new TLongArrayList(16);
        TLongArrayList excludedTiles =
                new TLongArrayList(16);
        TLongArrayList qualifiedRandom =
                new TLongArrayList(64);
        TLongArrayList excludedRandom =
                new TLongArrayList(32);

        TObjectIntHashMap<BlockPos> globalRegistrations = null;
        WeakHashMap<World, TObjectIntHashMap<BlockPos>> registrations =
                ((EffectAccelerationAccessor) (Object)
                        EffectAcceleration.INSTANCE)
                        .capnsbeeaddon$getRegisteredPositions();
        if (registrations != null) {
            globalRegistrations = registrations.get(world);
        }

        boolean sourceRegisteredGlobally = globalRegistrations != null
                && globalRegistrations.containsKey(source);
        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;
        long totalChunkCount = (long) (maxChunkX - minChunkX + 1)
                * (maxChunkZ - minChunkZ + 1);
        int totalChunks = (int) Math.min(Integer.MAX_VALUE, totalChunkCount);
        int loadedChunks = 0;
        long inspectedTileEntries = 0;
        long inspectedBlockPositions = 0;
        long qualifiedTileCount = 0;
        long excludedTileCount = 0;
        long qualifiedRandomCount = 0;
        long excludedRandomCount = 0;
        long globallyRegisteredQualifiedTiles = 0;
        BlockPos.MutableBlockPos scanPosition =
                new BlockPos.MutableBlockPos();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk chunk = world.getChunkProvider()
                        .getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                loadedChunks++;
                if (focusMode != TemporalFocusMode.GROWTH) {
                    for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
                        inspectedTileEntries++;
                        BlockPos targetPos = entry.getKey();
                        if (!inside(targetPos, minX, minY, minZ, maxX, maxY, maxZ
                        )) {continue;
                        }
                        if (targetPos.equals(source)) {
                            continue;
                        }

                        TileEntity tile = entry.getValue();
                        boolean qualified;
                        if (focusMode == TemporalFocusMode.NONE) {
                            IBlockState state = chunk.getBlockState(targetPos);
                            qualified = tile instanceof ITickable && !state.getBlock().isAir(
                                    state, world, targetPos
                            );
                        } else {
                            qualified = TemporalFocusUpgradeHelper.isEligibleTileTarget(focusMode, tile, targetPos, source
                            );
                        }

                        if (qualified) {
                            qualifiedTileCount++;
                            addCapped(qualifiedTiles, QUALIFIED_TILE_RENDER_CAP, targetPos.toLong()
                            );
                            if (globalRegistrations != null
                                    && globalRegistrations.containsKey(targetPos)) {
                                globallyRegisteredQualifiedTiles++;
                            }
                        } else {
                            excludedTileCount++;
                            addCapped(excludedTiles, EXCLUDED_TILE_RENDER_CAP, targetPos.toLong()
                            );
                        }
                    }
                }

                if (TemporalFocusUpgradeHelper.isTileFocus(focusMode)) {
                    continue;
                }

                int scanMinX = Math.max(minX, chunkX << 4);
                int scanMaxX = Math.min(maxX, (chunkX << 4) + 15);
                int scanMinZ = Math.max(minZ, chunkZ << 4);
                int scanMaxZ = Math.min(maxZ, (chunkZ << 4) + 15);
                int scanMinY = Math.max(0, minY);
                int scanMaxY = Math.min(255, maxY);
                ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();

                for (int sectionIndex = scanMinY >> 4;
                     scanMinY <= scanMaxY && sectionIndex <= scanMaxY >> 4;
                     sectionIndex++) {
                    ExtendedBlockStorage section = sections[sectionIndex];
                    if (section == null || !section.needsRandomTick()) {
                        continue;
                    }

                    int sectionMinY = Math.max(scanMinY, sectionIndex << 4);
                    int sectionMaxY = Math.min(scanMaxY, (sectionIndex << 4) + 15);

                    for (int x = scanMinX; x <= scanMaxX; x++) {
                        for (int z = scanMinZ; z <= scanMaxZ; z++) {
                            for (int y = sectionMinY;
                                 y <= sectionMaxY;
                                 y++) {
                                inspectedBlockPositions++;
                                IBlockState state = section.get(x & 15, y & 15, z & 15);
                                if (!TemporalFocusUpgradeHelper
                                        .isRandomTickTarget(state)) {
                                    continue;
                                }

                                scanPosition.setPos(x, y, z);
                                if (focusMode == TemporalFocusMode.NONE && state.getBlock().isAir(state, world, scanPosition
                                )) {continue;
                                }

                                long packed = pack(x, y, z);
                                if (focusMode == TemporalFocusMode.NONE || focusMode == TemporalFocusMode.GROWTH) {
                                    qualifiedRandomCount++;
                                    addCapped(qualifiedRandom, QUALIFIED_RANDOM_RENDER_CAP, packed
                                    );
                                } else {
                                    excludedRandomCount++;
                                    addCapped(excludedRandom, EXCLUDED_RANDOM_RENDER_CAP, packed
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        return new TemporalTerritorySnapshot(world.provider.getDimension(), source.toLong(), Status.ACTIVE, focusMode,
                minX, minY, minZ, maxX, maxY, maxZ, loadedChunks, totalChunks, inspectedTileEntries, inspectedBlockPositions,
                qualifiedTileCount, excludedTileCount, qualifiedRandomCount, excludedRandomCount, globallyRegisteredQualifiedTiles, sourceRegisteredGlobally,
                qualifiedTiles.toArray(), excludedTiles.toArray(), qualifiedRandom.toArray(), excludedRandom.toArray(), new String[0]
        );
    }

    private static TemporalTerritorySnapshot emptySource(
            int dimension, long source, Status status, TemporalFocusMode focusMode
    ) {
        return empty(dimension, source, status, focusMode, 0, 0, 0, 0, 0, 0, new String[0]
        );
    }

    private static TemporalTerritorySnapshot empty(
            int dimension, long source, Status status, TemporalFocusMode focusMode, int minX, int minY,
            int minZ, int maxX, int maxY, int maxZ, String[] errors
    ) {
        return new TemporalTerritorySnapshot(dimension, source, status, focusMode, minX, minY, minZ,
                maxX, maxY, maxZ, 0, 0, 0, 0, 0,
                0, 0, 0, 0, false, new long[0], new long[0],
                new long[0], new long[0], errors
        );
    }

    private static boolean inside(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ
    ) {
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    private static void addCapped(
            TLongArrayList positions,
            int cap,
            long position
    ) {
        if (positions.size() < cap) {
            positions.add(position);
        }
    }

    private static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | (long) z & 0x3FFFFFFL;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeLong(source);
        buffer.writeByte(status.ordinal());
        buffer.writeByte(focusMode.ordinal());
        buffer.writeInt(minX);
        buffer.writeInt(minY);
        buffer.writeInt(minZ);
        buffer.writeInt(maxX);
        buffer.writeInt(maxY);
        buffer.writeInt(maxZ);
        buffer.writeInt(loadedChunks);
        buffer.writeInt(totalChunks);
        buffer.writeLong(inspectedTileEntries);
        buffer.writeLong(inspectedBlockPositions);
        buffer.writeLong(qualifiedTileCount);
        buffer.writeLong(excludedTileCount);
        buffer.writeLong(qualifiedRandomCount);
        buffer.writeLong(excludedRandomCount);
        buffer.writeLong(globallyRegisteredQualifiedTiles);
        buffer.writeBoolean(sourceRegisteredGlobally);
        writePositions(buffer, qualifiedTiles);
        writePositions(buffer, excludedTiles);
        writePositions(buffer, qualifiedRandomBlocks);
        writePositions(buffer, excludedRandomBlocks);
        buffer.writeByte(errors.length);
        for (String error : errors) {
            ByteBufUtils.writeUTF8String(buffer, error);
        }
    }

    public static TemporalTerritorySnapshot fromBytes(ByteBuf buffer) {
        int dimension = buffer.readInt();
        long source = buffer.readLong();
        Status status = readEnum(buffer.readUnsignedByte(), Status.values());
        TemporalFocusMode focusMode = readEnum(
                buffer.readUnsignedByte(),
                TemporalFocusMode.values()
        );
        int minX = buffer.readInt();
        int minY = buffer.readInt();
        int minZ = buffer.readInt();
        int maxX = buffer.readInt();
        int maxY = buffer.readInt();
        int maxZ = buffer.readInt();
        int loadedChunks = buffer.readInt();
        int totalChunks = buffer.readInt();
        long inspectedTileEntries = buffer.readLong();
        long inspectedBlockPositions = buffer.readLong();
        long qualifiedTileCount = buffer.readLong();
        long excludedTileCount = buffer.readLong();
        long qualifiedRandomCount = buffer.readLong();
        long excludedRandomCount = buffer.readLong();
        long globallyRegisteredQualifiedTiles = buffer.readLong();
        boolean sourceRegisteredGlobally = buffer.readBoolean();
        long[] qualifiedTiles = readPositions(buffer, QUALIFIED_TILE_RENDER_CAP);
        long[] excludedTiles = readPositions(buffer, EXCLUDED_TILE_RENDER_CAP);
        long[] qualifiedRandomBlocks = readPositions(buffer, QUALIFIED_RANDOM_RENDER_CAP);
        long[] excludedRandomBlocks = readPositions(buffer, EXCLUDED_RANDOM_RENDER_CAP);
        int errorCount = buffer.readUnsignedByte();
        if (errorCount > ERROR_CAP) {
            throw new IllegalArgumentException("Too many Temporal errors");
        }
        String[] errors = new String[errorCount];
        for (int i = 0; i < errorCount; i++) {
            errors[i] = ByteBufUtils.readUTF8String(buffer);
        }

        return new TemporalTerritorySnapshot(dimension, source, status, focusMode, minX, minY, minZ, maxX,
                maxY, maxZ, loadedChunks, totalChunks, inspectedTileEntries, inspectedBlockPositions, qualifiedTileCount, excludedTileCount,
                qualifiedRandomCount, excludedRandomCount, globallyRegisteredQualifiedTiles, sourceRegisteredGlobally, qualifiedTiles, excludedTiles, qualifiedRandomBlocks,
                excludedRandomBlocks, errors
        );
    }

    private static void writePositions(ByteBuf buffer, long[] positions) {
        buffer.writeInt(positions.length);
        for (long position : positions) {
            buffer.writeLong(position);
        }
    }

    private static long[] readPositions(ByteBuf buffer, int cap) {
        int length = buffer.readInt();
        if (length < 0 || length > cap) {
            throw new IllegalArgumentException(
                    "Invalid Temporal position count: " + length
            );
        }
        long[] positions = new long[length];
        for (int i = 0; i < length; i++) {
            positions[i] = buffer.readLong();
        }
        return positions;
    }

    private static <T> T readEnum(int ordinal, T[] values) {
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid Temporal enum ordinal: " + ordinal);
        }
        return values[ordinal];
    }

    public int getDimension() {return dimension;}
    public long getSource() {return source;}
    public Status getStatus() {return status;}
    public TemporalFocusMode getFocusMode() {return focusMode;}
    public int getMinX() {return minX;}
    public int getMinY() {return minY;}
    public int getMinZ() {return minZ;}
    public int getMaxX() {return maxX;}
    public int getMaxY() {return maxY;}
    public int getMaxZ() {return maxZ;}
    public int getLoadedChunks() {return loadedChunks;}
    public int getTotalChunks() {return totalChunks;}
    public long getInspectedTileEntries() {return inspectedTileEntries;}
    public long getInspectedBlockPositions() {return inspectedBlockPositions;}
    public long getQualifiedTileCount() {return qualifiedTileCount;}
    public long getExcludedTileCount() {return excludedTileCount;}
    public long getQualifiedRandomCount() {return qualifiedRandomCount;}
    public long getExcludedRandomCount() {return excludedRandomCount;}
    public long getGloballyRegisteredQualifiedTiles() {return globallyRegisteredQualifiedTiles;}
    public boolean isSourceRegisteredGlobally() {return sourceRegisteredGlobally;}
    public long[] getQualifiedTiles() {return qualifiedTiles;}
    public long[] getExcludedTiles() {return excludedTiles;}
    public long[] getQualifiedRandomBlocks() {return qualifiedRandomBlocks;}
    public long[] getExcludedRandomBlocks() {return excludedRandomBlocks;}
    public String[] getErrors() {return errors;}
    public boolean hasTerritory() {return status == Status.ACTIVE || status == Status.INACTIVE;}

    public enum Status {
        SOURCE_UNAVAILABLE,
        NO_TEMPORAL_QUEEN,
        NOT_TEMPORAL,
        INVALID_TEMPORAL_SPECIES,
        INACTIVE,
        ACTIVE
    }
}