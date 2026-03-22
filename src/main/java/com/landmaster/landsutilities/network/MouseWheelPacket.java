package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.item.MouseWheelItem;
import com.landmaster.landsutilities.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record MouseWheelPacket(boolean up) implements CustomPacketPayload {
    public static final Type<MouseWheelPacket> TYPE = new Type<>(Util.loc("mouse_wheel"));

    public static final StreamCodec<ByteBuf, MouseWheelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MouseWheelPacket::up,
            MouseWheelPacket::new
    );

    public void handle(IPayloadContext ctx) {
        var player = ctx.player();
        for (var hand: InteractionHand.values()) {
            if (player.getItemInHand(hand).getItem() instanceof MouseWheelItem mouseWheelItem) {
                mouseWheelItem.onMouseWheel(player, hand, up);
                break;
            }
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
