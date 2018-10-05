package com.sergeysav.drone

import com.pi4j.io.i2c.I2CDevice
import com.pi4j.io.i2c.I2CFactory

private typealias I2CInterface = GPIO.I2CDevice

/**
 * @author sergeys
 *
 * @constructor Creates a new Pi4jGPIO
 */
class Pi4jGPIO : GPIO {
    
    override fun getI2CDevice(busNumber: Int, deviceNumber: Int): I2CInterface =
            Pi4jI2CDevice(I2CFactory.getInstance(busNumber).getDevice(deviceNumber))
    
    class Pi4jI2CDevice(val i2CDevice: I2CDevice) : I2CInterface {
        override fun read(address: Byte): Byte? {
            val result = i2CDevice.read(address.toInt())
            return if (result >= 0) result.toByte() else null
        }
    
        override fun write(address: Byte, value: Byte) {
            i2CDevice.write(address.toInt(), value)
        }
    }
}