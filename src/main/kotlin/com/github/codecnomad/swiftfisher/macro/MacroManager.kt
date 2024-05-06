package com.github.codecnomad.swiftfisher.macro

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemFishingRod
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class MacroManager {
    private var phase: Phase = Phase.SETUP_FISHING_ROD
    private var player: EntityPlayerSP = Minecraft.getMinecraft().thePlayer

    fun enable() {
        phase = Phase.SETUP_FISHING_ROD

        MinecraftForge.EVENT_BUS.unregister(this)
    }

    fun disable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        when (phase) {
            Phase.SETUP_FISHING_ROD -> {

                var hasFound = false
                for (i in 0..8) {
                    val stack: ItemStack = player.inventory.getStackInSlot(i)
                    if (stack.item !is ItemFishingRod) continue

                    player.inventory.currentItem = i
                    hasFound = true
                    break
                }

                if (!hasFound)
                    disable()

                phase = Phase.WAIT_FOR_MOB
            }

            Phase.WAIT_FOR_MOB -> {

                phase = Phase.KILL_MOB
            }

            Phase.KILL_MOB -> {

                phase = Phase.SETUP_FISHING_ROD
            }
        }
    }

    enum class Phase {
        SETUP_FISHING_ROD,
        WAIT_FOR_MOB,
        KILL_MOB
    }
}