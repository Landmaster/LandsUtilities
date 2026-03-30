package com.landmaster.landsutilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record UpgradeInfo(String type, int level) {
    public static final Codec<UpgradeInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(UpgradeInfo::type),
            Codec.INT.fieldOf("level").forGetter(UpgradeInfo::level)
    ).apply(instance, UpgradeInfo::new));

    public static final StreamCodec<ByteBuf, UpgradeInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UpgradeInfo::type,
            ByteBufCodecs.VAR_INT, UpgradeInfo::level,
            UpgradeInfo::new
    );
}
