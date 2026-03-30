package com.landmaster.landsutilities.item;

import com.landmaster.landsutilities.block.TooltipBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ModBlockItem extends BlockItem {
    public ModBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nonnull TooltipContext context, @Nonnull TooltipDisplay display, @Nonnull Consumer<Component> builder, @Nonnull TooltipFlag tooltipFlag) {
        if (getBlock() instanceof TooltipBlock tooltipBlock) {
            tooltipBlock.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        }
        if (itemStack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            builder.accept(Component.translatable("tooltip.landsutilities.contents_saved").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public @Nonnull Component getName(@Nonnull ItemStack itemStack) {
        return getBlock().getName();
    }
}
