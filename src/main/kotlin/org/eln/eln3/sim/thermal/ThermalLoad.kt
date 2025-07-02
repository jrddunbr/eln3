package org.eln.eln3.sim.thermal

import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.thermal.ThermalConnection
import kotlin.math.abs

open class ThermalLoad {
    // === Thermal State Variables ===
    /**
     * Current temperature in Celsius (°C).
     * Note: Internal calculations should consider using Kelvin for absolute temperature.
     */
    var temperatureCelsius: Double

    // === Thermal Properties ===
    /**
     * Thermal resistance to ambient environment in K/W (Kelvin per Watt).
     * Represents how this thermal load loses heat to the surrounding environment.
     * Higher values = better insulation from ambient.
     */
    var thermalResistanceToAmbient: Double = 0.0

    /**
     * Internal thermal resistance in K/W (Kelvin per Watt).
     * Represents thermal resistance within this load (e.g., junction to case).
     * Currently used in thermal connection calculations.
     */
    var internalThermalResistance: Double = 0.0

    /**
     * Heat capacity in J/K (Joules per Kelvin).
     * Determines how much energy is needed to change temperature by 1K.
     * Higher values = slower temperature changes.
     */
    var heatCapacity: Double = 0.0

    // === Power Values (Previous Simulation Step) ===
    /**
     * Net thermal power from previous simulation step in W (Watts).
     * Positive = heating, Negative = cooling.
     */
    var netThermalPower: Double = 0.0

    /**
     * Resistive/conductive heat transfer power from previous step in W (Watts).
     * Always positive. Represents heat lost through thermal connections.
     */
    var conductiveHeatTransfer: Double = 0.0

    /**
     * Total power activity from previous step in W (Watts).
     * Represents total thermal activity magnitude.
     */
    var totalThermalActivity: Double = 0.0

    // === Power Accumulators (Current Simulation Step) ===
    /**
     * Accumulated conductive heat transfer for current simulation step in W (Watts).
     * Reset to zero after each thermal calculation step.
     */
    var conductiveHeatTransferAccumulator: Double = 0.0

    /**
     * Accumulated total thermal activity for current simulation step in W (Watts).
     * Reset to zero after each thermal calculation step.
     */
    var thermalActivityAccumulator: Double = 0.0

    /**
     * Accumulated net thermal power for current simulation step in W (Watts).
     * Reset to zero after each thermal calculation step.
     */
    var netThermalPowerAccumulator: Double

    // === Simulation Properties ===
    /**
     * Indicates if this thermal load uses slow thermal simulation.
     * Fast loads are simulated more frequently than slow loads.
     */
    var isSlow: Boolean = false

    /**
     * @return Maximum safe operating temperature in °C
     */
    // === Operating Limit Properties ===
    var maxOperatingTemperature: Double = Double.Companion.MAX_VALUE
        private set

    /**
     * @return Minimum safe operating temperature in °C
     */
    var minOperatingTemperature: Double = Double.Companion.MIN_VALUE
        private set

    constructor() {
        setHighThermalImpedance()
        temperatureCelsius = 0.0
        netThermalPowerAccumulator = 0.0
        netThermalPower = 0.0
        conductiveHeatTransfer = 0.0
        totalThermalActivity = 0.0
    }

    constructor(
        temperatureCelsius: Double,
        thermalResistanceToAmbient: Double,
        internalThermalResistance: Double,
        heatCapacity: Double
    ) {
        this.temperatureCelsius = temperatureCelsius
        this.thermalResistanceToAmbient = thermalResistanceToAmbient
        this.internalThermalResistance = internalThermalResistance
        this.heatCapacity = heatCapacity
        netThermalPowerAccumulator = 0.0
    }

    /**
     * Sets internal thermal resistance based on thermal time constant.
     * @param thermalTimeConstant Thermal time constant in seconds (s)
     */
    fun setInternalResistanceByTimeConstant(thermalTimeConstant: Double) {
        internalThermalResistance = thermalTimeConstant / heatCapacity
    }

    /**
     * Configures this thermal load as thermally isolated (high impedance).
     * Used for loads that don't participate meaningfully in thermal simulation.
     */
    fun setHighThermalImpedance() {
        internalThermalResistance = HIGH_THERMAL_RESISTANCE
        heatCapacity = MINIMAL_HEAT_CAPACITY
        thermalResistanceToAmbient = HIGH_THERMAL_RESISTANCE
    }

    /**
     * Sets all thermal properties at once.
     * @param internalThermalResistance Internal thermal resistance in K/W
     * @param thermalResistanceToAmbient Ambient thermal resistance in K/W
     * @param heatCapacity Heat capacity in J/K
     */
    fun setThermalProperties(
        internalThermalResistance: Double,
        thermalResistanceToAmbient: Double,
        heatCapacity: Double
    ) {
        this.thermalResistanceToAmbient = thermalResistanceToAmbient
        this.internalThermalResistance = internalThermalResistance
        this.heatCapacity = heatCapacity
    }

    @Deprecated("This calculation may not be physically meaningful")
    val estimatedThermalPower: Double
        /**
         * Calculates estimated average thermal power in W (Watts).
         * @return Estimated thermal power in W (Watts)
         */
        get() {
            if (java.lang.Double.isNaN(conductiveHeatTransfer) || java.lang.Double.isNaN(netThermalPower) ||
                java.lang.Double.isNaN(temperatureCelsius) || java.lang.Double.isNaN(thermalResistanceToAmbient) ||
                java.lang.Double.isNaN(totalThermalActivity)
            ) {
                return 0.0
            }
            return (conductiveHeatTransfer + abs(netThermalPower) + temperatureCelsius / thermalResistanceToAmbient + totalThermalActivity) / 2
        }

    /**
     * Adds thermal power to this load (e.g., from electrical dissipation).
     * @param power Power to add in W (Watts)
     */
    fun addThermalPower(power: Double) {
        if (java.lang.Double.isNaN(power)) return
        netThermalPowerAccumulator += power
        thermalActivityAccumulator += power
    }

    val temperature: Double
        /**
         * Gets current temperature, ensuring it's not NaN.
         * @return Temperature in °C (Celsius)
         */
        get() {
            if (java.lang.Double.isNaN(temperatureCelsius)) {
                temperatureCelsius = 0.0
            }
            return temperatureCelsius
        }

    // === Engineering Configuration Methods ===
    /**
     * Configures thermal properties based on maximum operating conditions and time constants.
     * This replaces the functionality of ThermalLoadInitializer.
     *
     * @param maxTemperature Maximum operating temperature in °C
     * @param minTemperature Minimum operating temperature in °C
     * @param maxPower Maximum power dissipation in W
     * @param heatingTimeConstant Time constant for heating in s
     * @param conductionTimeConstant Time constant for heat conduction in s
     */
    fun configureFromOperatingLimits(
        maxTemperature: Double, minTemperature: Double,
        maxPower: Double, heatingTimeConstant: Double,
        conductionTimeConstant: Double
    ) {
        // Calculate thermal properties from engineering parameters
        this.heatCapacity = maxPower * heatingTimeConstant / maxTemperature
        this.thermalResistanceToAmbient = maxTemperature / maxPower
        this.internalThermalResistance = conductionTimeConstant / this.heatCapacity / 2

        // Store limits for later use
        this.maxOperatingTemperature = maxTemperature
        this.minOperatingTemperature = minTemperature

        validateThermalProperties()
    }

    /**
     * Configures thermal properties based on power drop across thermal resistance.
     * This replaces the functionality of ThermalLoadInitializerByPowerDrop.
     *
     * @param maxTemperature Maximum operating temperature in °C
     * @param minTemperature Minimum operating temperature in °C
     * @param maxPower Maximum power dissipation in W
     * @param heatingTimeConstant Time constant for heating in s
     * @param thermalConductivityDrop Temperature drop due to thermal conductivity in °C
     */
    fun configureFromPowerDrop(
        maxTemperature: Double, minTemperature: Double,
        maxPower: Double, heatingTimeConstant: Double,
        thermalConductivityDrop: Double
    ) {
        // Calculate thermal properties
        this.heatCapacity = maxPower * heatingTimeConstant / maxTemperature
        this.thermalResistanceToAmbient = maxTemperature / maxPower
        this.internalThermalResistance = thermalConductivityDrop / maxPower / 2

        // Store limits
        this.maxOperatingTemperature = maxTemperature
        this.minOperatingTemperature = minTemperature

        validateThermalProperties()
    }


    /**
     * Sets operating temperature limits.
     * @param maxTemperature Maximum safe temperature in °C
     * @param minTemperature Minimum safe temperature in °C
     */
    fun setOperatingTemperatureLimits(maxTemperature: Double, minTemperature: Double) {
        this.maxOperatingTemperature = maxTemperature
        this.minOperatingTemperature = minTemperature
    }

    val isTemperatureWithinLimits: Boolean
        /**
         * Checks if current temperature is within safe operating limits.
         * @return True if temperature is safe
         */
        get() = temperatureCelsius >= minOperatingTemperature &&
                temperatureCelsius <= maxOperatingTemperature

    // === Validation ===
    private fun validateThermalProperties() {
        require(!(heatCapacity <= 0)) { "Heat capacity must be positive" }
        require(!(thermalResistanceToAmbient <= 0)) { "Thermal resistance to ambient must be positive" }
        require(!(internalThermalResistance < 0)) { "Internal thermal resistance cannot be negative" }
    }

    /**
     * Creates a copy of this thermal load with the same thermal properties.
     * @return New ThermalLoad instance with copied properties
     */
    fun copy(): ThermalLoad {
        val copy = ThermalLoad(
            this.temperatureCelsius,
            this.thermalResistanceToAmbient,
            this.internalThermalResistance,
            this.heatCapacity
        )
        copy.maxOperatingTemperature = this.maxOperatingTemperature
        copy.minOperatingTemperature = this.minOperatingTemperature
        copy.isSlow = this.isSlow
        return copy
    }

    companion object {
        // === Constants ===
        const val HIGH_THERMAL_RESISTANCE: Double = 1e9 // K/W
        const val MINIMAL_HEAT_CAPACITY: Double = 1.0 // J/K
        // === Static Instances ===
        /**
         * External thermal load representing ambient environment at 20°C.
         * Used as a thermal reference point.
         */
        val EXTERNAL_AMBIENT: ThermalLoad = ThermalLoad(20.0, 0.0, 0.0, 0.0)

        /**
         * Transfers thermal energy between two thermal loads over a time period.
         * @param energy Energy to transfer in J (Joules)
         * @param timeStep Time step in s (seconds)
         * @param from Source thermal load
         * @param to Destination thermal load
         */
        fun transferThermalEnergy(energy: Double, timeStep: Double, from: ThermalLoad, to: ThermalLoad) {
            if (java.lang.Double.isNaN(energy) || java.lang.Double.isNaN(timeStep) || timeStep == 0.0 ||
                java.lang.Double.isNaN(from.netThermalPowerAccumulator) ||
                java.lang.Double.isNaN(from.thermalActivityAccumulator)
            ) return

            val power = energy / timeStep
            val absPower = abs(power)

            from.netThermalPowerAccumulator -= power
            to.netThermalPowerAccumulator += power
            from.thermalActivityAccumulator += absPower
            to.thermalActivityAccumulator += absPower
        }

        /**
         * Transfers thermal power between two thermal loads.
         * @param power Power to transfer in W (Watts)
         * @param from Source thermal load
         * @param to Destination thermal load
         */
        fun transferThermalPower(power: Double, from: ThermalLoad, to: ThermalLoad) {
            if (java.lang.Double.isNaN(power) || java.lang.Double.isNaN(from.netThermalPowerAccumulator) || java.lang.Double.isNaN(
                    from.thermalActivityAccumulator
                )
            ) return
            val absPower = abs(power)
            from.netThermalPowerAccumulator -= power
            to.netThermalPowerAccumulator += power
            from.thermalActivityAccumulator += absPower
            to.thermalActivityAccumulator += absPower
        }

        // === Factory Methods ===
        /**
         * Creates a thermal load configured for typical electronic component.
         * @param maxTemperature Maximum operating temperature in °C (e.g., 85°C for commercial grade)
         * @param maxPower Maximum power dissipation in W
         * @param thermalTimeConstant Thermal time constant in s
         * @return Configured thermal load
         */
        fun createElectronicComponent(
            maxTemperature: Double, maxPower: Double,
            thermalTimeConstant: Double
        ): ThermalLoad {
            val load = ThermalLoad()
            load.configureFromOperatingLimits(
                maxTemperature, -40.0, maxPower,
                thermalTimeConstant, thermalTimeConstant * 0.1
            )
            return load
        }

        /**
         * Creates a thermal load with high thermal mass (slow response).
         * @param maxTemperature Maximum operating temperature in °C
         * @param maxPower Maximum power dissipation in W
         * @param thermalMass Thermal mass factor (higher = slower response)
         * @return Configured thermal load
         */
        fun createHighThermalMass(
            maxTemperature: Double, maxPower: Double,
            thermalMass: Double
        ): ThermalLoad {
            val load = ThermalLoad()
            load.configureFromOperatingLimits(
                maxTemperature, 0.0, maxPower,
                thermalMass * 100, thermalMass * 10
            )
            load.isSlow = true
            return load
        }

        fun thermalStep(
            dt: Double,
            connectionList: Iterable<ThermalConnection>,
            processList: Iterable<IProcess>?,
            loadList: Iterable<ThermalLoad>
        ) {
            // Compute heat transferred over each thermal connection:
            for (c in connectionList) {
                val heatFlow: Double

                // Use the connection's own thermal resistance
                if (c.thermalResistance == 0.0) {
                    // Perfect thermal connection - use a very small resistance to avoid division by zero
                    heatFlow = (c.L2.temperatureCelsius - c.L1.temperatureCelsius) / 1e-9
                } else {
                    heatFlow = (c.L2.temperatureCelsius - c.L1.temperatureCelsius) / c.thermalResistance
                }

                c.L1.netThermalPowerAccumulator += heatFlow
                c.L2.netThermalPowerAccumulator -= heatFlow

                val absHeatFlow = abs(heatFlow)
                c.L1.conductiveHeatTransferAccumulator += absHeatFlow
                c.L2.conductiveHeatTransferAccumulator += absHeatFlow
            }

            if (processList != null) {
                for (process in processList) {
                    process.process(dt)
                }
            }

            for (load in loadList) {
                load.netThermalPowerAccumulator -= load.temperatureCelsius / load.thermalResistanceToAmbient
                load.temperatureCelsius += load.netThermalPowerAccumulator * dt / load.heatCapacity
                load.netThermalPower = load.netThermalPowerAccumulator
                load.conductiveHeatTransfer = load.conductiveHeatTransferAccumulator
                load.totalThermalActivity = load.thermalActivityAccumulator
                load.netThermalPowerAccumulator = 0.0
                load.conductiveHeatTransferAccumulator = 0.0
                load.thermalActivityAccumulator = 0.0
            }
        }
    }
}