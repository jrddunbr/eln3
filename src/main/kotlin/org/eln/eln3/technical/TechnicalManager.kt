package org.eln.eln3.technical

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.saveddata.SavedData
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.eln.eln3.Eln3
import java.util.*
import java.util.function.Consumer

class TechnicalManager: SavedData() {


    fun addTechnical(block: Block, state: BlockState, entity: BlockEntity?, pos: BlockPos, level: Level) {
        try {
            var ite: ITechnicalEntity? = null
            // Not all Eln3 blocks are block entities. In fact, currently, none are. Lol.
            if (entity != null && entity is ITechnicalEntity) {
                ite = entity
            }
            val tb = (block as ITechnicalBlock).newTechnical(state, pos, level, ite)
            technicalData[tb.uuid] = tb
            tb.connect()
            setDirty()
            Eln3.LOGGER.info("Added technical at $level $pos")
        } catch (e: Exception) {
            Eln3.LOGGER.error("Failed to add technical data for $level $pos because of an exception.", e)
        }
    }

    fun removeTechnical(uuid: String) {
        val technical = technicalData[uuid]
        if (technical != null) {
            Eln3.LOGGER.info("REMOVE: Removing technical $uuid of type ${technical.javaClass.simpleName} at ${technical.pos}")
            // Log the stack trace to see what's calling this
            //val stackTrace = Thread.currentThread().stackTrace
            //Eln3.LOGGER.info("REMOVE: Called from ${stackTrace[2].className}.${stackTrace[2].methodName}:${stackTrace[2].lineNumber}")
           // if (stackTrace.size > 3) {
            //    Eln3.LOGGER.info("REMOVE: Origin ${stackTrace[3].className}.${stackTrace[3].methodName}:${stackTrace[3].lineNumber}")
            //}

            technical.disconnect()
            technicalData.remove(uuid)
            //Eln3.LOGGER.info("REMOVE: Successfully removed technical $uuid")
        } else {
            Eln3.LOGGER.warn("REMOVE: Attempted to remove non-existent technical $uuid")
        }
    }

    fun removeTechnicalsFromLocation(pos: BlockPos, level: Level) {
        val technicalsAtLocation = technicalData.filter { it.value.level == level && it.value.pos == pos }
        //Eln3.LOGGER.info("REMOVE_LOCATION: Found ${technicalsAtLocation.size} technicals at $pos in $level")

        technicalsAtLocation.forEach { (uuid, technical) ->
            //Eln3.LOGGER.info("REMOVE_LOCATION: Removing ${technical.javaClass.simpleName} $uuid at $pos")
            removeTechnical(uuid)
        }

        if (technicalsAtLocation.isEmpty()) {
            //Eln3.LOGGER.info("REMOVE_LOCATION: No technicals found at $pos to remove")
        }
    }


    fun getTechnicalsFromLocation(pos: BlockPos, level: Level): Map<String, TechnicalBase> {
        return technicalData.filter { it.value.level == level && it.value.pos == pos }
    }

    fun getAllTechnicalsFromLevel(level: Level): Map<String, TechnicalBase> {
        return technicalData.filter { it.value.level == level }
    }

    fun updateTechnicalBlockState(pos: BlockPos, level: Level, newState: BlockState) {
        val technicals = getTechnicalsFromLocation(pos, level)
        technicals.values.forEach { tech ->
            tech.state = newState
            Eln3.LOGGER.debug("Updated block state for technical ${tech.uuid} at $pos")
        }
    }


    fun save(nbt: CompoundTag) {
        Eln3.LOGGER.info("Saving ${technicalData.size} technicals.")
        technicalData.forEach {
            //Eln3.LOGGER.info("Saving $it")
            val uuid = it.key
            val tech = it.value
            try {
                if (!tech.mustBeSaved()) {
                    Eln3.LOGGER.info("Not saving $tech because it mustBeSaved() returned false.")
                    return@forEach
                }
                val node = CompoundTag()
                node.putString("uuid", uuid)
                node.putString("level", tech.level.dimension().toString())
                node.putLong("pos", tech.pos.asLong())
                node.putString("techType", tech.javaClass.name)
                node.putString("blockType", tech.block.javaClass.name)
                //Eln3.LOGGER.info("Type: ${tech.javaClass.name}")
                tech.writeToNBT(node)
                nbt.put(uuid, node)
                //Eln3.LOGGER.info("Saved $tech to NBT.")
            } catch (e: Exception) {
                Eln3.LOGGER.error("Unable to save NBT for $tech", e)
            }
        }
    }

    override fun save(nbt: CompoundTag, pRegistries: HolderLookup.Provider): CompoundTag {
        save(nbt)
        return nbt
    }

    companion object {
        private val technicalData: HashMap<String, TechnicalBase> = HashMap()

        fun create(): TechnicalManager {
            Eln3.LOGGER.info("Creating new TechnicalManager")
            return TechnicalManager()
        }

        fun load(nbt: CompoundTag, lookup: HolderLookup.Provider): TechnicalManager {
            if (technicalData.isEmpty()) {
                Eln3.LOGGER.info("Loading technical data from NBT (${nbt.allKeys.size} entries)")
            } else {
                Eln3.LOGGER.debug("TechnicalManager already has ${technicalData.size} technicals, skipping load")
                return create()
            }

            val levelsByName: Map<String, Level> =
                ServerLifecycleHooks.getCurrentServer()?.allLevels?.associateBy { it.dimension().toString() }.orEmpty()

            nbt.allKeys.forEach { key ->
                try {
                    val data = nbt.getCompound(key)
                    val uuid = data.getString("uuid")
                    val levelName = data.getString("level")
                    val level = levelsByName[levelName]
                    val pos = BlockPos.of(data.getLong("pos"))
                    val savedType = data.getString("techType")
                    val blockTypeName = data.getString("blockType")

                    if (level == null) {
                        Eln3.LOGGER.warn("Skipping technical $uuid: level $levelName not found")
                        return@forEach
                    }

                    val registeredBlock = findRegisteredBlock(blockTypeName)
                    if (registeredBlock == null) {
                        Eln3.LOGGER.warn("Skipping technical $uuid: block type $blockTypeName not found")
                        return@forEach
                    }

                    val block = registeredBlock as? ITechnicalBlock
                    if (block == null) {
                        Eln3.LOGGER.warn("Skipping technical $uuid: registered block is not ITechnicalBlock")
                        return@forEach
                    }

                    val dummyState = registeredBlock.defaultBlockState()
                    val tech = block.newTechnical(dummyState, pos, level, null)

                    if (savedType != tech.javaClass.name) {
                        Eln3.LOGGER.error("Type mismatch for $uuid: saved=$savedType, actual=${tech.javaClass.name}")
                        return@forEach
                    }

                    tech.readFromNBT(data)
                    tech.uuid = uuid
                    technicalData[uuid] = tech
                    Eln3.LOGGER.info("Loaded technical $uuid at $pos in $levelName, connecting now")
                    tech.connect()

                } catch (e: Exception) {
                    Eln3.LOGGER.error("Failed to load technical from key $key", e)
                }
            }
            return create()
        }

        fun clearData() {
            Eln3.LOGGER.info("Clearing all technical data (${technicalData.size} technicals)")
            technicalData.clear()
        }

        private fun validateTechnical(technical: TechnicalBase): Boolean {
            val level = technical.level
            val pos = technical.pos

            if (!level.hasChunk(pos.x shr 4, pos.z shr 4)) {
                throw RuntimeException("Chunk not loaded for technical at $pos but validation was requested")
            }

            val blockAtPos = level.getBlockState(pos).block
            return blockAtPos is ITechnicalBlock
        }

        fun validateTechnicalsInChunk(chunk: ChunkAccess) {
            technicalData
                .filter { it.value.level == chunk.level }
                .filter { it.value.pos.x shr 4 == chunk.pos.x && it.value.pos.z shr 4 == chunk.pos.z }
                .forEach { (uuid, tech) ->
                    validateTechnical(tech).let { valid ->
                        if (!valid) {
                            Eln3.LOGGER.error("Chunk ${chunk.pos} has invalid technical $uuid at ${tech.pos}, removing")
                            technicalData.remove(uuid)
                        } else {
                            try {
                                tech.connect()
                            } catch (e: Exception) {
                                Eln3.LOGGER.error("Failed to connect technical ${tech.uuid}", e)
                            }
                        }
                    }
                }

        }

        private fun findRegisteredBlock(blockTypeName: String): Block? {
            // Search through all registered blocks to find one matching the class name
            for (entry in net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
                if (entry.javaClass.name == blockTypeName) {
                    return entry
                }
            }
            Eln3.LOGGER.warn("Could not find registered block with class name: $blockTypeName")
            return null
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