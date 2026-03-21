package com.landmaster.landsutilities.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum RedstoneConfig implements StringRepresentable, TranslatableEnum {
    IGNORE, LOW, HIGH;

    public static final Codec<RedstoneConfig> CODEC = StringRepresentable.fromEnum(RedstoneConfig::values);
    public static final StreamCodec<FriendlyByteBuf, RedstoneConfig> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(RedstoneConfig.class);

    @Nonnull
    @Override
    public String getSerializedName() {
        return name();
    }

    @Nonnull
    @Override
    public Component getTranslatedName() {
        return Component.translatable("gui.landsutilities.redstone." + name().toLowerCase(Locale.US));
    }
}
