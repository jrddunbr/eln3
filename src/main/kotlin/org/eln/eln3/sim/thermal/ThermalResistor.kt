package org.eln.eln3.sim.thermal

import org.eln.eln3.sim.IProcess

class ThermalResistor(var a: ThermalLoad, var b: ThermalLoad) : IProcess {
    var thermalResistance: Double = 0.0
    val thermalResistanceInverse: Double
        get() = 1 / thermalResistance

    init {
        highImpedance()
    }

    override fun process(time: Double) {
        val power = (a.temperatureCelsius - b.temperatureCelsius) * thermalResistanceInverse
        a.netThermalPowerAccumulator = a.netThermalPowerAccumulator - power
        b.netThermalPowerAccumulator = b.netThermalPowerAccumulator + power
    }

    val power: Double
        get() = (a.temperatureCelsius - b.temperatureCelsius) * thermalResistanceInverse

    fun highImpedance() {
        thermalResistance = ThermalLoad.HIGH_THERMAL_RESISTANCE
    }
}