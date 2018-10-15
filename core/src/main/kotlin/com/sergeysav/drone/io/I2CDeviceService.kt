package com.sergeysav.drone.io

interface I2CDeviceService : AutoCloseable {
    fun read(address: Int): Byte?
    fun read(address: Int, buffer: ByteArray): Int
    fun write(address: Int, value: Byte)
    fun write(address: Int, buffer: ByteArray)
}