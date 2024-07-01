package org.eln.eln3.single.cable

import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.ProbeMode
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.Eln3
import org.eln.eln3.misc.Utils
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

    override fun addProbeInfo(
        mode: ProbeMode?,
        probeInfo: IProbeInfo,
        player: Player?,
        world: Level?,
        blockState: BlockState,
        data: IProbeHitData
    ) {
        probeInfo.text(Utils.plotVolt(electricalLoad.voltage))
        probeInfo.text(Utils.plotAmpere(electricalLoad.current))
    }
}