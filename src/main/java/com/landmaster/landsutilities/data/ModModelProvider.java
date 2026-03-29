package com.landmaster.landsutilities.data;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

@EventBusSubscriber(modid = LandsUtilities.MODID, value = Dist.CLIENT)
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, LandsUtilities.MODID);
    }

    @Override
    protected void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
        blockModels.createNonTemplateModelBlock(LandsUtilities.FLUID_XP_BLOCK.get());
        blockModels.createRotatableColumn(LandsUtilities.XP_COLLECTOR.get());

        itemModels.generateFlatItem(LandsUtilities.REMOTE_CONTROL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LandsUtilities.REDSTONE_WAND.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(LandsUtilities.FLUID_XP_BUCKET.get(), ModelTemplates.FLAT_ITEM);
    }

    @SubscribeEvent
    private static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
    }

    @Override
    @Nonnull
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(block -> block.value() != LandsUtilities.AUTO_ANVIL.get());
    }
}
