package org.eln.eln3.technical

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState

interface ITechnicalBlock {

    fun onPlaceTech(
        block: Block,
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pOldState: BlockState,
        pMovedByPiston: Boolean
    ) {
        TechnicalManager.instance!!.addTechnical(block, pState, null, pPos, pLevel)
    }

    fun onRemoveTech(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pNewState: BlockState,
        pMovedByPiston: Boolean
    ) {
        TechnicalManager.instance!!.removeTechnicalsFromLocation(pPos, pLevel)
    }

    fun onBlockStateChangeTech(level: LevelReader, pos: BlockPos, oldState: BlockState, newState: BlockState) {
        // ???
    }

    fun onDestroyedByPlayerTech(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        willHarvest: Boolean,
        fluid: FluidState
    ) {
        TechnicalManager.instance!!.removeTechnicalsFromLocation(pos, level)
    }
}