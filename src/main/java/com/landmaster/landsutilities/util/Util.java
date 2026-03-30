package com.landmaster.landsutilities.util;

import com.landmaster.landsutilities.LandsUtilities;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static final TagKey<Fluid> XP = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "experience"));

    public static final Codec<Long2ObjectMap<RedstoneWandState>> WAND_STATES_CODEC = Codec.pair(Codec.LONG.fieldOf("pos").codec(), RedstoneWandState.CODEC.fieldOf("state").codec())
            .listOf().xmap(
                    l -> l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a,b) -> a, Long2ObjectOpenHashMap::new)),
                    m -> m.long2ObjectEntrySet().stream().map(e -> Pair.of(e.getLongKey(), e.getValue())).toList()
            );
    public static final StreamCodec<FriendlyByteBuf, Long2ObjectMap<RedstoneWandState>> WAND_STATES_STREAM_CODEC = ByteBufCodecs.map(
            Long2ObjectOpenHashMap::new,
            ByteBufCodecs.VAR_LONG,
            RedstoneWandState.STREAM_CODEC
    );
    public static final Codec<Long2ObjectMap<RedstoneWandState>> WAND_STATES_OLD_CODEC = Codec.LONG_STREAM.flatComapMap(
            stream -> stream.<Long2ObjectMap<RedstoneWandState>>collect(
                    Long2ObjectOpenHashMap::new,
                    (map, val) -> map.put(val, new RedstoneWandState()),
                    Long2ObjectMap::putAll
            ),
            map -> DataResult.error(() -> "Can't serialize from old wand state codec!")
    );
    public static final Codec<LongSet> LONG_SET_CODEC = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);
    public static final StreamCodec<ByteBuf, LongSet> LONG_SET_STREAM_CODEC = ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.collection(LongOpenHashSet::new));

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(LandsUtilities.MODID, path);
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

    public static <T extends Enum<?>> @Nonnull T cycleEnum(@Nonnull T value) {
        return cycleEnum(value, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> @Nonnull T cycleEnum(@Nonnull T value, boolean reverse) {
        var values = value.getClass().getEnumConstants();
        return (T) values[(value.ordinal() + (reverse ? values.length-1 : 1)) % values.length];
    }

    public static @Nullable Direction cycleDirection(@Nullable Direction original, boolean allowNone) {
        if (original == null) {
            return Direction.DOWN;
        } else if (original == Direction.EAST) {
            return allowNone ? null : cycleDirection(null, true);
        }
        return cycleEnum(original);
    }

    public static Component directionToComponent(@Nullable Direction dir) {
        if (dir == null) {
            return Component.translatable("gui.landsutilities.direction.none");
        }
        return Component.translatable("gui.landsutilities.direction." + dir.getName());
    }

    public static ItemStack[] toArray(Container cont) {
        var result = new ItemStack[cont.getContainerSize()];
        for (int i=0; i<cont.getContainerSize(); ++i) {
            result[i] = cont.getItem(i);
        }
        return result;
    }

    public static void initFromList(Container cont, List<ItemStack> stacks) {
        for (int i=0; i<cont.getContainerSize(); ++i) {
            cont.setItem(i, stacks.get(i));
        }
    }

    public static Item createUpgradeItem(Identifier name, UpgradeInfo upgradeInfo) {
        return new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, name)).component(LandsUtilities.UPGRADE_INFO, upgradeInfo));
    }

    public static Component formatBucketValue(int amount) {
        if (amount < 100000) {
            return Component.literal(String.format("%d mB", amount));
        } else if (amount < 10000000) {
            return Component.literal(String.format("%.1f B", amount / 1000.0));
        } else {
            return Component.literal(String.format("%.1f kB", amount / 1000000.0));
        }
    }
}
