package com.sergeysav.drone.menu

import com.sergeysav.drone.MPU6050
import com.sergeysav.drone.MPU6050Config
import com.sergeysav.drone.saveMPUConfig
import java.util.Scanner

/**
 * @author sergeys
 *
 * @constructor Creates a new MPUCalibrationMenu
 */
class MPUCalibrationMenu(val mpu6050: MPU6050) : Menu {
    override val title = "Calibrate MPU6050 Sensor"
    override val options: Array<Pair<String, (Scanner)->Menu?>> =
            arrayOf("Calibrate" to { _ ->
                println("Calibrating... Please wait...")
                mpu6050.calibrate(10000)
                
                saveMPUConfig(MPU6050Config(mpu6050))
                null
            })
}