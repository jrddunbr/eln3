package org.eln.eln3.sim.mna.component;

import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.mna.SubSystem;
import org.eln.eln3.sim.mna.misc.IRootSystemPreStepProcess;
import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.nbt.TagSerializable;
import org.jetbrains.annotations.NotNull;

public class PowerSource extends VoltageSource implements IRootSystemPreStepProcess, TagSerializable {

    String name;

    double power, maximumVoltage, maximumCurrent;

    public PowerSource(String name, State aPin) {
        super(name, aPin, null);
        this.name = name;
    }

    public void setPower(double P) {
        this.power = P;
    }

    void setMaximums(double Umax, double Imax) {
        this.maximumVoltage = Umax;
        this.maximumCurrent = Imax;
    }

    public void setMaximumCurrent(double maximumCurrent) {
        this.maximumCurrent = maximumCurrent;
    }

    public void setMaximumVoltage(double maximumVoltage) {
        this.maximumVoltage = maximumVoltage;
    }

    public double getPower() {
        return power;
    }

    @Override
    public void quitSubSystem() {
        getSubSystem().getRoot().removeProcess(this);
        super.quitSubSystem();
    }

    @Override
    public void addToSubsystem(SubSystem s) {
        super.addToSubsystem(s);
        getSubSystem().getRoot().addProcess(this);
        s.addProcess(this);
    }

    @Override
    public void rootSystemPreStepProcess() {
        SubSystem.Thevenin t = aPin.getSubSystem().getTh(aPin, this);

        double U = (Math.sqrt(t.voltage * t.voltage + 4 * power * t.resistance) + t.voltage) / 2;
        U = Math.min(Math.min(U, maximumVoltage), t.voltage + t.resistance * maximumCurrent);
        if (Double.isNaN(U)) U = 0;
        if (U < t.voltage) U = t.voltage;

        setVoltage(U);
    }

    public double getEffectivePower() {
        return getVoltage() * getCurrent();
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        super.saveAdditionalData(nbt, str);
        str += name;
        nbt.putDouble(str + "P", getPower());
        nbt.putDouble(str + "Umax", maximumVoltage);
        nbt.putDouble(str + "Imax", maximumCurrent);
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        super.loadAdditionalData(nbt, str);
        str += name;
        setPower(nbt.getDouble(str + "P"));
        setMaximumVoltage(nbt.getDouble(str + "Umax"));
        setMaximumCurrent(nbt.getDouble(str + "Imax"));
    }
}
