package org.eln.eln3.single.cable

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.Eln3
import org.eln.eln3.position.Direction
import org.eln.eln3.position.LRDU
import org.eln.eln3.sim.ElectricalLoad
import org.eln.eln3.sim.nbt.NbtElectricalLoad
import org.eln.eln3.technical.ITechnicalBlock
import org.eln.eln3.technical.ITechnicalEntity
import org.eln.eln3.technical.single.SingleBlock
import org.eln.eln3.technical.single.SingleBlockEntity
import org.eln.eln3.technical.single.SingleTechnical

class CableBlock: SingleBlock() {
    override fun getTechnical(): Class<*> {
        return CableTechnical::class.java
    }
}

class CableBlockEntity(pType: BlockEntityType<*>, pPos: BlockPos, pBlockState: BlockState) :
    SingleBlockEntity(pType, pPos, pBlockState) {}

class CableTechnical(uuid: String, block: ITechnicalBlock, state: BlockState, entity: ITechnicalEntity?, pos: BlockPos, level: Level):
    SingleTechnical(uuid, block, state, entity, pos, level) {

    var electricalLoad = NbtElectricalLoad("electricalLoad")


    init {
        electricalLoad.setCanBeSimplifiedByLine(true)
        electricalLoadList.add(electricalLoad)
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad? {
        return electricalLoad
    }

    override fun getSideConnectionMask(side: Direction, lrdu: LRDU): Int {
        Eln3.LOGGER.info("Connection mask: ${Companion.maskElectricalAll}")
        return Companion.maskElectricalAll
    }
}