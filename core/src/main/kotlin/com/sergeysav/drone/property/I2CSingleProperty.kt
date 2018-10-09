package com.sergeysav.drone.property

import com.sergeysav.drone.I2CDeviceService
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author sergeys
 *
 * @constructor Creates a new I2CSingleProperty
 */
class I2CSingleProperty(private val i2CDeviceService: I2CDeviceService, private val location: Int) : ReadWriteProperty<Any, Byte> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Byte {
        return i2CDeviceService.read(location) ?: throw IllegalStateException("I2C value not found")
    }
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Byte) {
        return i2CDeviceService.write(location, value)
    }
}