package org.eln.eln3.single

import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.ProbeMode
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.misc.Utils
import org.eln.eln3.position.LRDU
import org.eln.eln3.sim.ElectricalLoad
import org.eln.eln3.sim.MnaConst
import org.eln.eln3.sim.nbt.NbtElectricalLoad
import org.eln.eln3.technical.ITechnicalBlock
import org.eln.eln3.technical.ITechnicalEntity
import org.eln.eln3.technical.TechnicalBase
import org.eln.eln3.technical.single.SingleBlock
import org.eln.eln3.technical.single.SingleTechnical
import java.util.UUID

class CableBlock: SingleBlock() {
    override fun newTechnical(
        state: BlockState,
        blockPos: BlockPos,
        level: Level,
        entity: ITechnicalEntity?
    ): TechnicalBase {
        return CableTechnical(this, state, blockPos, level, UUID.randomUUID().toString())
    }
}

class CableTechnical(block: ITechnicalBlock, state: BlockState, pos: BlockPos, level: Level, uuid: String):
    SingleTechnical(block, state, pos, level, uuid) {

    var electricalLoad = NbtElectricalLoad("electricalLoad")


    init {
        electricalLoad.setCanBeSimplifiedByLine(true)
        electricalLoad.serialResistance = MnaConst.cableImpedance
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
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data)
        probeInfo.text(Utils.plotVolt(electricalLoad.voltage))
        probeInfo.text(Utils.plotAmpere(electricalLoad.current))
        probeInfo.text(Utils.plotOhm(electricalLoad.serialResistance))
    }

    override fun getLabelString(side: Direction?): String {
        return Utils.plotOhm(electricalLoad.blockResistance)
    }

    override fun getVoltmeterString(side: Direction?): String {
        return Utils.plotVolt(electricalLoad.voltage)
    }

    override fun getAmmeterString(side: Direction?): String {
        return Utils.plotAmpere(electricalLoad.current)
    }

    override fun getClampingVoltmeterString(side: Direction?): String {
        return "${super.getClampingVoltmeterString(side)} ${Utils.plotPower(electricalLoad.voltage * electricalLoad.current)}"
    }
}