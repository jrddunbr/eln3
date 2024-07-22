package org.eln.eln3

import com.mojang.logging.LogUtils
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.eln.eln3.compat.TopCompat
import org.eln.eln3.sim.MnaConst
import org.eln.eln3.sim.Simulator
import org.eln.eln3.single.*
import org.eln.eln3.single.CableBlock
import org.eln.eln3.singleentity.SingleEntityTestBlock
import org.eln.eln3.singleentity.SingleEntityTestBlockEntity
import org.eln.eln3.technical.TechnicalManager
import java.util.function.Supplier

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Eln3.MODID)
class Eln3

    (modEventBus: IEventBus, modContainer: ModContainer) {
    companion object {
        // Define mod id in a common place for everything to reference
        const val MODID = "eln3"
        // Directly reference a slf4j logger
        val LOGGER = LogUtils.getLogger();

        @JvmStatic
        val simulator = Simulator(
            MnaConst.ELECTRICAL_FREQUENCY,
            MnaConst.ELECTRICAL_FREQUENCY,
            MnaConst.ELECTRICAL_OVERSAMPLING.toInt(),
            MnaConst.THERMAL_FREQUENCY
        )

        // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID)

        // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
        val TEST_BLOCK: DeferredBlock<Block> = BLOCKS.registerSimpleBlock("test_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))
        // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("test_block", TEST_BLOCK)

        val TEST_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem("test_item")

        val stbs: Supplier<SingleEntityTestBlock> = Supplier { SingleEntityTestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)) }
        val SIMPLE_TEST_BLOCK = BLOCKS.register("simple_test_block", stbs)

        val SIMPLE_TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(SIMPLE_TEST_BLOCK)

        val singleEntityBlockEntityProvider = fun (pos: BlockPos, state: BlockState): SingleEntityTestBlockEntity {
            return SingleEntityTestBlockEntity(SIMPLE_BLOCK_ENTITY.get(), pos, state)
        }

        val SIMPLE_BLOCK_ENTITY: Supplier<BlockEntityType<SingleEntityTestBlockEntity>> =
            BLOCK_ENTITY_TYPES.register("simple_block_entity", Supplier { BlockEntityType.Builder.of(
                singleEntityBlockEntityProvider,
                SIMPLE_TEST_BLOCK.get()
            ).build(null) })

        val cableBlock: Supplier<CableBlock> = Supplier { CableBlock() }
        val CABLE_BLOCK = BLOCKS.register("cable_block", cableBlock)
        val CABLE_ITEM = ITEMS.registerSimpleBlockItem(CABLE_BLOCK)

        val voltageSourceBlock: Supplier<VoltageSourceBlock> = Supplier { VoltageSourceBlock() }
        val VS_BLOCK = BLOCKS.register("voltage_source_block", voltageSourceBlock)
        val VS_ITEM = ITEMS.registerSimpleBlockItem(VS_BLOCK)

        val groundBlock: Supplier<GroundBlock> = Supplier { GroundBlock() }
        val GROUND_BLOCK = BLOCKS.register("ground_block", groundBlock)
        val GROUND_ITEM = ITEMS.registerSimpleBlockItem(GROUND_BLOCK)

        // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
        val ELN3_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register(
            MODID,
            Supplier {
                CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.$MODID")) //The language key for the title of your CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon { TEST_ITEM.get().defaultInstance }
                    .displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                        output.accept(TEST_ITEM.get()) // Add the example item to the tab. For your own tabs, this method is preferred over the event
                        output.accept(SIMPLE_TEST_BLOCK_ITEM.get())
                        output.accept(CABLE_ITEM.get())
                        output.accept(VS_ITEM.get())
                        output.accept(GROUND_ITEM.get())
                    }.build()
            })

        // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
        @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
        object ClientModEvents {
            @SubscribeEvent
            fun onClientSetup(event: FMLClientSetupEvent?) {
                // Some client setup code
                LOGGER.info("HELLO FROM CLIENT SETUP")
                LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
            }
        }
    }

    init {
        modEventBus.addListener(::commonSetup) // Register the commonSetup method for modloading
        BLOCKS.register(modEventBus) // Register the Deferred Register to the mod event bus so blocks get registered
        ITEMS.register(modEventBus) // Register the Deferred Register to the mod event bus so items get registered
        BLOCK_ENTITY_TYPES.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus) // Register the Deferred Register to the mod event bus so tabs get registered
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this)
        modEventBus.addListener(::addCreative) // Register the item to a creative tab
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC) // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        TechnicalManager.instance = TechnicalManager()
        NeoForge.EVENT_BUS.register(simulator)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP")
        TopCompat.register()
    }

    // Add the example block item to the building blocks tab
    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey === CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM)
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting")
    }
}