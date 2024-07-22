package org.eln.eln3.registry

import org.eln.eln3.Eln3.Companion.BLOCKS
import org.eln.eln3.Eln3.Companion.ITEMS
import org.eln.eln3.single.CableBlock
import org.eln.eln3.single.GroundBlock
import org.eln.eln3.single.VoltageSourceBlock
import java.util.function.Supplier

object ElnBlocks {
    val voltageSourceBlock: Supplier<VoltageSourceBlock> = Supplier { VoltageSourceBlock() }
    val VS_BLOCK = BLOCKS.register("voltage_source_block", voltageSourceBlock)
    val VS_ITEM = ITEMS.registerSimpleBlockItem(VS_BLOCK)

    val cableBlock: Supplier<CableBlock> = Supplier { CableBlock() }
    val CABLE_BLOCK = BLOCKS.register("cable_block", cableBlock)
    val CABLE_ITEM = ITEMS.registerSimpleBlockItem(CABLE_BLOCK)

    val groundBlock: Supplier<GroundBlock> = Supplier { GroundBlock() }
    val GROUND_BLOCK = BLOCKS.register("ground_block", groundBlock)
    val GROUND_ITEM = ITEMS.registerSimpleBlockItem(GROUND_BLOCK)
}