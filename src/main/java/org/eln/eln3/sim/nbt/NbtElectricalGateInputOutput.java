package org.eln.eln3.sim.nbt;

import org.eln.eln3.misc.Utils;
import org.eln.eln3.sim.MnaConst;

public class NbtElectricalGateInputOutput extends NbtElectricalLoad {

    public NbtElectricalGateInputOutput(String name) {
        super(name);
        //Eln.instance.signalCableDescriptor.applyTo(this);
    }

    public String plot(String str) {
        return str + " " + Utils.plotVolt("", getVoltage()) + Utils.plotAmpere("", getCurrent());
    }

    public boolean isInputHigh() {
        return getVoltage() > MnaConst.SVU * 0.6;
    }

    public boolean isInputLow() {
        return getVoltage() < MnaConst.SVU * 0.2;
    }

    public double getInputNormalized() {
        double norm = getVoltage() * (1/ MnaConst.SVU);
        if (norm < 0.0) norm = 0.0;
        if (norm > 1.0) norm = 1.0;
        return norm;
    }

    public double getInputVoltage() {
        double voltage = this.getVoltage();
        if (voltage < 0.0) voltage = 0.0;
        if (voltage > MnaConst.SVU) voltage = MnaConst.SVU;
        return voltage;
    }
}
