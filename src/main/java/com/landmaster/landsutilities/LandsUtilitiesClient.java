package com.landmaster.landsutilities;

import com.landmaster.landsutilities.menu.AutoAnvilScreen;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = LandsUtilities.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LandsUtilities.MODID, value = Dist.CLIENT)
public class LandsUtilitiesClient {
    public LandsUtilitiesClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(LandsUtilities.AUTO_ANVIL_MENU.get(), AutoAnvilScreen::new);
    }

    @SubscribeEvent
    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        var texture = new Material(Util.loc("block/fluid_xp"), true);
        event.register(new FluidModel.Unbaked(
                texture,
                texture,
                texture,
                null
        ), LandsUtilities.FLUID_XP_STILL, LandsUtilities.FLUID_XP_FLOWING);
    }

}
