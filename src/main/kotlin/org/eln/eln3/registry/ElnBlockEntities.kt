package org.eln.eln3.registry

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import org.eln.eln3.Eln3.Companion.BLOCKS
import org.eln.eln3.Eln3.Companion.BLOCK_ENTITY_TYPES
import org.eln.eln3.Eln3.Companion.ITEMS
import org.eln.eln3.singleentity.SingleEntityTestBlock
import org.eln.eln3.singleentity.SingleEntityTestBlockEntity
import java.util.function.Supplier

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object ElnBlockEntities {

    val simpleTestBlockSupplier: Supplier<SingleEntityTestBlock> = Supplier { SingleEntityTestBlock(
        BlockBehaviour.Properties.of().mapColor(
            MapColor.STONE)) }
    val SIMPLE_TEST_BLOCK = BLOCKS.register("simple_test_block", simpleTestBlockSupplier)
    val SIMPLE_TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(SIMPLE_TEST_BLOCK)
    val singleEntityBlockEntityProvider = fun (pos: BlockPos, state: BlockState): SingleEntityTestBlockEntity {
        return SingleEntityTestBlockEntity(SIMPLE_BLOCK_ENTITY.get(), pos, state)
    }
    val SIMPLE_BLOCK_ENTITY: Supplier<BlockEntityType<SingleEntityTestBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("simple_block_entity", Supplier { BlockEntityType.Builder.of(
            singleEntityBlockEntityProvider,
            SIMPLE_TEST_BLOCK.get()
        ).build(null) })
}