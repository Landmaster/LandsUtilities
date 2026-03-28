package com.landmaster.landsutilities.data;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.core.HolderLookup;
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

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(
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

        shaped(
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

        shapeless(RecipeCategory.MISC, LandsUtilities.REMOTE_CONTROL)
                .requires(LandsUtilities.REMOTE_CONTROL)
                .unlockedBy("has_remote_control", has(LandsUtilities.REMOTE_CONTROL))
                .save(output, "remote_control_reset");

        shaped(
                RecipeCategory.REDSTONE, LandsUtilities.REDSTONE_WAND
        )
                .define('c', Tags.Items.OBSIDIANS_CRYING)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .pattern("  r")
                .pattern(" c ")
                .pattern("c  ")
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                .save(output);
    }

    // The runner to add to the data generator
    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        @Nonnull
        protected RecipeProvider createRecipeProvider(@Nonnull HolderLookup.Provider provider, @Nonnull RecipeOutput output) {
            return new ModRecipeProvider(provider, output);
        }

        @Override
        @Nonnull
        public String getName() {
            return "Land's Utilities Recipes";
        }
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Server event) {
        event.createProvider(Runner::new);
    }
}
