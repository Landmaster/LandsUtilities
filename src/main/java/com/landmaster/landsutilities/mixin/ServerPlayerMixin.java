package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.item.RemoteControlItem;
import com.landmaster.landsutilities.network.RemoteAdvancedMenuPacket;
import com.landmaster.landsutilities.network.RemoteMenuPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @SuppressWarnings("UnstableApiUsage")
    @ModifyArg(method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private CustomPacketPayload adjustAdvancedMenuPacket(CustomPacketPayload packet) {
        var desiredRange = RemoteControlItem.DESIRED_RANGE.get();
        var desiredPos = RemoteControlItem.DESIRED_POS.get();
        if (desiredRange > 1.0 && packet instanceof AdvancedOpenScreenPayload openScreenPayload) {
            return new RemoteAdvancedMenuPacket(desiredRange, desiredPos, openScreenPayload);
        }
        return packet;
    }

    @Redirect(method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void adjustMenuPacket(ServerGamePacketListenerImpl instance, Packet<?> packet) {
        var desiredRange = RemoteControlItem.DESIRED_RANGE.get();
        var desiredPos = RemoteControlItem.DESIRED_POS.get();
        if (desiredRange > 1.0 && packet instanceof ClientboundOpenScreenPacket openScreenPacket) {
            instance.send(new RemoteMenuPacket(desiredRange, desiredPos, openScreenPacket));
        } else {
            instance.send(packet);
        }
    }
}
