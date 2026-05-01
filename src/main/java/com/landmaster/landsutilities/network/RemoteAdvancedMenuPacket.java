package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.item.RemoteControlItem;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;

import javax.annotation.Nonnull;

@SuppressWarnings("UnstableApiUsage")
public record RemoteAdvancedMenuPacket(double range, BlockPos pos, AdvancedOpenScreenPayload openScreenPayload) implements CustomPacketPayload {
    public static final Type<RemoteAdvancedMenuPacket> TYPE = new Type<>(Util.loc("remote_advanced_menu_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteAdvancedMenuPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, RemoteAdvancedMenuPacket::range,
            BlockPos.STREAM_CODEC, RemoteAdvancedMenuPacket::pos,
            AdvancedOpenScreenPayload.STREAM_CODEC, RemoteAdvancedMenuPacket::openScreenPayload,
            RemoteAdvancedMenuPacket::new
    );

    public void handle(IPayloadContext context) {
        var player = context.player();
        var level = player.level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) != null) {
            var oldMenu = player.containerMenu;
            ClientPayloadHandler.handle(openScreenPayload, context);
            var newMenu = player.containerMenu;
            if (newMenu != oldMenu) {
                RemoteControlItem.MENU_TO_REMOTE_RANGE.put(newMenu, range);
            }
        } else {
            player.closeContainer();
        }
    }

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
