package com.landmaster.landsutilities.data;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = LandsUtilities.MODID, value = Dist.CLIENT)
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LandsUtilities.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleBlockItem(LandsUtilities.AUTO_ANVIL.get());

        basicItem(LandsUtilities.REMOTE_CONTROL.get());
    }

    @SubscribeEvent
    private static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var fileHelper = event.getExistingFileHelper();

        generator.addProvider(
                event.includeClient(),
                new ModItemModelProvider(packOutput, fileHelper)
        );
    }
}
