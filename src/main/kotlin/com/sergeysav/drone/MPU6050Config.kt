package com.sergeysav.drone

import com.beust.klaxon.Klaxon
import com.sergeysav.drone.math.Vector3
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

val path = Paths.get("mpu6050config.json")
val utf8Charset = Charset.forName("UTF-8")

data class MPU6050Config(var accelOffset: Vector3 = Vector3(), var gyroOffset: Vector3 = Vector3()) {
    constructor(mpU6050: MPU6050) : this(mpU6050.accelOffset, mpU6050.gyroOffset)
    
    fun setMPU(mpu6050: MPU6050) {
        mpu6050.accelOffset = accelOffset
        mpu6050.gyroOffset = gyroOffset
    }
}
fun saveMPUConfig(mpu6050Config: MPU6050Config) {
    Files.write(path, Klaxon().toJsonString(mpu6050Config).toByteArray(utf8Charset))
}

fun loadMPUConfig(): MPU6050Config? {
    if (Files.exists(path)) {
        return Klaxon().parse<MPU6050Config>(Files.readAllBytes(path).toString(utf8Charset)) ?: throw RuntimeException(
                "Error parsing mpu config json file")
    }
    return null
}