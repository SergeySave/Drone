package com.sergeysav.drone

import com.sergeysav.drone.MPU6050.Companion.GRAVITY
import com.sergeysav.drone.io.Pi4JGPIOService
import com.sergeysav.drone.math.Vector3
import com.sergeysav.drone.math.Z
import com.sergeysav.drone.math.timesAssign
import kotlin.math.abs

/**
 * @author sergeys
 */
fun main(args: Array<String>) {
    val gpio = Pi4JGPIOService()
    val mpu6050 = MPU6050(gpio, 0x68)
    
    mpu6050.reset()
    mpu6050.enable()
    mpu6050.sampleRateDivisor = 0
    mpu6050.accelRange = MPU6050.Companion.AccelRange.G4
    mpu6050.gyroRange = MPU6050.Companion.GyroRange.DEG250
    mpu6050.dlpf = MPU6050.Companion.DLPF.TWO
    mpu6050.thermometerDisable = true
    
    run { //Clear out any bad data
        var i = 0
        var last = System.nanoTime()
        while (i < 100) {
            if (System.nanoTime() - last >= 2e6) {
                last = System.nanoTime()
                val (a, g) = mpu6050.accelGyro
            
                //Do something so it doesn't get deleted
                a += g
                
                i++
            }
        }
    }
    
//    run {
//        val (aOffset, gOffset) = calibrate(mpu6050, 100, 100000)
//
//        println(aOffset)
//        println(gOffset)
//    }
//
//    if (true) {
//        mpu6050.close()
//        return
//    }

    val aOffset = Vector3(x=-1.1098743677139282, y=0.06038401648402214, z=0.2973174750804901)
    val gOffset = Vector3(x=0.003240607911720872, y=-0.4606495797634125, z=-0.6764510869979858)
    
    
    val intAcc = Integrator()
    val intVel = Integrator()

    val intGyr = GyroIntegrator()
    
    var lastString = ""
    
    val scaledAccelDeadzone = 8 * mpu6050.accelRange.scaleFactor
    val scaledGyroDeadzone = 2 * mpu6050.gyroRange.scaleFactor
    
    aOffset.reduce()
    gOffset.reduce()
    
    var last = System.nanoTime()
    while (true) {
        if (System.nanoTime() - last >= 5000000) { //5 milliseconds
            val curr = System.nanoTime()
            val delta = (curr - last)/1_000_000_000.0
            last = curr
    
            val (accelerometer, gyroscope) = mpu6050.accelGyro
//            val gyroscope = mpu6050.gyroscope
//            val accelerometer = mpu6050.accelerometer
            gyroscope.reduce()
            accelerometer.reduce()
            gyroscope -= gOffset
            gyroscope.reduce()
            if (abs(gyroscope.x) < scaledGyroDeadzone) gyroscope.x = 0.0
            if (abs(gyroscope.y) < scaledGyroDeadzone) gyroscope.y = 0.0
            if (abs(gyroscope.z) < scaledGyroDeadzone) gyroscope.z = 0.0
            
            val orientation = intGyr(gyroscope, delta)
    
            accelerometer -= aOffset
            accelerometer *= orientation.transpose()
            accelerometer.reduce()
            accelerometer -= Z * GRAVITY
            if (abs(accelerometer.x) < scaledAccelDeadzone) accelerometer.x = 0.0
            if (abs(accelerometer.y) < scaledAccelDeadzone) accelerometer.y = 0.0
            if (abs(accelerometer.z) < scaledAccelDeadzone) accelerometer.z = 0.0
            
//            orientation.transpose() * ( - aOffset)
//            accelerometer -= aOffset
            val velocity = intAcc(accelerometer, delta)
            velocity.reduce()
            val position = intVel(velocity, delta)
            position.reduce()
    
//            val g = "% .4f % .4f % .4f".format(accelerometer.x, accelerometer.y, accelerometer.z)
            val a = "% .4f % .4f % .4f".format(accelerometer.x, accelerometer.y, accelerometer.z)
            val v = "% .4f % .4f % .4f".format(velocity.x, velocity.y, velocity.z)
            val p = "% .4f % .4f % .4f".format(position.x, position.y, position.z)
            print("\b".repeat(lastString.length))
            lastString = "$a${" ".repeat(80-a.length)}$v${" ".repeat(80-v.length)}$p${" ".repeat(80-p.length)}"
            print(lastString)
//            println("${orientation.p00}\t${orientation.p11}\t${orientation.p22}")
        }
    }
}

fun Vector3.reduce(): Vector3 {
    x = x.toFloat().toDouble()
    y = y.toFloat().toDouble()
    z = z.toFloat().toDouble()
    return this
}

fun calibrateAccelAxes(mpu6050: MPU6050, threshold: Double, minCounts: Int): Vector3 {
    var mX = 0
    val x = Vector3()
    var mY = 0
    val y = Vector3()
    var mZ = 0
    val z = Vector3()
    
    var lastString = "X: $mX Y: $mY Z: $mZ"
    print(lastString)
    var last = System.nanoTime()
    while (mX <= minCounts || mY <= minCounts || mZ <= minCounts) {
        val accelerometer = mpu6050.accelerometer
        val (x1, y1, z1) = accelerometer.normalize()
        if (Math.abs(x1) > threshold) {
            mX++
            print("\b".repeat(lastString.length))
            lastString = "X: $mX Y: $mY Z: $mZ"
            print(lastString)
            x += accelerometer
        }
        if (Math.abs(y1) > threshold) {
            mY++
            print("\b".repeat(lastString.length))
            lastString = "X: $mX Y: $mY Z: $mZ"
            print(lastString)
            y += accelerometer
        }
        if (Math.abs(z1) > threshold) {
            mZ++
            print("\b".repeat(lastString.length))
            lastString = "X: $mX Y: $mY Z: $mZ"
            print(lastString)
            z += accelerometer
        }
        while (System.nanoTime() - last < 5000000); //5 milliseconds
        last = System.nanoTime()
    }
    print("\b".repeat(lastString.length))
    
    return Vector3(Math.abs((x / mX.toDouble()).x), Math.abs((y / mY.toDouble()).y), Math.abs((z / mZ.toDouble()).z))
}

fun calibrate(mpu6050: MPU6050, pre: Int, measurements: Int): Pair<Vector3, Vector3> {
    var i = -pre
    val delayNanos = 2E6
    
    val aMean = Vector3()
    val gMean = Vector3()
    
    var last = System.nanoTime()
    while (i < measurements) {
        if (System.nanoTime() - last >= delayNanos) {
            last = System.nanoTime()
            val (a, g) = mpu6050.accelGyro
            a.reduce()
            g.reduce()

            a -= Z * GRAVITY

            if (i >= 0) {
                aMean += a
                gMean += g
            }
            i++
        }
    }
    
    aMean /= measurements.toDouble()
    gMean /= measurements.toDouble()
    
    return aMean.reduce() to gMean.reduce()
}
