package org.eln.eln3.technical.single

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.MapColor
import org.eln.eln3.Eln3
import org.eln.eln3.technical.ITechnicalBlock

open class SingleBlock() :
    Block(Properties.of().mapColor(MapColor.STONE)), EntityBlock, ITechnicalBlock {

    override fun onPlace(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pOldState: BlockState,
        pMovedByPiston: Boolean
    ) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston)
        onPlaceTech( this, pState, pLevel, pPos, pOldState, pMovedByPiston)
    }

    override fun onRemove(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pNewState: BlockState,
        pMovedByPiston: Boolean
    ) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston)
        onRemoveTech(pState, pLevel, pPos, pNewState, pMovedByPiston)
    }

    override fun onBlockStateChange(level: LevelReader, pos: BlockPos, oldState: BlockState, newState: BlockState) {
        super.onBlockStateChange(level, pos, oldState, newState)
        onBlockStateChangeTech(level, pos, oldState, newState)
    }

    override fun onDestroyedByPlayer(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        willHarvest: Boolean,
        fluid: FluidState
    ): Boolean {
        onDestroyedByPlayerTech(state, level, pos, player, willHarvest, fluid)
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid)
    }

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity? {
        return SingleBlockEntity(Eln3.CABLE_BLOCK_ENTITY.get(), pPos, pState)
    }

    override fun getTechnical(): Class<*> {
        return SingleTechnical::class.java
    }
}