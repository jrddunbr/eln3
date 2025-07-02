package org.eln.eln3.sim.nbt

import net.minecraft.nbt.CompoundTag
import org.eln.eln3.sim.process.thermal.FurnaceProcess
import org.eln.eln3.sim.thermal.ThermalLoad

class NbtFurnaceProcess(var name: String?, load: ThermalLoad) : FurnaceProcess(load), TagSerializable {
    override fun loadAdditionalData(nbt: CompoundTag, str: String) {
        combustibleEnergy = nbt.getFloat(str + name + "Q").toDouble()
        setGain(nbt.getDouble(str + name + "gain"))
    }

    override fun saveAdditionalData(nbt: CompoundTag, str: String) {
        nbt.putFloat(str + name + "Q", combustibleEnergy.toFloat())
        nbt.putDouble(str + name + "gain", getGain())
    }
}