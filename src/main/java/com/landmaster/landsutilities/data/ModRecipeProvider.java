package com.landmaster.landsutilities.data;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
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

        shaped(
                RecipeCategory.MISC, LandsUtilities.XP_COLLECTOR
        )
                .define('e', Items.EXPERIENCE_BOTTLE)
                .define('b', Items.BUCKET)
                .define('o', Tags.Items.OBSIDIANS_NORMAL)
                .pattern(" e ")
                .pattern("obo")
                .unlockedBy("has_experience_bottle", has(Items.EXPERIENCE_BOTTLE))
                .save(output);

        shaped(
                RecipeCategory.MISC, LandsUtilities.XP_INTERFACE
        )
                .define('e', Items.EXPERIENCE_BOTTLE)
                .define('d', Items.PRISMARINE)
                .define('p', Tags.Items.GEMS_PRISMARINE)
                .pattern("pdp")
                .pattern("ded")
                .pattern("pdp")
                .unlockedBy("has_experience_bottle", has(Items.EXPERIENCE_BOTTLE))
                .save(output);

        shaped(
                RecipeCategory.MISC, LandsUtilities.BLOCK_INTERFACER
        )
                .define('h', ItemTags.SKULLS)
                .define('w', ItemTags.PLANKS)
                .define('a', Items.ARROW)
                .pattern("waw")
                .pattern("whw")
                .pattern("www")
                .unlockedBy("has_skull", has(ItemTags.SKULLS))
                .save(output);

        shapeless(RecipeCategory.MISC, LandsUtilities.REMOTE_CONTROL)
                .requires(LandsUtilities.REMOTE_CONTROL)
                .unlockedBy("has_remote_control", has(LandsUtilities.REMOTE_CONTROL))
                .save(output, ResourceKey.create(Registries.RECIPE, Util.loc("remote_control_reset")));

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

        shaped(RecipeCategory.MISC, LandsUtilities.CAPACITY_UPGRADES.get(0))
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('b', Items.BUCKET)
                .pattern("cic")
                .pattern("ibi")
                .pattern("cic")
                .unlockedBy("has_bucket", has(Items.BUCKET))
                .save(output);

        shaped(RecipeCategory.MISC, LandsUtilities.CAPACITY_UPGRADES.get(1))
                .define('g', Tags.Items.INGOTS_GOLD)
                .define('a', Tags.Items.GEMS_AMETHYST)
                .define('u', LandsUtilities.CAPACITY_UPGRADES.get(0))
                .pattern("aga")
                .pattern("gug")
                .pattern("aga")
                .unlockedBy("has_capacity_upgrade", has(LandsUtilities.CAPACITY_UPGRADES.get(0)))
                .save(output);

        shaped(RecipeCategory.MISC, LandsUtilities.CAPACITY_UPGRADES.get(2))
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('p', Tags.Items.GEMS_PRISMARINE)
                .define('u', LandsUtilities.CAPACITY_UPGRADES.get(1))
                .pattern("pdp")
                .pattern("dud")
                .pattern("pdp")
                .unlockedBy("has_capacity_upgrade", has(LandsUtilities.CAPACITY_UPGRADES.get(1)))
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
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(Runner::new);
    }
}
