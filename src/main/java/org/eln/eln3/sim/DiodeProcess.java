package org.eln.eln3.sim;

import org.eln.eln3.sim.mna.component.ResistorSwitch;

public class DiodeProcess implements IProcess {

    ResistorSwitch resistor;

    public DiodeProcess(ResistorSwitch resistor) {
        this.resistor = resistor;
    }

    @Override
    public void process(double time) {
        resistor.setState(resistor.getVoltage() > 0);
    }
}
