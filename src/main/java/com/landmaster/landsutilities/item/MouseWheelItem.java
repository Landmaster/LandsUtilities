package com.landmaster.landsutilities.item;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.network.MouseWheelPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

public interface MouseWheelItem {
    void onMouseWheel(Player player, InteractionHand hand, boolean up);

    @EventBusSubscriber(modid = LandsUtilities.MODID, value = Dist.CLIENT)
    class ClientEvents {
        @SubscribeEvent
        private static void onMouseWheel(InputEvent.MouseScrollingEvent event) {
            if (event.getScrollDeltaY() == 0) {
                return;
            }

            var player = Minecraft.getInstance().player;
            if (player.isSecondaryUseActive()) {
                if (player.getMainHandItem().getItem() instanceof MouseWheelItem
                    || player.getOffhandItem().getItem() instanceof MouseWheelItem) {
                    ClientPacketDistributor.sendToServer(new MouseWheelPacket(event.getScrollDeltaY() > 0));
                    event.setCanceled(true);
                }
            }
        }
    }
}
