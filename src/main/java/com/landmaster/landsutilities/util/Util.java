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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Collectors;

public class Util {
    public static final TagKey<Fluid> XP = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "experience"));

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

    public static final Codec<Long2ObjectMap<BlockState>> FACADE_STATES_CODEC = Codec.pair(
            Codec.LONG.fieldOf("pos").codec(), BlockState.CODEC.fieldOf("facade").codec()
    ).listOf().xmap(
            list -> list.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a, Long2ObjectOpenHashMap::new)),
            map -> map.long2ObjectEntrySet().stream().map(entry -> Pair.of(entry.getLongKey(), entry.getValue())).toList()
    );
    public static final StreamCodec<ByteBuf, Long2ObjectMap<BlockState>> FACADE_STATES_STREAM_CODEC = ByteBufCodecs.map(
            Long2ObjectOpenHashMap::new,
            ByteBufCodecs.VAR_LONG,
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)
    );

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

    public static <T extends Enum<?>> @Nonnull T cycleEnum(@Nonnull T value) {
        return cycleEnum(value, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> @Nonnull T cycleEnum(@Nonnull T value, boolean reverse) {
        var values = value.getClass().getEnumConstants();
        return (T) values[(value.ordinal() + (reverse ? values.length-1 : 1)) % values.length];
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
