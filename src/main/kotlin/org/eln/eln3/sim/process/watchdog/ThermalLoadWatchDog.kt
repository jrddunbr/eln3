package org.eln.eln3.sim.process.watchdog

import org.eln.eln3.sim.ThermalLoad

class ThermalLoadWatchDog(var state: ThermalLoad): ValueWatchdog() {

    override fun getValue(): Double {
        return state.temperature
    }

    /**
     * Sets maximum temperature limit with default minimum of -40°C.
     * @param maximumTemperature Maximum safe temperature in °C
     */
    fun setMaximumTemperature(maximumTemperature: Double): ThermalLoadWatchDog {
        max = maximumTemperature
        min = -40.0
        timeoutReset = maximumTemperature * 0.1 * 10
        return this
    }

    /**
     * Sets temperature limits manually.
     * @param maximumTemperature Maximum safe temperature in °C
     * @param minimumTemperature Minimum safe temperature in °C
     */
    fun setTemperatureLimits(maximumTemperature: Double, minimumTemperature: Double): ThermalLoadWatchDog {
        max = maximumTemperature
        min = minimumTemperature
        timeoutReset = max * 0.1 * 10
        return this
    }

    /**
     * Uses the operating temperature limits from the thermal load itself.
     * This is the preferred method when the ThermalLoad has been properly configured.
     */
    fun useLoadTemperatureLimits(): ThermalLoadWatchDog {
        max = state.maxOperatingTemperature
        min = state.minOperatingTemperature
        timeoutReset = max * 0.1 * 10
        return this
    }

    /**
     * Auto-configures limits based on the thermal load's operating limits.
     * Falls back to sensible defaults if limits aren't set on the load.
     */
    fun autoConfigureFromLoad(): ThermalLoadWatchDog {
        val maxTemp = if (state.maxOperatingTemperature == Double.MAX_VALUE) 85.0 else state.maxOperatingTemperature
        val minTemp = if (state.minOperatingTemperature == Double.MIN_VALUE) -40.0 else state.minOperatingTemperature

        max = maxTemp
        min = minTemp
        timeoutReset = max * 0.1 * 10
        return this
    }
}
