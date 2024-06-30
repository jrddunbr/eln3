package org.eln.eln3.technical

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.saveddata.SavedData
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.eln.eln3.Eln3
import java.util.*


class TechnicalManager {
    private val technicalData: HashMap<String, TechnicalBase> = HashMap()
    private val previousLoadStorage = mutableMapOf<Pair<Level, BlockPos>, CompoundTag>()
    var technicalStorage: TechnicalStorage? = null

    fun addTechnical(block: Block, state: BlockState, entity: BlockEntity?, pos: BlockPos, level: Level) {
        val uuid = UUID.randomUUID().toString()
        try {
            val tb = TechnicalBase(uuid, block as ITechnicalBlock, state, entity as ITechnicalEntity?, pos, level)
            if (previousLoadStorage.containsKey(Pair(level, pos))) {
                val tag = previousLoadStorage[Pair(level, pos)]
                if (tag != null) {
                    Eln3.LOGGER.info("Was able to locate prior save data for $level $pos, loading NBT")
                    tb.readFromNBT(tag)
                }
            }
            technicalData[uuid] = tb
            Eln3.LOGGER.info("Added technical at $level $pos")
        } catch (e: Exception) {
            Eln3.LOGGER.error("Failed to add technical data for $level $pos because of an exception.", e)
        }
    }

    fun removeTechnical(uuid: String) {
        if (technicalData.containsKey(uuid)) {
            val level = technicalData[uuid]?.level
            val pos = technicalData[uuid]?.pos
            technicalData.remove(uuid)
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

    fun unload(level: Level) {
        technicalData.filter { it.value.level == level }.forEach {
            technicalData.remove(it.value.uuid)
        }
        val overworldServerLevel = ServerLifecycleHooks.getCurrentServer()?.overworld()
        overworldServerLevel?.dataStorage?.set(Eln3.MODID, technicalStorage as SavedData)
    }

    companion object {
        var instance: TechnicalManager? = null
    }

    init {
        instance = this
        val overworldServerLevel = ServerLifecycleHooks.getCurrentServer()?.overworld()
        overworldServerLevel?.dataStorage?.get(TechnicalStorage.factory(overworldServerLevel), Eln3.MODID)
    }

    fun load(nbt: CompoundTag?) {
        val levelsByName: Map<String, Level> =
            ServerLifecycleHooks.getCurrentServer()?.allLevels?.associateBy { it.toString() }.orEmpty()

        nbt?.allKeys?.forEach {
            val data = nbt.getCompound(it)
            val uuid = data.getString("uuid")
            val level = levelsByName[data.getString("level")]
            val pos = BlockPos.of(data.getLong("pos"))
            if (level == null) return@forEach
            previousLoadStorage[Pair(level, pos)] = data
        }
    }

    fun save(nbt: CompoundTag) {
        technicalData.forEach {
            val uuid = it.key
            val tech = it.value
            try {
                if (!tech.mustBeSaved()) return@forEach
                val node = CompoundTag()
                node.putString("uuid", uuid)
                node.putString("level", tech.level.toString())
                node.putLong("pos", tech.pos.asLong())
                tech.writeToNBT(node)
                nbt.put(uuid, node)
            }catch (e:Exception){
                Eln3.LOGGER.error("Unable to save NBT for $tech", e)
            }
        }
    }

    /*
    fun loadFromNbt(nbt: CompoundTag?) {
        val addedNode: MutableList<TechnicalBase> = ArrayList()
        for (o in getTags(nbt!!)) {
            val tag = o
            val nodeClass = UUIDToClass[tag.getString("tag")]
            try {
                val node = nodeClass!!.getConstructor().newInstance() as TechnicalBase
                node.readFromNBT(tag)
                addNode(node)
                addedNode.add(node)
                node.initializeFromNBT()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        for (n in addedNode) {
            n.globalBoot()
        }
    }

    fun saveToNbt(nbt: CompoundTag, dim: Int) {
        var nodeCounter = 0
        val nodesCopy: MutableList<TechnicalBase> = ArrayList()
        nodesCopy.addAll(nodes)
        for (node in nodesCopy) {
            try {
                if (node.mustBeSaved() == false) continue
                if (dim != Int.MIN_VALUE && node.coordinate.dimension != dim) continue
                val nbtNode = CompoundTag()
                nbtNode.setString("tag", node.nodeUuid)
                node.writeToNBT(nbtNode)
                nbt.setTag("n" + nodeCounter++, nbtNode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    */
}