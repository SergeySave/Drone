package com.sergeysav.drone

/**
 * @author sergeys
 */
fun main(args: Array<String>) {
    val gpio = Pi4jGPIO()
    val mpu6050 = MPU6050(gpio, 0x68)
    
    while (true) {
        println()
        println(mpu6050.accelerometer)
        println(mpu6050.gyroscope)
        println(mpu6050.thermometer)
    }
}