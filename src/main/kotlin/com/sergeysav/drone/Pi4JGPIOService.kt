package com.sergeysav.drone

import com.pi4j.io.i2c.I2CFactory

/**
 * @author sergeys
 *
 * @constructor Creates a new Pi4JGPIOService
 */
class Pi4JGPIOService : GPIOService {
    
    override fun getI2CDevice(busNumber: Int, deviceNumber: Int): I2CDeviceService =
            Pi4JI2CDeviceService(I2CFactory.getInstance(busNumber).getDevice(deviceNumber))
}