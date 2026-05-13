package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyVariable(method = "canInteractWithBlock(Lnet/minecraft/core/BlockPos;D)Z", at = @At("STORE"), ordinal = 1)
    private double injectCanInteractWithBlock(double distance) {
        var player = (Player) (Object) this;
        var rangeData = RemoteControlItem.MENU_TO_REMOTE_RANGE.get(player.containerMenu);
        if (rangeData != null && rangeData.range() > 1.0f) {
            distance *= rangeData.range();
        }
        return distance;
    }
}
