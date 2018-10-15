package com.sergeysav.drone.io

/**
 * @author sergeys
 */
interface GPIOService {
    fun getI2CDevice(busNumber: Int, deviceNumber: Int): I2CDeviceService
}