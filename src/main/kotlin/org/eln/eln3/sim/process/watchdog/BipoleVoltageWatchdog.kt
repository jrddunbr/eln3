package org.eln.eln3.sim.process.watchdog

import org.eln.eln3.sim.mna.component.Bipole

class BipoleVoltageWatchdog(var bipole: Bipole) : ValueWatchdog() {

    fun setNominalVoltage(nominalVoltage: Double): BipoleVoltageWatchdog {
        max = nominalVoltage * 1.3
        min = -max
        timeoutReset = nominalVoltage * 0.10 * 5
        return this
    }

    override fun getValue(): Double {
        return bipole.voltage
    }
}
