package org.eln.eln3.sim.fsm

interface State {
    fun enter()
    fun state(time: Double): State?
    fun leave()
}
