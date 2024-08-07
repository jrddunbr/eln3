package org.eln.eln3.sim;

import org.eln.eln3.misc.Utils;
import org.eln.eln3.sim.mna.component.InterSystem;

public class ElectricalConnection extends InterSystem {

    ElectricalLoad L1, L2;

    public ElectricalConnection(ElectricalLoad L1, ElectricalLoad L2) {
        this.L1 = L1;
        this.L2 = L2;
        if(L1 == L2) Utils.println("WARNING: Attempt to connect load to itself?");
    }

    public void notifyRsChange() {
        double resistance = ((ElectricalLoad) aPin).getSerialResistance() + ((ElectricalLoad) bPin).getSerialResistance();
        setResistance(resistance);
    }

    @Override
    public void onAddToRootSystem() {
        this.connectTo(L1, L2);
        notifyRsChange();
    }

    @Override
    public void onRemoveFromRootSystem() {
        this.breakConnection();
    }
}
