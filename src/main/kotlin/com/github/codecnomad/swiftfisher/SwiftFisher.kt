package com.github.codecnomad.swiftfisher

import com.github.codecnomad.swiftfisher.util.rotation.RotationManager
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod(modid = "swiftfisher", useMetadata = true)
class SwiftFisher {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.message.toString().contains("x!")) RotationManager.rotateTo(BlockPos(3, 6, 3))
    }
}
