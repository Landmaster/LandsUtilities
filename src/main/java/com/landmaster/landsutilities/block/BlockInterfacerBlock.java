package com.landmaster.landsutilities.block;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.BlockInterfacerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class BlockInterfacerBlock extends FunctionalBlock<BlockInterfacerBlockEntity> implements DisplayRangeBlock, TooltipBlock {
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

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nonnull Item.TooltipContext context, @Nonnull TooltipDisplay display, @Nonnull Consumer<Component> builder, @Nonnull TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.landsutilities.block_interfacer").withStyle(ChatFormatting.AQUA));
    }
}
