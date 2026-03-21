package com.landmaster.landsutilities.data;

import com.google.common.collect.ImmutableSet;
import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class ModBlockLootSubProvider extends BlockLootSubProvider {
    protected ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(ImmutableSet.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Nonnull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return LandsUtilities.BLOCKS.getEntries().stream()
                .map(v -> (Block) v.value())
                .toList();
    }

    @Override
    protected void generate() {
        dropSelf(LandsUtilities.AUTO_ANVIL.get());
    }
}
