package org.eln.eln3.sim.process.destruct

import org.eln.eln3.Eln3
import org.eln.eln3.sim.IProcess

class DelayedDestruction(val dest: IDestructible, var timeout: Double): IProcess {
    init {
        Eln3.simulator.addSlowProcess(this)
    }

    override fun process(time: Double) {
        timeout -= time
        if(timeout <= 0.0) {
            dest.destructImpl()
            Eln3.simulator.removeSlowProcess(this)
        }
    }
}
