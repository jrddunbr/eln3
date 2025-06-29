package org.eln.eln3.technical

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.saveddata.SavedData
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.eln.eln3.Eln3
import java.util.*
import java.util.function.Consumer

class TechnicalManager: SavedData() {
    private val technicalData: HashMap<String, TechnicalBase> = HashMap()
    private val previousLoadStorage = mutableMapOf<Pair<Level, BlockPos>, CompoundTag>()

    fun addTechnical(block: Block, state: BlockState, entity: BlockEntity?, pos: BlockPos, level: Level) {
        try {
            var ite: ITechnicalEntity? = null
            // Not all Eln3 blocks are block entities. In fact, currently, none are. Lol.
            if (entity != null && entity is ITechnicalEntity) {
                ite = entity
            }
            val tb = (block as ITechnicalBlock).newTechnical(state, pos, level, ite)
            if (previousLoadStorage.containsKey(Pair(level, pos))) {
                val tag = previousLoadStorage[Pair(level, pos)]
                if (tag != null) {
                    Eln3.LOGGER.info("Was able to locate prior save data for $level $pos, loading NBT")
                    tb.readFromNBT(tag)
                }
            }
            technicalData[tb.uuid] = tb
            tb.connect()
            setDirty()
            Eln3.LOGGER.info("Added technical at $level $pos")
        } catch (e: Exception) {
            Eln3.LOGGER.error("Failed to add technical data for $level $pos because of an exception.", e)
        }
    }

    fun removeTechnical(uuid: String) {
        if (technicalData.containsKey(uuid)) {
            val level = technicalData[uuid]?.level
            val pos = technicalData[uuid]?.pos
            technicalData[uuid]?.disconnect()
            technicalData.remove(uuid)
            setDirty()
            Eln3.LOGGER.info("Removed technical at $level $pos")
        }
    }

    fun removeTechnicalsFromLocation(pos: BlockPos, level: Level) {
        technicalData.filter { it.value.level == level && it.value.pos == pos }.forEach {
            removeTechnical(it.value.uuid)
        }
    }

    fun getTechnicalsFromLocation(pos: BlockPos, level: Level): Map<String, TechnicalBase> {
        return technicalData.filter { it.value.level == level && it.value.pos == pos }
    }

    fun getAllTechnicalsFromLevel(level: Level): Map<String, TechnicalBase> {
        return technicalData.filter { it.value.level == level }
    }

    fun save(nbt: CompoundTag) {
        Eln3.LOGGER.info("Saving ${technicalData.size} technicals.")
        technicalData.forEach {
            Eln3.LOGGER.info("Saving $it")
            val uuid = it.key
            val tech = it.value
            try {
                if (!tech.mustBeSaved()) {
                    Eln3.LOGGER.info("Not saving $tech because it mustBeSaved() returned false.")
                    return@forEach
                }
                val node = CompoundTag()
                node.putString("uuid", uuid)
                node.putString("level", tech.level.toString())
                node.putLong("pos", tech.pos.asLong())
                tech.writeToNBT(node)
                nbt.put(uuid, node)
                Eln3.LOGGER.info("Saved $tech to NBT.")
            } catch (e: Exception) {
                Eln3.LOGGER.error("Unable to save NBT for $tech", e)
            }
        }
    }

    override fun save(nbt: CompoundTag, pRegistries: HolderLookup.Provider): CompoundTag {
        Eln3.LOGGER.info("Saving technical data.")
        save(nbt)
        return nbt
    }

    companion object {
        fun create(): TechnicalManager {
            Eln3.LOGGER.info("Creating new TechnicalManager")
            return TechnicalManager()
        }

        fun load(nbt: CompoundTag, lookup: HolderLookup.Provider): TechnicalManager {
            Eln3.LOGGER.info("Loading technical data into manager from NBT")
            val tm = create()

            val levelsByName: Map<String, Level> =
                ServerLifecycleHooks.getCurrentServer()?.allLevels?.associateBy { it.toString() }.orEmpty()

            Eln3.LOGGER.info("Loaded ${levelsByName.size} levels from the server: $levelsByName")

            nbt.allKeys.forEach {
                val data = nbt.getCompound(it)
                val uuid = data.getString("uuid")
                val level = levelsByName[data.getString("level")]
                val pos = BlockPos.of(data.getLong("pos"))
                if (level == null) return@forEach
                tm.previousLoadStorage[Pair(level, pos)] = data
            }

            Eln3.LOGGER.info("Loaded ${tm.previousLoadStorage.size} previous load storage entries.")
            return tm
        }

        fun factory(): SavedData.Factory<TechnicalManager> {
            return Factory<TechnicalManager> (
                TechnicalManager::create,
                TechnicalManager.Companion::load,
                null
            )
        }

        fun use(level: Level, s: Consumer<TechnicalManager>) {
            val tm = get(level)
            if (tm != null) {
                s.accept(tm)
            }
        }

        fun get(level: Level): TechnicalManager? {
            if (level is ServerLevel) {
                val tm = level.dataStorage.computeIfAbsent(factory(), Eln3.MODID)
                if (tm == null) {
                    Eln3.LOGGER.error("Attempted to get TechnicalManager for ServerLevel $level but it was null.")
                }
                return tm
            }
            Eln3.LOGGER.error("Attempted to get TechnicalManager for non-ServerLevel $level")
            return null
        }
    }
}