package com.landmaster.landsutilities.data;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@Nonnull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(
                RecipeCategory.MISC, LandsUtilities.AUTO_ANVIL
        )
                .define('a', Items.ANVIL)
                .define('b', Items.EXPERIENCE_BOTTLE)
                .define('h', Items.HOPPER)
                .define('e', Items.ENDER_EYE)
                .pattern(" e ")
                .pattern("bab")
                .pattern(" h ")
                .unlockedBy("has_anvil", has(Items.ANVIL))
                .save(output);

        ShapedRecipeBuilder.shaped(
                RecipeCategory.MISC, LandsUtilities.REMOTE_CONTROL
        )
                .define('e', Items.ENDER_EYE)
                .define('b', ItemTags.STONE_BUTTONS)
                .define('i', Tags.Items.INGOTS_COPPER)
                .pattern("iei")
                .pattern("ibi")
                .pattern("ibi")
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, LandsUtilities.REMOTE_CONTROL)
                .requires(LandsUtilities.REMOTE_CONTROL)
                .unlockedBy("has_remote_control", has(LandsUtilities.REMOTE_CONTROL))
                .save(output, Util.loc("remote_control_reset"));

        ShapedRecipeBuilder.shaped(
                RecipeCategory.REDSTONE, LandsUtilities.REDSTONE_WAND
        )
                .define('c', Tags.Items.OBSIDIANS_CRYING)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .pattern("  r")
                .pattern(" c ")
                .pattern("c  ")
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(
                RecipeCategory.MISC, LandsUtilities.FACADE_WAND
        )
                .define('g', Items.GHAST_TEAR)
                .define('s', Items.STICK)
                .pattern("  g")
                .pattern(" s ")
                .pattern("s  ")
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(output);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(
                event.includeServer(),
                new ModRecipeProvider(output, lookupProvider)
        );
    }
}
