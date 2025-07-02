package org.eln.eln3.sim.nbt

import net.minecraft.nbt.CompoundTag
import org.eln.eln3.misc.FunctionTable
import org.eln.eln3.sim.BatteryProcess
import org.eln.eln3.sim.thermal.ThermalLoad
import org.eln.eln3.sim.mna.component.VoltageSource
import org.eln.eln3.sim.mna.state.VoltageState

class NbtBatteryProcess(
    positiveLoad: VoltageState?,
    negativeLoad: VoltageState?,
    voltageFunction: FunctionTable,
    IMax: Double,
    voltageSource: VoltageSource,
    thermalLoad: ThermalLoad
) : BatteryProcess(positiveLoad, negativeLoad, voltageFunction, IMax, voltageSource, thermalLoad), TagSerializable {

    override fun loadAdditionalData(nbt: CompoundTag, str: String) {
        Q = nbt.getDouble(str + "NBP" + "Q")
        if (!Q.isFinite()) Q = 0.0
        life = nbt.getDouble(str + "NBP" + "life")
        if (!life.isFinite()) life = 1.0
    }

    override fun saveAdditionalData(nbt: CompoundTag, str: String) {
        nbt.putDouble(str + "NBP" + "Q", Q)
        nbt.putDouble(str + "NBP" + "life", life)
    }
}
