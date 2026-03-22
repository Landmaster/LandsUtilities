package com.landmaster.landsutilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record RemoteControlLink(Component name, BlockPos pos, ResourceKey<Level> dimension, Direction face) {
    public static final Codec<RemoteControlLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(RemoteControlLink::name),
            BlockPos.CODEC.fieldOf("pos").forGetter(RemoteControlLink::pos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(RemoteControlLink::dimension),
            Direction.CODEC.fieldOf("face").forGetter(RemoteControlLink::face)
    ).apply(instance, RemoteControlLink::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteControlLink> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, RemoteControlLink::name,
            BlockPos.STREAM_CODEC, RemoteControlLink::pos,
            ResourceKey.streamCodec(Registries.DIMENSION), RemoteControlLink::dimension,
            Direction.STREAM_CODEC, RemoteControlLink::face,
            RemoteControlLink::new
    );
}
