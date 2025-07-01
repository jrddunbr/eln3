package org.eln.eln3.singleentity

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import org.eln.eln3.Eln3
import org.eln.eln3.registry.ElnBlockEntities.SIMPLE_BLOCK_ENTITY
import org.eln.eln3.technical.ITechnicalBlock
import org.eln.eln3.technical.ITechnicalEntity
import org.eln.eln3.technical.TechnicalBase
import org.eln.eln3.technical.single.SingleTechnical
import org.eln.eln3.technical.singleentity.SingleEntityTechnical
import java.util.UUID

class SingleEntityTestBlock(properties: Properties) : Block(properties), EntityBlock, ITechnicalBlock {

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
        return SingleEntityTestBlockEntity(SIMPLE_BLOCK_ENTITY.get(), pPos, pState)
    }

    override fun newTechnical(
        state: BlockState,
        blockPos: BlockPos,
        level: Level,
        entity: ITechnicalEntity?
    ): TechnicalBase {
        return SingleEntityTechnical(this, state, entity, blockPos, level,UUID.randomUUID().toString())
    }
}