package com.github.codecnomad.swiftfisher.macro

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.item.ItemFishingRod
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.lang.Thread.sleep

class MacroManager {
    private var phase: Phase = Phase.SETUP_FISHING_ROD
    private var entityHook: EntityFishHook? = null
    private var entityMarker: EntityArmorStand? = null
    private var entityMonster: Entity? = null
    private val player: EntityPlayerSP = Minecraft.getMinecraft().thePlayer
    private val world: World = Minecraft.getMinecraft().theWorld

    fun enable() {
        phase = Phase.SETUP_FISHING_ROD

        MinecraftForge.EVENT_BUS.unregister(this)
    }

    private fun disable() {
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

                if (!hasFound) return disable()

                KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.keyCode)
                phase = Phase.WAIT_FOR_CATCH
            }

            Phase.WAIT_FOR_CATCH -> {
                callbackIn(150) {

                    if (entityHook == null || entityHook!!.isDead) {
                        for (entity: Entity in world.loadedEntityList) {
                            if (entity !is EntityFishHook) continue
                            entityHook = entity
                        }
                    }

                    if (entityMarker != null && !entityMarker!!.isDead) {
                        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.keyCode)
                        phase = Phase.KILL_MOB
                    } else
                        phase = Phase.WAIT_FOR_CATCH
                }
            }

            Phase.KILL_MOB -> {

            }

            Phase.SUSPEND -> {

            }
        }
    }

    enum class Phase {
        SETUP_FISHING_ROD, WAIT_FOR_CATCH, KILL_MOB, SUSPEND
    }

    private fun callbackIn(ms: Long, callback: Runnable) {
        phase = Phase.SUSPEND

        Thread {
            sleep(ms)
            callback.run()
        }.start()
    }

    @SubscribeEvent
    fun onEntitySpawn(event: EntityJoinWorldEvent) {
        if (entityHook == null || /*event.entity is EntitySquid ||*/ event.entity.name.equals("item.tile.stone.stone")) return

        // The countdown that shows up when entity is starting to bite
        if (event.entity is EntityArmorStand && event.entity.getDistanceToEntity(entityHook) < 0.1) entityMarker =
            event.entity as EntityArmorStand
        // The entity that comes at you when you catch smt
        else if (event.entity.getDistanceToEntity(entityHook) <= 1.5) entityMonster = event.entity as Entity

    }
}