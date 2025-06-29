package org.eln.eln3.compat

import mcjty.theoneprobe.api.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.Eln3
import org.eln.eln3.single.GroundBlock
import org.eln.eln3.single.GroundTechnical
import org.eln.eln3.single.VoltageSourceBlock
import org.eln.eln3.single.VoltageSourceTechnical
import org.eln.eln3.single.CableBlock
import org.eln.eln3.single.CableTechnical
import org.eln.eln3.technical.single.SingleBlock

object TopCompatibilityKotlin {

    @JvmStatic
    val probeInfoProvider = object : IProbeInfoProvider {
        override fun getID(): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(Eln3.MODID, "eln3")
        }

        fun addErrorInfo(
            probeInfo: IProbeInfo,
            error: String
        ) {
            probeInfo.text(error)
        }

        override fun addProbeInfo(
            mode: ProbeMode?,
            probeInfo: IProbeInfo,
            player: Player?,
            world: Level?,
            blockState: BlockState,
            data: IProbeHitData
        ) {
            when(blockState.block) {
                is CableBlock -> {
                    val block = blockState.block as SingleBlock
                    val technical = block.getTechnical(data.pos, world!!) as CableTechnical?
                    if (technical == null) {
                        addErrorInfo(probeInfo, "Technical is null :(")
                    } else {
                        technical.addProbeInfo(mode, probeInfo, player, world, blockState, data)
                    }
                }
                is VoltageSourceBlock -> {
                    val block = blockState.block as SingleBlock
                    val technical = block.getTechnical(data.pos, world!!) as VoltageSourceTechnical?
                    if (technical == null) {
                        addErrorInfo(probeInfo, "Technical is null :(")
                    } else {
                        technical.addProbeInfo(mode, probeInfo, player, world, blockState, data)
                    }
                }
                is GroundBlock -> {
                    val block = blockState.block as SingleBlock
                    val technical = block.getTechnical(data.pos, world!!) as GroundTechnical?
                    if (technical == null) {
                        addErrorInfo(probeInfo, "Technical is null :(")
                    } else {
                        technical.addProbeInfo(mode, probeInfo, player, world, blockState, data)
                    }
                }
            }

            /*

            // For operating on block entities, I guess.

            if (blockState.block is ) {
                Eln3.LOGGER.info("This is a CableBlock")
                val defaultStyle = probeInfo.defaultLayoutStyle()
                val selectedStyle: ILayoutStyle =
                    probeInfo.defaultLayoutStyle().copy().borderColor(Color.rgb(255, 255, 255)).spacing(2)

                val be = world?.getBlockEntity(data.pos)
                if (be is SingleEntityBlockEntity) {
                    Eln3.LOGGER.info("This is a SingleEntityBlockEntity")
                    val technical = be.getTechnicalReference()?: return
                    technical.addProbeInfo(mode, probeInfo, player, world, blockState, data)
                }
            }*/
        }
    }
}