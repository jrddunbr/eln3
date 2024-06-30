package org.eln.eln3.technical

import org.eln.eln3.Eln3
import org.eln.eln3.position.Direction
import org.eln.eln3.position.LRDU
import org.eln.eln3.sim.ElectricalConnection
import org.eln.eln3.sim.ThermalConnection

class TechnicalConnection(var N1: TechnicalBase, var dir1: Direction, var lrdu1: LRDU, var N2: TechnicalBase, var dir2: Direction, var lrdu2: LRDU) {
    var EC: MutableList<ElectricalConnection> = ArrayList()
    var TC: MutableList<ThermalConnection> = ArrayList()
    fun destroy() {
        for (ec in EC) Eln3.simulator.removeElectricalComponent(ec)
        for (tc in TC) Eln3.simulator.removeThermalConnection(tc)
        N1.externalDisconnect(dir1, lrdu1)
        N2.externalDisconnect(dir2, lrdu2)
    }


    fun addConnection(ec: ElectricalConnection) {
        EC.add(ec)
    }

    fun addConnection(tc: ThermalConnection) {
        TC.add(tc)
    }
}
