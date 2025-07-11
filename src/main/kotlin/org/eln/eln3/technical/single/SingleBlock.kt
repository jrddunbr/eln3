package org.eln.eln3.technical.single

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult
import org.eln.eln3.technical.ITechnicalBlock
import org.eln.eln3.technical.ITechnicalEntity
import org.eln.eln3.technical.TechnicalBase

open class SingleBlock() :
    Block(Properties.of().mapColor(MapColor.STONE)), ITechnicalBlock {

    override fun onPlace(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pOldState: BlockState,
        pMovedByPiston: Boolean
    ) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston)
        if (pLevel.isClientSide) {
            return
        }
        /*
        val block = pLevel.getBlockState(pPos).block
        if (block is SingleBlock) {
            block.getTechnical()
        }*/
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
        if (pLevel.isClientSide) {
            return
        }
        onRemoveTech(pState, pLevel, pPos, pNewState, pMovedByPiston)
    }

    override fun onBlockStateChange(level: LevelReader, pos: BlockPos, oldState: BlockState, newState: BlockState) {
        super.onBlockStateChange(level, pos, oldState, newState)
        if (level.isClientSide) {
            return
        }
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
        if (level.isClientSide) {
            return true
        }
        onDestroyedByPlayerTech(state, level, pos, player, willHarvest, fluid)
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid)
    }

    override fun useItemOn(
        pStack: ItemStack,
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHand: InteractionHand,
        pHitResult: BlockHitResult
    ): ItemInteractionResult {
        if (pLevel.isClientSide) {
            return ItemInteractionResult.SUCCESS
        }
        return useItemOnTech(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult)
    }

    override fun useWithoutItem(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHitResult: BlockHitResult
    ): InteractionResult {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS
        }
        return useWithoutItemTech(pState, pLevel, pPos, pPlayer, pHitResult)
    }

    override fun newTechnical(state: BlockState, blockPos: BlockPos, level: Level, entity: ITechnicalEntity?): TechnicalBase {
        throw RuntimeException("SingleBlock does not support newTechnical, please use an implementation class")
    }
}