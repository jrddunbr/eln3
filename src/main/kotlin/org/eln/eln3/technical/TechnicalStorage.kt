package org.eln.eln3.technical

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import net.neoforged.neoforge.server.ServerLifecycleHooks

class TechnicalStorage(level: ServerLevel?): SavedData() {

    override fun save(nbt: CompoundTag, pRegistries: HolderLookup.Provider): CompoundTag {
        TechnicalManager.instance?.save(nbt)
        return nbt
    }

    override fun isDirty(): Boolean {
        return true
    }

    companion object {
        fun factory(level: ServerLevel?): Factory<TechnicalStorage> {
            return Factory({ TechnicalStorage(level) },
                { nbt: CompoundTag?, pRegistries: HolderLookup.Provider? ->
                    if (ServerLifecycleHooks.getCurrentServer()?.overworld() == level) {
                        // Only stores Eln3 resources on the overworld level.
                        TechnicalManager.instance?.load(nbt)
                        TechnicalManager.instance?.technicalStorage = TechnicalStorage(level)
                    }
                    TechnicalManager.instance?.technicalStorage
                }
            )
        }
    }
}
