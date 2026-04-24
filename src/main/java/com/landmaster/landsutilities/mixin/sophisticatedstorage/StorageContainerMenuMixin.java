package com.landmaster.landsutilities.mixin.sophisticatedstorage;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StorageContainerMenu.class)
public class StorageContainerMenuMixin {
    @Redirect(method = "stillValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(DDD)D"))
    private double adjustPlayerDistance(Player instance, double x, double y, double z) {
        var data = RemoteControlItem.MENU_TO_REMOTE_RANGE.get(instance.containerMenu);
        var range = data == null ? 0.0 : data.range();
        return instance.distanceToSqr(x, y, z) / Math.max(range, 1.0);
    }
}
