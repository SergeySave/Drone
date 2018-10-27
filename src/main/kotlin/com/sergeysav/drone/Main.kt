package com.sergeysav.drone

import com.sergeysav.drone.io.Pi4JGPIOService
import com.sergeysav.drone.menu.MainMenu
import com.sergeysav.drone.menu.Menu
import java.util.Deque
import java.util.LinkedList
import java.util.Scanner

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
//    mpu6050.clockMode = MPU6050.Companion.ClockMode.PLL_Z_GYRO
    
    
//    val writer = Files.newBufferedWriter(Paths.get("datadump.csv"), utf8Charset)
//
//    writer.append("num,ax,ay,az,t,gx,gy,gz")
//    writer.newLine()
//    var i = 0
//    var last = System.nanoTime()
//    while (i < 100000) {
//        if (System.nanoTime() - last >= 5000000) { //5 milliseconds
//            val curr = System.nanoTime()
//            val delta = (curr - last)/1_000_000_000.0
//            last = curr
//
//            val (accelerometer, temp, gyroscope) = mpu6050.accelThermGyro
//
//            writer.append(i++.toString())
//            writer.append(',')
//            writer.append(accelerometer.x.toString())
//            writer.append(',')
//            writer.append(accelerometer.y.toString())
//            writer.append(',')
//            writer.append(accelerometer.z.toString())
//            writer.append(',')
//            writer.append(temp.toString())
//            writer.append(',')
//            writer.append(gyroscope.x.toString())
//            writer.append(',')
//            writer.append(gyroscope.y.toString())
//            writer.append(',')
//            writer.append(gyroscope.z.toString())
//            writer.newLine()
//        }
//    }
//
//    writer.close()
    
    val mpuConfig = loadMPUConfig()
    if (mpuConfig != null) {
        mpuConfig.setMPU(mpu6050)
    }

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

    //Run the menu
    Scanner(System.`in`).use { scan ->
        val menus: Deque<Menu> = LinkedList()
        menus.push(MainMenu(mpu6050))

        while (menus.isNotEmpty()) {
            val menu = menus.peek()
            val options = menu.options

            val next: Menu? = when {
                options.isEmpty() -> null
                options.size == 1 -> menu.options[0].second(scan)
                else              -> {
                    println(menu.title)
                    println("---")
                    var result: String
                    do {
                        val last = options.indices.last
                        val length = last.toString().length
                        for (i in options.indices) {
                            val len = i.toString().length

                            println("${" ".repeat(length - len)}$i : ${options[i].first}")
                        }
                        println()

                        result = scan.nextLine()
                    } while (result.toIntOrNull() == null || result.toInt() !in options.indices)

                    menu.options[result.toInt()].second(scan)
                }
            }

            if (next == null) {
                menus.pop()
            } else {
                menus.push(next)
            }
        }
    }

    mpu6050.close()
}
