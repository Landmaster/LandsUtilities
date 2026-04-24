package com.landmaster.landsutilities.mixin.sophisticatedstorage;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

@Mixin(MovingStorageOpenersCounter.class)
public class MovingStorageOpenersCounterMixin {
    @Unique
    private static final Method IS_OWN_CONTAINER;

    static {
        try {
            IS_OWN_CONTAINER = MovingStorageOpenersCounter.class.getDeclaredMethod("isOwnContainer", Player.class);
            IS_OWN_CONTAINER.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(
            method = "getPlayersWithContainerOpen(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)
    private void getPlayersWithContainerOpen(Level level, BlockPos pos, CallbackInfoReturnable<List<Player>> cir) {
        if (!RemoteControlItem.MENU_TO_REMOTE_RANGE.isEmpty()) {
            var toAdd = RemoteControlItem.MENU_TO_REMOTE_RANGE.values().stream()
                    .map(RemoteControlItem.MenuData::player)
                    .filter(player -> {
                        try {
                            return (boolean) IS_OWN_CONTAINER.invoke(this, player);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
            cir.setReturnValue(Stream.concat(cir.getReturnValue().stream(), toAdd).toList());
        }
    }
}
