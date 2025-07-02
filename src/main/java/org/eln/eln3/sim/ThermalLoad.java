package org.eln.eln3.sim;

public class ThermalLoad {

    // === Thermal State Variables ===
    /**
     * Current temperature in Celsius (°C).
     * Note: Internal calculations should consider using Kelvin for absolute temperature.
     */
    public double temperatureCelsius;

    // === Thermal Properties ===
    /**
     * Thermal resistance to ambient environment in K/W (Kelvin per Watt).
     * Represents how this thermal load loses heat to the surrounding environment.
     * Higher values = better insulation from ambient.
     */
    public double thermalResistanceToAmbient;

    /**
     * Internal thermal resistance in K/W (Kelvin per Watt).
     * Represents thermal resistance within this load (e.g., junction to case).
     * Currently used in thermal connection calculations.
     */
    public double internalThermalResistance;

    /**
     * Heat capacity in J/K (Joules per Kelvin).
     * Determines how much energy is needed to change temperature by 1K.
     * Higher values = slower temperature changes.
     */
    public double heatCapacity;

    // === Power Values (Previous Simulation Step) ===
    /**
     * Net thermal power from previous simulation step in W (Watts).
     * Positive = heating, Negative = cooling.
     */
    public double netThermalPower;

    /**
     * Resistive/conductive heat transfer power from previous step in W (Watts).
     * Always positive. Represents heat lost through thermal connections.
     */
    public double conductiveHeatTransfer;

    /**
     * Total power activity from previous step in W (Watts).
     * Represents total thermal activity magnitude.
     */
    public double totalThermalActivity;

    // === Power Accumulators (Current Simulation Step) ===
    /**
     * Accumulated conductive heat transfer for current simulation step in W (Watts).
     * Reset to zero after each thermal calculation step.
     */
    public double conductiveHeatTransferAccumulator = 0;

    /**
     * Accumulated total thermal activity for current simulation step in W (Watts).
     * Reset to zero after each thermal calculation step.
     */
    public double thermalActivityAccumulator = 0;

    /**
     * Accumulated net thermal power for current simulation step in W (Watts).
     * Reset to zero after each thermal calculation step.
     */
    public double netThermalPowerAccumulator;

    // === Simulation Properties ===
    /**
     * Indicates if this thermal load uses slow thermal simulation.
     * Fast loads are simulated more frequently than slow loads.
     */
    boolean isSlow;

    // === Operating Limit Properties ===
    private double maxOperatingTemperature = Double.MAX_VALUE;
    private double minOperatingTemperature = Double.MIN_VALUE;

    // === Constants ===
    public static final double HIGH_THERMAL_RESISTANCE = 1e9; // K/W
    public static final double MINIMAL_HEAT_CAPACITY = 1.0;   // J/K

    public ThermalLoad() {
        setHighThermalImpedance();
        temperatureCelsius = 0;
        netThermalPowerAccumulator = 0;
        netThermalPower = 0;
        conductiveHeatTransfer = 0;
        totalThermalActivity = 0;
    }

    public ThermalLoad(
            double temperatureCelsius,
            double thermalResistanceToAmbient,
            double internalThermalResistance,
            double heatCapacity
    ) {
        this.temperatureCelsius = temperatureCelsius;
        this.thermalResistanceToAmbient = thermalResistanceToAmbient;
        this.internalThermalResistance = internalThermalResistance;
        this.heatCapacity = heatCapacity;
        netThermalPowerAccumulator = 0;
    }

    /**
     * Sets internal thermal resistance based on thermal time constant.
     * @param thermalTimeConstant Thermal time constant in seconds (s)
     */
    public void setInternalResistanceByTimeConstant(double thermalTimeConstant) {
        internalThermalResistance = thermalTimeConstant / heatCapacity;
    }

    /**
     * Configures this thermal load as thermally isolated (high impedance).
     * Used for loads that don't participate meaningfully in thermal simulation.
     */
    public void setHighThermalImpedance() {
        internalThermalResistance = HIGH_THERMAL_RESISTANCE;
        heatCapacity = MINIMAL_HEAT_CAPACITY;
        thermalResistanceToAmbient = HIGH_THERMAL_RESISTANCE;
    }

    /**
     * Sets thermal resistance to ambient environment.
     * @param thermalResistanceToAmbient Thermal resistance in K/W
     */
    public void setThermalResistanceToAmbient(double thermalResistanceToAmbient) {
        this.thermalResistanceToAmbient = thermalResistanceToAmbient;
    }

    /**
     * Sets all thermal properties at once.
     * @param internalThermalResistance Internal thermal resistance in K/W
     * @param thermalResistanceToAmbient Ambient thermal resistance in K/W
     * @param heatCapacity Heat capacity in J/K
     */
    public void setThermalProperties(double internalThermalResistance, double thermalResistanceToAmbient, double heatCapacity) {
        this.thermalResistanceToAmbient = thermalResistanceToAmbient;
        this.internalThermalResistance = internalThermalResistance;
        this.heatCapacity = heatCapacity;
    }

    /**
     * Calculates estimated average thermal power in W (Watts).
     * @return Estimated thermal power in W (Watts)
     * @deprecated This calculation may not be physically meaningful
     */
    @Deprecated
    public double getEstimatedThermalPower() {
        if (Double.isNaN(conductiveHeatTransfer) || Double.isNaN(netThermalPower) ||
                Double.isNaN(temperatureCelsius) || Double.isNaN(thermalResistanceToAmbient) ||
                Double.isNaN(totalThermalActivity)) {
            return 0.0;
        }
        return (conductiveHeatTransfer + Math.abs(netThermalPower) +
                temperatureCelsius / thermalResistanceToAmbient + totalThermalActivity) / 2;
    }

    /**
     * Transfers thermal energy between two thermal loads over a time period.
     * @param energy Energy to transfer in J (Joules)
     * @param timeStep Time step in s (seconds)
     * @param from Source thermal load
     * @param to Destination thermal load
     */
    public static void transferThermalEnergy(double energy, double timeStep, ThermalLoad from, ThermalLoad to) {
        if (Double.isNaN(energy) || Double.isNaN(timeStep) || timeStep == 0.0 ||
                Double.isNaN(from.netThermalPowerAccumulator) ||
                Double.isNaN(from.thermalActivityAccumulator)) return;

        double power = energy / timeStep;
        double absPower = Math.abs(power);

        from.netThermalPowerAccumulator -= power;
        to.netThermalPowerAccumulator += power;
        from.thermalActivityAccumulator += absPower;
        to.thermalActivityAccumulator += absPower;

    }

    /**
     * Transfers thermal power between two thermal loads.
     * @param power Power to transfer in W (Watts)
     * @param from Source thermal load
     * @param to Destination thermal load
     */
    public static void transferThermalPower(double power, ThermalLoad from, ThermalLoad to) {
        if(Double.isNaN(power) || Double.isNaN(from.netThermalPowerAccumulator) || Double.isNaN(from.thermalActivityAccumulator)) return;
        double absPower = Math.abs(power);
        from.netThermalPowerAccumulator -= power;
        to.netThermalPowerAccumulator += power;
        from.thermalActivityAccumulator += absPower;
        to.thermalActivityAccumulator += absPower;
    }

    /**
     * Adds thermal power to this load (e.g., from electrical dissipation).
     * @param power Power to add in W (Watts)
     */
    public void addThermalPower(double power) {
        if(Double.isNaN(power)) return;
        netThermalPowerAccumulator += power;
        thermalActivityAccumulator += power;
    }

    /**
     * Gets current temperature, ensuring it's not NaN.
     * @return Temperature in °C (Celsius)
     */
    public double getTemperature() {
        if (Double.isNaN(temperatureCelsius)) {
            temperatureCelsius = 0.0;
        }
        return temperatureCelsius;
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
    public void configureFromOperatingLimits(double maxTemperature, double minTemperature,
                                             double maxPower, double heatingTimeConstant,
                                             double conductionTimeConstant) {
        // Calculate thermal properties from engineering parameters
        this.heatCapacity = maxPower * heatingTimeConstant / maxTemperature;
        this.thermalResistanceToAmbient = maxTemperature / maxPower;
        this.internalThermalResistance = conductionTimeConstant / this.heatCapacity / 2;

        // Store limits for later use
        this.maxOperatingTemperature = maxTemperature;
        this.minOperatingTemperature = minTemperature;

        validateThermalProperties();
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
    public void configureFromPowerDrop(double maxTemperature, double minTemperature,
                                       double maxPower, double heatingTimeConstant,
                                       double thermalConductivityDrop) {
        // Calculate thermal properties
        this.heatCapacity = maxPower * heatingTimeConstant / maxTemperature;
        this.thermalResistanceToAmbient = maxTemperature / maxPower;
        this.internalThermalResistance = thermalConductivityDrop / maxPower / 2;

        // Store limits
        this.maxOperatingTemperature = maxTemperature;
        this.minOperatingTemperature = minTemperature;

        validateThermalProperties();
    }



    /**
     * @return Maximum safe operating temperature in °C
     */
    public double getMaxOperatingTemperature() {
        return maxOperatingTemperature;
    }

    /**
     * @return Minimum safe operating temperature in °C
     */
    public double getMinOperatingTemperature() {
        return minOperatingTemperature;
    }

    /**
     * Sets operating temperature limits.
     * @param maxTemperature Maximum safe temperature in °C
     * @param minTemperature Minimum safe temperature in °C
     */
    public void setOperatingTemperatureLimits(double maxTemperature, double minTemperature) {
        this.maxOperatingTemperature = maxTemperature;
        this.minOperatingTemperature = minTemperature;
    }

    /**
     * Checks if current temperature is within safe operating limits.
     * @return True if temperature is safe
     */
    public boolean isTemperatureWithinLimits() {
        return temperatureCelsius >= minOperatingTemperature &&
                temperatureCelsius <= maxOperatingTemperature;
    }

// === Factory Methods ===

    /**
     * Creates a thermal load configured for typical electronic component.
     * @param maxTemperature Maximum operating temperature in °C (e.g., 85°C for commercial grade)
     * @param maxPower Maximum power dissipation in W
     * @param thermalTimeConstant Thermal time constant in s
     * @return Configured thermal load
     */
    public static ThermalLoad createElectronicComponent(double maxTemperature, double maxPower,
                                                        double thermalTimeConstant) {
        ThermalLoad load = new ThermalLoad();
        load.configureFromOperatingLimits(maxTemperature, -40.0, maxPower,
                thermalTimeConstant, thermalTimeConstant * 0.1);
        return load;
    }

    /**
     * Creates a thermal load with high thermal mass (slow response).
     * @param maxTemperature Maximum operating temperature in °C
     * @param maxPower Maximum power dissipation in W
     * @param thermalMass Thermal mass factor (higher = slower response)
     * @return Configured thermal load
     */
    public static ThermalLoad createHighThermalMass(double maxTemperature, double maxPower,
                                                    double thermalMass) {
        ThermalLoad load = new ThermalLoad();
        load.configureFromOperatingLimits(maxTemperature, 0.0, maxPower,
                thermalMass * 100, thermalMass * 10);
        load.setAsSlow();
        return load;
    }

    // === Validation ===
    private void validateThermalProperties() {
        if (heatCapacity <= 0) {
            throw new IllegalArgumentException("Heat capacity must be positive");
        }
        if (thermalResistanceToAmbient <= 0) {
            throw new IllegalArgumentException("Thermal resistance to ambient must be positive");
        }
        if (internalThermalResistance < 0) {
            throw new IllegalArgumentException("Internal thermal resistance cannot be negative");
        }
    }

    /**
     * Creates a copy of this thermal load with the same thermal properties.
     * @return New ThermalLoad instance with copied properties
     */
    public ThermalLoad copy() {
        ThermalLoad copy = new ThermalLoad(this.temperatureCelsius,
                this.thermalResistanceToAmbient,
                this.internalThermalResistance,
                this.heatCapacity);
        copy.maxOperatingTemperature = this.maxOperatingTemperature;
        copy.minOperatingTemperature = this.minOperatingTemperature;
        copy.isSlow = this.isSlow;
        return copy;
    }

    public boolean isSlow() {
        return isSlow;
    }

    public void setAsSlow() {
        isSlow = true;
    }

    public void setAsFast() {
        isSlow = false;
    }

    // === Static Instances ===
    /**
     * External thermal load representing ambient environment at 20°C.
     * Used as a thermal reference point.
     */
    public static final ThermalLoad EXTERNAL_AMBIENT = new ThermalLoad(20, 0, 0, 0);
}
