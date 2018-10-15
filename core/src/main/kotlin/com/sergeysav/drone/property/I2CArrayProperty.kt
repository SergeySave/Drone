package com.sergeysav.drone.property

import com.sergeysav.drone.io.I2CDeviceService
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author sergeys
 *
 * @constructor Creates a new I2CSingleProperty
 */
class I2CArrayProperty(private val i2CDeviceService: I2CDeviceService, private val min: Int, max: Int) : ReadWriteProperty<Any, ByteArray> {
    
    private val arr = ByteArray(max - min + 1)
    
    override fun getValue(thisRef: Any, property: KProperty<*>): ByteArray {
        i2CDeviceService.read(min, arr)
        return arr
    }
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: ByteArray) {
        return i2CDeviceService.write(min, value)
    }
}