package org.eln.eln3.sim.process.thermal

import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.mna.component.Resistor
import org.eln.eln3.sim.thermal.ThermalLoad

class ElectricalResistorHeatThermalLoad(var electricalResistor: Resistor, var thermalLoad: ThermalLoad) : IProcess {
    override fun process(time: Double) {
        thermalLoad.netThermalPowerAccumulator = thermalLoad.netThermalPowerAccumulator + electricalResistor.getPower()
    }
}