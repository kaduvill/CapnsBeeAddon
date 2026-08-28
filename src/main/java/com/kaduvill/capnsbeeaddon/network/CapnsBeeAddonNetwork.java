package com.kaduvill.capnsbeeaddon.network;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.careerbees.TemporalTerritorySnapshot;
import forestry.api.apiculture.IBeeHousing;
import io.netty.buffer.ByteBuf;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

public final class CapnsBeeAddonNetwork {

    private static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(CapnsBeeAddon.MODID);

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
    }

    public static void requestTemporalSnapshot(
            int dimension,
            long source
    ) {
        CHANNEL.sendToServer(new TemporalRequest(dimension, source));
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
                WorldServer world = player.getServerWorld();
                if (world.provider.getDimension() != message.dimension) {
                    sendUnavailable(player, message.dimension, message.source);
                    return;
                }

                BlockPos source = BlockPos.fromLong(message.source);
                if (world.getChunkProvider().getLoadedChunk(
                        source.getX() >> 4,
                        source.getZ() >> 4
                ) == null) {
                    sendUnavailable(player, message.dimension, message.source);
                    return;
                }

                TileEntity tile = world.getTileEntity(source);
                if (!(tile instanceof IIndustrialApiary)
                        || tile.isInvalid()) {
                    sendUnavailable(player, message.dimension, message.source);
                    return;
                }

                IBeeHousing housing = (IBeeHousing) tile;
                if (housing.getWorldObj() != world
                        || !source.equals(housing.getCoordinates())) {
                    sendUnavailable(player, message.dimension, message.source);
                    return;
                }

                CHANNEL.sendTo(
                        new TemporalResponse(
                                TemporalTerritorySnapshot.create(
                                        world,
                                        source,
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
                IThreadListener thread = FMLCommonHandler.instance()
                        .getWorldThread(context.netHandler);
                thread.addScheduledTask(
                        () -> MinecraftForge.EVENT_BUS.post(
                                new TemporalSnapshotEvent(message.snapshot)
                        )
                );
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
}