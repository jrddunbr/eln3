package org.eln.eln3.sim.mna.component;


import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.mna.SubSystem;
import org.eln.eln3.sim.mna.misc.ISubSystemProcessI;
import org.eln.eln3.sim.mna.state.CurrentState;
import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.nbt.TagSerializable;
import org.jetbrains.annotations.NotNull;

public class VoltageSource extends Bipole implements ISubSystemProcessI, TagSerializable {

    String name;

    double voltage = 0;
    private final CurrentState currentState = new CurrentState();

    public VoltageSource(String name) {
        this.name = name;
    }

    public VoltageSource(String name, State aPin, State bPin) {
        super(aPin, bPin);
        this.name = name;
    }

    public VoltageSource setVoltage(double voltage) {
        this.voltage = voltage;
        return this;
    }

    public double getVoltage() {
        return voltage;
    }

    @Override
    public void quitSubSystem() {
        subSystem.states.remove(getCurrentState());
        subSystem.removeProcess(this);
        super.quitSubSystem();
    }

    @Override
    public void addToSubsystem(SubSystem s) {
        super.addToSubsystem(s);
        s.addState(getCurrentState());
        s.addProcess(this);
    }

    @Override
    public void applyToSubsystem(SubSystem s) {
        s.addToA(aPin, getCurrentState(), 1.0);
        s.addToA(bPin, getCurrentState(), -1.0);
        s.addToA(getCurrentState(), aPin, 1.0);
        s.addToA(getCurrentState(), bPin, -1.0);
    }

    @Override
    public void simProcessI(SubSystem s) {
        s.addToI(getCurrentState(), voltage);
    }

    @Override
    public double getCurrent() {
        return -getCurrentState().state;
    }

    public CurrentState getCurrentState() {
        return currentState;
    }

    public double getPower() {
        return getVoltage() * getCurrent();
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        str += name;
        setVoltage(nbt.getDouble(str + "U"));
        currentState.state = (nbt.getDouble(str + "Istate"));
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        str += name;
        nbt.putDouble(str + "U", voltage);
        nbt.putDouble(str + "Istate", currentState.state);
    }
}
