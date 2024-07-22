package org.eln.eln3.sim

object MnaConst {
    const val ultraImpedance: Double = 1e16
    const val highImpedance: Double = 1e9
    const val pullDown: Double = 1e9
    const val cableImpedance: Double = 0.001 // TODO: Make this some copper resistance?
    const val noImpedance: Double = 1e-9

    // Voltage Levels
    const val SVU = 5.0
    const val LVU = 5.0
    const val MVU = 200.0
    const val HVU = 800.0
    const val VVU = 3200.0
    const val CCU = 120_000.0

    const val ELECTRICAL_FREQUENCY = 20.0
    const val ELECTRICAL_OVERSAMPLING = 50.0
    const val THERMAL_FREQUENCY = 400.0

    const val gateOutputCurrent = 0.100

    const val SVII = gateOutputCurrent / SVU
}
