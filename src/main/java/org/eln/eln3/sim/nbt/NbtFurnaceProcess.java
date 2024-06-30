package org.eln.eln3.sim.nbt;


import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.FurnaceProcess;
import org.eln.eln3.sim.ThermalLoad;
import org.jetbrains.annotations.NotNull;

public class NbtFurnaceProcess extends FurnaceProcess implements TagSerializable {

    String name;

    public NbtFurnaceProcess(String name, ThermalLoad load) {
        super(load);
        this.name = name;
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        combustibleEnergy = nbt.getFloat(str + name + "Q");
        setGain(nbt.getDouble(str + name + "gain"));
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        nbt.putFloat(str + name + "Q", (float) combustibleEnergy);
        nbt.putDouble(str + name + "gain", getGain());
    }
}
