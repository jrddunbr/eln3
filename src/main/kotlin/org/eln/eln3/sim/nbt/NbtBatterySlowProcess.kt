package org.eln.eln3.sim.nbt

import org.eln.eln3.sim.BatteryProcess
import org.eln.eln3.sim.BatterySlowProcess
import org.eln.eln3.sim.thermal.ThermalLoad
import org.eln.eln3.technical.TechnicalBase

class NbtBatterySlowProcess(
    var node: TechnicalBase,
    batteryProcess: BatteryProcess,
    thermalLoad: ThermalLoad
) : BatterySlowProcess(batteryProcess, thermalLoad) {

    private var explosionRadius = 2f

    override fun destroy() {
        node.physicalSelfDestruction(explosionRadius)
    }
}
