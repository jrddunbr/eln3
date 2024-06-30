package org.eln.eln3.sim.nbt;


import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.mna.component.Resistor;
import org.eln.eln3.sim.mna.state.State;
import org.jetbrains.annotations.NotNull;

public class NbtResistor extends Resistor implements TagSerializable {

    String name;

    public NbtResistor(String name, State aPin, State bPin) {
        super(aPin, bPin);
        this.name = name;
    }

    // TODO: I think += str below is a bug.
    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        name += str;
        setResistance(nbt.getDouble(str + "R"));
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        nbt.putDouble(str + "R", getResistance());
    }
}
