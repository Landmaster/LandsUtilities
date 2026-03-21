package com.landmaster.landsutilities.block;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class FunctionalBlock<T extends BaseBlockEntity> extends BaseEntityBlock {
    protected final Supplier<BlockEntityType<T>> blockEntityType;

    public FunctionalBlock(Properties properties, Supplier<BlockEntityType<T>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
    }

    @Override
    public @Nullable BaseBlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
        return blockEntityType.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <U extends BlockEntity> BlockEntityTicker<U> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<U> blockEntityType) {
        if (blockEntityType == this.blockEntityType.get() && !level.isClientSide) {
            return (level_, pos, state_, tile) -> ((BaseBlockEntity) tile).tick();
        }
        return null;
    }

    @Nonnull
    @Override
    protected InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult hitResult) {
        var te = level.getBlockEntity(pos);
        if (te instanceof MenuProvider provider && te instanceof BaseBlockEntity) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            player.openMenu(provider, pos);
            return InteractionResult.CONSUME;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Nonnull
    @Override
    protected RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }
}
