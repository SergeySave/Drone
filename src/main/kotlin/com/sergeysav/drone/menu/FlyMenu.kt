package com.sergeysav.drone.menu

import com.sergeysav.drone.GyroIntegrator
import com.sergeysav.drone.Integrator
import com.sergeysav.drone.MPU6050
import com.sergeysav.drone.MPU6050.Companion.GRAVITY
import com.sergeysav.drone.math.Z
import com.sergeysav.drone.utf8Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Scanner
import kotlin.math.PI

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

                val intGyr = GyroIntegrator(0.994)
                
                var lastString = ""
                
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
    
                val writer = Files.newBufferedWriter(Paths.get("rotation_data.csv"), utf8Charset)
                writer.append("deltaNano,w,x,y,z")
                writer.newLine()
                val start = System.nanoTime()

                var last = System.nanoTime()
                while (running) {
                    if (System.nanoTime() - last >= 5000000) { //5 milliseconds
                        val curr = System.nanoTime()
                        val delta = (curr - last)/1_000_000_000.0
                        last = curr

                        val (accelerometer, gyroscope) = mpu6050.accelGyro
                        gyroscope *= (PI / 180)
                        gyroscope.reduceToFloat()
                        accelerometer.reduceToFloat()
                        
                        val orientation = intGyr(gyroscope, accelerometer, delta)
    
                        accelerometer.rotated(orientation)
                        accelerometer -= Z * GRAVITY
                        accelerometer.reduceToFloat()
                        
                        val velocity = intAcc(accelerometer, delta)
                        velocity.reduceToFloat()
                        val position = intVel(velocity, delta)
                        position.reduceToFloat()
    
                        writer.append((System.nanoTime() - start).toString())
                        writer.append(',')
                        writer.append(orientation.w.toString())
                        writer.append(',')
                        writer.append(orientation.x.toString())
                        writer.append(',')
                        writer.append(orientation.y.toString())
                        writer.append(',')
                        writer.append(orientation.z.toString())
                        writer.newLine()
                        val a = "% .4f % .4f % .4f".format(accelerometer.x, accelerometer.y, accelerometer.z)
                        val v = "% .4f % .4f % .4f".format(velocity.x, velocity.y, velocity.z)
                        val p = "% .4f % .4f % .4f".format(position.x, position.y, position.z)
                        print("\b".repeat(lastString.length))
                        lastString = "$a${" ".repeat(80-a.length)}$v${" ".repeat(80-v.length)}$p${" ".repeat(80-p.length)}"
                        print(lastString)
                    }
                }
                
                writer.close()
    
                null
            })
}