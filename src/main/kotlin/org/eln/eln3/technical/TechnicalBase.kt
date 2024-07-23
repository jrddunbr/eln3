package org.eln.eln3.technical


import mcjty.theoneprobe.api.IProbeHitData
import mcjty.theoneprobe.api.IProbeInfo
import mcjty.theoneprobe.api.ProbeMode
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.Config
import org.eln.eln3.Eln3
import org.eln.eln3.position.Direction
import org.eln.eln3.position.LRDU
import org.eln.eln3.position.LRDUCubeMask
import org.eln.eln3.sim.*
import java.io.DataOutputStream
import java.util.*
import kotlin.experimental.or

/**
 * This handles any server-side technical blocks, as well as the state storage for them.
 *
 * NO CLIENT CALLS IN THIS CLASS
 */
open class TechnicalBase(var block: ITechnicalBlock, var state: BlockState, var entity: ITechnicalEntity?, var pos: BlockPos, var level: Level) {
    val uuid = UUID.randomUUID().toString()
    var neighborOpaque: Byte = 0
    var neighborWrapable: Byte = 0
    var nodeConnectionList = ArrayList<TechnicalConnection>(4)
    private var initialized = false
    private var isAdded = false
    var needPublish = false

    open fun mustBeSaved(): Boolean {
        return true
    }

    /*
    open fun networkUnserialize(stream: DataInputStream, player: EntityPlayerMP?) {}
    fun notifyNeighbor() {
        coordinate.world().notifyBlockChange(coordinate.x, coordinate.y, coordinate.z, coordinate.block)
    }
     */


    var lrduCubeMask = LRDUCubeMask()
    fun neighborBlockRead() {
        val vector = IntArray(3)
        neighborOpaque = 0
        neighborWrapable = 0
        for (direction in Direction.values()) {
            vector[0] = pos!!.x
            vector[1] = pos!!.y
            vector[2] = pos!!.z
            direction.applyTo(vector, 1)
            val b = level!!.getBlockState(BlockPos(vector[0], vector[1], vector[2])).block
            neighborOpaque = neighborOpaque or (1 shl direction.int).toByte()
            if (Companion.isBlockWrappable(level, pos!!)) neighborWrapable = neighborWrapable or (1 shl direction.int).toByte()
        }
    }

    open fun hasGui(side: Direction): Boolean {
        return false
    }

    open fun onNeighborBlockChange() {
        neighborBlockRead()
        if (isAdded) {
            reconnect()
        }
    }

    fun isBlockWrappable(direction: Direction): Boolean {
        return neighborWrapable.toInt() shr direction.int and 1 != 0
    }

    fun isBlockOpaque(direction: Direction): Boolean {
        return neighborOpaque.toInt() shr direction.int and 1 != 0
    }


    var isDestructing = false
    fun physicalSelfDestruction(explosionStrength: Float) {
        var explosionStrength = explosionStrength
        if (isDestructing) return
        isDestructing = true
        if (!Config.explosions) explosionStrength = 0f
        disconnect()
        level!!.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
        TechnicalManager.instance!!.removeTechnical(this.uuid)
        if (explosionStrength != 0f) {
            level!!.explode(null, pos!!.x + 0.5, pos!!.y + 0.5, pos!!.z + 0.5, explosionStrength, Level.ExplosionInteraction.BLOCK)
        }
    }

    /*
    fun onBlockPlacedBy(coordinate: Coordinate, front: Direction, entityLiving: EntityLivingBase?, itemStack: ItemStack?) {
        neighborBlockRead()
        TechnicalManager.instance!!.addNode(this)
        initializeFromThat(front, entityLiving, itemStack)
        if (itemStack != null) println("Node::constructor( meta = " + itemStack.damageValue + ")")
    }
     */

    //abstract fun initializeFromThat(front: Direction, entityLiving: EntityLivingBase?, itemStack: ItemStack?)

    fun getNeighbor(direction: Direction): List<TechnicalBase> {
        val position = IntArray(3)
        position[0] = pos!!.x
        position[1] = pos!!.y
        position[2] = pos!!.z
        direction.applyTo(position, 1)
        val testPos = BlockPos(position[0], position[1], position[2])
        return TechnicalManager.instance!!.getTechnicalsFromLocation(testPos, level!!).values.toList()
    }

    /*
    open fun onBreakBlock() {
        isDestructing = true
        disconnect()
        TechnicalManager.instance!!.removeNode(this)
        println("Node::onBreakBlock()")
    }
     */

    /*
    open fun onBlockActivated(entityPlayer: EntityPlayer, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (!entityPlayer.worldObj.isRemote && entityPlayer.currentEquippedItem != null) {
            val equipped = entityPlayer.currentEquippedItem
            if (Eln.multiMeterElement.checkSameItemStack(equipped)) {
                val str = multiMeterString(side)
                addChatMessage(entityPlayer, str)
                return true
            }
            if (Eln.thermometerElement.checkSameItemStack(equipped)) {
                val str = thermoMeterString(side)
                addChatMessage(entityPlayer, str)
                return true
            }
            if (Eln.allMeterElement.checkSameItemStack(equipped)) {
                val str1 = multiMeterString(side)
                val str2 = thermoMeterString(side)
                var str = ""
                str += str1
                str += str2
                if (str != "") addChatMessage(entityPlayer, str)
                return true
            }
            if (Eln.configCopyToolElement.checkSameItemStack(equipped)) {
                if (!equipped.hasTagCompound()) {
                    equipped.tagCompound = CompoundTag()
                }
                val act: String
                var snd = beepError
                if (entityPlayer.isSneaking && ServerKeyHandler.get(ServerKeyHandler.WRENCH)) {
                    if (writeConfigTool(side, equipped.tagCompound, entityPlayer)) snd = beepDownloaded
                    act = "write"
                } else {
                    if (readConfigTool(side, equipped.tagCompound, entityPlayer)) snd = beepUploaded
                    act = "read"
                }
                snd.set(
                    entityPlayer.posX,
                    entityPlayer.posY,
                    entityPlayer.posZ,
                    entityPlayer.worldObj
                ).play()
                println(String.format("NB.oBA: act %s data %s", act, equipped.tagCompound.toString()))
                return true
            }
        }
        if (hasGui(side)) {
            entityPlayer.openGui(Eln.instance, GuiHandler.nodeBaseOpen + side.int, coordinate.world(), coordinate.x, coordinate.y, coordinate.z)
            return true
        }
        return false
    }
     */

    fun reconnect() {
        disconnect()
        connect()
    }

    open fun getSideConnectionMask(side: Direction, lrdu: LRDU): Int = 0
    open fun getThermalLoad(side: Direction, lrdu: LRDU, mask: Int): ThermalLoad? = null
    open fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad? = null
    open fun checkCanStay(onCreate: Boolean) {}

    open fun addProbeInfo(
        mode: ProbeMode?,
        probeInfo: IProbeInfo,
        player: Player?,
        world: Level?,
        blockState: BlockState,
        data: IProbeHitData
    ) {}

    open fun connectJob() {
        // EXTERNAL OTHERS SIXNODE
        val emptyBlockCoord = IntArray(3)
        val otherBlockCoord = IntArray(3)

        /*
        // I think this was only used for SixNode connectivity

        for (direction in Direction.values()) {
            if (isBlockWrappable(direction)) {
                emptyBlockCoord[0] = pos.x
                emptyBlockCoord[1] = pos.y
                emptyBlockCoord[2] = pos.z
                direction.applyTo(emptyBlockCoord, 1)
                for (lrdu in LRDU.values()) {
                    val elementSide = direction.applyLRDU(lrdu)
                    otherBlockCoord[0] = emptyBlockCoord[0]
                    otherBlockCoord[1] = emptyBlockCoord[1]
                    otherBlockCoord[2] = emptyBlockCoord[2]
                    elementSide.applyTo(otherBlockCoord, 1)
                    val otherNode = TechnicalManager.instance!!.getTechnicalsFromLocation(BlockPos(otherBlockCoord[0], otherBlockCoord[1], otherBlockCoord[2]), level).values.first()
                    val otherDirection = elementSide.inverse
                    val otherLRDU = otherDirection.getLRDUGoingTo(direction)!!.inverse()
                    //if (this is SixNode || otherNode is SixNode) {
                    //    tryConnectTwoNode(this, direction, lrdu, otherNode, otherDirection, otherLRDU)
                    //}
                }
            }
        }*/
        for (dir in Direction.entries) {
            val otherNode = getNeighbor(dir).firstOrNull()
            Eln3.LOGGER.info("Trying to connect $pos to ${otherNode?.pos}")
            if (otherNode != null && otherNode.isAdded) {
                for (lrdu in LRDU.entries) {
                    tryConnectTwoNode(this, dir, lrdu, otherNode, dir.inverse, lrdu.inverseIfLR())
                }
            }
        }
    }

    open fun disconnectJob() {
        for (c in nodeConnectionList) {
            if (c.N1 !== this) {
                c.N1.nodeConnectionList.remove(c)
                c.N1.needPublish = true
                c.N1.lrduCubeMask[c.dir1, c.lrdu1] = false
            }
            if (c.N2 !== this) {
                c.N2.nodeConnectionList.remove(c)
                c.N2.needPublish = true
                c.N2.lrduCubeMask[c.dir2, c.lrdu2] = false
            }
            c.destroy()
        }
        lrduCubeMask.clear()
        nodeConnectionList.clear()
    }

    open fun externalDisconnect(side: Direction?, lrdu: LRDU?) {}
    open fun newConnectionAt(connection: TechnicalConnection?, isA: Boolean) {}
    open fun connectInit() {
        lrduCubeMask.clear()
        nodeConnectionList.clear()
    }

    fun connect() {
        if (isAdded) {
            disconnect()
        }
        connectInit()
        connectJob()
        isAdded = true
        needPublish = true
    }

    fun disconnect() {
        if (!isAdded) {
            println("Node destroy error already destroy")
            return
        }
        disconnectJob()
        isAdded = false
    }

    open fun nodeAutoSave(): Boolean {
        return true
    }

    open fun readFromNBT(nbt: CompoundTag) {
        //coordinate.loadAdditional(nbt, "c")
        neighborOpaque = nbt.getByte("NBOpaque")
        neighborWrapable = nbt.getByte("NBWrap")
        initialized = true
    }

    open fun writeToNBT(nbt: CompoundTag) {
        //coordinate.saveAdditional(nbt, "c")
        nbt.putByte("NBOpaque", neighborOpaque)
        nbt.putByte("NBWrap", neighborWrapable)
    }

    open fun getVoltmeterString(side: net.minecraft.core.Direction?): String {
        return ""
    }

    open fun getThermalProbeString(side: net.minecraft.core.Direction?): String {
        return ""
    }

    open fun readConfigTool(side: net.minecraft.core.Direction?, tag: CompoundTag?, invoker: Player?): Boolean {
        return false
    }

    open fun writeConfigTool(side: net.minecraft.core.Direction?, tag: CompoundTag?, invoker: Player?): Boolean {
        return false
    }

    private fun isINodeProcess(process: IProcess): Boolean {
        for (c in process.javaClass.interfaces) {
            //if (c == INBTTReady::class.java) return true
        }
        return false
    }

    @JvmField
    var needNotify = false
    open fun publishSerialize(stream: DataOutputStream) {}
    /*
    fun preparePacketForClient(stream: DataOutputStream) {
        try {
            stream.writeByte(Eln.packetForClientNode.toInt())
            stream.writeInt(coordinate.x)
            stream.writeInt(coordinate.y)
            stream.writeInt(coordinate.z)
            stream.writeByte(coordinate.dimension)
            stream.writeUTF(nodeUuid!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendPacketToClient(bos: ByteArrayOutputStream?, player: EntityPlayerMP?) {
        Utils.sendPacketToClient(bos!!, player!!)
    }

    @JvmOverloads
    fun sendPacketToAllClient(bos: ByteArrayOutputStream?, range: Double = 100000.0) {
        val server = FMLCommonHandler.instance().minecraftServerInstance
        for (obj in server.configurationManager.playerEntityList) {
            val player = obj as EntityPlayerMP?
            val worldServer = MinecraftServer.getServer().worldServerForDimension(player!!.dimension) as WorldServer
            val playerManager = worldServer.playerManager
            if (player.dimension != coordinate.dimension) continue
            if (!playerManager.isPlayerWatchingChunk(player, coordinate.x / 16, coordinate.z / 16)) continue
            if (coordinate.distanceTo(player) > range) continue
            Utils.sendPacketToClient(bos!!, player)
        }
    }

    val publishPacket: ByteArrayOutputStream?
        get() {
            val bos = ByteArrayOutputStream(64)
            val stream = DataOutputStream(bos)
            try {
                stream.writeByte(Eln.packetNodeSingleSerialized.toInt())
                stream.writeInt(coordinate.x)
                stream.writeInt(coordinate.y)
                stream.writeInt(coordinate.z)
                stream.writeByte(coordinate.dimension)
                stream.writeUTF(nodeUuid!!)
                publishSerialize(stream)
                return bos
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

    fun publishToAllPlayer() {
        val server = FMLCommonHandler.instance().minecraftServerInstance
        for (obj in server.configurationManager.playerEntityList) {
            val player = obj as EntityPlayerMP?
            val worldServer = MinecraftServer.getServer().worldServerForDimension(player!!.dimension) as WorldServer
            val playerManager = worldServer.playerManager
            if (player.dimension != coordinate.dimension) continue
            if (!playerManager.isPlayerWatchingChunk(player, coordinate.x / 16, coordinate.z / 16)) continue
            Utils.sendPacketToClient(publishPacket!!, player)
        }
        if (needNotify) {
            needNotify = false
            notifyNeighbor()
        }
        needPublish = false
    }

    fun publishToPlayer(player: EntityPlayerMP?) {
        Utils.sendPacketToClient(publishPacket!!, player!!)
    }
     */

    /*
    fun dropInventory(inventory: IInventory?) {
        if (inventory == null) return
        for (idx in 0 until inventory.sizeInventory) {
            dropItem(inventory.getStackInSlot(idx))
        }
    }*/

    //abstract fun initializeFromNBT()
    open fun globalBoot() {}
    fun needPublish() {
        needPublish = true
    }

    open fun unload() {
        disconnect()
    }

    companion object {
        const val maskElectricalPower = 1 shl 0
        const val maskThermal = 1 shl 1
        const val maskElectricalGate = 1 shl 2
        const val maskElectricalAll = maskElectricalPower or maskElectricalGate
        const val maskElectricalInputGate = maskElectricalGate
        const val maskElectricalOutputGate = maskElectricalGate
        const val maskWire = 0
        const val maskElectricalWire = 1 shl 3
        const val maskThermalWire = maskWire + maskThermal
        const val maskSignal = 1 shl 9
        const val maskRs485 = 1 shl 10
        const val maskSignalBus = 1 shl 11
        const val maskConduit = 1 shl 12
        const val maskColorData = 0xF shl 16
        const val maskColorShift = 16
        const val maskColorCareShift = 20
        const val maskColorCareData = 1 shl 20
        const val networkSerializeUFactor = 10.0
        const val networkSerializeIFactor = 100.0
        const val networkSerializeTFactor = 10.0
        var teststatic = 0


        fun isBlockWrappable(testLevel: Level?, testPos: BlockPos): Boolean {

            return testLevel?.getBlockState(testPos)?.isCollisionShapeFullBlock(testLevel, testPos)?: false
            /*
            if (block.isReplaceable(w, x, y, z)) return true
            if (block === Blocks.air) return true
            if (block === Eln.sixNodeBlock) return true
            if (block is GhostBlock) return true
            if (block === Blocks.torch) return true
            if (block === Blocks.redstone_torch) return true
            if (block === Blocks.unlit_redstone_torch) return true
            return block === Blocks.redstone_wire
             */
        }

        //var beepUploaded = SoundCommand("eln:beep_accept_2").smallRange()!!
        //var beepDownloaded = SoundCommand("eln:beep_accept").smallRange()!!
        //var beepError = SoundCommand("eln:beep_error").smallRange()!!

        fun tryConnectTwoNode(nodeA: TechnicalBase, directionA: Direction, lrduA: LRDU, nodeB: TechnicalBase, directionB: Direction, lrduB: LRDU) {
            Eln3.LOGGER.info("Trying to connect two nodes: $nodeA, $nodeB")
            val mskA = nodeA.getSideConnectionMask(directionA, lrduA)
            val mskB = nodeB.getSideConnectionMask(directionB, lrduB)
            Eln3.LOGGER.info("mskA: $mskA, mskB: $mskB")
            if (compareConnectionMask(mskA, mskB)) {
                Eln3.LOGGER.info("Connection masks match, trying connections")
                val eCon: ElectricalConnection?
                val tCon: ThermalConnection?
                val nodeConnection = TechnicalConnection(nodeA, directionA, lrduA, nodeB, directionB, lrduB)
                nodeA.nodeConnectionList.add(nodeConnection)
                nodeB.nodeConnectionList.add(nodeConnection)
                nodeA.needPublish = true
                nodeB.needPublish = true
                nodeA.lrduCubeMask[directionA, lrduA] = true
                nodeB.lrduCubeMask[directionB, lrduB] = true
                nodeA.newConnectionAt(nodeConnection, true)
                nodeB.newConnectionAt(nodeConnection, false)

                var eLoad: ElectricalLoad?
                if (nodeA.getElectricalLoad(directionA, lrduA, mskB).also { eLoad = it } != null) {
                    val otherELoad = nodeB.getElectricalLoad(directionB, lrduB, mskA)
                    if (otherELoad != null) {
                        eCon = ElectricalConnection(eLoad, otherELoad)
                        Eln3.simulator.addElectricalComponent(eCon)
                        nodeConnection.addConnection(eCon)
                    }
                }
                var tLoad: ThermalLoad?
                if (nodeA.getThermalLoad(directionA, lrduA, mskB).also { tLoad = it } != null) {
                    val otherTLoad = nodeB.getThermalLoad(directionB, lrduB, mskA)
                    if (otherTLoad != null) {
                        tCon = ThermalConnection(tLoad, otherTLoad)
                        Eln3.simulator.addThermalConnection(tCon)
                        nodeConnection.addConnection(tCon)
                    }
                }
            }
        }

        private fun compareConnectionMask(mask1: Int, mask2: Int): Boolean {
            if (mask1 and 0xFFFF and (mask2 and 0xFFFF) == 0) return false
            if (mask1 and maskColorCareData and (mask2 and maskColorCareData) == 0) return true
            return mask1 and maskColorData == mask2 and maskColorData
        }
    }
}