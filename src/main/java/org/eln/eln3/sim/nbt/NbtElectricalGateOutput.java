package org.eln.eln3.sim.nbt;


import org.eln.eln3.misc.Utils;

public class NbtElectricalGateOutput extends NbtElectricalLoad {

    public NbtElectricalGateOutput(String name) {
        super(name);
        //Eln.instance.signalCableDescriptor.applyTo(this);
    }

    public String plot(String str) {
        return Utils.plotSignal(getVoltage());
    }
}
