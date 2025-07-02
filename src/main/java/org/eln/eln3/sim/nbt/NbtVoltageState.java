package org.eln.eln3.sim.nbt;

import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.mna.state.VoltageState;
import org.jetbrains.annotations.NotNull;

public class NbtVoltageState extends VoltageState implements TagSerializable {

    String name;

    public NbtVoltageState(String name) {
        super();
        this.name = name;
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        setVoltage(nbt.getFloat(str + name + "Uc"));
        if (Double.isNaN(getVoltage())) setVoltage(0);
        if (getVoltage() == Float.NEGATIVE_INFINITY) setVoltage(0);
        if (getVoltage() == Float.POSITIVE_INFINITY) setVoltage(0);
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        nbt.putFloat(str + name + "Uc", (float) getVoltage());
    }
}
