package com.sergeysav.drone

/**
 * @author sergeys
 */
interface GPIO {
    fun getI2CDevice(busNumber: Int, deviceNumber: Int): I2CDevice
    
    interface I2CDevice {
        fun read(address: Byte): Byte?
        fun write(address: Byte, value: Byte)
    }
}