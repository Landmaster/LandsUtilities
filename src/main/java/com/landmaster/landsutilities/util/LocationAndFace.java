package com.landmaster.landsutilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

public record LocationAndFace(Location location, Direction face) {
    public static final Codec<LocationAndFace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Location.CODEC.fieldOf("location").forGetter(LocationAndFace::location),
            Direction.CODEC.fieldOf("face").forGetter(LocationAndFace::face)
    ).apply(instance, LocationAndFace::new));

    public static final StreamCodec<ByteBuf, LocationAndFace> STREAM_CODEC = StreamCodec.composite(
            Location.STREAM_CODEC, LocationAndFace::location,
            Direction.STREAM_CODEC, LocationAndFace::face,
            LocationAndFace::new
    );
}
