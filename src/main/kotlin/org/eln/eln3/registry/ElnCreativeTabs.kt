package org.eln.eln3.registry

import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.neoforge.registries.DeferredHolder
import org.eln.eln3.Eln3.Companion.CREATIVE_MODE_TABS
import org.eln.eln3.Eln3.Companion.MODID
import org.eln.eln3.registry.ElnBlockEntities.SIMPLE_TEST_BLOCK_ITEM
import org.eln.eln3.registry.ElnBlocks.CABLE_ITEM
import org.eln.eln3.registry.ElnBlocks.GROUND_ITEM
import org.eln.eln3.registry.ElnBlocks.VS_ITEM
import org.eln.eln3.registry.ElnItems.VOLTMETER_ITEM
import java.util.function.Supplier

object ElnCreativeTabs {
    val ELN3_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register(
        MODID,
        Supplier {
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.$MODID")) //The language key for the title of your CreativeModeTab
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon { CABLE_ITEM.get().defaultInstance }
                .displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                    output.accept(VS_ITEM.get())
                    output.accept(CABLE_ITEM.get())
                    output.accept(GROUND_ITEM.get())
                    output.accept(VOLTMETER_ITEM.get())
                    output.accept(SIMPLE_TEST_BLOCK_ITEM.get())
                }.build()
        })
}