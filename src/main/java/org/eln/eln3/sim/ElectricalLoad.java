package org.eln.eln3.sim;

import org.eln.eln3.sim.mna.component.Bipole;
import org.eln.eln3.sim.mna.component.Component;
import org.eln.eln3.sim.mna.component.Line;
import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.mna.state.VoltageStateLineReady;

public class ElectricalLoad extends VoltageStateLineReady {

    public static final State groundLoad = null;

    private double serialResistance = MnaConst.highImpedance;

    public ElectricalLoad() {
    }

    public void setSerialResistance(double serialResistance) {
        if (this.serialResistance != serialResistance) {
            this.serialResistance = serialResistance;
            for (Component c : getConnectedComponents()) {
                if (c instanceof ElectricalConnection) {
                    ((ElectricalConnection) c).notifyRsChange();
                }
            }
        }
    }

    public double getSerialResistance() {
        return serialResistance;
    }

    public double getBlockResistance() {
        return serialResistance / 2;
    }

    public void setBlockResistance(double blockResistance) {
        setSerialResistance(blockResistance * 2);
    }

    public void highImpedance() {
        setSerialResistance(MnaConst.highImpedance);
    }

    public double getCurrent() {
        double current = 0;
        for (Component c : getConnectedComponents()) {
            if (c instanceof Bipole && (!(c instanceof Line)))
                current += Math.abs(((Bipole) c).getCurrent());
        }
        return current * 0.5;
    }
}
