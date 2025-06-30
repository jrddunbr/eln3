package org.eln.eln3.single

import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.ProbeMode
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.misc.Utils
import org.eln.eln3.position.Direction
import org.eln.eln3.position.LRDU
import org.eln.eln3.sim.ElectricalLoad
import org.eln.eln3.sim.nbt.NbtElectricalLoad
import org.eln.eln3.technical.ITechnicalBlock
import org.eln.eln3.technical.ITechnicalEntity
import org.eln.eln3.technical.TechnicalBase
import org.eln.eln3.technical.single.SingleBlock
import org.eln.eln3.technical.single.SingleTechnical

class ResistorBlock: SingleBlock(){
    override fun newTechnical(
        state: BlockState,
        blockPos: BlockPos,
        level: Level,
        entity: ITechnicalEntity?
    ): TechnicalBase {
        return ResistorTechnical(this, state, blockPos, level)
    }
}

class ResistorTechnical(block: ITechnicalBlock, state: BlockState, pos: BlockPos, level: Level):
    SingleTechnical(block, state, pos, level) {

    var electricalLoad = NbtElectricalLoad("Resistor")

    init {
        electricalLoad.setCanBeSimplifiedByLine(true)
        electricalLoad.serialResistance = 100.0
        electricalLoadList.add(electricalLoad)
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad? {
        return electricalLoad
    }

    override fun getSideConnectionMask(side: Direction, lrdu: LRDU): Int {
        return maskElectricalAll
    }

    override fun addProbeInfo(
        mode: ProbeMode?,
        probeInfo: IProbeInfo,
        player: Player?,
        world: Level?,
        blockState: BlockState,
        data: IProbeHitData
    ) {
        probeInfo.text(Utils.plotAmpere(electricalLoad.current))
        probeInfo.text(Utils.plotOhm(electricalLoad.serialResistance))
        // P = I^2 * R
        probeInfo.text(Utils.plotPower("Heat Loss", electricalLoad.current * electricalLoad.current * electricalLoad.serialResistance))
    }

    override fun getVoltmeterString(side: net.minecraft.core.Direction?): String {
        return ""
    }
}
