package org.eln.eln3.sim.process.watchdog

import org.eln.eln3.sim.mna.state.VoltageState

class VoltageStateWatchDog(var state: VoltageState): ValueWatchdog() {

    override fun getValue(): Double {
        return state.voltage
    }

    fun setNominalVoltage(nominalVoltage: Double): VoltageStateWatchDog {
        max = nominalVoltage * 1.3
        min = -nominalVoltage * 1.3
        timeoutReset = nominalVoltage * 0.05 * 5
        return this
    }
}
