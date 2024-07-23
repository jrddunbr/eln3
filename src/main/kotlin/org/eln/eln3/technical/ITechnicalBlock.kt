package org.eln.eln3.technical

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
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
import net.minecraft.world.phys.BlockHitResult
import org.eln.eln3.registry.ElnItems.VOLTMETER_ITEM

interface ITechnicalBlock {

    fun newTechnical(state: BlockState, blockPos: BlockPos, level: Level, entity: ITechnicalEntity?): TechnicalBase

    fun getTechnical(blockPos: BlockPos, level: Level): TechnicalBase? {
        return TechnicalManager.instance?.getTechnicalsFromLocation(blockPos, level)?.values?.firstOrNull()
    }

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

    fun useItemOnTech(
        pStack: ItemStack,
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHand: InteractionHand,
        pHitResult: BlockHitResult
    ): ItemInteractionResult {
        if (pStack.item == VOLTMETER_ITEM.asItem()) {
            val voltmeterString = TechnicalManager.instance!!.getTechnicalsFromLocation(pPos, pLevel)
                .values.firstOrNull()?.getVoltmeterString(null)?: ""
            pPlayer.displayClientMessage(Component.literal(voltmeterString), false)
            return ItemInteractionResult.CONSUME
        }
        return ItemInteractionResult.SUCCESS
    }

    fun useWithoutItemTech(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHitResult: BlockHitResult
    ): InteractionResult {
        return InteractionResult.SUCCESS
    }
}