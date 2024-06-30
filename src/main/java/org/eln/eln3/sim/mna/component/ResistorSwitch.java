package org.eln.eln3.sim.mna.component;


import net.minecraft.nbt.CompoundTag;
import org.eln.eln3.sim.MnaConst;
import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.nbt.TagSerializable;
import org.jetbrains.annotations.NotNull;

public class ResistorSwitch extends Resistor implements TagSerializable {

    boolean ultraImpedance = false;
    String name;

    boolean state = false;

    protected double baseResistance = 1;

    public ResistorSwitch(String name, State aPin, State bPin) {
        super(aPin, bPin);
        this.name = name;
    }

    public void setState(boolean state) {
        this.state = state;
        setResistance(baseResistance);
    }

    @Override
    public Resistor setResistance(double resistance) {
        baseResistance = resistance;
        return super.setResistance(state ? resistance : (ultraImpedance ? MnaConst.ultraImpedance : MnaConst.highImpedance));
    }

    public boolean getState() {
        return state;
    }

    public void mustUseUltraImpedance() {
        ultraImpedance = true;
    }

    @Override
    public void loadAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        str += name;
        setResistance(nbt.getDouble(str + "R"));
        if (Double.isNaN(baseResistance) || baseResistance == 0) {
            if (ultraImpedance) ultraImpedance();
            else highImpedance();
        }
        setState(nbt.getBoolean(str + "State"));
    }

    @Override
    public void saveAdditionalData(@NotNull CompoundTag nbt, @NotNull String str) {
        str += name;
        nbt.putDouble(str + "R", baseResistance);
        nbt.putBoolean(str + "State", getState());
    }
}
