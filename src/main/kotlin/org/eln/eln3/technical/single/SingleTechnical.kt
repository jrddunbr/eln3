package org.eln.eln3.technical.single

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.Eln3
import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.ThermalConnection
import org.eln.eln3.sim.mna.component.Component
import org.eln.eln3.sim.mna.state.State
import org.eln.eln3.sim.nbt.NbtThermalLoad
import org.eln.eln3.sim.nbt.TagSerializable
import org.eln.eln3.technical.ITechnicalBlock
import org.eln.eln3.technical.TechnicalBase

open class SingleTechnical(
    block: ITechnicalBlock,
    state: BlockState,
    pos: BlockPos,
    level: Level,
    uuid: String
): TechnicalBase(block, state, null, pos, level, uuid), TagSerializable {

    var slowProcessList = ArrayList<IProcess>(4)
    var electricalProcessList = ArrayList<IProcess>(4)
    var electricalComponentList = ArrayList<Component>(4)
    var electricalLoadList = ArrayList<State>(4)
    var thermalFastProcessList = ArrayList<IProcess>(4)
    var thermalSlowProcessList = ArrayList<IProcess>(4)
    var thermalConnectionList = ArrayList<ThermalConnection>(4)
    var thermalLoadList = ArrayList<NbtThermalLoad>(4)

    override fun connectJob() {
        super.connectJob()
        Eln3.simulator.addAllSlowProcess(slowProcessList)
        Eln3.simulator.addAllElectricalComponent(electricalComponentList)
        for (load in electricalLoadList) Eln3.simulator.addElectricalLoad(load)
        Eln3.simulator.addAllElectricalProcess(electricalProcessList)
        Eln3.simulator.addAllThermalConnection(thermalConnectionList)
        for (load in thermalLoadList) Eln3.simulator.addThermalLoad(load)
        Eln3.simulator.addAllThermalFastProcess(thermalFastProcessList)
        Eln3.simulator.addAllThermalSlowProcess(thermalSlowProcessList)
    }

    override fun disconnectJob() {
        super.disconnectJob()
        Eln3.simulator.removeAllSlowProcess(slowProcessList)
        Eln3.simulator.removeAllElectricalComponent(electricalComponentList)
        for (load in electricalLoadList) Eln3.simulator.removeElectricalLoad(load)
        Eln3.simulator.removeAllElectricalProcess(electricalProcessList)
        Eln3.simulator.removeAllThermalConnection(thermalConnectionList)
        for (load in thermalLoadList) Eln3.simulator.removeThermalLoad(load)
        Eln3.simulator.removeAllThermalFastProcess(thermalFastProcessList)
        Eln3.simulator.removeAllThermalSlowProcess(thermalSlowProcessList)
    }

    override fun loadAdditionalData(nbt: CompoundTag, str: String) {
        // TODO: Read front from NBT
        electricalLoadList.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
        thermalLoadList.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
        electricalComponentList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
        slowProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
        electricalProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
        thermalFastProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
        thermalSlowProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).loadAdditionalData(nbt, "")
        }
    }

    override fun saveAdditionalData(nbt: CompoundTag, str: String) {
        // TODO: Write front to NBT
        electricalLoadList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
        thermalLoadList.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
        electricalComponentList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
        slowProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
        electricalProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
        thermalFastProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
        thermalSlowProcessList.filter {it is TagSerializable}.forEach {
            (it as TagSerializable).saveAdditionalData(nbt, "")
        }
    }
}