package com.sergeysav.drone

import com.beust.klaxon.Klaxon
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

val path = Paths.get("mpu6050config.json")
val utf8Charset = Charset.forName("UTF-8")

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