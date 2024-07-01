package org.eln.eln3.compat

import mcjty.theoneprobe.api.*
import net.minecraft.core.GlobalPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.eln.eln3.Eln3
import org.eln.eln3.single.cable.CableBlock
import org.eln.eln3.single.cable.CableBlockEntity
import org.eln.eln3.single.cable.CableTechnical
import org.eln.eln3.technical.single.SingleBlockEntity
import org.eln.eln3.technical.single.SingleTechnical

object TopCompatibilityKotlin {

    @JvmStatic
    val probeInfoProvider = object : IProbeInfoProvider {
        override fun getID(): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(Eln3.MODID, "eln3")
        }

        override fun addProbeInfo(
            mode: ProbeMode?,
            probeInfo: IProbeInfo,
            player: Player?,
            world: Level?,
            blockState: BlockState,
            data: IProbeHitData
        ) {
            if (blockState.block is CableBlock) {
                Eln3.LOGGER.info("This is a CableBlock")
                val defaultStyle = probeInfo.defaultLayoutStyle()
                val selectedStyle: ILayoutStyle =
                    probeInfo.defaultLayoutStyle().copy().borderColor(Color.rgb(255, 255, 255)).spacing(2)

                val be = world?.getBlockEntity(data.pos)
                if (be is SingleBlockEntity) {
                    Eln3.LOGGER.info("This is a SingleBlockEntity")
                    val technical = be.getTechnicalReference()?: return
                    technical.addProbeInfo(mode, probeInfo, player, world, blockState, data)
                }
            }

            /*
            if (blockState.block is ProcessorBlock) {
                val vec =
                    data.hitVec.subtract(data.pos.x.toDouble(), data.pos.y.toDouble(), data.pos.z.toDouble())
                val quadrant: Int = ProcessorBlock.getQuadrant(data.sideHit, vec)

                val defaultStyle = probeInfo.defaultLayoutStyle()
                val selectedStyle: ILayoutStyle =
                    probeInfo.defaultLayoutStyle().copy().borderColor(Color.rgb(255, 255, 255)).spacing(2)

                val button0 = blockState.getValue<Boolean>(ProcessorBlock.BUTTON10)
                probeInfo.horizontal(if (quadrant == 0) selectedStyle else defaultStyle)
                    .text(
                        Component.translatable(
                            ProcessorBlockEntity.ACTION_MELT,
                            if (button0) "On" else "Off"
                        )
                    )
                val button1 = blockState.getValue<Boolean>(ProcessorBlock.BUTTON00)
                probeInfo.horizontal(if (quadrant == 1) selectedStyle else defaultStyle)
                    .text(
                        Component.translatable(
                            ProcessorBlockEntity.ACTION_BREAK,
                            if (button1) "On" else "Off"
                        )
                    )
                val button2 = blockState.getValue<Boolean>(ProcessorBlock.BUTTON11)
                probeInfo.horizontal(if (quadrant == 2) selectedStyle else defaultStyle)
                    .text(
                        Component.translatable(
                            ProcessorBlockEntity.ACTION_SOUND,
                            if (button2) "On" else "Off"
                        )
                    )
                val button3 = blockState.getValue<Boolean>(ProcessorBlock.BUTTON01)
                probeInfo.horizontal(if (quadrant == 3) selectedStyle else defaultStyle)
                    .text(
                        Component.translatable(
                            ProcessorBlockEntity.ACTION_SPAWN,
                            if (button3) "On" else "Off"
                        )
                    )
            }*/
        }
    }
}