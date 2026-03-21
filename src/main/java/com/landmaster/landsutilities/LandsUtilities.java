package com.landmaster.landsutilities;

import com.landmaster.landsutilities.block.AutoAnvilBlock;
import com.landmaster.landsutilities.block.entity.AutoAnvilBlockEntity;
import com.landmaster.landsutilities.item.RemoteControlItem;
import com.landmaster.landsutilities.menu.AutoAnvilMenu;
import com.landmaster.landsutilities.network.IOConfigPacket;
import com.landmaster.landsutilities.network.RedstoneConfigPacket;
import com.landmaster.landsutilities.util.LocationAndFace;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

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

    public static final DeferredBlock<AutoAnvilBlock> AUTO_ANVIL = BLOCKS.registerBlock("auto_anvil", AutoAnvilBlock::new,
            BlockBehaviour.Properties.of().noOcclusion());
    public static final DeferredItem<BlockItem> AUTO_ANVIL_ITEM = ITEMS.registerSimpleBlockItem(AUTO_ANVIL);

    public static final DeferredItem<RemoteControlItem> REMOTE_CONTROL = ITEMS.registerItem("remote_control", RemoteControlItem::new);

    public static final Supplier<BlockEntityType<AutoAnvilBlockEntity>> AUTO_ANVIL_TE = BLOCK_ENTITIES.register("auto_anvil",
            () -> BlockEntityType.Builder.of(AutoAnvilBlockEntity::new, AUTO_ANVIL.get()).build(null));

    public static final Supplier<MenuType<AutoAnvilMenu>> AUTO_ANVIL_MENU = MENU_TYPES.register("auto_anvil",
            () -> IMenuTypeExtension.create(AutoAnvilMenu::new));

    public static final Supplier<CreativeModeTab> TAB = CREATIVE_TABS.register("landsutilities", () ->
            CreativeModeTab.builder()
                    .icon(AUTO_ANVIL::toStack)
                    .title(Component.translatable("tab.landsutilities"))
                    .displayItems((params, out) -> {
                        out.accept(AUTO_ANVIL);
                        out.accept(REMOTE_CONTROL);
                    })
                    .build());

    public static final Supplier<DataComponentType<LocationAndFace>> LINKED_MENU_BLOCK = DATA_COMPONENTS.registerComponentType(
            "linked_menu_block", builder -> builder
                    .persistent(LocationAndFace.CODEC)
                    .networkSynchronized(LocationAndFace.STREAM_CODEC)
    );

    public LandsUtilities(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    private static void registerPacketHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playBidirectional(IOConfigPacket.TYPE, IOConfigPacket.STREAM_CODEC, IOConfigPacket::handle);
        registrar.playBidirectional(RedstoneConfigPacket.TYPE, RedstoneConfigPacket.STREAM_CODEC, RedstoneConfigPacket::handle);
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AUTO_ANVIL_TE.get(), (te, dir) -> te.automationItemHandler());
    }
}
