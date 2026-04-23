package com.landmaster.landsutilities.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.neoforged.neoforge.network.connection.ConnectionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import java.net.SocketAddress;
import java.util.Set;

/**
 * Adapted from IntegratedTunnels
 */
public class FakeNetHandlerPlayServer extends ServerGamePacketListenerImpl {

    public FakeNetHandlerPlayServer(MinecraftServer server, ServerPlayer player) {
        super(server, new Connection(PacketFlow.CLIENTBOUND) {
            @Override
            public void channelActive(@Nonnull ChannelHandlerContext p_channelActive_1_) throws Exception {

            }

            @Override
            public void channelInactive(@Nonnull ChannelHandlerContext p_channelInactive_1_) {

            }

            @Override
            public void exceptionCaught(@Nonnull ChannelHandlerContext p_exceptionCaught_1_, @Nonnull Throwable p_exceptionCaught_2_) {

            }

            @Override
            public void send(@Nonnull Packet<?> packetIn) {

            }

            @Override
            public void send(@Nonnull Packet<?> packet, @Nullable ChannelFutureListener p_428399_) {

            }

            @Override
            public void send(@Nonnull Packet<?> packet, @Nullable ChannelFutureListener p_428328_, boolean p_428543_) {

            }

            @Override
            public SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public boolean isMemoryConnection() {
                return false;
            }

            @Override
            public void setEncryptionKey(@Nonnull Cipher p_244777_1_, @Nonnull Cipher p_244777_2_) {

            }

            @Override
            public boolean isConnected() {
                return false;
            }

            @Override
            public PacketListener getPacketListener() {
                return null;
            }

            @Override
            public void setReadOnly() {

            }

            @Override
            public void handleDisconnection() {

            }

            @Override
            public Channel channel() {
                return super.channel();
            }
        }, player, new CommonListenerCookie(null, 1, null, false, ConnectionType.NEOFORGE));
    }

    @Override
    public void tick() {

    }

    @Override
    public void disconnect(@Nonnull Component textComponent) {

    }

    @Override
    public void handlePlayerInput(@Nonnull ServerboundPlayerInputPacket packetIn) {

    }

    @Override
    public void handleMoveVehicle(@Nonnull ServerboundMoveVehiclePacket packetIn) {

    }

    @Override
    public void handleAcceptTeleportPacket(@Nonnull ServerboundAcceptTeleportationPacket packetIn) {

    }

    @Override
    public void handleMovePlayer(@Nonnull ServerboundMovePlayerPacket packetIn) {

    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    public void teleport(@Nonnull PositionMoveRotation posMoveRotation, @Nonnull Set<Relative> relatives) {
        super.teleport(posMoveRotation, relatives);
    }

    @Override
    public void handlePlayerAction(@Nonnull ServerboundPlayerActionPacket packetIn) {

    }

    @Override
    public void handleUseItemOn(@Nonnull ServerboundUseItemOnPacket packetIn) {

    }

    @Override
    public void handleUseItem(@Nonnull ServerboundUseItemPacket packetIn) {

    }

    @Override
    public void handleTeleportToEntityPacket(@Nonnull ServerboundTeleportToEntityPacket packetIn) {

    }

    @Override
    public void handleResourcePackResponse(@Nonnull ServerboundResourcePackPacket p_295695_) {

    }


    @Override
    public void handlePaddleBoat(@Nonnull ServerboundPaddleBoatPacket packetIn) {

    }

    @Override
    public void send(@Nonnull final Packet<?> packetIn) {

    }

    @Override
    public void handleSetCarriedItem(@Nonnull ServerboundSetCarriedItemPacket packetIn) {

    }

    @Override
    public void handleChat(@Nonnull ServerboundChatPacket packetIn) {

    }

    @Override
    public void handleAnimate(@Nonnull ServerboundSwingPacket packetIn) {

    }

    @Override
    public void handlePlayerCommand(@Nonnull ServerboundPlayerCommandPacket packetIn) {

    }

    @Override
    public void handleInteract(@Nonnull ServerboundInteractPacket packetIn) {

    }

    @Override
    public void handleClientCommand(@Nonnull ServerboundClientCommandPacket packetIn) {

    }

    @Override
    public void handleContainerClose(@Nonnull ServerboundContainerClosePacket packetIn) {

    }

    @Override
    public void handleContainerClick(@Nonnull ServerboundContainerClickPacket packetIn) {

    }

    @Override
    public void handleContainerButtonClick(@Nonnull ServerboundContainerButtonClickPacket packetIn) {

    }

    @Override
    public void handleSetCreativeModeSlot(@Nonnull ServerboundSetCreativeModeSlotPacket packetIn) {

    }

    @Override
    public void handleSignUpdate(@Nonnull ServerboundSignUpdatePacket packetIn) {

    }

    @Override
    public void handleKeepAlive(@Nonnull ServerboundKeepAlivePacket packetIn) {

    }

    @Override
    public void handlePlayerAbilities(@Nonnull ServerboundPlayerAbilitiesPacket packetIn) {

    }

    @Override
    public void handleCustomCommandSuggestions(@Nonnull ServerboundCommandSuggestionPacket packetIn) {

    }

    @Override
    public void handleClientInformation(@Nonnull ServerboundClientInformationPacket packetIn) {

    }

    @Override
    public void handleCustomPayload(@Nonnull ServerboundCustomPayloadPacket packetIn) {

    }
}
