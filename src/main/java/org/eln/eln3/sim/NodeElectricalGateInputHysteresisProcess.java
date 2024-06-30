package org.eln.eln3.sim;

import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.nbt.NbtElectricalGateInput;
import org.eln.eln3.sim.nbt.TagSerializable;
import org.jetbrains.annotations.NotNull;

public abstract class NodeElectricalGateInputHysteresisProcess implements IProcess, TagSerializable {

    NbtElectricalGateInput gate;
    String name;

    boolean state = false;

    public NodeElectricalGateInputHysteresisProcess(String name, NbtElectricalGateInput gate) {
        this.gate = gate;
        this.name = name;
    }

    protected abstract void setOutput(boolean value);

    @Override
    public void process(double time) {
        if (state) {
            if (gate.getVoltage() < MnaConst.SVU * 0.3) {
                state = false;
                setOutput(false);
            } else setOutput(true);
        } else {
            if (gate.getVoltage() > MnaConst.SVU * 0.7) {
                state = true;
                setOutput(true);
            } else setOutput(false);
        }
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        state = nbt.getBoolean(str + name + "state");
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        nbt.putBoolean(str + name + "state", state);
    }
}
