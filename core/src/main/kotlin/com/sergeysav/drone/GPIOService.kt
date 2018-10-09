package com.sergeysav.drone

/**
 * @author sergeys
 */
interface GPIOService {
    fun getI2CDevice(busNumber: Int, deviceNumber: Int): I2CDeviceService
}