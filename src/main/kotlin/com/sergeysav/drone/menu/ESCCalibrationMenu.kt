package com.sergeysav.drone.menu

import java.util.Scanner

/**
 * @author sergeys
 *
 * @constructor Creates a new MPUCalibrationMenu
 */
class ESCCalibrationMenu : Menu {
    override val title = "Calibrate ESCs"
    override val options: Array<Pair<String, (Scanner)->Menu?>> =
            arrayOf()
}