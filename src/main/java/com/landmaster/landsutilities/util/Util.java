package com.landmaster.landsutilities.util;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Util {
    public static final TagKey<Fluid> XP = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "experience"));

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(LandsUtilities.MODID, path);
    }


    public static long levelToFluidXp(int level) {
        // source: https://minecraft.wiki/w/Experience

        int level2 = level * level;
        if (level <= 16) {
            return 20L * level2 + 120L * level;
        } else if (level <= 32) {
            return 50L * level2 - 810L * level + 7200L;
        }
        return 90L * level2 - 3250L * level + 44400L;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> @Nonnull T cycleEnum(@Nonnull T value) {
        var values = value.getClass().getEnumConstants();
        return (T) values[(value.ordinal() + 1) % values.length];
    }

    public static Optional<Direction> cycleConfiguration(@Nullable Direction original, boolean allowNone) {
        if (original == null) {
            return Optional.of(Direction.DOWN);
        } else if (original == Direction.EAST) {
            return allowNone ? Optional.empty() : cycleConfiguration(null, true);
        }
        return Optional.of(cycleEnum(original));
    }

    public static Component configToComponent(@Nullable Direction dir) {
        if (dir == null) {
            return Component.translatable("gui.landsutilities.direction.none");
        }
        return Component.translatable("gui.landsutilities.direction." + dir.getName());
    }
}
