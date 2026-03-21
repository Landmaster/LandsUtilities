package com.landmaster.landsutilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Location(BlockPos pos, ResourceKey<Level> dimension) {
    public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Location::pos),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Location::dimension)
    ).apply(instance, Location::new));

    public static final StreamCodec<ByteBuf, Location> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Location::pos,
            ResourceKey.streamCodec(Registries.DIMENSION), Location::dimension,
            Location::new
    );
}
