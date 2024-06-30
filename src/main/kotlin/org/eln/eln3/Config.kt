package org.eln.eln3

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

@EventBusSubscriber(modid = Eln3.MODID, bus = EventBusSubscriber.Bus.MOD)
object Config {
    private val BUILDER: ModConfigSpec.Builder = ModConfigSpec.Builder()

    private val DEBUG_MODE: ModConfigSpec.BooleanValue =
        BUILDER.comment("Enables Debugging Mode").define("debug", true)

    private val EXPLOSIONS: ModConfigSpec.BooleanValue =
        BUILDER.comment("Enables Explosions").define("explosions", false)

    private val VALUE_WATCHDOG_TESTING: ModConfigSpec.BooleanValue =
        BUILDER.comment("Disables ValueWatchdog removals").define("vwd_testing", true)

    private val BATTERY_AGING: ModConfigSpec.BooleanValue =
        BUILDER.comment("Disables Battery Aging").define("battery_aging", true)

    val SPEC: ModConfigSpec = BUILDER.build()

    var debug: Boolean = false
    var explosions: Boolean = false
    var vwdTesting: Boolean = false
    @JvmStatic
    var batteryAging: Boolean = false

    private fun validateItemName(obj: Any): Boolean {
        return obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
    }

    @SubscribeEvent
    fun onLoad(event: ModConfigEvent) {
        debug = DEBUG_MODE.get()
        explosions = EXPLOSIONS.get()
        vwdTesting = VALUE_WATCHDOG_TESTING.get()
        batteryAging = BATTERY_AGING.get()
    }
}