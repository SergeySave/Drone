package com.sergeysav.drone

import com.sergeysav.drone.math.Vector3

/**
 * @author sergeys
 */
data class MPU6050Config(var accelOffset: Vector3 = Vector3(), var gyroOffset: Vector3 = Vector3()) {
    constructor(mpU6050: MPU6050) : this(mpU6050.accelOffset, mpU6050.gyroOffset)
    
    fun setMPU(mpu6050: MPU6050) {
        mpu6050.accelOffset = accelOffset
        mpu6050.gyroOffset = gyroOffset
    }
}