package com.kaduvill.capnsbeeaddon.network;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerTerritorySupport;
import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerTerritoryInfo;
import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerTerritoryResolver;
import com.kaduvill.capnsbeeaddon.compat.careerbees.TemporalTerritorySnapshot;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import forestry.api.apiculture.IBeeHousing;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

public final class CapnsBeeAddonNetwork {

    private static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(CapnsBeeAddon.MODID);
    private static final int REQUEST_INTERVAL_TICKS = 20;
    // Shared across basic/advanced requests; accessed only on the server thread.
    private static final Map<EntityPlayerMP, Integer> LAST_REQUEST_TICKS = new WeakHashMap<>();

    private CapnsBeeAddonNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(
                TemporalRequest.Handler.class,
                TemporalRequest.class,
                0,
                Side.SERVER
        );
        CHANNEL.registerMessage(
                TemporalResponse.Handler.class,
                TemporalResponse.class,
                1,
                Side.CLIENT
        );
        CHANNEL.registerMessage(BasicBoundaryRequest.Handler.class,
                BasicBoundaryRequest.class, 2, Side.SERVER);
        CHANNEL.registerMessage(BasicBoundaryResponse.Handler.class,
                BasicBoundaryResponse.class, 3, Side.CLIENT);
    }

    public static void requestTemporalSnapshot(
            int dimension,
            long source
    ) {
        CHANNEL.sendToServer(new TemporalRequest(dimension, source));
    }

    public static void requestBasicBoundary(int dimension, long source, int generation) {
        CHANNEL.sendToServer(new BasicBoundaryRequest(dimension, source, generation));
    }

    private static boolean allowRequest(EntityPlayerMP player) {
        int tick = player.getServer().getTickCounter();
        Integer lastTick = LAST_REQUEST_TICKS.get(player);
        if (lastTick != null) {
            int elapsed = tick - lastTick;
            if (elapsed >= 0 && elapsed < REQUEST_INTERVAL_TICKS) {
                return false;
            }
        }
        LAST_REQUEST_TICKS.put(player, tick);
        return true;
    }

    @Nullable
    private static IBeeHousing getEnabledHousing(EntityPlayerMP player, int dimension, long position) {
        WorldServer world = player.getServerWorld();
        BlockPos source = BlockPos.fromLong(position);
        if (world.provider.getDimension() != dimension
                || world.getChunkProvider().getLoadedChunk(source.getX() >> 4, source.getZ() >> 4) == null) {
            return null;
        }
        TileEntity tile = world.getTileEntity(source);
        if (!(tile instanceof IBeeHousing) || tile.isInvalid()) {
            return null;
        }
        IBeeHousing housing = (IBeeHousing) tile;
        return CareerTerritorySupport.isEnabled(housing)
                && housing.getWorldObj() == world && source.equals(housing.getCoordinates())
                ? housing : null;
    }

    private static void postToClient(MessageContext context, Event event) {
        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
        thread.addScheduledTask(() -> MinecraftForge.EVENT_BUS.post(event));
    }

    public static final class TemporalRequest implements IMessage {

        private int dimension;
        private long source;

        public TemporalRequest() {
        }

        private TemporalRequest(int dimension, long source) {
            this.dimension = dimension;
            this.source = source;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            dimension = buffer.readInt();
            source = buffer.readLong();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(dimension);
            buffer.writeLong(source);
        }

        public static final class Handler
                implements IMessageHandler<TemporalRequest, IMessage> {

            @Override
            public IMessage onMessage(
                    TemporalRequest message,
                    MessageContext context
            ) {
                EntityPlayerMP player = context.getServerHandler().player;
                player.getServerWorld().addScheduledTask(
                        () -> handle(message, player)
                );
                return null;
            }

            private static void handle(
                    TemporalRequest message,
                    EntityPlayerMP player
            ) {
                if (!allowRequest(player)) {
                    return;
                }

                if (!CapnsBeeAddonConfig.get().isAdvancedTemporalOverlayEnabled()) {
                    sendUnavailable(player, message.dimension, message.source);
                    return;
                }

                IBeeHousing housing = getEnabledHousing(player, message.dimension, message.source);
                if (housing == null) {
                    sendUnavailable(player, message.dimension, message.source);
                    return;
                }

                CHANNEL.sendTo(
                        new TemporalResponse(
                                TemporalTerritorySnapshot.create(
                                        player.getServerWorld(),
                                        BlockPos.fromLong(message.source),
                                        housing
                                )
                        ),
                        player
                );
            }

            private static void sendUnavailable(
                    EntityPlayerMP player,
                    int dimension,
                    long source
            ) {
                CHANNEL.sendTo(
                        new TemporalResponse(
                                TemporalTerritorySnapshot.unavailable(
                                        dimension,
                                        source
                                )
                        ),
                        player
                );
            }
        }
    }

    public static final class TemporalResponse implements IMessage {

        private TemporalTerritorySnapshot snapshot;

        public TemporalResponse() {
        }

        private TemporalResponse(TemporalTerritorySnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            snapshot = TemporalTerritorySnapshot.fromBytes(buffer);
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            snapshot.toBytes(buffer);
        }

        public static final class Handler
                implements IMessageHandler<TemporalResponse, IMessage> {

            @Override
            public IMessage onMessage(
                    TemporalResponse message,
                    MessageContext context
            ) {
                postToClient(context, new TemporalSnapshotEvent(message.snapshot));
                return null;
            }
        }
    }

    public static final class TemporalSnapshotEvent extends Event {

        private final TemporalTerritorySnapshot snapshot;

        private TemporalSnapshotEvent(
                TemporalTerritorySnapshot snapshot
        ) {
            this.snapshot = snapshot;
        }

        public TemporalTerritorySnapshot getSnapshot() {
            return snapshot;
        }
    }

    public static final class BasicBoundaryRequest implements IMessage {
        private int dimension;
        private long source;
        private int generation;

        public BasicBoundaryRequest() {
        }

        BasicBoundaryRequest(int dimension, long source, int generation) {
            this.dimension = dimension;
            this.source = source;
            this.generation = generation;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            dimension = buffer.readInt();
            source = buffer.readLong();
            generation = buffer.readInt();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(dimension);
            buffer.writeLong(source);
            buffer.writeInt(generation);
        }

        public static final class Handler implements IMessageHandler<BasicBoundaryRequest, IMessage> {
            @Override
            public IMessage onMessage(BasicBoundaryRequest message, MessageContext context) {
                EntityPlayerMP player = context.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    if (!allowRequest(player)) {
                        return;
                    }
                    IBeeHousing housing = CapnsBeeAddonConfig.get().forestryApiaryTerritory
                            ? getEnabledHousing(player, message.dimension, message.source) : null;
                    CareerTerritoryInfo boundary = housing != null
                            && CareerTerritorySupport.needsServerBoundary(housing)
                            ? CareerTerritoryResolver.resolve(housing) : null;
                    CHANNEL.sendTo(new BasicBoundaryResponse(message.dimension, message.source,
                            message.generation, boundary), player);
                });
                return null;
            }
        }
    }

    public static final class BasicBoundaryResponse implements IMessage {
        private int dimension;
        private long source;
        private int generation;
        @Nullable
        private CareerTerritoryInfo boundary;

        public BasicBoundaryResponse() {
        }

        BasicBoundaryResponse(int dimension, long source, int generation,
                              @Nullable CareerTerritoryInfo boundary) {
            this.dimension = dimension;
            this.source = source;
            this.generation = generation;
            this.boundary = boundary;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            dimension = buffer.readInt();
            source = buffer.readLong();
            generation = buffer.readInt();
            boundary = null;
            if (!buffer.readBoolean()) {
                return;
            }
            double minX = buffer.readDouble();
            double minY = buffer.readDouble();
            double minZ = buffer.readDouble();
            double maxX = buffer.readDouble();
            double maxY = buffer.readDouble();
            double maxZ = buffer.readDouble();
            String effectKey = ByteBufUtils.readUTF8String(buffer);
            boolean blockAligned = buffer.readBoolean();
            boolean temporal = buffer.readBoolean();
            if (Double.isFinite(minX) && Double.isFinite(minY) && Double.isFinite(minZ)
                    && Double.isFinite(maxX) && Double.isFinite(maxY) && Double.isFinite(maxZ)
                    && minX <= maxX && minY <= maxY && minZ <= maxZ) {
                boundary = new CareerTerritoryInfo(dimension, BlockPos.fromLong(source),
                        new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ),
                        effectKey, blockAligned, temporal);
            }
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(dimension);
            buffer.writeLong(source);
            buffer.writeInt(generation);
            buffer.writeBoolean(boundary != null);
            if (boundary != null) {
                AxisAlignedBB box = boundary.getBounds();
                buffer.writeDouble(box.minX);
                buffer.writeDouble(box.minY);
                buffer.writeDouble(box.minZ);
                buffer.writeDouble(box.maxX);
                buffer.writeDouble(box.maxY);
                buffer.writeDouble(box.maxZ);
                ByteBufUtils.writeUTF8String(buffer, boundary.getEffectTranslationKey());
                buffer.writeBoolean(boundary.isBlockAligned());
                buffer.writeBoolean(boundary.isTemporal());
            }
        }

        public static final class Handler implements IMessageHandler<BasicBoundaryResponse, IMessage> {
            @Override
            public IMessage onMessage(BasicBoundaryResponse message, MessageContext context) {
                postToClient(context, new BasicBoundaryEvent(message));
                return null;
            }
        }
    }

    public static final class BasicBoundaryEvent extends Event {
        public final int dimension;
        public final long source;
        public final int generation;
        @Nullable
        public final CareerTerritoryInfo boundary;

        BasicBoundaryEvent(BasicBoundaryResponse response) {
            dimension = response.dimension;
            source = response.source;
            generation = response.generation;
            boundary = response.boundary;
        }
    }
}
