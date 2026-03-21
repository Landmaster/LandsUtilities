package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.Optional;

public record IOConfigPacket(BlockPos pos, String key, Optional<Direction> direction) implements CustomPacketPayload {
    public static final Type<IOConfigPacket> TYPE = new Type<>(Util.loc("io_config"));

    public static final StreamCodec<ByteBuf, IOConfigPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, IOConfigPacket::pos,
            ByteBufCodecs.STRING_UTF8, IOConfigPacket::key,
            ByteBufCodecs.optional(Direction.STREAM_CODEC), IOConfigPacket::direction,
            IOConfigPacket::new
    );

    public void handle(IPayloadContext ctx) {
        var level = ctx.player().level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof BaseBlockEntity te) {
            if (te.setConfiguration(key, direction.orElse(null)) && !level.isClientSide) {
                te.setChanged();
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), this);
            }
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
