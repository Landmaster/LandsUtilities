package com.landmaster.landsutilities.block;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.XPInterfaceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.BaseEntityBlock;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class XPInterfaceBlock extends FunctionalBlock<XPInterfaceBlockEntity> implements TooltipBlock {
    private static final MapCodec<XPInterfaceBlock> CODEC = simpleCodec(XPInterfaceBlock::new);

    public XPInterfaceBlock(Properties properties) {
        super(properties, LandsUtilities.XP_INTERFACE_TE);
    }

    @Override
    @Nonnull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nonnull Item.TooltipContext context, @Nonnull TooltipDisplay display, @Nonnull Consumer<Component> builder, @Nonnull TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.landsutilities.xp_interface").withStyle(ChatFormatting.AQUA));
    }
}
