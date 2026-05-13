package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.item.RemoteControlItem;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record RemoteMenuPacket(float range, BlockPos pos, ClientboundOpenScreenPacket packet) implements CustomPacketPayload {
    public static final Type<RemoteMenuPacket> TYPE = new Type<>(Util.loc("remote_menu_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteMenuPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RemoteMenuPacket::range,
            BlockPos.STREAM_CODEC, RemoteMenuPacket::pos,
            ClientboundOpenScreenPacket.STREAM_CODEC, RemoteMenuPacket::packet,
            RemoteMenuPacket::new
    );

    public void handle(IPayloadContext context) {
        var oldMenu = context.player().containerMenu;
        if (context.listener() instanceof ClientGamePacketListener listener) {
            packet.handle(listener);
        }
        var newMenu = context.player().containerMenu;
        if (newMenu != oldMenu) {
            RemoteControlItem.MENU_TO_REMOTE_RANGE.put(newMenu, new RemoteControlItem.MenuData(context.player(), pos, range));
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
