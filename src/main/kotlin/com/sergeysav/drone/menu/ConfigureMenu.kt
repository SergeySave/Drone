package com.sergeysav.drone.menu

import com.sergeysav.drone.MPU6050
import java.util.Scanner

/**
 * @author sergeys
 *
 * @constructor Creates a new ConfigureMenu
 */
class ConfigureMenu(val mpu6050: MPU6050) : Menu {
    override val title = "Configure Drone"
    override val options: Array<Pair<String, (Scanner)->Menu?>> =
            arrayOf("Calibrate MPU Sensor" to { _ -> MPUCalibrationMenu(mpu6050) },
                    "Calibrate ESCs" to { _ -> ESCCalibrationMenu() },
                    "Exit" to { _ -> null })
}