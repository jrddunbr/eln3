package org.eln.eln3.sim.process.destruct

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import org.eln.eln3.Config

class WorldExplosion : IDestructible {
    private var origin: Any? = null
    var level: Level
    var pos: BlockPos
    private var strength = 0f
    var type: String = ""

    constructor(p: BlockPos, l: Level) {
        pos = p
        level = l
    }

    /*
    constructor(e: ShaftElement) {
        coordinate = e.coordonate()
        type = e.toString()
        origin = e
    }

    constructor(e: SixNodeElement) {
        coordinate = e.coordinate?: throw RuntimeException("WorldExplosion: Null SixNode Element received")
        type = e.toString()
        origin = e
    }

    constructor(e: TransparentNodeElement) {
        coordinate = e.coordinate()
        type = e.toString()
        origin = e
    }

    constructor(e: EnergyConverterElnToOtherNode) {
        coordinate = e.coordinate
        type = e.toString()
        origin = e
    }*/

    fun cableExplosion(): WorldExplosion {
        strength = 1.5f
        return this
    }

    fun machineExplosion(): WorldExplosion {
        strength = 3f
        return this
    }

    override fun destructImpl() {
        if (Config.explosions) {
            level.explode(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, strength, Level.ExplosionInteraction.BLOCK)
        } else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
    }

    override fun describe(): String {
        return "$pos $level exploded"
    }
}