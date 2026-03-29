package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.util.Util;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SyncMapPacket<T> implements CustomPacketPayload {
    public static final Type<SyncMapPacket<?>> TYPE = new Type<>(Util.loc("sync_map"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMapPacket<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncMapPacket<?> decode(RegistryFriendlyByteBuf input) {
            int id = input.readVarInt();
            var blockPos = input.readBlockPos();
            var auxBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), input.registryAccess(), input.getConnectionType());
            auxBuf.writeBytes(input);
            return new SyncMapPacket<>(id, blockPos, auxBuf);
        }

        public void encode(RegistryFriendlyByteBuf output, SyncMapPacket<?> value) {
            output.writeVarInt(value.id);
            output.writeBlockPos(value.pos);
            var auxBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), output.registryAccess(), output.getConnectionType());
            ((StreamCodec) value.streamCodec).encode(auxBuf, value.value);
            output.writeBytes(auxBuf);
        }
    };

    private final int id;
    private final BlockPos pos;
    private T value;
    private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
    private RegistryFriendlyByteBuf buf;

    public SyncMapPacket(int id, BlockPos pos, T value, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.id = id;
        this.pos = pos;
        this.value = value;
        this.streamCodec = streamCodec;
    }

    private SyncMapPacket(int id, BlockPos pos, RegistryFriendlyByteBuf buf) {
        this.id = id;
        this.pos = pos;
        this.buf = buf;
    }

    public void handle(IPayloadContext context) {
        var level = context.player().level();
        if (!level.isLoaded(pos)) {
            return;
        }
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseBlockEntity baseBlockEntity) {
            var syncMap = baseBlockEntity.syncMap();
            var streamCodec = syncMap.streamCodecAt(id);
            if (syncMap.set(id, streamCodec.decode(buf)) && !level.isClientSide()) {
                blockEntity.setChanged();
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, ChunkPos.containing(pos), syncMap.generatePacket(id, pos));
            }
        }
    }

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
