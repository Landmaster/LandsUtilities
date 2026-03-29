package com.landmaster.landsutilities.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Predicate;

public record SyncInfo<T>(String key, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, T defaultValue, Predicate<T> validator) {
}
