package com.github.codecnomad.swiftfisher.util.rotation

import java.lang.Math.toDegrees
import javax.vecmath.Vector3f
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

object RotationHelper {
    fun calculateNeededRotation(source: Vector3f, target: Vector3f): Rotation {
        val delta = Vector3f(target.x - source.x, target.y - source.y, target.z - source.z)

        return Rotation(
            toDegrees(atan2(-delta.x, delta.z).toDouble()).toFloat(),
            -toDegrees(atan2(delta.y, sqrt(delta.x * delta.x + delta.z * delta.z)).toDouble()).toFloat()
        )
    }

    fun calculateBezierPath(yawPoints: List<Float>, pitchPoints: List<Float>, t: Float): Rotation {
        return Rotation(
            calculateBezierAtT(yawPoints, t), calculateBezierAtT(pitchPoints, t)
        )
    }

    private fun calculateBezierAtT(controlPoints: List<Float>, t: Float): Float {
        val n: Int = controlPoints.size
        var x = 0f

        for (i in 0 until n) { // This may be wrong
            x += binomialCoefficient(n, (i + 1)) * (1 - t).pow(n - (i + 1)) * t.pow(i + 1) * controlPoints[i]
        }

        return x
    }

    private fun binomialCoefficient(n: Int, k: Int): Float {
        if (k > n) return 0f
        if (k == 0 || k == n) return 1f

        return binomialCoefficient(n - 1, k - 1) + binomialCoefficient(k - 1, k)
    }
}