package org.eln.eln3.sim.mna.component;

import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.mna.state.VoltageState;

public abstract class Monopole extends Component {

    VoltageState pin;

    public Monopole connectTo(VoltageState pin) {
        this.pin = pin;
        if (pin != null) pin.addComponent(this);
        return this;
    }

    @Override
    public State[] getConnectedStates() {
        return new State[]{pin};
    }
}
