package org.eln.eln3

import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.level.block.entity.BlockEntityType
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
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.registries.DeferredRegister
import org.eln.eln3.compat.TopCompat
import org.eln.eln3.registry.ElnBlockEntities
import org.eln.eln3.registry.ElnBlocks
import org.eln.eln3.registry.ElnCreativeTabs
import org.eln.eln3.registry.ElnItems
import org.eln.eln3.sim.MnaConst
import org.eln.eln3.sim.Simulator
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS


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

        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)
        val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID)

        // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
        @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
        object ClientModEvents {
            @SubscribeEvent
            fun onClientSetup(event: FMLClientSetupEvent?) {
                // Some client setup code
                //LOGGER.info("HELLO FROM CLIENT SETUP")
                //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
            }
        }
    }

    init {
        modEventBus.addListener(::commonSetup) // Register the commonSetup method for modloading
        BLOCKS.register(modEventBus) // Register the Deferred Register to the mod event bus so blocks get registered
        ITEMS.register(modEventBus) // Register the Deferred Register to the mod event bus so items get registered
        BLOCK_ENTITY_TYPES.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus) // Register the Deferred Register to the mod event bus so tabs get registered

        // Pull in our registries. Mentioning them brings them in.
        ElnItems
        ElnBlocks
        ElnBlockEntities
        ElnCreativeTabs

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this)
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC) // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        NeoForge.EVENT_BUS.register(simulator)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        //LOGGER.info("HELLO FROM COMMON SETUP")
        TopCompat.register()
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting")
    }

    @SubscribeEvent
    fun onServerStopped(event: ServerStoppedEvent) {
        //LOGGER.info("HELLO from server stopping")
    }
}