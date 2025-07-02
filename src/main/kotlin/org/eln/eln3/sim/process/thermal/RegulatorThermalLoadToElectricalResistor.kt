package org.eln.eln3.sim.process.thermal

import org.eln.eln3.sim.mna.component.Resistor
import org.eln.eln3.sim.process.machine.RegulatorProcess
import org.eln.eln3.sim.thermal.ThermalLoad

class RegulatorThermalLoadToElectricalResistor(
    name: String?,
    var thermalLoad: ThermalLoad,
    var electricalResistor: Resistor
) : RegulatorProcess(name) {
    var minimumResistance: Double = 0.0

    override val hit: Double
        get() = thermalLoad.temperatureCelsius

    override fun setCmd(cmd: Double) {
        if (cmd <= 0.001) {
            electricalResistor.highImpedance()
        } else if (cmd >= 1.0) {
            electricalResistor.setResistance(minimumResistance)
        } else {
            electricalResistor.setResistance(minimumResistance / cmd)
        }
    }
}