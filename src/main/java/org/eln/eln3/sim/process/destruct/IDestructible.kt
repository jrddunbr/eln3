package org.eln.eln3.sim.process.destruct

interface IDestructible {
    fun destructImpl()
    fun describe(): String?
}
