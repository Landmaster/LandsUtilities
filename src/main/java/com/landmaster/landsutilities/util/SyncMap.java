package com.landmaster.landsutilities.util;

import com.landmaster.landsutilities.network.SyncMapPacket;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import javax.annotation.Nonnull;

public class SyncMap implements ValueIOSerializable {
    private final SyncInfo<?>[] validFields;
    private final Object[] values;

    public SyncMap(SyncInfo<?>...validFields) {
        this.validFields = validFields;
        this.values = new Object[validFields.length];
        for (int i=0; i<validFields.length; ++i) {
            values[i] = validFields[i].defaultValue();
        }
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, ?> streamCodecAt(int index) {
        return validFields[index].streamCodec();
    }

    public <T> SyncMapPacket<T> generatePacket(int id, BlockPos blockPos, T value) {
        return new SyncMapPacket<T>(id, blockPos, value, (StreamCodec)streamCodecAt(id));
    }

    public SyncMapPacket<?> generatePacket(int id, BlockPos blockPos) {
        return generatePacket(id, blockPos, values[id]);
    }

    public Object get(int index) {
        return values[index];
    }

    public boolean set(int index, Object value) {
        if (((SyncInfo) validFields[index]).validator().test(value)) {
            values[index] = value;
            return true;
        }
        return false;
    }

    @Override
    public void serialize(@Nonnull ValueOutput output) {
        for (int i=0; i<validFields.length; ++i) {
            output.store(validFields[i].key(), (Codec) validFields[i].codec(), values[i]);
        }
    }

    @Override
    public void deserialize(@Nonnull ValueInput input) {
        for (int i=0; i<validFields.length; ++i) {
            values[i] = input.read(validFields[i].key(), validFields[i].codec()).get();
        }
    }
}
