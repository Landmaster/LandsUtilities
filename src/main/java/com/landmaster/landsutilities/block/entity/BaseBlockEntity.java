package com.landmaster.landsutilities.block.entity;

import com.landmaster.landsutilities.util.RedstoneConfig;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class BaseBlockEntity extends BlockEntity {
    private static final Codec<Map<String, Optional<Direction>>> IO_CONFIGURATION_CODEC = Codec.unboundedMap(
            Codec.STRING, ExtraCodecs.optionalEmptyMap(Direction.CODEC)
    );

    private Map<String, Optional<Direction>> ioConfiguration;
    @Getter
    @Setter
    private RedstoneConfig redstoneConfig = RedstoneConfig.IGNORE;
    @Getter
    protected boolean active = true;

    public BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, Map<String, Optional<Direction>> initialIoConfiguration) {
        super(type, pos, blockState);
        this.ioConfiguration = new HashMap<>(initialIoConfiguration);
    }

    public Optional<Direction> getConfiguration(String key) {
        var res = ioConfiguration.get(key);
        return res != null ? res : Optional.empty();
    }

    public boolean setConfiguration(String key, @Nullable Direction direction) {
        if (!ioConfiguration.containsKey(key)) {
            return false;
        }
        ioConfiguration.put(key, Optional.ofNullable(direction));
        return true;
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ioConfiguration = new HashMap<>(IO_CONFIGURATION_CODEC.parse(NbtOps.INSTANCE, tag.getCompound("ioConfiguration")).getOrThrow());
        redstoneConfig = RedstoneConfig.CODEC.parse(NbtOps.INSTANCE, tag.get("redstoneConfig")).result().orElse(RedstoneConfig.IGNORE);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("ioConfiguration", IO_CONFIGURATION_CODEC.encodeStart(NbtOps.INSTANCE, ioConfiguration).getOrThrow());
        tag.put("redstoneConfig", RedstoneConfig.CODEC.encodeStart(NbtOps.INSTANCE, redstoneConfig).getOrThrow());
    }

    protected static RegistryOps<Tag> registryOps(HolderLookup.Provider registries) {
        return RegistryOps.create(NbtOps.INSTANCE, registries);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    public void tick() {
        switch (redstoneConfig) {
            case IGNORE:
                active = true;
                break;
            case LOW:
            case HIGH:
                active = level.hasNeighborSignal(worldPosition) == (redstoneConfig == RedstoneConfig.HIGH);
                break;
        }
    }
}
