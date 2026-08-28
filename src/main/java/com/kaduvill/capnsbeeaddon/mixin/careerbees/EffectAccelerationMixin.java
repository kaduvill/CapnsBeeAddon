package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.kaduvill.capnsbeeaddon.compat.careerbees.FocusedScanState;
import com.kaduvill.capnsbeeaddon.compat.gendustry.TemporalFocusUpgradeHelper;
import com.kaduvill.capnsbeeaddon.compat.gendustry.TemporalFocusUpgradeHelper.TemporalFocusMode;
import com.rwtema.careerbees.effects.EffectAcceleration;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(value = EffectAcceleration.class, remap = false)
public abstract class EffectAccelerationMixin {

    @Shadow
    @Final
    private WeakHashMap<World, TObjectIntHashMap<BlockPos>> posToTick;

    @Shadow
    private boolean processing;

    @Unique
    private final WeakHashMap<World, FocusedScanState>
            capnsbeeaddon$focusedScanStates = new WeakHashMap<>();

    @Inject(
            method = "doEffectBase",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void capnsbeeaddon$applyFocusedScan(
            IBeeGenome genome,
            IEffectData storedData,
            IBeeHousing housing,
            IEffectSettingsHolder settings,
            CallbackInfoReturnable<IEffectData> cir
    ) {
        // Keep Career Bees' cheap gates ahead of Gendustry's upgrade-list read.
        if (processing) {
            cir.setReturnValue(storedData);
            return;
        }

        World world = housing.getWorldObj();
        if (world.isRemote) {
            cir.setReturnValue(storedData);
            return;
        }

        long worldTime = world.getTotalWorldTime();
        if ((worldTime % 20L) != 0L) {
            cir.setReturnValue(storedData);
            return;
        }

        TemporalFocusMode mode =
                TemporalFocusUpgradeHelper.getFocusMode(housing);

        if (mode == TemporalFocusMode.NONE) {
            // Leave Career Bees completely unchanged without a focus upgrade.
            return;
        }

        // Focused scans require loaded-chunk access without fallback loading.
        if (!(world instanceof WorldServer)) {
            cir.setReturnValue(storedData);
            return;
        }

        BlockPos source = housing.getCoordinates().toImmutable();
        boolean registersTileTargets = TemporalFocusUpgradeHelper.isTileFocus(mode);
        TObjectIntHashMap<BlockPos> existingTargets = posToTick.get(world);

        // Preserve Career Bees' source anti-cascade behavior for every focus.
        if (existingTargets != null && existingTargets.containsKey(source)) {
            cir.setReturnValue(storedData);
            return;
        }

        FocusedScanState scanState =
                capnsbeeaddon$focusedScanStates.computeIfAbsent(
                        world,
                        ignored -> new FocusedScanState()
                );

        if (!scanState.markScanned(worldTime, source)) {
            cir.setReturnValue(storedData);
            return;
        }

        try {
            processing = true;

            if (registersTileTargets) {
                TObjectIntHashMap<BlockPos> targets = existingTargets;
                if (targets == null) {
                    targets = posToTick.computeIfAbsent(
                            world,
                            ignored -> new TObjectIntHashMap<>()
                    );
                }
                capnsbeeaddon$registerTileTargets(
                        (WorldServer) world,
                        source,
                        genome,
                        housing,
                        mode,
                        targets
                );
            } else if (mode == TemporalFocusMode.GROWTH) {
                capnsbeeaddon$scheduleGrowthTargets(
                        (WorldServer) world,
                        source,
                        genome,
                        housing
                );
            }
        } finally {
            processing = false;
        }

        cir.setReturnValue(storedData);
    }

    @Inject(
            method = "handleBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void capnsbeeaddon$filterDirectBlockTarget(
            World world,
            BlockPos pos,
            EnumFacing facing,
            IBeeGenome genome,
            IBeeHousing housing,
            CallbackInfoReturnable<Boolean> cir
    ) {
        TemporalFocusMode mode =
                TemporalFocusUpgradeHelper.getFocusMode(housing);

        if (mode == TemporalFocusMode.NONE) {
            return;
        }

        if (!world.isRemote && world.isBlockLoaded(pos, false)) {
            if (TemporalFocusUpgradeHelper.isTileFocus(mode)) {
                TileEntity tile = world.getTileEntity(pos);

                if (TemporalFocusUpgradeHelper.isEligibleTileTarget(
                        mode,
                        tile,
                        pos,
                        housing.getCoordinates()
                )) {
                    TObjectIntHashMap<BlockPos> targets =
                            posToTick.computeIfAbsent(
                                    world,
                                    ignored -> new TObjectIntHashMap<>()
                            );

                    targets.put(pos.toImmutable(), 40);
                }
            } else if (mode == TemporalFocusMode.GROWTH) {
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();

                if (TemporalFocusUpgradeHelper.isRandomTickTarget(state)) {
                    world.scheduleUpdate(
                            pos.toImmutable(),
                            block,
                            1
                    );
                }
            }
        }

        // Preserve EffectAcceleration.handleBlock()'s normal return value.
        cir.setReturnValue(true);
    }

    @Unique
    private void capnsbeeaddon$registerTileTargets(
            WorldServer world,
            BlockPos source,
            IBeeGenome genome,
            IBeeHousing housing,
            TemporalFocusMode mode,
            TObjectIntHashMap<BlockPos> targets
    ) {
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

        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX;
             chunkX <= maxChunkX;
             chunkX++) {

            for (int chunkZ = minChunkZ;
                 chunkZ <= maxChunkZ;
                 chunkZ++) {

                Chunk chunk = world.getChunkProvider()
                        .getLoadedChunk(chunkX, chunkZ);

                if (chunk == null) {
                    continue;
                }

                for (Map.Entry<BlockPos, TileEntity> entry
                        : chunk.getTileEntityMap().entrySet()) {

                    BlockPos targetPos = entry.getKey();

                    if (targetPos.getX() < minX
                            || targetPos.getX() > maxX
                            || targetPos.getY() < minY
                            || targetPos.getY() > maxY
                            || targetPos.getZ() < minZ
                            || targetPos.getZ() > maxZ) {
                        continue;
                    }

                    if (TemporalFocusUpgradeHelper.isEligibleTileTarget(
                            mode,
                            entry.getValue(),
                            targetPos,
                            source
                    )) {
                        targets.putIfAbsent(targetPos.toImmutable(), 40);
                    }
                }
            }
        }
    }

    @Unique
    private void capnsbeeaddon$scheduleGrowthTargets(
            WorldServer world,
            BlockPos source,
            IBeeGenome genome,
            IBeeHousing housing
    ) {
        Vec3d territory = EffectBase.getTerritory(genome, housing);

        int radiusX = MathHelper.floor(territory.x);
        int radiusY = MathHelper.floor(territory.y);
        int radiusZ = MathHelper.floor(territory.z);

        int minX = source.getX() - radiusX;
        int minY = Math.max(0, source.getY() - radiusY);
        int minZ = source.getZ() - radiusZ;

        int maxX = source.getX() + radiusX;
        int maxY = Math.min(255, source.getY() + radiusY);
        int maxZ = source.getZ() + radiusZ;

        if (minY > maxY) {
            return;
        }

        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;

        BlockPos.MutableBlockPos position =
                new BlockPos.MutableBlockPos();

        for (int chunkX = minChunkX;
             chunkX <= maxChunkX;
             chunkX++) {

            int chunkMinX = chunkX << 4;
            int scanMinX = Math.max(minX, chunkMinX);
            int scanMaxX = Math.min(maxX, chunkMinX + 15);

            for (int chunkZ = minChunkZ;
                 chunkZ <= maxChunkZ;
                 chunkZ++) {

                Chunk chunk = world.getChunkProvider()
                        .getLoadedChunk(chunkX, chunkZ);

                if (chunk == null) {
                    continue;
                }

                int chunkMinZ = chunkZ << 4;
                int scanMinZ = Math.max(minZ, chunkMinZ);
                int scanMaxZ = Math.min(maxZ, chunkMinZ + 15);

                for (int x = scanMinX; x <= scanMaxX; x++) {
                    for (int z = scanMinZ; z <= scanMaxZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            position.setPos(x, y, z);

                            IBlockState state =
                                    chunk.getBlockState(position);

                            Block block = state.getBlock();

                            if (TemporalFocusUpgradeHelper.isRandomTickTarget(
                                    state
                            )) {
                                world.scheduleUpdate(
                                        position.toImmutable(),
                                        block,
                                        1
                                );
                            }
                        }
                    }
                }
            }
        }
    }

}