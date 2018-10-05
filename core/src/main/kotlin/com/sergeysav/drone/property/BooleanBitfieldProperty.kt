package com.sergeysav.drone

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * @author sergeys
 *
 * @constructor Creates a new BooleanBitfieldProperty
 */
class BooleanBitfieldProperty(bit: Int, private val parent: KMutableProperty0<Byte>) : ReadWriteProperty<Any, Boolean> {
    
    private val bits: Byte = (1 shl bit).toByte()
    private val negBits = bits.inv()
    
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return (parent.get() and bits) != 0.toByte()
    }
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        val configVal = parent.get()
        parent.set((configVal and negBits) or (if (value) bits else 0).toByte())
    }
}
