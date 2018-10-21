package com.sergeysav.drone.menu

import com.sergeysav.drone.GyroIntegrator
import com.sergeysav.drone.Integrator
import com.sergeysav.drone.MPU6050
import com.sergeysav.drone.MPU6050.Companion.GRAVITY
import com.sergeysav.drone.math.Z
import com.sergeysav.drone.math.timesAssign
import java.util.Scanner
import kotlin.math.abs

/**
 * @author sergeys
 *
 * @constructor Creates a new FlyMenu
 */
class FlyMenu(mpu6050: MPU6050) : Menu {
    override val title = "Fly"
    override val options: Array<Pair<String, (Scanner)->Menu?>> =
            arrayOf("Test Integration" to { scanner ->
                val intAcc = Integrator()
                val intVel = Integrator()

                val intGyr = GyroIntegrator()

                var lastString = ""

                val scaledAccelDeadzone = 8 * mpu6050.accelRange.scaleFactor
                val scaledGyroDeadzone = 2 * mpu6050.gyroRange.scaleFactor
                
                var running = true
                
                Thread {
                    while (!scanner.hasNextLine()) {
                        Thread.sleep(100)
                    }
                    scanner.nextLine()
                    running = false
                }.apply {
                    isDaemon = true
                    start()
                }

                var last = System.nanoTime()
                while (running) {
                    if (System.nanoTime() - last >= 5000000) { //5 milliseconds
                        val curr = System.nanoTime()
                        val delta = (curr - last)/1_000_000_000.0
                        last = curr

                        val (accelerometer, gyroscope) = mpu6050.accelGyro
                        gyroscope.reduceToFloat()
                        accelerometer.reduceToFloat()
                        
                        if (abs(gyroscope.x) < scaledGyroDeadzone) gyroscope.x = 0.0
                        if (abs(gyroscope.y) < scaledGyroDeadzone) gyroscope.y = 0.0
                        if (abs(gyroscope.z) < scaledGyroDeadzone) gyroscope.z = 0.0
                        val orientation = intGyr(gyroscope, delta)

                        accelerometer *= orientation.transpose()
                        accelerometer -= Z * GRAVITY
                        accelerometer.reduceToFloat()
                        
                        if (abs(accelerometer.x) < scaledAccelDeadzone) accelerometer.x = 0.0
                        if (abs(accelerometer.y) < scaledAccelDeadzone) accelerometer.y = 0.0
                        if (abs(accelerometer.z) < scaledAccelDeadzone) accelerometer.z = 0.0

                        val velocity = intAcc(accelerometer, delta)
                        velocity.reduceToFloat()
                        val position = intVel(velocity, delta)
                        position.reduceToFloat()

                        val a = "% .4f % .4f % .4f".format(accelerometer.x, accelerometer.y, accelerometer.z)
                        val v = "% .4f % .4f % .4f".format(velocity.x, velocity.y, velocity.z)
                        val p = "% .4f % .4f % .4f".format(position.x, position.y, position.z)
                        print("\b".repeat(lastString.length))
                        lastString = "$a${" ".repeat(80-a.length)}$v${" ".repeat(80-v.length)}$p${" ".repeat(80-p.length)}"
                        print(lastString)
                    }
                }
    
                null
            })
}