package org.eln.eln3.sim.process.heater

import org.eln.eln3.sim.ElectricalLoad
import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.ThermalLoad


class ElectricalLoadHeatThermalLoad(var resistor: ElectricalLoad, var load: ThermalLoad) : IProcess {
    override fun process(time: Double) {
        if (resistor.isNotSimulated) return
        val current = resistor.current
        // println("Moving heat: ${current * current * resistor.serialResistance * 2} watts at $resistor $load")
        load.movePowerTo(current * current * resistor.serialResistance * 2)
    }
}
