package com.landmaster.landsutilities.data;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class RegisterLootTables {
    @SubscribeEvent
    private static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new LootTableProvider(
                generator.getPackOutput(),
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)),
                event.getLookupProvider()
        ));
    }
}
