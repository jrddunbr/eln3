package org.eln.eln3.singleentity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.technical.ITechnicalEntity

class SingleEntityTestBlockEntity(pType: BlockEntityType<*>, pPos: BlockPos, pBlockState: BlockState) :
    BlockEntity(pType, pPos, pBlockState), ITechnicalEntity {
    }