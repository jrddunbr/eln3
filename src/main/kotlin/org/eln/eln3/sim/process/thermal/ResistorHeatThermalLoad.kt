package org.eln.eln3.sim.process.thermal

import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.thermal.ThermalLoad
import org.eln.eln3.sim.mna.component.Resistor

class ResistorHeatThermalLoad(var resistor: Resistor, var load: ThermalLoad) : IProcess {
    override fun process(time: Double) {
        load.addThermalPower(resistor.power)
    }
}