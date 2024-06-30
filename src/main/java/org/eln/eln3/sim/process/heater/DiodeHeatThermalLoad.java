package org.eln.eln3.sim.process.heater;

import org.eln.eln3.sim.IProcess;
import org.eln.eln3.sim.ThermalLoad;
import org.eln.eln3.sim.mna.component.Resistor;

public class DiodeHeatThermalLoad implements IProcess {

    Resistor resistor;
    ThermalLoad load;
    double lastResistance;

    public DiodeHeatThermalLoad(Resistor r, ThermalLoad load) {
        this.resistor = r;
        this.load = load;
        lastResistance = r.getResistance();
    }

    @Override
    public void process(double time) {
        if (resistor.getResistance() == lastResistance) {
            load.movePowerTo(resistor.getPower());
        } else {
            lastResistance = resistor.getResistance();
        }
    }
}
