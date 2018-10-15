package com.sergeysav.drone.io

import com.pi4j.io.i2c.I2CBus
import com.pi4j.io.i2c.I2CDevice

class Pi4JI2CDeviceService(val i2cBus: I2CBus, val i2CDevice: I2CDevice) : I2CDeviceService {
    override fun read(address: Int): Byte? {
        val result = i2CDevice.read(address)
        return if (result >= 0) result.toByte() else null
    }

    override fun read(address: Int, buffer: ByteArray): Int {
        return i2CDevice.read(address, buffer, 0, buffer.size)
    }

    override fun write(address: Int, value: Byte) {
        i2CDevice.write(address, value)
    }

    override fun write(address: Int, buffer: ByteArray) {
        i2CDevice.write(address, buffer)
    }
    
    override fun close() {
        i2cBus.close()
    }
}