package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.util.RedstoneConfig;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record RedstoneConfigPacket(BlockPos pos, RedstoneConfig config) implements CustomPacketPayload {
    public static final Type<RedstoneConfigPacket> TYPE = new Type<>(Util.loc("redstone_config"));

    public static final StreamCodec<FriendlyByteBuf, RedstoneConfigPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RedstoneConfigPacket::pos,
            RedstoneConfig.STREAM_CODEC, RedstoneConfigPacket::config,
            RedstoneConfigPacket::new
    );

    public void handle(IPayloadContext ctx) {
        var level = ctx.player().level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof BaseBlockEntity te) {
            te.redstoneConfig(config);
            if (!level.isClientSide()) {
                te.setChanged();
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, ChunkPos.containing(pos), this);
            }
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
