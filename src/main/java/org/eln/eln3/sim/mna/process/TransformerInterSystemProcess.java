package org.eln.eln3.sim.mna.process;

import org.eln.eln3.sim.mna.SubSystem;
import org.eln.eln3.sim.mna.component.VoltageSource;
import org.eln.eln3.sim.mna.misc.IRootSystemPreStepProcess;
import org.eln.eln3.sim.mna.state.State;

public class TransformerInterSystemProcess implements IRootSystemPreStepProcess {
    State aState, bState;
    VoltageSource aVoltgeSource, bVoltgeSource;

    double ratio = 1;

    public TransformerInterSystemProcess(State aState, State bState, VoltageSource aVoltgeSource, VoltageSource bVoltgeSource) {
        this.aState = aState;
        this.bState = bState;
        this.aVoltgeSource = aVoltgeSource;
        this.bVoltgeSource = bVoltgeSource;
    }

    @Override
    public void rootSystemPreStepProcess() {
        SubSystem.Thevenin a = aVoltgeSource.getSubSystem().getTh(aState, aVoltgeSource);
        SubSystem.Thevenin b = bVoltgeSource.getSubSystem().getTh(bState, bVoltgeSource);

        double voltage = (a.voltage * b.resistance + ratio * b.voltage * a.resistance) / (b.resistance + ratio * ratio * a.resistance);
        if (Double.isNaN(voltage)) {
            voltage = 0;
        }

        aVoltgeSource.setVoltage(voltage);
        bVoltgeSource.setVoltage(voltage * ratio);
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getRatio() {
        return this.ratio;
    }
}
