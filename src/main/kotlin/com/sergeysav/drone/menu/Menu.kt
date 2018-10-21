package com.sergeysav.drone.menu

import com.sergeysav.drone.MPU6050
import java.util.Scanner

/**
 * @author sergeys
 */
interface Menu {
    val title: String
    val options: Array<Pair<String, (Scanner)->Menu?>>
}

class MainMenu(val mpu6050: MPU6050) : Menu {
    override val title = "Drone Main Menu"
    override val options: Array<Pair<String, (Scanner)->Menu?>> =
            arrayOf("Configure" to { _ -> ConfigureMenu(mpu6050) },
                    "Fly" to { _ -> FlyMenu(mpu6050)},
                    "Exit" to { _ -> null })
}
