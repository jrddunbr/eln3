package org.eln.eln3.sim.nbt;


import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.ThermalLoad;
import org.jetbrains.annotations.NotNull;

public class NbtThermalLoad extends ThermalLoad implements TagSerializable {

    String name;

    public NbtThermalLoad(String name, double Tc, double Rp, double Rs, double C) {
        super(Tc, Rp, Rs, C);
        this.name = name;
    }

    public NbtThermalLoad(String name) {
        super();
        this.name = name;
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        temperatureCelsius = nbt.getFloat(str + name + "Tc");
        if (Double.isNaN(temperatureCelsius)) temperatureCelsius = 0;
        if (temperatureCelsius == Float.NEGATIVE_INFINITY) temperatureCelsius = 0;
        if (temperatureCelsius == Float.POSITIVE_INFINITY) temperatureCelsius = 0;
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        nbt.putFloat(str + name + "Tc", (float) temperatureCelsius);
    }
}
