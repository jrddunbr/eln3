package org.eln.eln3.sim.thermal

import org.eln.eln3.sim.MnaConst

object ThermalValidator {
    private fun getMinimalThermalC(Rs: Double, Rp: Double): Double {
        return MnaConst.THERMAL_FREQUENCY * 3 / ((1 / (1 / Rp + 1 / Rs)))
    }

    @JvmStatic
    fun checkThermalLoad(thermalRs: Double, thermalRp: Double, thermalC: Double): Boolean {
        check(!(thermalC < getMinimalThermalC(thermalRs, thermalRp))) { "Thermal load outside safe limits." }
        return true
    }
}