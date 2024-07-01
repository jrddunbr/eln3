package org.eln.eln3.technical.single

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

open class SingleBlockEntity(pType: BlockEntityType<*>, pPos: BlockPos, pBlockState: BlockState) :
    BlockEntity(pType, pPos, pBlockState) {
}