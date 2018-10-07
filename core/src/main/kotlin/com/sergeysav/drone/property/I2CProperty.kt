package com.sergeysav.drone.property

import com.sergeysav.drone.GPIO
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author sergeys
 *
 * @constructor Creates a new I2CProperty
 */
class I2CProperty(private val i2CDevice: GPIO.I2CDevice, private val location: Byte) : ReadWriteProperty<Any, Byte> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Byte {
        return i2CDevice.read(location) ?: throw IllegalStateException("I2C value not found")
    }
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Byte) {
        return i2CDevice.write(location, value)
    }
}