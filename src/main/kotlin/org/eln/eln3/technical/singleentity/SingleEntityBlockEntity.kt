package org.eln.eln3.technical.singleentity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.technical.TechnicalBase
import org.eln.eln3.technical.TechnicalManager

open class SingleEntityBlockEntity(pType: BlockEntityType<*>, val pPos: BlockPos, pBlockState: BlockState) :
    BlockEntity(pType, pPos, pBlockState) {

        fun getTechnicalReference(): TechnicalBase? {
            return TechnicalManager.instance?.getTechnicalsFromLocation(
                pPos, level?: throw RuntimeException("Could not get level")
            )?.values?.firstOrNull()
        }
}