package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {
    @Inject(method = "getEntitiesWithContainerOpen(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)
    private void getEntitiesWithContainerOpen(Level level, BlockPos pos, CallbackInfoReturnable<List<ContainerUser>> cir) {
        if (!RemoteControlItem.MENU_TO_REMOTE_RANGE.isEmpty()) {
            var toAdd = RemoteControlItem.MENU_TO_REMOTE_RANGE.values().stream()
                    .map(RemoteControlItem.MenuData::player)
                    .filter(player -> ((ContainerOpenersCounter) (Object) this).hasContainerOpen(player, pos));
            cir.setReturnValue(Stream.concat(cir.getReturnValue().stream(), toAdd)
                    .distinct()
                    .toList());
        }
    }
}
