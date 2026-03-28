package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Container.class)
public interface ContainerMixin {
    @ModifyVariable(
            method = "stillValidBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/player/Player;F)Z",
            at = @At("HEAD"),
            argsOnly = true)
    private static float injectStillValidBlockEntity(float distanceBuffer, BlockEntity blockEntity, Player player) {
        var data = RemoteControlItem.MENU_TO_REMOTE_RANGE.get(player.containerMenu);
        if (data != null && data.range() > 1.0f) {
            distanceBuffer *= data.range();
        }
        return distanceBuffer;
    }
}
