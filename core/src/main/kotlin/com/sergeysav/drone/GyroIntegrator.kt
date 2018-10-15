package com.sergeysav.drone

import com.sergeysav.drone.math.Matrix3
import com.sergeysav.drone.math.Vector3

/**
 * @author sergeys
 *
 * @constructor Creates a new GyroIntegrator
 */
class GyroIntegrator {
    
    private val total: Matrix3 = Matrix3()
    
    operator fun invoke(vec: Vector3, delta: Double): Matrix3 {
        total leftMultiplyAssign Matrix3.rotation(vec * (delta * RAD_CONVERSION))
        return total
    }
}

const val RAD_CONVERSION = Math.PI / 180
