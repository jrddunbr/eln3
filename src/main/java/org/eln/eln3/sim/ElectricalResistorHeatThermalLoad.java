package org.eln.eln3.sim;

import org.eln.eln3.sim.mna.component.Resistor;

public class ElectricalResistorHeatThermalLoad implements IProcess {

    Resistor electricalResistor;
    ThermalLoad thermalLoad;

    public ElectricalResistorHeatThermalLoad(Resistor electricalResistor, ThermalLoad thermalLoad) {
        this.electricalResistor = electricalResistor;
        this.thermalLoad = thermalLoad;
    }

    @Override
    public void process(double time) {
        thermalLoad.netThermalPowerAccumulator += electricalResistor.getPower();
    }
}
