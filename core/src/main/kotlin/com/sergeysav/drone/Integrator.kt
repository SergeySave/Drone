package com.sergeysav.drone

import com.sergeysav.drone.math.Vector3

/**
 * @author sergeys
 *
 * @constructor Creates a new Integrator
 */
class Integrator {
    
    private val total: Vector3 = Vector3()
    private var init: Boolean = true
    private lateinit var last: Vector3
    
    operator fun invoke(vec: Vector3, delta: Double): Vector3 {
        if (init) {
            last = vec
        }
        
        total.x += (vec.x + last.x) * delta / 2
        total.y += (vec.y + last.y) * delta / 2
        total.z += (vec.z + last.z) * delta / 2
        
        last = vec
        
        return total
    }
}