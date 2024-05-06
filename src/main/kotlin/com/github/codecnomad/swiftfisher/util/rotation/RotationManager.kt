package com.github.codecnomad.swiftfisher.util.rotation

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.util.BlockPos
import javax.vecmath.Vector3f
import kotlin.math.abs

object RotationManager {
    fun rotateTo(entity: Entity, override: Boolean = false) {
        rotateToFromPlayer(Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat()), override)
    }

    fun rotateTo(position: BlockPos, override: Boolean = false) {
        rotateToFromPlayer(Vector3f(position.x.toFloat(), position.y.toFloat(), position.z.toFloat()), override)
    }

    private fun rotateToFromPlayer(target: Vector3f, override: Boolean = false) {
        val player: EntityPlayerSP = Minecraft.getMinecraft().thePlayer ?: return
        val rotation: Rotation = RotationHelper.calculateNeededRotation(
            Vector3f(player.posX.toFloat(), player.posY.toFloat(), player.posZ.toFloat()), target
        )
        rotateTo(rotation, override)
    }

    private var rotationThread: Thread = Thread {}
    private fun rotateTo(target: Rotation, override: Boolean = false) {
        if (rotationThread.isAlive) {
            if (override) {
                rotationThread.interrupt()
                rotationThread.join()
            } else return
        }

        rotationThread = Thread {
            val player: EntityPlayerSP = Minecraft.getMinecraft().thePlayer ?: return@Thread

            val current = Rotation(player.rotationYaw, player.rotationPitch)

            // TODO: Change these values
            val msPD = 3f // ms / degree of turn
            val yawControlPoints: List<Float> = listOf(0f, .3f, .7f, 1f) // yaw interpolation BÃ©zier curve
            val pitchControlPoints: List<Float> = listOf(0f, .3f, .7f, 1f) // pitch

            val difference = Rotation((target.yaw - current.yaw), (target.pitch - current.pitch))
            difference.yaw = (difference.yaw + 180) % 360 - 180
            difference.pitch = (difference.pitch + 180) % 360 - 180

            val rotationPath = mutableListOf<Rotation>()
            val totalTime: Float = (abs(difference.yaw) + abs(difference.pitch)) * msPD

            var t: Float = 1 / totalTime
            while (t < 1) {
                t += 1 / totalTime

                rotationPath.add(RotationHelper.calculateBezierPath(yawControlPoints, pitchControlPoints, t))
            }

            for (rotation in rotationPath) {
                if (rotationThread.isInterrupted) return@Thread

                player.rotationYaw = current.yaw + difference.yaw * rotation.yaw
                player.rotationPitch = current.pitch + difference.pitch * rotation.pitch

                Thread.sleep(((totalTime / rotationPath.size).toLong()))
            }
        }

        rotationThread.start()
    }
}