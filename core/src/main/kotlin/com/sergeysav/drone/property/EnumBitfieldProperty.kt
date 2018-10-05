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
 * @constructor Creates a new EnumBitfieldProperty<T>
 */
class EnumBitfieldProperty<T>(private val minBit: Int, maxBit: Int, private val parent: KMutableProperty0<Byte>, options: Array<T>) : ReadWriteProperty<Any, T> where T: Enum<T>, T : BValued {
    
    private val bits: Byte = ((-1 shl (maxBit - minBit + 1)).inv() shl minBit).toByte()
    private val negBits = bits.inv()
    private val map = options.groupBy { it.value }.mapValues { it.value[0] }
    
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return map[((parent.get() and bits).toInt() ushr minBit).toByte()]!!
    }
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val configVal = parent.get()
        parent.set((configVal and negBits) or (value.value.toInt() shl minBit).toByte())
    }
}
