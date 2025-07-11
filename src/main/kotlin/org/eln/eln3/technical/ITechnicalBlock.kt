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
import org.eln.eln3.registry.ElnItems.AMMETER_ITEM
import org.eln.eln3.registry.ElnItems.VOLTMETER_ITEM

interface ITechnicalBlock {

    fun newTechnical(state: BlockState, blockPos: BlockPos, level: Level, entity: ITechnicalEntity?): TechnicalBase

    fun getTechnical(blockPos: BlockPos, level: Level): TechnicalBase? {
        val technicals = TechnicalManager.get(level)?.getTechnicalsFromLocation(blockPos, level)
        return technicals?.values?.firstOrNull()
    }

    fun onPlaceTech(
        block: Block,
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pOldState: BlockState,
        pMovedByPiston: Boolean
    ) {
        //Eln3.LOGGER.info("BLOCK_PLACE: Block placed at $pPos, new: ${pState.block.javaClass.simpleName}, old: ${pOldState.block.javaClass.simpleName}, piston: $pMovedByPiston")
        TechnicalManager.get(pLevel)?.addTechnical(block, pState, null, pPos, pLevel)
    }


    fun onRemoveTech(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pNewState: BlockState,
        pMovedByPiston: Boolean
    ) {
        //Eln3.LOGGER.info("BLOCK_REMOVE: Block removed at $pPos, old: ${pState.block.javaClass.simpleName}, new: ${pNewState.block.javaClass.simpleName}, piston: $pMovedByPiston")
        TechnicalManager.get(pLevel)?.removeTechnicalsFromLocation(pPos, pLevel)
    }


    fun onBlockStateChangeTech(level: LevelReader, pos: BlockPos, oldState: BlockState, newState: BlockState) {
        if (level is Level) {
            TechnicalManager.get(level)?.updateTechnicalBlockState(pos, level, newState)
        }
    }

    fun onDestroyedByPlayerTech(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        willHarvest: Boolean,
        fluid: FluidState
    ) {
        //Eln3.LOGGER.info("BLOCK_DESTROY: Block destroyed by player at $pos, type: ${state.block.javaClass.simpleName}, willHarvest: $willHarvest")
        TechnicalManager.get(level)?.removeTechnicalsFromLocation(pos, level)
    }

    data class ToolResult(val message: String, val result: ItemInteractionResult)

    interface ToolInterface{
        fun getTechnicalData(technical: TechnicalBase): ToolResult
    }

    private fun sendToolMessage(pLevel: Level, pPos: BlockPos, pPlayer: Player, ti: ToolInterface): ItemInteractionResult {
        val technicalManager = TechnicalManager.get(pLevel)
        val result = if (technicalManager == null) {
            ToolResult("Error: No TechnicalManager found", ItemInteractionResult.CONSUME)
        } else {
            val technicals = technicalManager.getTechnicalsFromLocation(pPos, pLevel)
            if (technicals.isEmpty()) {
                ToolResult("Error: No technical data at $pPos", ItemInteractionResult.CONSUME)
            } else {
                val technical = technicals.values.first()
                ti.getTechnicalData(technical)
            }
        }
        pPlayer.displayClientMessage(Component.literal(result.message), false)
        return result.result
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
            return sendToolMessage(pLevel, pPos, pPlayer, object : ToolInterface {
                override fun getTechnicalData(technical: TechnicalBase): ToolResult {
                    return ToolResult("${technical.getLabelString(null)} ${technical.getVoltmeterString(null)}", ItemInteractionResult.CONSUME)
                }
            })
        } else if (pStack.item == AMMETER_ITEM.asItem()) {
            return sendToolMessage(pLevel, pPos, pPlayer, object : ToolInterface {
                override fun getTechnicalData(technical: TechnicalBase): ToolResult {
                    return ToolResult("${technical.getLabelString(null)} ${technical.getAmmeterString(null)}", ItemInteractionResult.CONSUME)
                }
            })
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
    }

    fun useWithoutItemTech(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHitResult: BlockHitResult
    ): InteractionResult {
        return InteractionResult.PASS
    }
}