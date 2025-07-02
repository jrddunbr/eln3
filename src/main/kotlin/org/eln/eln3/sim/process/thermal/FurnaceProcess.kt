package org.eln.eln3.sim.process.thermal

import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.thermal.ThermalLoad

open class FurnaceProcess(var load: ThermalLoad) : IProcess {
    var combustibleEnergy: Double = 0.0
    var nominalCombustibleEnergy: Double = 1.0
    var nominalPower: Double = 1.0
    private var gain = 1.0
    private var gainMin = 0.0

    override fun process(time: Double) {
        val energyConsumed = this.power * time
        combustibleEnergy -= energyConsumed
        load.netThermalPowerAccumulator = load.netThermalPowerAccumulator + energyConsumed / time
    }

    fun setGain(gain: Double) {
        var gain = gain
        if (gain < gainMin) gain = gainMin
        if (gain > 1.0) gain = 1.0
        this.gain = gain
    }

    fun setGainMin(gainMin: Double) {
        this.gainMin = gainMin
        setGain(getGain())
    }

    fun getGain(): Double {
        return gain
    }

    val power: Double
        get() = combustibleEnergy / nominalCombustibleEnergy * nominalPower * gain
}