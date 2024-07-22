package org.eln.eln3.registry

import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredItem
import org.eln.eln3.Eln3.Companion.ITEMS

object ElnItems {
    val VOLTMETER_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem("voltmeter", Item.Properties().durability(1024))
}