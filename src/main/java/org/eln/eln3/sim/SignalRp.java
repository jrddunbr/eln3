package org.eln.eln3.sim;

import org.eln.eln3.sim.mna.component.Resistor;
import org.eln.eln3.sim.mna.state.State;

public class SignalRp extends Resistor {
    public SignalRp(State aPin) {
        super(aPin, null);
        setResistance(MnaConst.SVU / MnaConst.SVII);
    }
}
