package org.eln.eln3.sim.nbt;


import org.eln.eln3.misc.Utils;
import org.eln.eln3.sim.MnaConst;

public class NbtElectricalGateInput extends NbtElectricalLoad {

    public NbtElectricalGateInput(String name) {
        super(name);
        //Eln.instance.signalCableDescriptor.applyTo(this);
    }

    public String plot(String str) {
        return Utils.plotSignal(getVoltage());
    }

    public boolean stateHigh() {
        return getVoltage() > MnaConst.SVU * 0.6;
    }

    public boolean stateLow() {
        return getVoltage() < MnaConst.SVU * 0.2;
    }

    public double getNormalized() {
        double norm = getVoltage() * (1/MnaConst.SVU);
        if (norm < 0.0) norm = 0.0;
        if (norm > 1.0) norm = 1.0;
        return norm;
    }

    public double getSignalVoltage() {
        double voltage = this.getVoltage();
        if (voltage < 0.0) voltage = 0.0;
        if (voltage > MnaConst.SVU) voltage = MnaConst.SVU;
        return voltage;
    }
}
