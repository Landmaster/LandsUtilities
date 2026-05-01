package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.item.RemoteControlItem;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("UnstableApiUsage")
public record RemoteAdvancedMenuPacket(float range, BlockPos pos, AdvancedOpenScreenPayload openScreenPayload) implements CustomPacketPayload {
    public static final Type<RemoteAdvancedMenuPacket> TYPE = new Type<>(Util.loc("remote_advanced_menu_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteAdvancedMenuPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RemoteAdvancedMenuPacket::range,
            BlockPos.STREAM_CODEC, RemoteAdvancedMenuPacket::pos,
            AdvancedOpenScreenPayload.STREAM_CODEC, RemoteAdvancedMenuPacket::openScreenPayload,
            RemoteAdvancedMenuPacket::new
    );

    private static final Class<?> CLIENT_PAYLOAD_HANDLER;
    private static final Method CLIENT_PAYLOAD_HANDLER_HANDLE;

    static {
        try {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                CLIENT_PAYLOAD_HANDLER = Class.forName("net.neoforged.neoforge.client.network.ClientPayloadHandler");
                CLIENT_PAYLOAD_HANDLER_HANDLE = CLIENT_PAYLOAD_HANDLER.getDeclaredMethod(
                        "handle", AdvancedOpenScreenPayload.class, IPayloadContext.class);
                CLIENT_PAYLOAD_HANDLER_HANDLE.setAccessible(true);
            } else {
                CLIENT_PAYLOAD_HANDLER = null;
                CLIENT_PAYLOAD_HANDLER_HANDLE = null;
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(IPayloadContext context) {
        var player = context.player();
        var level = player.level();
        if (level.isLoaded(pos) && level.getBlockEntity(pos) != null) {
            var oldMenu = player.containerMenu;
            try {
                CLIENT_PAYLOAD_HANDLER_HANDLE.invoke(null, openScreenPayload, context);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            var newMenu = player.containerMenu;
            if (newMenu != oldMenu) {
                RemoteControlItem.MENU_TO_REMOTE_RANGE.put(newMenu, new RemoteControlItem.MenuData(context.player(), range));
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
