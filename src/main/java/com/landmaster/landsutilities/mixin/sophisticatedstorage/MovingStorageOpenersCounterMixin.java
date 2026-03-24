package com.landmaster.landsutilities.mixin.sophisticatedstorage;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(MovingStorageOpenersCounter.class)
public class MovingStorageOpenersCounterMixin {
    @Redirect(method = "getPlayersWithContainerOpen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Player> adjustPlayersWithContainerOpen(Level instance, EntityTypeTest<Entity, Player> entityTypeTest, AABB bounds, Predicate<? super Player> predicate) {
        if (!RemoteControlItem.MENU_TO_REMOTE_RANGE.isEmpty()) {
            return instance.players().stream().filter(predicate).collect(Collectors.toList());
        }
        return instance.getEntities(entityTypeTest, bounds, predicate);
    }
}
