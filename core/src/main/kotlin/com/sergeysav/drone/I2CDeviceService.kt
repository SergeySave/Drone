package com.sergeysav.drone

interface I2CDeviceService {
    fun read(address: Int): Byte?
    fun read(address: Int, buffer: ByteArray): Int
    fun write(address: Int, value: Byte)
    fun write(address: Int, buffer: ByteArray)
}