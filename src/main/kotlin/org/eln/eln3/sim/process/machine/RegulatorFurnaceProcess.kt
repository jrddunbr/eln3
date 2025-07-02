package org.eln.eln3.sim.process.machine

import org.eln.eln3.sim.process.thermal.FurnaceProcess


class RegulatorFurnaceProcess(name: String?, var furnace: FurnaceProcess) : RegulatorProcess(name) {
    override val hit: Double
        get() = furnace.load.temperatureCelsius

    override fun setCmd(cmd: Double) {
        furnace.setGain(cmd)
    }
}
