package org.eln.eln3.sim.process.heater;

import org.eln.eln3.sim.IProcess;
import org.eln.eln3.sim.ThermalLoad;
import org.eln.eln3.sim.mna.component.Resistor;

public class ResistorHeatThermalLoad implements IProcess {

    Resistor resistor;
    ThermalLoad load;

    public ResistorHeatThermalLoad(Resistor r, ThermalLoad load) {
        this.resistor = r;
        this.load = load;
    }

    @Override
    public void process(double time) {
        load.movePowerTo(resistor.getPower());
    }
}
