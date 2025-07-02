package org.eln.eln3.sim.thermal

class ThermalConnection
@JvmOverloads constructor(
    @JvmField var L1: ThermalLoad,
    @JvmField var L2: ThermalLoad,
    var thermalResistance: Double = 0.0  // K/W - thermal resistance of the connection path
)