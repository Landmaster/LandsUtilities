package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.XPInterfaceBlockEntity;
import com.landmaster.landsutilities.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;

public record RequestXPInterfacePacket(BlockPos pos, int levels) implements CustomPacketPayload {
    public static final Type<RequestXPInterfacePacket> TYPE = new Type<>(Util.loc("request_xp_interface"));

    public static final StreamCodec<ByteBuf, RequestXPInterfacePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RequestXPInterfacePacket::pos,
            ByteBufCodecs.VAR_INT, RequestXPInterfacePacket::levels,
            RequestXPInterfacePacket::new
    );

    public void handle(IPayloadContext context) {
        var player = context.player();
        var level = player.level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof XPInterfaceBlockEntity blockEntity) {
            var cap = blockEntity.fluidHandler();
            var resource = FluidResource.of(LandsUtilities.FLUID_XP_STILL);
            long currentXPFluid = player.totalExperience * 20L;
            long targetXPFluid = Util.levelToFluidXp(Math.clamp(player.experienceLevel + levels, 0, Integer.MAX_VALUE));
            if (currentXPFluid > targetXPFluid) {
                int toInsert = Math.clamp(currentXPFluid - targetXPFluid, 0, Integer.MAX_VALUE);
                try (var txn = Transaction.openRoot()) {
                    toInsert = (cap.insert(resource, toInsert, txn) / 20) * 20;
                }
                try (var txn = Transaction.openRoot()) {
                    if (cap.insert(resource, toInsert, txn) >= toInsert) {
                        player.giveExperiencePoints(-toInsert / 20);
                        txn.commit();
                    }
                }
            } else {
                int toExtract = Math.clamp(targetXPFluid - currentXPFluid, 0, Integer.MAX_VALUE);
                try (var txn = Transaction.openRoot()) {
                    toExtract = (cap.extract(resource, toExtract, txn) / 20) * 20;
                }
                try (var txn = Transaction.openRoot()) {
                    if (cap.extract(resource, toExtract, txn) >= toExtract) {
                        player.giveExperiencePoints(toExtract / 20);
                        txn.commit();
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
