package com.sergeysav.drone.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

/**
 * @author sergeys
 *
 * @constructor Creates a new Quaternion
 */
data class Quaternion(var w: Double = 1.0, var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    constructor(vec: Vector3): this(0.0, vec.x, vec.y, vec.z)
    
    operator fun times(other: Quaternion) = copy().apply { this *= other }
    operator fun timesAssign(other: Quaternion) {
        val w = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z
        val x = other.w * this.x + other.x * this.w - other.y * this.z + other.z * this.y
        val y = other.w * this.y + other.x * this.z + other.y * this.w - other.z * this.x
        val z = other.w * this.z - other.x * this.y + other.y * this.x + other.z * this.w
        this.w = w
        this.x = x
        this.y = y
        this.z = z
    }
    
    fun toVector() = Vector3(x, y, z)
    fun inverse() = Quaternion(w, -x, -y, -z)
}

fun quaternionFromAxisAngle(radians: Double, axis: Vector3): Quaternion {
    val w = cos(radians/2)
    val sin = sin(radians/2)
    
    return Quaternion(w, sin * axis.x, sin * axis.y, sin * axis.z)
}

fun quaternionFromEulerAngles(pitch: Double, roll: Double, yaw: Double): Quaternion {
    val cy = cos(yaw * 0.5)
    val sy = sin(yaw * 0.5)
    val cr = cos(roll * 0.5)
    val sr = sin(roll * 0.5)
    val cp = cos(pitch * 0.5)
    val sp = sin(pitch * 0.5)
    
    return Quaternion(cy * cr * cp + sy * sr * sp,
                      cy * sr * cp - sy * cr * sp,
                      cy * cr * sp + sy * sr * cp,
                      sy * cr * cp - cy * sr * sp)
}

fun Quaternion.toEulerAngles(): Vector3 {
    val sinr_cosp = +2.0 * (w * x + y * z)
    val cosr_cosp = +1.0 - 2.0 * (x * x + y * y)
    val roll = atan2(sinr_cosp, cosr_cosp)
    
    val sinp = +2.0 * (w * y - z * x)
    val pitch = if (abs(sinp) >= 1) {
        sign(sinp) * PI
    } else {
        asin(sinp)
    }
    
    val siny_cosp = +2.0 * (w * z + x * y)
    val cosy_cosp = +1.0 - 2.0 * (y * y + z * z)
    val yaw = atan2(siny_cosp, cosy_cosp)
    return Vector3(pitch, roll, yaw)
}