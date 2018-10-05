package com.sergeysav.drone

import kotlin.math.sqrt

/**
 * @author sergeys
 *
 * @constructor Creates a new Vector3
 */
data class Vector3(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    
    val length: Double
        get() = sqrt(x * x + y * y + z * z)
    val length2: Double
        get() = x * x + y * y + z * z
    
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun plusAssign(other: Vector3) {
        x += other.x
        y += other.y
        z += other.z
    }
    
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun minusAssign(other: Vector3) {
        x -= other.x
        y -= other.y
        z -= other.z
    }
    
    operator fun times(scalar: Double) = Vector3(scalar * x, scalar * y, scalar * z)
    operator fun timesAssign(scalar: Double) {
        x *= scalar
        y *= scalar
        z *= scalar
    }
    
    infix fun dot(other: Vector3) = x * other.x + y * other.y + z * other.z
    infix fun cross(other: Vector3) = Vector3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
    
    fun normalize() = timesAssign(1/length)
}

val ZERO: Vector3
    get() = Vector3()

val X: Vector3
    get() = Vector3(1.0, 0.0, 0.0)

val Y: Vector3
    get() = Vector3(0.0, 1.0, 0.0)

val Z: Vector3
    get() = Vector3(0.0, 0.0, 1.0)

//These operators are here so you have to choose to import these, so if you want to use your own definitions
// for these functions
//Design decision to not make a copy for unary operators
operator fun Vector3.unaryPlus() = this
operator fun Vector3.unaryMinus(): Vector3 {
    x = -x
    y = -y
    z = -z
    return this
}
//Design decision to make dot product the default vector * vector operation
operator fun Vector3.times(other: Vector3) = this dot other

