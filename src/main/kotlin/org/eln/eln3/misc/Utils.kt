package org.eln.eln3.misc

import org.lwjgl.opengl.GL11
import java.lang.SecurityException
import java.lang.IllegalAccessException
import java.lang.NoSuchFieldException
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.AABB
import net.neoforged.neoforge.common.extensions.IItemStackExtension
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.eln.eln3.Config
import org.eln.eln3.Eln3
import org.eln.eln3.sim.PhysicalConstant
import java.lang.IllegalArgumentException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Utils {
    const val minecraftDay = (60 * 24).toDouble()
    val random = Random()
    const val burnTimeToEnergyFactor = 1.0

    @JvmStatic
    fun rand(min: Double, max: Double): Double {
        return random.nextDouble() * (max - min) + min
    }

    @JvmStatic
    fun println(str: String?) {
        if (Config.debug) Eln3.LOGGER.info(str)
    }

    @JvmStatic
    fun println(str: Any?) {
        if (str != null) Eln3.LOGGER.info(str.toString())
    }

    @JvmStatic
    fun println(format: String?, vararg data: Any?) {
        Eln3.LOGGER.info(String.format(format!!, *data))
    }

    @JvmStatic
    fun floatToStr(f: Double, high: Int, low: Int): String {
        var temp = ""
        for (idx in 0 until high) temp += "0"
        temp = "$temp."
        for (idx in 0 until low) temp += "0"
        val str = DecimalFormat(temp).format(f)
        var idx = 0
        val ch = str.toCharArray()
        while (true) {
            if (str.length == idx) break
            if (ch[idx] == '.') {
                ch[idx - 1] = '0'
                break
            }
            if (ch[idx] != '0' && ch[idx] != ' ') break
            ch[idx] = '_'
            idx++
        }
        return String(ch)
    }

    @JvmStatic
    fun isTheClass(o: Any, c: Class<*>): Boolean {
        if (o.javaClass == c) return true
        var classIterator: Class<*>? = o.javaClass.superclass
        while (classIterator != null) {
            if (classIterator == c) {
                return true
            }
            classIterator = classIterator.superclass
        }
        return false
    }

    /*
    @JvmStatic
    fun entityLivingViewDirection(entityLiving: EntityLivingBase): Direction {
        if (entityLiving.rotationPitch > 45) return Direction.YN
        if (entityLiving.rotationPitch < -45) return Direction.YP
        val dirx = Mth.floor((entityLiving.rotationYaw * 4.0f / 360.0f).toDouble() + 0.5) and 3
        if (dirx == 3) return Direction.XP
        if (dirx == 0) return Direction.ZP
        return if (dirx == 1) Direction.XN else Direction.ZN
    }

    @JvmStatic
    fun entityLivingHorizontalViewDirection(entityLiving: EntityLivingBase): Direction {
        val dirx = Mth.floor((entityLiving.rotationYaw * 4.0f / 360.0f).toDouble() + 0.5) and 3
        if (dirx == 3) return Direction.XP
        if (dirx == 0) return Direction.ZP
        return if (dirx == 1) Direction.XN else Direction.ZN
    }*/

    fun getItemEnergy(par0ItemStack: ItemStack?): Double {
        return burnTimeToEnergyFactor * 80000.0 / 1600 * (par0ItemStack as IItemStackExtension).getBurnTime(RecipeType.SMELTING)
    }

    val coalEnergyReference: Double
        get() = burnTimeToEnergyFactor * 80000.0

    @JvmStatic
    fun plotValue(value: Double): String {
        val valueAbs = abs(value)
        return when {
            valueAbs < 0.0001 ->
                "0"
            valueAbs < 0.000999 ->
                String.format("%1.2fµ", value * 10_000)
            valueAbs < 0.00999 ->
                String.format("%1.2fm", value * 1_000)
            valueAbs < 0.0999 ->
                String.format("%2.1fm", value * 1_000)
            valueAbs < 0.999 ->
                String.format("%3.0fm", value * 1_000)
            valueAbs < 9.99 ->
                String.format("%1.2f", value)
            valueAbs < 99.9 ->
                String.format("%2.1f", value)
            valueAbs < 999 ->
                String.format("%3.0f", value)
            valueAbs < 9999 ->
                String.format("%1.2fk", value / 1_000.0)
            valueAbs < 99999 ->
                String.format("%2.1fk", value / 1_000.0)
            valueAbs < 999999 ->
                String.format("%3.0fK", value / 1_000.0)
            valueAbs < 9999999 ->
                String.format("%1.2fM", value / 1_000_000.0)
            valueAbs < 99999999 ->
                String.format("%2.1fM", value / 1_000_000.0)
            else ->
                String.format("%3.0fM", value / 1_000_000.0)
        }
    }

    @JvmStatic
    fun plotValue(value: Double, unit: String): String {
        return plotValue(value) + unit
    }

    @JvmStatic
    fun plotVolt(value: Double): String {
        return plotValue(value, "V  ")
    }

    @JvmStatic
    fun plotVolt(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotVolt(value)
    }

    @JvmStatic
    fun plotAmpere(value: Double): String {
        return plotValue(value, "A  ")
    }

    @JvmStatic
    fun plotAmpere(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotAmpere(value)
    }

    @JvmStatic
    fun plotCelsius(header: String, value: Double): String {
        var header = header
        var value = value
        value += PhysicalConstant.ambientTemperatureKelvin - PhysicalConstant.zeroCelsiusInKelvin
        if (header != "") header += " "
        return header + plotValue(value, "\u00B0C ")
    }

    @JvmStatic
    fun plotPercent(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return if (value >= 1.0) header + String.format("%3.0f", value * 100.0) + "%   " else header + String.format("%3.1f", value * 100.0) + "%   "
    }

    fun plotEnergy(value: Double): String {
        return plotValue(value, "J  ")
    }

    fun plotEnergy(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotEnergy(value)
    }

    fun plotRads(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotValue(value, "rad/s ")
    }

    fun plotER(E: Double, R: Double): String {
        return plotEnergy("E", E) + plotRads("R", R)
    }

    fun plotPower(value: Double): String {
        return plotValue(value, "W  ")
    }

    fun plotPower(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotPower(value)
    }

    fun plotOhm(value: Double): String {
        return plotValue(value, "\u2126 ")
    }

    fun plotOhm(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotOhm(value)
    }

    fun plotUIP(U: Double, I: Double): String {
        return plotVolt("U", U) + plotAmpere("I", I) + plotPower("P", abs(U * I))
    }

    fun plotUIP(U: Double, I: Double, R: Double): String {
        return plotVolt("U", U) + plotAmpere("I", I) + plotPower("P", I * I * R)
    }

    fun plotTime(value: Double): String {
        var value = value
        var str = ""
        if (value == 0.0) return str + "0''"
        val h: Int = (value / 3600).toInt()
        value %= 3600
        val mn: Int = (value / 60).toInt()
        value %= 60
        val s: Int = (value / 1).toInt()
        if (h != 0) str += h.toString() + "h"
        if (mn != 0) str += "$mn'"
        if (s != 0) str += "$s''"
        return str
    }

    fun plotTime(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotTime(value)
    }

    fun plotBuckets(header: String, buckets: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotValue(buckets, "B ")
    }

    /*
    fun readFromNBT(nbt: CompoundTag, str: String?, inventory: IInventory) {
        val var2 = nbt.getList(str, 10)
        //val var2 = nbt.getTagList(str, 10)
        for (var3 in 0 until var2.tagCount()) {
            val var4 = var2.getCompound(var3)
            val var5: Int = (var4.getByte("Slot") and (255).toByte()).toInt()
            if (var5 >= 0 && var5 < inventory.sizeInventory) {
                inventory.setInventorySlotContents(var5, ItemStack.loadItemStackFromNBT(var4))
            }
        }
    }
     */

    /*
    fun writeToNBT(nbt: CompoundTag, str: String?, inventory: IInventory) {
        val var2 = NBTTagList()
        for (var3 in 0 until inventory.sizeInventory) {
            if (inventory.getStackInSlot(var3) != null) {
                val var4 = CompoundTag()
                var4.putByte("Slot", var3.toByte())
                inventory.getStackInSlot(var3).writeToNBT(var4)
                var2.appendTag(var4)
            }
        }
        nbt.setTag(str, var2)
    }
    */

    /*
    @JvmStatic
    fun sendPacketToClient(bos: ByteArrayOutputStream, player: EntityPlayerMP) {
        val packet = S3FPacketCustomPayload(Eln.channelName, bos.toByteArray())
        player.playerNetServerHandler.sendPacket(packet)
    }*/

    fun setGlColorFromDye(damage: Int) {
        setGlColorFromDye(damage, 1.0f)
    }

    fun setGlColorFromDye(damage: Int, gain: Float) {
        setGlColorFromDye(damage, gain, 0f)
    }

    fun setGlColorFromDye(damage: Int, gain: Float, bias: Float) {
        when (damage) {
            0 -> GL11.glColor3f(0.2f * gain + bias, 0.2f * gain + bias, 0.2f * gain + bias)
            1 -> GL11.glColor3f(1.0f * gain + bias, 0.05f * gain + bias, 0.05f * gain + bias)
            2 -> GL11.glColor3f(0.2f * gain + bias, 0.5f * gain + bias, 0.1f * gain + bias)
            3 -> GL11.glColor3f(0.3f * gain + bias, 0.2f * gain + bias, 0.1f * gain + bias)
            4 -> GL11.glColor3f(0.2f * gain + bias, 0.2f * gain + bias, 1.0f * gain + bias)
            5 -> GL11.glColor3f(0.7f * gain + bias, 0.05f * gain + bias, 1.0f * gain + bias)
            6 -> GL11.glColor3f(0.2f * gain + bias, 0.7f * gain + bias, 0.9f * gain + bias)
            7 -> GL11.glColor3f(0.7f * gain + bias, 0.7f * gain + bias, 0.7f * gain + bias)
            8 -> GL11.glColor3f(0.4f * gain + bias, 0.4f * gain + bias, 0.4f * gain + bias)
            9 -> GL11.glColor3f(1.0f * gain + bias, 0.5f * gain + bias, 0.5f * gain + bias)
            10 -> GL11.glColor3f(0.05f * gain + bias, 1.0f * gain + bias, 0.05f * gain + bias)
            11 -> GL11.glColor3f(0.9f * gain + bias, 0.8f * gain + bias, 0.1f * gain + bias)
            12 -> GL11.glColor3f(0.4f * gain + bias, 0.5f * gain + bias, 1.0f * gain + bias)
            13 -> GL11.glColor3f(0.9f * gain + bias, 0.3f * gain + bias, 0.9f * gain + bias)
            14 -> GL11.glColor3f(1.0f * gain + bias, 0.6f * gain + bias, 0.3f * gain + bias)
            15 -> GL11.glColor3f(1.0f * gain + bias, 1.0f * gain + bias, 1.0f * gain + bias)
            else -> GL11.glColor3f(0.05f * gain + bias, 0.05f * gain + bias, 0.05f * gain + bias)
        }
    }

    fun setGlColorFromLamp(colorIdx: Int) {
        when (colorIdx) {
            15 -> GL11.glColor3f(1.0f, 1.0f, 1.0f)
            0 -> GL11.glColor3f(0.25f, 0.25f, 0.25f)
            1 -> GL11.glColor3f(1.0f, 0.5f, 0.5f)
            2 -> GL11.glColor3f(0.5f, 1.0f, 0.5f)
            3 -> GL11.glColor3f(0.5647f, 0.36f, 0.36f)
            4 -> GL11.glColor3f(0.5f, 0.5f, 1.0f)
            5 -> GL11.glColor3f(0.78125f, 0.46666f, 1.0f)
            6 -> GL11.glColor3f(0.5f, 1.0f, 1.0f)
            7 -> GL11.glColor3f(0.75f, 0.75f, 0.75f)
            8 -> GL11.glColor3f(0.5f, 0.5f, 0.5f)
            9 -> GL11.glColor3f(1.0f, 0.5f, 0.65882f)
            10 -> GL11.glColor3f(0.75f, 1.0f, 0.5f)
            11 -> GL11.glColor3f(1.0f, 1.0f, 0.5f)
            12 -> GL11.glColor3f(0.5f, 0.75f, 1.0f)
            13 -> GL11.glColor3f(1.0f, 0.5f, 1.0f)
            14 -> GL11.glColor3f(1.0f, 0.80f, 0.5f)
            else -> GL11.glColor3f(1.0f, 1.0f, 1.0f)
        }
    }

    /*
    // Into utilsClient To
    @JvmStatic
    fun getWeatherNoLoad(dim: Int): Double {
        if (!getWorldExist(dim)) return 0.0
        val world = getWorld(dim)
        if (world.isThundering) return 1.0
        return if (world.isRaining) 0.5 else 0.0
    }*/

    /*
    @JvmStatic
    fun getWorld(dim: Int): Level {
        return FMLCommonHandler.instance().minecraftServerInstance.worldServerForDimension(dim)
    }

    @JvmStatic
    fun getWorldExist(dim: Int): Boolean {
        return DimensionManager.getWorld(dim) != null
    }*/

    /*
    fun getWind(worldId: Int, y: Int): Double {
        return if (!getWorldExist(worldId)) {
            0.0.coerceAtLeast(Eln.wind.getWind(y))
        } else {
            val world = getWorld(worldId)
            val factor = 1f + world.getRainStrength(0f) * 0.2f + world.getWeightedThunderStrength(0f) * 0.2f
            0.0.coerceAtLeast(
                Eln.wind.getWind(y) * factor + world.getRainStrength(0f) * 1f + world.getWeightedThunderStrength(
                    0f
                ) * 2f
            )
        }
    }*/

    fun dropItem(itemStack: ItemStack, level: Level, pos: BlockPos) {
        val doTileDrops = level.gameRules.getBoolean(GameRules.RULE_DOENTITYDROPS)
        if (doTileDrops) {
            val range = 0.7
            val x = (level.random.nextFloat() * range) + pos.x
            val y = (level.random.nextFloat() * range) + pos.y
            val z = (level.random.nextFloat() * range) + pos.z
            val dropEntity = ItemEntity(level, x, y, z, itemStack)
            dropEntity.setPickUpDelay(10)
            level.addFreshEntity(dropEntity)
        }
    }

    /*
    @JvmStatic
    fun tryPutStackInInventory(stack: ItemStack, inventory: IInventory?): Boolean {
        if (inventory == null) return false
        val limit = inventory.inventoryStackLimit

        // First, make a list of possible target slots.
        val slots = ArrayList<Int>(4)
        var need = stack.count
        run {
            var i = 0
            while (i < inventory.sizeInventory && need > 0) {
                val slot = inventory.getStackInSlot(i)
                if (slot != null && slot.stackSize < limit && slot.isItemEqual(stack)) {
                    slots.add(i)
                    need -= limit - slot.stackSize
                }
                i++
            }
        }
        var i = 0
        while (i < inventory.sizeInventory && need > 0) {
            if (inventory.getStackInSlot(i) == null) {
                slots.add(i)
                need -= limit
            }
            i++
        }

        // Is there space enough?
        if (need > 0) {
            return false
        }

        // Yes. Proceed.
        var toPut = stack.count
        for (slot in slots) {
            val target = inventory.getStackInSlot(slot)
            if (target == null) {
                val amount = toPut.coerceAtMost(limit)
                inventory.setInventorySlotContents(slot, ItemStack(stack.item, amount, stack.damageValue))
                toPut -= amount
            } else {
                val space = limit - target.stackSize
                val amount = toPut.coerceAtMost(space)
                target.stackSize += amount
                toPut -= amount
            }
            if (toPut <= 0) break
        }
        return true
    }
     */

    /*
    // Can attest, this seems pretty broken.
    @JvmStatic
    fun canPutStackInInventory(stackList: Array<ItemStack>, inventory: IInventory, slotsIdList: IntArray): Boolean {
        val limit = inventory.inventoryStackLimit
        val outputStack = arrayOfNulls<ItemStack>(slotsIdList.size)
        val inputStack = arrayOfNulls<ItemStack>(stackList.size)
        for (idx in outputStack.indices) {
            if (inventory.getStackInSlot(slotsIdList[idx]) != null) outputStack[idx] = inventory.getStackInSlot(slotsIdList[idx]).copy()
        }
        for (idx in stackList.indices) {
            inputStack[idx] = stackList[idx].copy()
        }
        var oneStackDone: Boolean
        for (stack in inputStack) {
            // if(stack == null) continue;
            oneStackDone = false
            for (idx in slotsIdList.indices) {
                val targetStack = outputStack[idx]
                if (targetStack == null) {
                    outputStack[idx] = stack
                    oneStackDone = true
                    break
                } else if (targetStack.isItemEqual(stack)) {
                    // inventory.decrStackSize(idx, -stack.stackSize);
                    val transferMax = limit - targetStack.stackSize
                    if (transferMax > 0) {
                        var transfer = stack!!.stackSize
                        if (transfer > transferMax) transfer = transferMax
                        outputStack[idx]!!.stackSize += transfer
                        stack.stackSize -= transfer
                    }
                    if (stack!!.stackSize == 0) {
                        oneStackDone = true
                        break
                    }
                }
            }
            if (!oneStackDone) return false
        }
        return true
    }

     */

    /*
    @JvmStatic
    fun tryPutStackInInventory(stackList: Array<ItemStack>, inventory: IInventory, slotsIdList: IntArray): Boolean {
        val limit = inventory.inventoryStackLimit
        for (stack in stackList) {
            for (idx in slotsIdList.indices) {
                val targetStack = inventory.getStackInSlot(slotsIdList[idx])
                if (targetStack == null) {
                    inventory.setInventorySlotContents(slotsIdList[idx], stack.copy())
                    stack.stackSize = 0
                    break
                } else if (targetStack.isItemEqual(stack)) {
                    // inventory.decrStackSize(idx, -stack.stackSize);
                    val transferMax = limit - targetStack.stackSize
                    if (transferMax > 0) {
                        var transfer = stack.stackSize
                        if (transfer > transferMax) transfer = transferMax
                        inventory.decrStackSize(slotsIdList[idx], -transfer)
                        stack.stackSize -= transfer
                    }
                    if (stack.stackSize == 0) {
                        break
                    }
                }
            }
        }
        return true
    }

    fun voltageMargeFactorSub(value: Double): Double {
        if (value > 1 + voltageMageFactor) {
            return value - voltageMageFactor
        } else if (value > 1) {
            return 1.0
        }
        return value
    }
     */

    /*
    @JvmStatic
    @Throws(IOException::class)
    fun serialiseItemStack(stream: DataOutputStream, stack: ItemStack?) {
        if (stack == null) {
            stream.writeShort(-1)
            stream.writeShort(-1)
        } else {
            stream.writeShort(Item.getId(stack.item))
            stream.writeShort(stack.damageValue)
        }
    }


    @JvmStatic
    @Throws(IOException::class)
    fun unserialiseItemStack(stream: DataInputStream): ItemStack? {
        val id: Short = stream.readShort()
        val damage: Short = stream.readShort()
        return if (id.toInt() == -1) null else newItemStack(id.toInt(), 1, damage.toInt())
    }*/

    /*
    @JvmStatic
    @Throws(IOException::class)
    fun unserializeItemStackToEntityItem(stream: DataInputStream, old: EntityItem?, tileEntity: BlockEntity): EntityItem? {
        var itemId: Short
        val ItemDamage: Short
        return if (stream.readShort().also { itemId = it }.toInt() == -1) {
            stream.readShort()
            null
        } else {
            ItemDamage = stream.readShort()
            if (old == null || Item.getIdFromItem(old.entityItem.item) != itemId.toInt() || old.entityItem.itemDamage != ItemDamage.toInt()) EntityItem(tileEntity.worldObj, tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 1.2, newItemStack(itemId.toInt(), 1, ItemDamage.toInt())) else old
        }
    }
*/
    @JvmStatic
    val isGameInPause: Boolean
        get() = ServerLifecycleHooks.getCurrentServer()?.isPaused?: throw RuntimeException("Unable to determine if game paused")

    @JvmStatic
    fun getLight(w: Level, x: Int, y: Int, z: Int): Int {
        return w.getLightEmission(BlockPos(x, y, z))
    }

    /*
    @JvmStatic
    fun notifyNeighbor(t: BlockEntity) {
        val x = t.blockPos.x
        val y = t.blockPos.y
        val z = t.blockPos.z
        val w = t.level
        var o: BlockEntity? = w?.getBlockEntity(BlockPos(x + 1, y, z))
        if (o != null && o is ITileEntitySpawnClient) (o as ITileEntitySpawnClient).tileEntityNeighborSpawn()
        o = w?.getBlockEntity(BlockPos(x - 1, y, z))
        if (o != null && o is ITileEntitySpawnClient) (o as ITileEntitySpawnClient).tileEntityNeighborSpawn()
        o = w?.getBlockEntity(BlockPos(x, y + 1, z))
        if (o != null && o is ITileEntitySpawnClient) (o as ITileEntitySpawnClient).tileEntityNeighborSpawn()
        o = w?.getBlockEntity(BlockPos(x, y - 1, z))
        if (o != null && o is ITileEntitySpawnClient) (o as ITileEntitySpawnClient).tileEntityNeighborSpawn()
        o = w?.getBlockEntity(BlockPos(x, y, z + 1))
        if (o != null && o is ITileEntitySpawnClient) (o as ITileEntitySpawnClient).tileEntityNeighborSpawn()
        o = w?.getBlockEntity(BlockPos(x, y, z - 1))
        if (o != null && o is ITileEntitySpawnClient) (o as ITileEntitySpawnClient).tileEntityNeighborSpawn()
    }

     */

    /*
    @JvmStatic
    fun playerHasMeter(entityPlayer: EntityPlayer): Boolean {
        val cur = entityPlayer.currentEquippedItem
        return (Eln.multiMeterElement.checkSameItemStack(cur)
                || Eln.thermometerElement.checkSameItemStack(cur)
                || Eln.allMeterElement.checkSameItemStack(cur)
                || Eln.configCopyToolElement.checkSameItemStack(cur))
    }*/

    /*
    @JvmStatic
    fun getRedstoneLevelAround(coord: Coordinate, side: Direction): Int {
        var side = side
        var level = coord.world().getStrongestIndirectPower(coord.x, coord.y, coord.z)
        if (level >= 15) return 15
        side = side.inverse
        when (side) {
            Direction.YN, Direction.YP -> {
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x + 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x - 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y, coord.z + 1, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y, coord.z - 1, side.toSideValue()))
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y + 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y - 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y, coord.z + 1, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y, coord.z - 1, side.toSideValue()))
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x + 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x - 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y + 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y - 1, coord.z, side.toSideValue()))
            }
            Direction.XN, Direction.XP -> {
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y + 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y - 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y, coord.z + 1, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y, coord.z - 1, side.toSideValue()))
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x + 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x - 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y + 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y - 1, coord.z, side.toSideValue()))
            }
            Direction.ZN, Direction.ZP -> {
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x + 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x - 1, coord.y, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y + 1, coord.z, side.toSideValue()))
                if (level >= 15) return 15
                level = level.coerceAtLeast(coord.world().getIndirectPowerLevelTo(coord.x, coord.y - 1, coord.z, side.toSideValue()))
            }
        }
        return level
    }*/

    @JvmStatic
    fun isPlayerAround(level: Level, AABB: AABB): Boolean {
        return level.players().any { AABB.contains(it.x, it.y, it.z) }
    }

    /*
    @JvmStatic
    fun getItemObject(stack: ItemStack?): Any? {
        if (stack == null) return null
        val i = stack.item
        if (i is GenericItemUsingDamage<*>) {
            return i.getDescriptor(stack)
        }
        return if (i is GenericItemBlockUsingDamage<*>) {
            i.getDescriptor(stack)
        } else i
    }*/

    /*
	 * public static void drawIcon(Icon icon) { Utils.bindTextureByName(icon.getIconName()); Utils.disableCulling(); GL11.glBegin(GL11.GL_QUADS); GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f(0.5f,-0.5f,0f); GL11.glTexCoord2f(0f, 0f);GL11.glVertex3f(-0.5f,-0.5f,0f); GL11.glTexCoord2f(0f, 1f);GL11.glVertex3f(-0.5f,0.5f,0f); GL11.glTexCoord2f(1f, 1f);GL11.glVertex3f(0.5f,0.5f,0f); GL11.glEnd(); Utils.enableCulling(); }
	 *
	 * public static void drawEnergyBare(float e) { float x = 14f/16f,y = 15f/16f-e*14f/16f; GL11.glColor3f(e, e, 0f); GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glBegin(GL11.GL_QUADS); GL11.glVertex3f(x+1f/16f,y,0.01f); GL11.glVertex3f(x,y,0f); GL11.glVertex3f(x,15f/16f,0f); GL11.glVertex3f(x+1f/16f,15f/16f,0.01f); GL11.glEnd(); GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glColor3f(1f, 1f, 1f); }
	 */

    /*
    @JvmStatic
    fun getItemStack(name: String, list: MutableList<ItemStack>) {
        val aitem: Iterator<*> = Item.itemRegistry.iterator()
        val tempList: List<ItemStack?> = ArrayList(3000)
        var item: Item?
        while (aitem.hasNext()) {
            item = aitem.next() as Item?
            if (item != null && item.creativeTab != null) {
                item.getSubItems(item, null as CreativeTabs?, tempList)
            }
        }
        val s = name.lowercase()
        for (itemstack in tempList) {
            // String s1 = itemstack.getDisplayName();
            if (itemstack!!.displayName.lowercase().contains(s)) {
                list.add(itemstack)
            }
        }
    }*/

    //val side: Side
    //    get() = FMLCommonHandler.instance().effectiveSide
    //val isServer: Boolean
    //    get() = side == Side.SERVER

    fun modbusToShort(outputNormalized: Double, i: Int): Short {
        val bit = java.lang.Float.floatToRawIntBits(outputNormalized.toFloat())
        return if (i == 1) bit.toShort() else (bit ushr 16).toShort()
    }

    fun modbusToFloat(first: Short, second: Short): Float {
        val bit = (first.toInt() and 0xFFFF shl 16) + (second.toInt() and 0xFFFF)
        return java.lang.Float.intBitsToFloat(bit)
    }

    /*
    fun areSame(stack: ItemStack, output: ItemStack): Boolean {
        try {
            if (stack.item === output.item && stack.damageValue == output.damageValue) return true
            val stackIds = OreDictionary.getOreIDs(stack)
            val outputIds = OreDictionary.getOreIDs(output)
            // System.out.println(Arrays.toString(stackIds) + "   " + Arrays.toString(outputIds));
            for (i in outputIds) {
                for (j in stackIds) {
                    if (i == j) return true
                }
            }
        } catch (_: Exception) {
        }
        return false
    }
     */

    /*
    @JvmStatic
    fun getVec05(c: Coordinate): Vec3 {
        return Vec3.createVectorHelper(c.x + (if (c.x < 0) -1 else 1) * 0.5, c.y + (if (c.y < 0) -1 else 1) * 0.5, c.z + (if (c.z < 0) -1 else 1) * 0.5)
    }

    fun getHeadPosY(e: Entity): Double {
        return if (e is EntityOtherPlayerMP) e.posY + e.getEyeHeight() else e.posY
    }*/

    /*
	 * public static boolean isPlayerInteractRiseWith(EntityPlayerMP entity, ItemStack stack) {
	 *
	 * return entity.inventory.getCurrentItem() == stack && Eln.playerManager.get(entity).getInteractRise(); }
	 */
    /*
    @JvmStatic
    fun isCreative(entityPlayer: EntityPlayerMP): Boolean {
        return entityPlayer.theItemInWorldManager.isCreative
        /*
		 * Minecraft m = Minecraft.getMinecraft(); return m.getIntegratedServer().getGameType().isCreative();
		 */
    }*/

    /*
    @JvmStatic
    fun mustDropItem(entityPlayer: EntityPlayerMP?): Boolean {
        return if (entityPlayer == null) true else !isCreative(entityPlayer)
    }

    @JvmStatic
    fun serverTeleport(e: Entity, x: Double, y: Double, z: Double) {
        if (e is EntityPlayerMP) e.setPositionAndUpdate(x, y, z) else e.setPosition(x, y, z)
    }*/

    @JvmStatic
    fun traceRay(world: Level, sourcePos: BlockPos, destPos: BlockPos): ArrayList<Block> {
        var x= sourcePos.x.toDouble()
        var y = sourcePos.y.toDouble()
        var z = sourcePos.z.toDouble()
        val blockList = ArrayList<Block>()
        var dx: Double = destPos.x - x
        var dy: Double = destPos.y - y
        var dz: Double = destPos.z - z
        val norm = sqrt(dx * dx + dy * dy + dz * dz)
        val normInv = 1 / (norm + 0.000000001)
        dx *= normInv
        dy *= normInv
        dz *= normInv
        var d = 0.0
        while (d < norm) {
            if (isBlockLoaded(world, x, y, z)) {
                val b = getBlock(world, x, y, z)
                blockList.add(b)
            }
            x += dx
            y += dy
            z += dz
            d += 1.0
        }
        return blockList
    }

    @JvmStatic
    fun traceRay(level: Level, posX: Double, posY: Double, posZ: Double, targetX: Double, targetY: Double, targetZ: Double, weight: TraceRayWeight): Float {
        val posXint = posX.roundToInt()
        val posYint = posY.roundToInt()
        val posZint = posZ.roundToInt()
        var x = (posX - posXint).toFloat()
        var y = (posY - posYint).toFloat()
        var z = (posZ - posZint).toFloat()
        var vx = (targetX - posX).toFloat()
        var vy = (targetY - posY).toFloat()
        var vz = (targetZ - posZ).toFloat()
        val rangeMax = sqrt((vx * vx + vy * vy + vz * vz).toDouble()).toFloat()
        val normInv = 1f / rangeMax
        vx *= normInv
        vy *= normInv
        vz *= normInv
        if (vx == 0f) vx += 0.0001f
        if (vy == 0f) vy += 0.0001f
        if (vz == 0f) vz += 0.0001f
        val vxInv = 1f / vx
        val vyInv = 1f / vy
        val vzInv = 1f / vz
        var stackRed = 0f
        var d = 0f
        while (d < rangeMax) {
            val xFloor = Mth.floor(x).toFloat()
            val yFloor = Mth.floor(y).toFloat()
            val zFloor = Mth.floor(z).toFloat()
            var dx = x - xFloor
            var dy = y - yFloor
            var dz = z - zFloor
            dx = if (vx > 0) (1 - dx) * vxInv else -dx * vxInv
            dy = if (vy > 0) (1 - dy) * vyInv else -dy * vyInv
            dz = if (vz > 0) (1 - dz) * vzInv else -dz * vzInv
            val dBest = dx.coerceAtMost(dy).coerceAtMost(dz) + 0.01f
            val xInt = xFloor.toInt()
            val yInt = yFloor.toInt()
            val zInt = zFloor.toInt()

            val place = BlockPos(xInt + posXint, yInt + posYint, zInt + posZint)
            val blockExists = level.getBlockState(place).isEmpty
            var block: Block? = null
            if (blockExists)
                block = level.getBlockState(place).block
            var dToStack: Float = if (d + dBest < rangeMax) dBest else {
                rangeMax - d
            }
            stackRed += weight.getWeight(block) * dToStack
            x += vx * dBest
            y += vy * dBest
            z += vz * dBest
            d += dBest
        }
        return stackRed
    }

    fun isBlockLoaded(world: Level, x: Double, y: Double, z: Double): Boolean {
        return world.isLoaded(BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z)))
    }

    fun getBlock(world: Level, x: Double, y: Double, z: Double): Block {
        return world.getBlockState(BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z))).block
    }

    @JvmStatic
    fun getLength(x: Double, y: Double, z: Double, tx: Double, ty: Double, tz: Double): Double {
        val dx: Double = tx - x
        val dy: Double = ty - y
        val dz: Double = tz - z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun <T> readPrivateInt(o: Any, fieldName: String?): Int {
        try {
            val f = fieldName?.let { o.javaClass.getDeclaredField(it) }
            if (f != null) {
                f.isAccessible = true
                return f.getInt(o)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        return 0
    }

    fun <T> readPrivateDouble(o: Any, fieldName: String?): Double {
        try {
            val f = fieldName?.let { o.javaClass.getDeclaredField(it) }
            if (f != null) {
                f.isAccessible = true
                return f.getDouble(o)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        return 0.0
    }

    /*
    @JvmStatic
    fun getItemStackGrid(r: IRecipe?): Array<Array<ItemStack?>>? {
        val stacks = Array(3) { arrayOfNulls<ItemStack>(3) }
        try {
            if (r is ShapedRecipes) {
                val s = r
                for (idx2 in 0..2) {
                    for (idx in 0..2) {
                        var rStack: ItemStack? = null
                        if (idx < s.recipeWidth && idx2 < s.recipeHeight) {
                            rStack = s.recipeItems[idx + idx2 * s.recipeWidth]
                        }
                        stacks[idx2][idx] = rStack
                    }
                }
                return stacks
            }
            if (r is ShapedOreRecipe) {
                val s = r
                val width = readPrivateInt<Any>(s, "width")
                val height = readPrivateInt<Any>(s, "height")
                val inputs = s.input
                for (idx2 in 0 until height) {
                    for (idx in 0 until width) {
                        val o = inputs[idx + idx2 * width]
                        var stack: ItemStack? = null
                        if (o is List<*>) {
                            if (o.isNotEmpty()) stack = o[0] as ItemStack?
                        }
                        if (o is ItemStack) {
                            stack = o
                        }
                        stacks[idx2][idx] = stack
                    }
                }
                return stacks
            }
            if (r is ShapelessRecipes) {
                for ((idx, o) in r.recipeItems.withIndex()) {
                    val stack = o as ItemStack?
                    stacks[idx / 3][idx % 3] = stack
                }
                return stacks
            }
            if (r is ShapelessOreRecipe) {
                for ((idx, o) in r.input.withIndex()) {
                    var stack: ItemStack? = null
                    if (o is List<*> && o.isNotEmpty()) {
                        stack = o[0] as ItemStack?
                    }
                    if (o is ItemStack) {
                        stack = o
                    }
                    stacks[idx / 3][idx % 3] = stack
                }
                return stacks
            }
        } catch (e: Exception) {
            // TODO: handle exception
        }
        return null
    }*/

    /*
    @JvmStatic
    fun getRecipeInputs(r: IRecipe?): ArrayList<ItemStack?> {
        return try {
            val stacks = ArrayList<ItemStack?>()
            if (r is ShapedRecipes) {
                for (stack in r.recipeItems) {
                    stacks.add(stack)
                }
            }
            if (r is ShapelessRecipes) {
                for (stack in r.recipeItems) {
                    stacks.add(stack as ItemStack?)
                }
            }
            if (r is ShapedOreRecipe) {
                for (o in r.input) {
                    if (o is List<*>) {
                        for (item in o) {
                            if (item is ItemStack) {
                                stacks.add(item)
                            }
                        }
                    }
                    if (o is ItemStack) {
                        stacks.add(o)
                    }
                }
            }
            if (r is ShapelessOreRecipe) {
                for (o in r.input) {
                    if (o is List<*>) {
                        for (item in o) {
                            if (item is ItemStack) {
                                stacks.add(item)
                            }
                        }
                    }
                    if (o is ItemStack) {
                        stacks.add(o)
                    }
                }
            }
            stacks
        } catch (e: Exception) {
            ArrayList()
        }
    }

     */

    @JvmStatic
    fun getWorldTime(world: Level): Double {
        // This could be the wrong time value. Hopefully it's right.
        return world.gameTime / 23999.0
    }

    @JvmStatic
    fun addChatMessage(player: Player, string: String) {
        // TODO: Bleh! they made this super hard. Probably needs translating anyway
    }

    /*
    @JvmStatic
    fun newItemStack(i: Int, size: Int, damage: Int): ItemStack {
        return ItemStack(Item.getItemById(i), size, damage)
    }*/

    /*
    @JvmStatic
    fun newItemStack(i: Item?, size: Int, damage: Int): ItemStack {
        return ItemStack(i, size, damage)
    }

     */

    /*
    @JvmStatic
    fun getTags(nbt: CompoundTag): List<CompoundTag> {
        val set: Array<Any> = nbt.func_150296_c().filterNotNull().toTypedArray()
        val tags = ArrayList<CompoundTag>()
        for (idx in set.indices) {
            tags.add(nbt.getCompound(set[idx] as String))
        }
        return tags
    }*/

    fun getBlock(item: Item): Block {
        return Block.byItem(item)
    }

    fun getItemId(stack: ItemStack): Int {
        return Item.getId(stack.item)
    }

    /*
    @JvmStatic
    fun getSixNodePinDistance(obj: Obj3DPart): FloatArray {
        return floatArrayOf(abs(obj.zMin * 16), abs(obj.zMax * 16), abs(obj.yMin * 16), abs(obj.yMax * 16))
    }*/

    /*
    fun isWrench(stack: ItemStack): Boolean {
        return areSame(stack, Eln.wrenchItemStack) || stack.displayName.lowercase().contains("wrench")
    }

    @JvmStatic
    fun isPlayerUsingWrench(player: EntityPlayer?): Boolean {
        if (player == null) return false
        if (ServerKeyHandler.get(ServerKeyHandler.WRENCH)) return true
        val stack = player.inventory.getCurrentItem() ?: return false
        return isWrench(stack)
    }*/


    @JvmStatic
    fun plotSignal(u: Double): String {
        return ""
        //return plotVolt("U", u) + plotPercent("Value", u / Eln.SVU)
    }

    @JvmStatic
    fun limit(value: Float, min: Float, max: Float): Float {
        return value.coerceAtMost(max).coerceAtLeast(min)
    }

    @JvmStatic
    fun limit(value: Double, min: Double, max: Double): Double {
        return value.coerceAtMost(max).coerceAtLeast(min)
    }

    @JvmStatic
    fun printFunction(func: FunctionTable, start: Double, end: Double, step: Double) {
        println("********")
        var x: Double
        var idx = 0
        while (start + step * idx.also { x = it.toDouble() } < end + 0.00001) {
            println(func.getValue(x))
            idx++
        }
        println("********")
    }

    interface TraceRayWeight {
        fun getWeight(block: Block?): Float
    }
}
/*
    @JvmStatic
    fun renderSubSystemWaila(subSystem: SubSystem?): String {
        return if (subSystem != null) {
            val subSystemSize = subSystem.component.size
            val textColor: String = if (subSystemSize <= 8) {
                "§a"
            } else if (subSystemSize <= 15) {
                "§6"
            } else {
                "§c"
            }
            textColor + subSystemSize
        } else {
            "§cnull SubSystem"
        }
    }

    @JvmStatic
    fun renderDoubleSubsystemWaila(subSystemA: SubSystem?, subSystemB: SubSystem?): String {
        val leftSubSystemSize = subSystemA?.component?.size?: -1
        val rightSubSystemSize = subSystemB?.component?.size?: -1
        val textColorLeft = when {
            leftSubSystemSize <= 0 -> "null"
            leftSubSystemSize <= 8 -> "§a"
            leftSubSystemSize <= 15 -> "§6"
            else -> "§c"
        }
        val textColorRight = when {
            rightSubSystemSize <= 0 -> "null"
            rightSubSystemSize <= 8 -> "§a"
            rightSubSystemSize <= 15 -> "§6"
            else -> "§c"
        }
        return "$textColorLeft$leftSubSystemSize §r| $textColorRight$rightSubSystemSize"
    }
    */
