package org.eln.eln3.sim.process.thermal

import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.ThermalLoad
import org.eln.eln3.sim.mna.component.Resistor

class DiodeHeatThermalLoad(var resistor: Resistor, var load: ThermalLoad) : IProcess {
    var lastResistance: Double

    init {
        lastResistance = resistor.resistance
    }

    override fun process(time: Double) {
        if (resistor.resistance == lastResistance) {
            load.addThermalPower(resistor.power)
        } else {
            lastResistance = resistor.resistance
        }
    }
}