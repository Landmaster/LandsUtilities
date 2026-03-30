package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.block.entity.XPInterfaceBlockEntity;
import com.landmaster.landsutilities.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record SyncXPInterfacePacket(BlockPos pos, int amount) implements CustomPacketPayload {
    public static final Type<SyncXPInterfacePacket> TYPE = new Type<>(Util.loc("sync_xp_interface"));

    public static final StreamCodec<ByteBuf, SyncXPInterfacePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncXPInterfacePacket::pos,
            ByteBufCodecs.VAR_INT, SyncXPInterfacePacket::amount,
            SyncXPInterfacePacket::new
    );

    public void handle(IPayloadContext context) {
        var player = context.player();
        var level = player.level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof XPInterfaceBlockEntity xpInterface) {
            xpInterface.fluidXp(amount);
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
