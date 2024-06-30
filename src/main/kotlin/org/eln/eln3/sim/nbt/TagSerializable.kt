package org.eln.eln3.sim.nbt

import net.minecraft.nbt.CompoundTag

interface TagSerializable {
    fun loadAdditionalData(nbt: CompoundTag, str: String)
    fun saveAdditionalData(nbt: CompoundTag, str: String)
}