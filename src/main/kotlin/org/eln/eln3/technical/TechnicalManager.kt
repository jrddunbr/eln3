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
            val stackTrace = Thread.currentThread().stackTrace
            Eln3.LOGGER.info("REMOVE: Called from ${stackTrace[2].className}.${stackTrace[2].methodName}:${stackTrace[2].lineNumber}")
            if (stackTrace.size > 3) {
                Eln3.LOGGER.info("REMOVE: Origin ${stackTrace[3].className}.${stackTrace[3].methodName}:${stackTrace[3].lineNumber}")
            }

            technical.disconnect()
            technicalData.remove(uuid)
            Eln3.LOGGER.info("REMOVE: Successfully removed technical $uuid")
        } else {
            Eln3.LOGGER.warn("REMOVE: Attempted to remove non-existent technical $uuid")
        }
    }

    fun removeTechnicalsFromLocation(pos: BlockPos, level: Level) {
        val technicalsAtLocation = technicalData.filter { it.value.level == level && it.value.pos == pos }
        Eln3.LOGGER.info("REMOVE_LOCATION: Found ${technicalsAtLocation.size} technicals at $pos in $level")

        technicalsAtLocation.forEach { (uuid, technical) ->
            Eln3.LOGGER.info("REMOVE_LOCATION: Removing ${technical.javaClass.simpleName} $uuid at $pos")
            removeTechnical(uuid)
        }

        if (technicalsAtLocation.isEmpty()) {
            Eln3.LOGGER.info("REMOVE_LOCATION: No technicals found at $pos to remove")
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
                node.putString("techType", tech.javaClass.name)
                node.putString("blockType", tech.block.javaClass.name)
                Eln3.LOGGER.info("Type: ${tech.javaClass.name}")
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
        private val technicalData: HashMap<String, TechnicalBase> = HashMap()

        fun create(): TechnicalManager {
            Eln3.LOGGER.info("Creating new TechnicalManager")
            return TechnicalManager()
        }

        fun load(nbt: CompoundTag, lookup: HolderLookup.Provider): TechnicalManager {
            Eln3.LOGGER.info("Loading technical data into manager from NBT")
            technicalData.clear()

            val levelsByName: Map<String, Level> =
                ServerLifecycleHooks.getCurrentServer()?.allLevels?.associateBy { it.toString() }.orEmpty()

            Eln3.LOGGER.info("Loaded ${levelsByName.size} levels from the server: $levelsByName")
            Eln3.LOGGER.info("NBT contains ${nbt.allKeys.size} entries: ${nbt.allKeys}")

            // Store all loaded technicals for later validation
            val allLoadedTechnicals = mutableListOf<TechnicalBase>()

            nbt.allKeys.forEach { key ->
                try {
                    val data = nbt.getCompound(key)
                    val uuid = data.getString("uuid")
                    val levelName = data.getString("level")
                    val level = levelsByName[levelName]
                    val pos = BlockPos.of(data.getLong("pos"))
                    val savedType = data.getString("techType")
                    val blockTypeName = data.getString("blockType")

                    Eln3.LOGGER.info("Processing technical $uuid at $pos with type $savedType, block type $blockTypeName")

                    if (level == null) {
                        Eln3.LOGGER.warn("Skipping technical $uuid: level $levelName not found")
                        return@forEach
                    }

                    // Find the registered block instead of creating a new instance
                    val registeredBlock = findRegisteredBlock(blockTypeName)
                    if (registeredBlock == null) {
                        Eln3.LOGGER.warn("Skipping technical $uuid: block type $blockTypeName not found in registry")
                        return@forEach
                    }

                    val block = registeredBlock as? ITechnicalBlock
                    if (block == null) {
                        Eln3.LOGGER.warn("Skipping technical $uuid: registered block $blockTypeName is not ITechnicalBlock")
                        return@forEach
                    }

                    // Create a dummy BlockState
                    val dummyState = registeredBlock.defaultBlockState()

                    // Create the technical
                    val tech = block.newTechnical(dummyState, pos, level, null)

                    // Verify type matches
                    if (savedType != tech.javaClass.name) {
                        Eln3.LOGGER.error("Type mismatch for $uuid at $pos: saved=$savedType, actual=${tech.javaClass.name}")
                        return@forEach
                    }

                    // Load NBT data into the technical
                    tech.readFromNBT(data)

                    // Set the saved UUID
                    tech.uuid = uuid

                    allLoadedTechnicals.add(tech)
                    Eln3.LOGGER.info("Successfully loaded technical $uuid of type $savedType")

                } catch (e: Exception) {
                    Eln3.LOGGER.error("Failed to load technical from key $key", e)
                }
            }

            // Schedule validation for after chunks are loaded
            scheduleChunkLoadValidation(allLoadedTechnicals)

            Eln3.LOGGER.info("Loaded ${allLoadedTechnicals.size} technicals from NBT, validation scheduled for chunk load")
            return create()
        }

        private fun scheduleChunkLoadValidation(allLoadedTechnicals: List<TechnicalBase>) {
            // Group by position to detect conflicts
            val positionToTechnicals = allLoadedTechnicals.groupBy { tech ->
                "${tech.level}_${tech.pos.x}_${tech.pos.y}_${tech.pos.z}"
            }

            // Add all technicals temporarily
            allLoadedTechnicals.forEach { tech ->
                technicalData[tech.uuid] = tech
            }

            // Schedule validation for next server tick when chunks should be loaded
            val server = ServerLifecycleHooks.getCurrentServer()
            if (server != null) {
                server.executeIfPossible {
                    validateAndCleanupTechnicals(positionToTechnicals)
                }
            }
        }

        private fun validateAndCleanupTechnicals(positionToTechnicals: Map<String, List<TechnicalBase>>) {
            Eln3.LOGGER.info("Starting chunk-loaded validation of ${positionToTechnicals.size} positions")

            positionToTechnicals.forEach { (posKey, techList) ->
                if (techList.size > 1) {
                    Eln3.LOGGER.warn("Found ${techList.size} technicals at position $posKey:")
                    techList.forEach { tech ->
                        Eln3.LOGGER.warn("  - ${tech.uuid}: ${tech.javaClass.simpleName}")
                    }

                    val firstTech = techList.first()
                    val level = firstTech.level
                    val pos = firstTech.pos

                    // Now chunks should be loaded, check what block is actually there
                    try {
                        val actualBlock = level.getBlockState(pos).block
                        if (actualBlock is ITechnicalBlock) {
                            // Find the technical that matches the actual block
                            val correctTech = techList.find { tech ->
                                tech.block.javaClass.name == actualBlock.javaClass.name
                            }

                            if (correctTech != null) {
                                Eln3.LOGGER.info("Keeping correct technical ${correctTech.uuid} for ${actualBlock.javaClass.simpleName}")
                                // Remove the incorrect ones
                                techList.filter { it != correctTech }.forEach { tech ->
                                    Eln3.LOGGER.info("Removing incorrect technical ${tech.uuid}")
                                    technicalData.remove(tech.uuid)
                                }
                            } else {
                                Eln3.LOGGER.warn("No technical matches actual block ${actualBlock.javaClass.simpleName}, keeping first one")
                                // Remove all but the first
                                techList.drop(1).forEach { tech ->
                                    Eln3.LOGGER.info("Removing extra technical ${tech.uuid}")
                                    technicalData.remove(tech.uuid)
                                }
                            }
                        } else {
                            Eln3.LOGGER.warn("Block at $pos is not technical (${actualBlock.javaClass.simpleName}), removing all technicals")
                            // Remove all technicals for this position
                            techList.forEach { tech ->
                                Eln3.LOGGER.info("Removing orphaned technical ${tech.uuid}")
                                technicalData.remove(tech.uuid)
                            }
                        }
                    } catch (e: Exception) {
                        Eln3.LOGGER.error("Error validating position $pos", e)
                        // Keep only the first technical as fallback
                        techList.drop(1).forEach { tech ->
                            Eln3.LOGGER.info("Removing technical ${tech.uuid} due to validation error")
                            technicalData.remove(tech.uuid)
                        }
                    }
                }
                // Single technicals are fine, no action needed
            }

            // Now connect all remaining valid technicals
            Eln3.LOGGER.info("Connecting ${technicalData.size} validated technicals")
            technicalData.values.forEach { tech ->
                try {
                    tech.connect()
                } catch (e: Exception) {
                    Eln3.LOGGER.error("Failed to connect technical ${tech.uuid}", e)
                }
            }

            val removedCount = positionToTechnicals.values.sumOf { it.size } - technicalData.size
            Eln3.LOGGER.info("Validation complete: ${technicalData.size} valid technicals, $removedCount removed")
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