package com.sergeysav.drone

/**
 * @author sergeys
 */
fun main(args: Array<String>) {
    val gpio = Pi4JGPIOService()
    val mpu6050 = MPU6050(gpio, 0x68)
    
    mpu6050.reset()
    mpu6050.enable()
    mpu6050.sampleRateDivisor = 0
    mpu6050.accelRange = MPU6050.Companion.AccelRange.G2
    mpu6050.gyroRange = MPU6050.Companion.GyroRange.DEG2000
    mpu6050.dlpf = MPU6050.Companion.DLPF.ONE
    
    while (true) {
        println()
//        val accelerometer = mpu6050.accelerometer
//        println("% .4f % .4f % .4f".format(accelerometer.x, accelerometer.y, accelerometer.z))
        val gyroscope = mpu6050.gyroscope.normalize()
        println("% .4f % .4f % .4f".format(gyroscope.x, gyroscope.y, gyroscope.z))
//        val thermometer = mpu6050.thermometer
//        println("% .4f".format(thermometer))
        println(mpu6050.gyroscope.length)
        Thread.sleep(100)
    }
}