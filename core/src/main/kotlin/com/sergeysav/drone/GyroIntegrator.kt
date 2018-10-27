package com.sergeysav.drone

import com.sergeysav.drone.math.Quaternion
import com.sergeysav.drone.math.Vector3
import com.sergeysav.drone.math.quaternionFromEulerAngles
import kotlin.math.atan2
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * @author sergeys
 *
 * @constructor Creates a new GyroIntegrator
 */
class GyroIntegrator(val alpha: Double) {
    
    private val total: Vector3 = Vector3()
    private val invAlpha = 1 - alpha
    private val mu = 0.001
    
    operator fun invoke(eulerAngles: Vector3, accelerometer: Vector3, delta: Double): Quaternion {
        
        eulerAngles *= delta
        
        val angleY = atan2(-accelerometer.x, sqrt(accelerometer.y * accelerometer.y + accelerometer.z * accelerometer.z))
        val angleX = atan2(accelerometer.y, sign(accelerometer.z) * sqrt(accelerometer.z * accelerometer.z + mu * accelerometer.x * accelerometer.x))
        val angleZ = 0
        
        total.x = alpha * (total.x + eulerAngles.y) + invAlpha * angleY
        total.y = alpha * (total.y + eulerAngles.x) + invAlpha * angleX
        total.z = alpha * (total.z + eulerAngles.z) + invAlpha * angleZ

        return quaternionFromEulerAngles(pitch = total.x, roll = total.y, yaw = total.z)
    }
}
