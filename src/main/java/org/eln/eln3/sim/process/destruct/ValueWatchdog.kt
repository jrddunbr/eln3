package org.eln.eln3.sim.process.destruct

import org.eln.eln3.Config
import org.eln.eln3.Eln3
import org.eln.eln3.misc.Utils
import org.eln.eln3.sim.IProcess

abstract class ValueWatchdog : IProcess {
    private var destructible: IDestructible? = null
    var min = 0.0
    var max = 0.0
    var timeoutReset = 2.0
    var timeout = 0.0
    var boot = true

    // TODO: Rename. Hysteresis?
    private var joker = true
    override fun process(time: Double) {
        if (boot) {
            boot = false
            timeout = timeoutReset
        }
        val value = getValue()
        var overflow = (value - max).coerceAtLeast(min - value)
        if (overflow > 0) {
            if (joker) {
                joker = false
                overflow = 0.0
            }
        } else {
            joker = true
        }
        timeout -= time * overflow * Utils.rand(0.5, 1.5)
        if (timeout > timeoutReset) {
            timeout = timeoutReset
        }
        if (timeout < 0) {
            Eln3.LOGGER.info("${javaClass.simpleName} destroying $destructible")
            if (!Config.vwdTesting) destructible?.destructImpl()
        }
    }

    fun setDestroys(destructible: IDestructible): ValueWatchdog {
        this.destructible = destructible
        return this
    }

    abstract fun getValue(): Double

    fun reset() {
        boot = true
    }
}
