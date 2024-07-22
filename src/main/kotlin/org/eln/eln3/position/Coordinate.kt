package org.eln.eln3.position

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

@Deprecated("Consider using BlockPos or GlobalPos")
class Coordinate {
    @JvmField
    var x = 0
    @JvmField
    var y = 0
    @JvmField
    var z = 0
    @JvmField
    var dimension: ResourceKey<Level>? = null

    constructor() {
        x = 0
        y = 0
        z = 0
        dimension = null
    }

    constructor(coord: Coordinate) {
        x = coord.x
        y = coord.y
        z = coord.z
        dimension = coord.dimension
    }

    constructor(nbt: CompoundTag, str: String) {
        //loadAdditional(nbt, str)
    }

    override fun hashCode(): Int {
        return (x + y) * 0x10101010 + z
    }

    fun worldDimension(): ResourceKey<Level> {
        return dimension ?: throw RuntimeException("Dimension undefined")
    }

    private var w: Level? = null

    /*
    fun world(): Level {
        return if (w == null) {
            //ServerLifecycleHooks.getCurrentServer()
            FMLCommonHandler.instance().minecraftServerInstance.worldServerForDimension(worldDimension())
        } else w!!
    }*/

    /*
    constructor(entity: NodeBlockEntity) {
        val blockPos = entity.blockPos
        x = blockPos.x
        y = blockPos.y
        z = blockPos.z
        dimension = entity.level?.dimension()
    }*/

    constructor(x: Int, y: Int, z: Int, d: ResourceKey<Level>?) {
        this.x = x
        this.y = y
        this.z = z
        this.dimension = d
    }

    constructor(x: Int, y: Int, z: Int, world: Level) {
        this.x = x
        this.y = y
        this.z = z
        dimension = world.dimension()
        if (!world.isClientSide) w = world
    }

    constructor(entity: BlockEntity) {
        x = entity.blockPos.x
        y = entity.blockPos.y
        z = entity.blockPos.z
        dimension = entity.level?.dimension()
        if (entity.level?.isClientSide != true) w = entity.level
    }

    fun newWithOffset(x: Int, y: Int, z: Int): Coordinate {
        return Coordinate(this.x + x, this.y + y, this.z + z, dimension)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Coordinate) return false
        return other.x == x && other.y == y && other.z == z && other.dimension == dimension
    }

    /*
    override fun loadAdditional(nbt: CompoundTag, str: String) {
        x = nbt.getInt(str + "x")
        y = nbt.getInt(str + "y")
        z = nbt.getInt(str + "z")
        dimension = nbt.getInt(str + "d")
    }

    override fun saveAdditional(nbt: CompoundTag, str: String) {
        nbt.putInt(str + "x", x)
        nbt.putInt(str + "y", y)
        nbt.putInt(str + "z", z)
        nbt.putInt(str + "d", dimension)
    }*/

    override fun toString(): String {
        return "X : $x Y : $y Z : $z D : $dimension"
    }

    fun move(dir: Direction) {
        when (dir) {
            Direction.XN -> x--
            Direction.XP -> x++
            Direction.YN -> y--
            Direction.YP -> y++
            Direction.ZN -> z--
            Direction.ZP -> z++
        }
    }
/*
    var block: Block
        get() = world().getBlockState(BlockPos(x, y, z)).block
        set(b) {
            // TODO: I put 3 here as a guess. It may be 0
            world().setBlock(BlockPos(x, y, z), b.defaultBlockState(), 3)
        }*/

    fun getAABB(ray: Int): AABB {
        return AABB((
                x - ray).toDouble(), (y - ray).toDouble(), (z - ray).toDouble(), (
                x + ray + 1).toDouble(), (y + ray + 1).toDouble(), (z + ray + 1).toDouble())
    }

    /*fun distanceTo(e: Entity): Double {
        return abs(e.posX - (x + 0.5)) + abs(e.posY - (y + 0.5)) + abs(e.posZ - (z + 0.5))
    }*/

    fun copyTo(v: DoubleArray) {
        v[0] = x + 0.5
        v[1] = y + 0.5
        v[2] = z + 0.5
    }

    fun setPosition(vp: DoubleArray) {
        x = vp[0].toInt()
        y = vp[1].toInt()
        z = vp[2].toInt()
    }

    fun setPosition(vp: Vec3) {
        x = vp.x.toInt()
        y = vp.y.toInt()
        z = vp.z.toInt()
    }

    /*
    val tileEntity: BlockEntity?
        get() = world().getBlockEntity(BlockPos(x, y, z))
     */
    fun invalidate() {
        x = -1
        y = -1
        z = -1
        dimension = null
    }

    val isValid: Boolean
        get() = dimension != null

    fun trueDistanceTo(c: Coordinate): Double {
        val dx = (x - c.x).toLong()
        val dy = (y - c.y).toLong()
        val dz = (z - c.z).toLong()
        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble())
    }

    fun setDimension(dimension: ResourceKey<Level>) {
        this.dimension = dimension
        w = null
    }

    fun copyFrom(c: Coordinate) {
        x = c.x
        y = c.y
        z = c.z
        dimension = c.dimension
    }

    fun applyTransformation(front: Direction, coordinate: Coordinate) {
        front.rotateFromXN(this)
        x += coordinate.x
        y += coordinate.y
        z += coordinate.z
    }

    fun setWorld(worldObj: Level) {
        if (!worldObj.isClientSide) w = worldObj
        dimension = worldObj.dimension()
    }

    /*
    operator fun compareTo(o: Coordinate): Int {
        return when {
            dimension != o.dimension ->
                dimension - o.dimension
            x != o.x ->
                x - o.x
            y != o.y ->
                y - o.y
            z != o.z ->
                z - o.z
            else -> 0
        }
    }*/

    fun subtract(b: Coordinate): Coordinate {
        return newWithOffset(-b.x, -b.y, -b.z)
    }

    fun negate(): Coordinate {
        return Coordinate(-x, -y, -z, dimension)
    }

    fun toBlockPos(): BlockPos {
        return BlockPos(x, y, z)
    }

    companion object {
        @JvmStatic
        fun getAABB(a: Coordinate, b: Coordinate): AABB {
            return AABB(
                a.x.coerceAtMost(b.x).toDouble(), a.y.coerceAtMost(b.y).toDouble(), a.z.coerceAtMost(b.z).toDouble(),
                a.x.coerceAtLeast(b.x) + 1.0, a.y.coerceAtLeast(b.y) + 1.0, a.z.coerceAtLeast(b.z) + 1.0)
        }
    }
}
