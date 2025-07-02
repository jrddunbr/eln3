package org.eln.eln3.sim.nbt

import net.minecraft.nbt.CompoundTag
import org.eln.eln3.sim.thermal.ThermalLoad

class NbtThermalLoad : ThermalLoad, TagSerializable {
    var name: String?

    constructor(name: String?, Tc: Double, Rp: Double, Rs: Double, C: Double) : super(Tc, Rp, Rs, C) {
        this.name = name
    }

    constructor(name: String?) : super() {
        this.name = name
    }

    override fun loadAdditionalData(nbt: CompoundTag, str: String) {
        temperatureCelsius = nbt.getDouble(str + name + "temperatureCelsius")
        if (!temperatureCelsius.isFinite()) temperatureCelsius = 0.0
        if (temperatureCelsius == Float.Companion.NEGATIVE_INFINITY.toDouble()) temperatureCelsius = 0.0
        if (temperatureCelsius == Float.Companion.POSITIVE_INFINITY.toDouble()) temperatureCelsius = 0.0
    }

    override fun saveAdditionalData(nbt: CompoundTag, str: String) {
        nbt.putDouble(str + name + "temperatureCelsius", temperatureCelsius)
    }
}