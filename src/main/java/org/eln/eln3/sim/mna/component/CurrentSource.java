package org.eln.eln3.sim.mna.component;

import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.mna.SubSystem;
import org.eln.eln3.sim.mna.misc.ISubSystemProcessI;
import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.nbt.TagSerializable;
import org.jetbrains.annotations.NotNull;

public class CurrentSource extends Bipole implements ISubSystemProcessI, TagSerializable {
    double current;
    String name;

    public CurrentSource(String name) { this.name = name; }

    public CurrentSource(String name, State pinA, State pinB) {
        super(pinA,pinB);
        this.name = name;
    }

    public CurrentSource setCurrent(double i) {
        current = i;
        return this;
    }

    @Override
    public double getCurrent() {
        return current;
    }

    @Override
    public void applyToSubsystem(SubSystem s) {}

    @Override
    public void addToSubsystem(SubSystem s) {
        s.addProcess(this);
    }

    @Override
    public void quitSubSystem() {
        if (subSystem != null)
            subSystem.removeProcess(this);
    }

    @Override
    public void simProcessI(SubSystem s) {
        s.addToI(aPin, current);
        s.addToI(bPin, -current);
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        str += this.name;
        current = nbt.getDouble(str + "I");
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        str += this.name;
        nbt.putDouble(str + "I", current);
    }
}
