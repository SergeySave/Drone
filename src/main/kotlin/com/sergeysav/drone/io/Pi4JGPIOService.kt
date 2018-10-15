package com.sergeysav.drone.io

import com.pi4j.io.i2c.I2CFactory

/**
 * @author sergeys
 *
 * @constructor Creates a new Pi4JGPIOService
 */
class Pi4JGPIOService : GPIOService {
    
    override fun getI2CDevice(busNumber: Int, deviceNumber: Int): Pi4JI2CDeviceService {
        val i2CBus = I2CFactory.getInstance(busNumber)
        return Pi4JI2CDeviceService(i2CBus, i2CBus.getDevice(deviceNumber))
    }
    
}