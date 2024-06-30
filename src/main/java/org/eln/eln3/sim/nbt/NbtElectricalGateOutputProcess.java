package org.eln.eln3.sim.nbt;


import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.misc.Utils;
import org.eln.eln3.sim.ElectricalLoad;
import org.eln.eln3.sim.MnaConst;
import org.eln.eln3.sim.mna.SubSystem;
import org.eln.eln3.sim.mna.component.Capacitor;
import org.jetbrains.annotations.NotNull;

public class NbtElectricalGateOutputProcess extends Capacitor implements TagSerializable {

    double voltage;
    String name;

    boolean highImpedance = false;

    public NbtElectricalGateOutputProcess(String name, ElectricalLoad positiveLoad) {
        super(positiveLoad, null);
        this.name = name;
        setHighImpedance(false);
    }

    public void setHighImpedance(boolean enable) {
        this.highImpedance = enable;
        double baseC = MnaConst.gateOutputCurrent / MnaConst.ELECTRICAL_FREQUENCY / MnaConst.SVU;
        if (enable) {
            setCoulombs(baseC / 1000);
        } else {
            setCoulombs(baseC);
        }
    }

    @Override
    public void simProcessI(SubSystem s) {
        if (!highImpedance)
            aPin.state = voltage;
        super.simProcessI(s);
    }

    public boolean isHighImpedance() {
        return highImpedance;
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        setHighImpedance(nbt.getBoolean(str + name + "highImpedance"));
        voltage = nbt.getDouble(str + name + "U");
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        nbt.putBoolean(str + name + "highImpedance", isHighImpedance());
        nbt.putDouble(str + name + "U", voltage);
    }

    public void setOutputNormalized(double value) {
        setOutputNormalizedSafe(value);
    }

    public void state(boolean value) {
        if (value)
            voltage = MnaConst.SVU;
        else
            voltage = 0.0;
    }

    public double getOutputNormalized() {
        return voltage / MnaConst.SVU;
    }

    public boolean getOutputOnOff() {
        return voltage >= MnaConst.SVU / 2;
    }

    public void setOutputNormalizedSafe(double value) {
        if (value > 1.0) value = 1.0;
        if (value < 0.0) value = 0.0;
        if (Double.isNaN(value)) value = 0.0;
        voltage = value * MnaConst.SVU;
    }

    public void setVoltage(double U) {
        this.voltage = U;
    }

    public void setVoltageSafe(double value) {
        value = Utils.limit(value, 0, MnaConst.SVU);
        if (Double.isNaN(value)) value = 0.0;
        voltage = value;
    }

    public double getVoltage() {
        return voltage;
    }
}
