package org.eln.eln3.sim.mna.state;

public class VoltageStateLineReady extends VoltageState {

    boolean canBeSimplifiedByLine = false;

    public void setCanBeSimplifiedByLine(boolean v) {
        this.canBeSimplifiedByLine = v;
    }

    @Override
    public boolean canBeSimplifiedByLine() {
        return canBeSimplifiedByLine;
    }
}
