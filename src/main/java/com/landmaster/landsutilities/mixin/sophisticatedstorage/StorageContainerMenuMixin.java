package com.landmaster.landsutilities.mixin.sophisticatedstorage;

import com.landmaster.landsutilities.item.RemoteControlItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(StorageContainerMenu.class)
public class StorageContainerMenuMixin {
    @ModifyArg(method = "stillValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isWithinBlockInteractionRange(Lnet/minecraft/core/BlockPos;D)Z"))
    private double adjustPlayerInteractionRange(BlockPos pos, double range, @Local(argsOnly = true, name = "player") Player player) {
        var data = RemoteControlItem.MENU_TO_REMOTE_RANGE.get(player.containerMenu);
        if (data != null && data.range() > 1.0f) {
            range *= data.range();
        }
        return range;
    }
}
