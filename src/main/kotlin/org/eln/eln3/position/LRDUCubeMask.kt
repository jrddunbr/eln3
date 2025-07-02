package org.eln.eln3.position

import net.minecraft.core.Direction

class LRDUCubeMask {
    var lrduMaskArray = arrayOfNulls<LRDUMask>(6)

    fun getSide(direction: Direction): LRDUMask? {
        return lrduMaskArray[direction.ordinal]
    }

    fun clear() {
        for (lrduMask in lrduMaskArray) {
            lrduMask!!.set(0)
        }
    }

    operator fun set(direction: Direction, lrdu: LRDU, value: Boolean) {
        get(direction)!![lrdu] = value
    }

    operator fun get(direction: Direction, lrdu: LRDU): Boolean {
        return get(direction)!![lrdu]
    }

    operator fun get(direction: Direction): LRDUMask? {
        return lrduMaskArray[direction.ordinal]
    }

    fun getTranslate(side: Direction): LRDUMask {
        val mask = LRDUMask()
        for (lrdu in LRDU.entries) {
            val otherSide = applyLRDUToDirection(side, lrdu)
            val otherLrdu = getLRDUGoingToDirection(otherSide, side)
            mask[lrdu] = this[otherSide, otherLrdu!!]
        }
        return mask
    }

    /**
     * Apply LRDU transformation to a Minecraft core Direction
     * This replaces the deprecated Direction.applyLRDU() method
     */
    private fun applyLRDUToDirection(direction: Direction, lrdu: LRDU): Direction {
        return when (direction) {
            Direction.NORTH -> when (lrdu) {
                LRDU.Left -> Direction.WEST
                LRDU.Right -> Direction.EAST
                LRDU.Down -> Direction.DOWN
                LRDU.Up -> Direction.UP
            }
            Direction.SOUTH -> when (lrdu) {
                LRDU.Left -> Direction.EAST
                LRDU.Right -> Direction.WEST
                LRDU.Down -> Direction.DOWN
                LRDU.Up -> Direction.UP
            }
            Direction.EAST -> when (lrdu) {
                LRDU.Left -> Direction.NORTH
                LRDU.Right -> Direction.SOUTH
                LRDU.Down -> Direction.DOWN
                LRDU.Up -> Direction.UP
            }
            Direction.WEST -> when (lrdu) {
                LRDU.Left -> Direction.SOUTH
                LRDU.Right -> Direction.NORTH
                LRDU.Down -> Direction.DOWN
                LRDU.Up -> Direction.UP
            }
            Direction.UP -> when (lrdu) {
                LRDU.Left -> Direction.WEST
                LRDU.Right -> Direction.EAST
                LRDU.Down -> Direction.NORTH
                LRDU.Up -> Direction.SOUTH
            }
            Direction.DOWN -> when (lrdu) {
                LRDU.Left -> Direction.WEST
                LRDU.Right -> Direction.EAST
                LRDU.Down -> Direction.SOUTH
                LRDU.Up -> Direction.NORTH
            }
        }
    }

    /**
     * Get the LRDU direction needed to go from one Direction to another
     * This replaces the deprecated Direction.getLRDUGoingTo() method
     */
    private fun getLRDUGoingToDirection(from: Direction, to: Direction): LRDU? {
        for (lrdu in LRDU.entries) {
            if (applyLRDUToDirection(from, lrdu) == to) {
                return lrdu
            }
        }
        return null
    }

    init {
        for (idx in 0..5) {
            lrduMaskArray[idx] = LRDUMask()
        }
    }
}
