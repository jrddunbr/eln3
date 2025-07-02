package org.eln.eln3.sim;

public class ThermalConnection {

    public ThermalLoad L1;
    public ThermalLoad L2;
    public double thermalResistance; // K/W - thermal resistance of the connection path

    public ThermalConnection(ThermalLoad L1, ThermalLoad L2, double thermalResistance) {
        this.L1 = L1;
        this.L2 = L2;
        this.thermalResistance = thermalResistance;
    }

    // Convenience constructor for zero resistance (perfect thermal connection)
    public ThermalConnection(ThermalLoad L1, ThermalLoad L2) {
        this(L1, L2, 0.0);
    }
}
