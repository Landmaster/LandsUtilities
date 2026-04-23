package com.landmaster.landsutilities.block;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.BlockInterfacerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockInterfacerBlock extends FunctionalBlock<BlockInterfacerBlockEntity> implements DisplayRangeBlock {
    private static final MapCodec<BlockInterfacerBlock> CODEC = simpleCodec(BlockInterfacerBlock::new);

    public BlockInterfacerBlock(Properties properties) {
        super(properties, LandsUtilities.BLOCK_INTERFACER_TE);
    }

    @Nonnull
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable AABB displayRangeBox(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockInterfacerBlockEntity te) {
            return new AABB(te.targetLocation());
        }
        return null;
    }
}
