package com.landmaster.landsutilities.util;

import com.landmaster.landsutilities.level.RedstoneWandTickets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Locale;

public record RedstoneWandState(Type type, long extraData) {
    public enum Type implements StringRepresentable, TranslatableEnum {
        ALWAYS_ON, LEVER, BUTTON;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        public static final StreamCodec<FriendlyByteBuf, Type> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Type.class);

        @Nonnull
        @Override
        public String getSerializedName() {
            return name();
        }

        @Nonnull
        @Override
        public Component getTranslatedName() {
            return Component.translatable("tooltip.landsutilities.redstone_wand.type." + name().toLowerCase(Locale.US))
                    .withColor(getColor());
        }

        public int getColor() {
            return switch (this) {
                case ALWAYS_ON -> 0xFFFF0000;
                case LEVER ->  0xFF00FF00;
                case BUTTON ->   0xFF0000FF;
            };
        }

        public boolean hasRightClickHandling() {
            return this != Type.ALWAYS_ON;
        }
    }

    public static final Codec<RedstoneWandState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Type.CODEC.fieldOf("type").forGetter(RedstoneWandState::type),
                    Codec.LONG.fieldOf("extraData").forGetter(RedstoneWandState::extraData)
            ).apply(instance, RedstoneWandState::new)
    );

    public static final StreamCodec<FriendlyByteBuf, RedstoneWandState> STREAM_CODEC = StreamCodec.composite(
            Type.STREAM_CODEC, RedstoneWandState::type,
            ByteBufCodecs.VAR_LONG, RedstoneWandState::extraData,
            RedstoneWandState::new
    );

    public RedstoneWandState() {
        this(Type.ALWAYS_ON);
    }

    public RedstoneWandState(Type type) {
        this(type, switch (type) {
            case ALWAYS_ON, LEVER -> 0;
            case BUTTON -> -1;
        });
    }

    public boolean isOn(Level level) {
        return switch (type) {
            case ALWAYS_ON -> true;
            case LEVER -> extraData > 0;
            case BUTTON -> extraData >= 0 && level.getGameTime() - extraData < 20;
        };
    }

    public RedstoneWandState handleRightClick(ServerLevel level, BlockPos pos) {
        switch (type) {
            case ALWAYS_ON -> {
                return this;
            }
            case LEVER -> {
                return new RedstoneWandState(Type.LEVER, isOn(level) ? 0 : 1);
            }
            case BUTTON -> {
                if (isOn(level)) {
                    return this;
                } else {
                    RedstoneWandTickets.getTickets(level).submitTicket(pos, level.getGameTime() + 20);
                    return new RedstoneWandState(Type.BUTTON, level.getGameTime());
                }
            }
            default -> {
                throw new IllegalArgumentException("Unknown type: " + type);
            }
        }
    }
}
