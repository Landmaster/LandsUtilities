package com.landmaster.landsutilities;

import com.landmaster.landsutilities.block.AutoAnvilBlock;
import com.landmaster.landsutilities.block.entity.AutoAnvilBlockEntity;
import com.landmaster.landsutilities.command.RemoteDeleteLinkCommand;
import com.landmaster.landsutilities.command.RemoteRenameLinkCommand;
import com.landmaster.landsutilities.item.RedstoneWandItem;
import com.landmaster.landsutilities.item.RemoteControlItem;
import com.landmaster.landsutilities.menu.AutoAnvilMenu;
import com.landmaster.landsutilities.network.*;
import com.landmaster.landsutilities.util.RedstoneWandState;
import com.landmaster.landsutilities.util.RemoteControlLink;
import com.landmaster.landsutilities.util.Util;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LandsUtilities.MODID)
@EventBusSubscriber
public class LandsUtilities {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "landsutilities";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, MODID
    );
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            Registries.MENU, MODID
    );
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, MODID
    );
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister.DataComponents ENCHANTMENT_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MODID
    );
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);


    public static final Supplier<DataComponentType<List<RemoteControlLink>>> LINKED_MENU_BLOCKS = DATA_COMPONENTS.registerComponentType(
            "linked_menu_blocks", builder -> builder
                    .persistent(RemoteControlLink.CODEC.listOf())
                    .networkSynchronized(RemoteControlLink.STREAM_CODEC.apply(ByteBufCodecs.list()))
    );
    public static final Supplier<DataComponentType<Integer>> LINKED_MENU_INDEX = DATA_COMPONENTS.registerComponentType(
            "linked_menu_block", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    public static final Supplier<DataComponentType<RedstoneWandState.Type>> REDSTONE_WAND_MODE = DATA_COMPONENTS.registerComponentType(
            "redstone_wand_mode", builder -> builder
                    .persistent(RedstoneWandState.Type.CODEC)
                    .networkSynchronized(RedstoneWandState.Type.STREAM_CODEC)
    );

    public static final DeferredBlock<AutoAnvilBlock> AUTO_ANVIL = BLOCKS.registerBlock("auto_anvil", AutoAnvilBlock::new,
            props -> props.noOcclusion());
    public static final DeferredItem<BlockItem> AUTO_ANVIL_ITEM = ITEMS.registerSimpleBlockItem(AUTO_ANVIL);

    public static final DeferredItem<RemoteControlItem> REMOTE_CONTROL = ITEMS.registerItem("remote_control", RemoteControlItem::new,
            props -> props.stacksTo(1).enchantable(10));
    public static final DeferredItem<RedstoneWandItem> REDSTONE_WAND = ITEMS.registerItem("redstone_wand", RedstoneWandItem::new,
            props -> props.stacksTo(1));

    public static final Supplier<BlockEntityType<AutoAnvilBlockEntity>> AUTO_ANVIL_TE = BLOCK_ENTITIES.register("auto_anvil",
            () -> new BlockEntityType<>(AutoAnvilBlockEntity::new, AUTO_ANVIL.get()));

    public static final Supplier<MenuType<AutoAnvilMenu>> AUTO_ANVIL_MENU = MENU_TYPES.register("auto_anvil",
            () -> IMenuTypeExtension.create(AutoAnvilMenu::new));

    public static final Supplier<AttachmentType<Long2ObjectMap<RedstoneWandState>>> REDSTONE_WAND_ON_BLOCKS = ATTACHMENT_TYPES.register(
            "redstone_wand_on_blocks", () -> AttachmentType.<Long2ObjectMap<RedstoneWandState>>builder(() -> new Long2ObjectOpenHashMap<>())
                    .serialize(Codec.withAlternative(Util.WAND_STATES_CODEC, Util.WAND_STATES_OLD_CODEC).fieldOf("redstone_wand_on_blocks"))
                    .sync(Util.WAND_STATES_STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> REMOTE_RANGE = ENCHANTMENT_COMPONENT_TYPES.registerComponentType(
            "remote_range", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC).listOf())
    );

    public static final Supplier<DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> REMOTE_CONTROL_CAPACITY = ENCHANTMENT_COMPONENT_TYPES.registerComponentType(
            "remote_control_capacity", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC).listOf())
    );

    public static final DeferredHolder<FluidType, FluidType> FLUID_XP_TYPE = FLUID_TYPES.register("fluid_xp", () -> new FluidType(FluidType.Properties.create()
            .temperature(300)
            .lightLevel(10)
            .viscosity(1500)
            .density(800)
            .canConvertToSource(false)
            .canDrown(false)
            .canSwim(true)
            .descriptionId("block.landsutilities.fluid_xp")
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.EXPERIENCE_ORB_PICKUP)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.PLAYER_LEVELUP))
    {

    });

    private static BaseFlowingFluid.Properties fluidXpProperties;
    public static final Supplier<FlowingFluid> FLUID_XP_STILL = FLUIDS.register("fluid_xp", () -> new BaseFlowingFluid.Source(fluidXpProperties));
    public static final Supplier<FlowingFluid> FLUID_XP_FLOWING = FLUIDS.register("fluid_xp_flowing", () -> new BaseFlowingFluid.Flowing(fluidXpProperties));

    public static final DeferredBlock<LiquidBlock> FLUID_XP_BLOCK = BLOCKS.registerBlock(
            "fluid_xp",
            props -> new LiquidBlock(FLUID_XP_STILL.get(), props),
            props -> props.liquid().noCollision().replaceable().strength(100.0f).pushReaction(PushReaction.DESTROY).noLootTable()
    );
    public static final DeferredItem<BucketItem> FLUID_XP_BUCKET = ITEMS.registerItem(
            "fluid_xp_bucket",
            props -> new BucketItem(FLUID_XP_STILL.get(), props),
            props -> props.craftRemainder(Items.BUCKET).stacksTo(1)
    );

    static {
        fluidXpProperties = new BaseFlowingFluid.Properties(FLUID_XP_TYPE, FLUID_XP_STILL, FLUID_XP_FLOWING)
                .block(FLUID_XP_BLOCK);
    }


    public static final Supplier<CreativeModeTab> TAB = CREATIVE_TABS.register("landsutilities", () ->
            CreativeModeTab.builder()
                    .icon(AUTO_ANVIL::toStack)
                    .title(Component.translatable("tab.landsutilities"))
                    .displayItems((params, out) -> {
                        out.accept(AUTO_ANVIL);
                        out.accept(REMOTE_CONTROL);
                        out.accept(REDSTONE_WAND);
                        out.accept(FLUID_XP_BUCKET);
                    })
                    .build());

    public LandsUtilities(IEventBus modEventBus, ModContainer modContainer) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        ENCHANTMENT_COMPONENT_TYPES.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    private static void registerPacketHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playBidirectional(IOConfigPacket.TYPE, IOConfigPacket.STREAM_CODEC, IOConfigPacket::handle, IOConfigPacket::handle);
        registrar.playBidirectional(RedstoneConfigPacket.TYPE, RedstoneConfigPacket.STREAM_CODEC, RedstoneConfigPacket::handle, RedstoneConfigPacket::handle);
        registrar.playToServer(MouseWheelPacket.TYPE, MouseWheelPacket.STREAM_CODEC, MouseWheelPacket::handle);
        registrar.playToClient(RemoteAdvancedMenuPacket.TYPE, RemoteAdvancedMenuPacket.STREAM_CODEC, RemoteAdvancedMenuPacket::handle);
        registrar.playToClient(RemoteMenuPacket.TYPE, RemoteMenuPacket.STREAM_CODEC, RemoteMenuPacket::handle);
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, AUTO_ANVIL_TE.get(), (te, dir) -> te.automationItemHandler());
    }

    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal(MODID)
                        .then(RemoteRenameLinkCommand.register(dispatcher))
                        .then(RemoteDeleteLinkCommand.register(dispatcher))
        );
    }
}
