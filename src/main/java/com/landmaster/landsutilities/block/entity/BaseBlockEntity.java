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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;

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
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        ioConfiguration = input.read("ioConfiguration", IO_CONFIGURATION_CODEC).get();
        redstoneConfig = input.read("redstoneConfig", RedstoneConfig.CODEC).orElse(RedstoneConfig.IGNORE);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.store("ioConfiguration", IO_CONFIGURATION_CODEC, ioConfiguration);
        output.store("redstoneConfig", RedstoneConfig.CODEC, redstoneConfig);
    }

    protected static RegistryOps<Tag> registryOps(HolderLookup.Provider registries) {
        return RegistryOps.create(NbtOps.INSTANCE, registries);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, // Choose to discard all errors
                registries
        );
        saveAdditional(output);
        return output.buildResult();
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

    @Override
    public void preRemoveSideEffects(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        var cap = level.getCapability(Capabilities.Item.BLOCK, pos, state, this, null);
        if (cap != null) {
            for (int i=0; i<cap.size(); ++i) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), cap.getResource(i).toStack(cap.getAmountAsInt(i)));
            }
        }
    }
}
