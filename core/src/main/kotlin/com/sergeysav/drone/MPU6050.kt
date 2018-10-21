package com.sergeysav.drone

import com.sergeysav.drone.io.GPIOService
import com.sergeysav.drone.math.Vector3
import com.sergeysav.drone.math.Z
import com.sergeysav.drone.property.BooleanBitfieldProperty
import com.sergeysav.drone.property.ByteValued
import com.sergeysav.drone.property.EnumBitfieldProperty
import com.sergeysav.drone.property.I2CArrayProperty
import com.sergeysav.drone.property.I2CSingleProperty
import java.io.IOException
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Register values and formulas from
 * https://www.invensense.com/wp-content/uploads/2015/02/MPU-6000-Register-Map1.pdf
 *
 * @author sergeys
 */
class MPU6050(gpio: GPIOService, address: Int) {
    
    private val i2CDevice = gpio.getI2CDevice(1, address)
    
    /**
     * Sample rate = output rate / (1 + sampleRateDivisor)
     */
    var sampleRateDivisor by I2CSingleProperty(i2CDevice, SMPLRT_DIV)
    
    /**
     * Bits 7, 6 are unused
     * Bits 5-3 configure the external Frame Synchronization (FSYNC) pin sampling
     * Bits 2-0 configure the Digital Low Pass Filter (DLPF) setting
     */
    private var config by I2CSingleProperty(i2CDevice, CONFIG)
    
    /**
     * Bits 7, 6, 5 are X, Y, Z gyroscope self test bits
     * Bits 4-3 configure the gyroscope range
     * Bits 2-0 are unused
     */
    private var gyroConfig by I2CSingleProperty(i2CDevice, GYRO_CONFIG)
    
    /**
     * Bits 7, 6, 5 are X, Y, Z accelerometer self test bits
     * Bits 4-3 configure the accelerometer range
     * Bits 2-0 are unused
     */
    var accelConfig by I2CSingleProperty(i2CDevice, ACCEL_CONFIG)
    
    /**
     * Bits 4, 3, and 0 are used to set various interrupts
     */
    private var interruptEnable by I2CSingleProperty(i2CDevice, INT_ENABLE)
    
    /**
     * Bit 2 - Gyroscope reset
     * Bit 1 - Accelerometer reset
     * Bit 0 - Thermometer reet
     */
    private var signalReset by I2CSingleProperty(i2CDevice, SIG_RESET)
    
    /**
     * Bit 7 - Device reset
     * Bit 6 - Sleep
     * Bit 5 - Cycle
     * Bit 4 - Unused
     * Bit 3 - Thermometer disable
     * Bits 2-0 clock selection
     */
    private var powerManagement1 by I2CSingleProperty(i2CDevice, PWR_MGMT_1)
    
    /**
     * Bits 7-6 - Wake control
     * Bit 5 - Accelerometer X axis standby
     * Bit 4 - Accelorometer Y axis standby
     * Bit 3 - Accelorometer Z axis standby
     * Bit 2 - Gyroscope X axis standby
     * Bit 1 - Gyroscope Y axis standby
     * Bit 0 - Gyroscope Z axis standby
     */
    private var powerManagement2 by I2CSingleProperty(i2CDevice, PWR_MGMT_2)
    
    //Acceleration registers
    private var accel by I2CArrayProperty(i2CDevice, ACCEL_LOW, ACCEL_HIGH)
    
    //Temperature registers
    private var temp by I2CArrayProperty(i2CDevice, TEMP_LOW, TEMP_HIGH)
    
    //Gyroscope registers
    private var gyro by I2CArrayProperty(i2CDevice, GYRO_LOW, GYRO_HIGH)
    
    //Data registers
    private var fullOutputData by I2CArrayProperty(i2CDevice, ACCEL_LOW, GYRO_HIGH)
    
    var dlpf by EnumBitfieldProperty(0, 2, ::config, DLPF.values())
    var extSync by EnumBitfieldProperty(3, 5, ::config, ExtSync.values())
    private var gyroRangeRaw by EnumBitfieldProperty(3, 4, ::gyroConfig, GyroRange.values())
    var accelRangeRaw by EnumBitfieldProperty(3, 4, ::accelConfig, AccelRange.values())
    var fifoOverflowInterruptEnable by BooleanBitfieldProperty(4, ::interruptEnable)
    var i2cMasterInterruptEnable by BooleanBitfieldProperty(3, ::interruptEnable)
    var dataReadyInterruptEnable by BooleanBitfieldProperty(0, ::interruptEnable)
    private var gyroReset by BooleanBitfieldProperty(2, ::signalReset)
    private var accelReset by BooleanBitfieldProperty(1, ::signalReset)
    private var tempReset by BooleanBitfieldProperty(0, ::signalReset)
    var reset by BooleanBitfieldProperty(7, ::powerManagement1)
    var sleep by BooleanBitfieldProperty(6, ::powerManagement1)
    var cycle by BooleanBitfieldProperty(5, ::powerManagement1)
    var thermometerDisable by BooleanBitfieldProperty(3, ::powerManagement1)
    var clockMode by EnumBitfieldProperty(0, 2, ::powerManagement1, ClockMode.values())
    var wakeControl by EnumBitfieldProperty(6, 7, ::powerManagement2, WakeControl.values())
    var accelXStandby by BooleanBitfieldProperty(5, ::powerManagement2)
    var accelYStandby by BooleanBitfieldProperty(4, ::powerManagement2)
    var accelZStandby by BooleanBitfieldProperty(3, ::powerManagement2)
    var gyroXStandby by BooleanBitfieldProperty(2, ::powerManagement2)
    var gyroYStandby by BooleanBitfieldProperty(1, ::powerManagement2)
    var gyroZStandby by BooleanBitfieldProperty(0, ::powerManagement2)
    
    var accelOffset = Vector3()
    var gyroOffset = Vector3()
    
    var gyroRange: GyroRange = GyroRange.DEG250
        set(value) {
            gyroRangeRaw = value
            field = value
        }
    var accelRange: AccelRange = AccelRange.G2
        set(value) {
            accelRangeRaw = value
            field = value
        }
    
    init { //Set everything to defaults
        reset = true
        gyroRange = gyroRangeRaw
        accelRange = accelRangeRaw
    }
    
    fun reset() {
        reset = true
        Thread.sleep(100)
        gyroReset = true
        accelReset = true
        tempReset = true
        Thread.sleep(100)
    }
    
    fun enable() {
        sleep = false
    }
    
    val accelerometer: Vector3
        get() {
            var raw: ByteArray
            
            while (true) {
                try {
                    raw = accel
                    break
                } catch (e: IOException) {
                }
            }
            
            return (Vector3(
                    ((raw[0].toInt() shl 8).toShort() or (raw[1].toShort() and 0xFF)).toDouble(),
                    ((raw[2].toInt() shl 8).toShort() or (raw[3].toShort() and 0xFF)).toDouble(),
                    ((raw[4].toInt() shl 8).toShort() or (raw[5].toShort() and 0xFF)).toDouble()) * accelRange.scaleFactor) - accelOffset
        }
    
    val thermometer: Double
        get() {
            var raw: ByteArray
            
            while (true) {
                try {
                    raw = temp
                    break
                } catch (e: IOException) {
                }
            }
            
            return (((raw[0].toInt() shl 8).toShort() or (raw[1].toShort() and 0xFF)) / 340.0) + 36.53
        }
    
    val gyroscope: Vector3
        get() {
            var raw: ByteArray
            
            while (true) {
                try {
                    raw = gyro
                    break
                } catch (e: IOException) {
                }
            }
            
            return (Vector3(
                    ((raw[0].toInt() shl 8).toShort() or (raw[1].toShort() and 0xFF)).toDouble(),
                    ((raw[2].toInt() shl 8).toShort() or (raw[3].toShort() and 0xFF)).toDouble(),
                    ((raw[4].toInt() shl 8).toShort() or (raw[5].toShort() and 0xFF)).toDouble()) * gyroRange.scaleFactor) - gyroOffset
        }
    
    val accelGyro: Pair<Vector3, Vector3>
        get() {
            var raw: ByteArray
            
            while (true) {
                try {
                    raw = fullOutputData
                    break
                } catch (e: IOException) {
                }
            }
            
            return ((Vector3(
                    ((raw[0].toInt() shl 8).toShort() or (raw[1].toShort() and 0xFF)).toDouble(),
                    ((raw[2].toInt() shl 8).toShort() or (raw[3].toShort() and 0xFF)).toDouble(),
                    ((raw[4].toInt() shl 8).toShort() or (raw[5].toShort() and 0xFF)).toDouble()) * accelRange.scaleFactor) - accelOffset) to
                    (Vector3(
                            ((raw[8].toInt() shl 8).toShort() or (raw[9].toShort() and 0xFF)).toDouble(),
                            ((raw[10].toInt() shl 8).toShort() or (raw[11].toShort() and 0xFF)).toDouble(),
                            ((raw[12].toInt() shl 8).toShort() or (raw[13].toShort() and 0xFF)).toDouble()) * gyroRange.scaleFactor) - gyroOffset
        }
    
    val accelThermGyro: Triple<Vector3, Double, Vector3>
        get() {
            var raw: ByteArray
            
            while (true) {
                try {
                    raw = fullOutputData
                    break
                } catch (e: IOException) {
                }
            }
            
            return Triple((Vector3(
                    ((raw[0].toInt() shl 8).toShort() or (raw[1].toShort() and 0xFF)).toDouble(),
                    ((raw[2].toInt() shl 8).toShort() or (raw[3].toShort() and 0xFF)).toDouble(),
                    ((raw[4].toInt() shl 8).toShort() or (raw[5].toShort() and 0xFF)).toDouble()) * accelRange.scaleFactor) - accelOffset,
                          (((raw[6].toInt() shl 8).toShort() or (raw[7].toShort() and 0xFF)) / 340.0) + 36.53,
                          (Vector3(
                                  ((raw[8].toInt() shl 8).toShort() or (raw[9].toShort() and 0xFF)).toDouble(),
                                  ((raw[10].toInt() shl 8).toShort() or (raw[11].toShort() and 0xFF)).toDouble(),
                                  ((raw[12].toInt() shl 8).toShort() or (raw[13].toShort() and 0xFF)).toDouble()) * gyroRange.scaleFactor) - gyroOffset)
        }
    
    fun close() {
        i2CDevice.close()
    }
    
    fun calibrate(measurements: Int, delay: Double = 0.002, gravity: Vector3 = Z * GRAVITY) {
        accelOffset = Vector3()
        gyroOffset = Vector3()
        
        var i = -100
        val delayNanos = delay * 1E6
    
        val aMean = Vector3()
        val gMean = Vector3()
    
        var last = System.nanoTime()
        while (i < measurements) {
            if (System.nanoTime() - last >= delayNanos) {
                last = System.nanoTime()
                val (a, g) = accelGyro
            
                a -= gravity
            
                if (i >= 0) {
                    aMean += a
                    gMean += g
                }
                i++
            }
        }
    
        aMean /= measurements.toDouble()
        gMean /= measurements.toDouble()
    
        accelOffset = aMean
        accelOffset.reduceToFloat()
        gyroOffset = gMean
        gyroOffset.reduceToFloat()
    }
    
    companion object {
        const val GRAVITY: Double = 9.80665
        
        const val SMPLRT_DIV = 25
        const val CONFIG = 26
        const val GYRO_CONFIG = 27
        const val ACCEL_CONFIG = 28
        const val INT_ENABLE = 56
        const val SIG_RESET = 104
        const val PWR_MGMT_1 = 107
        const val PWR_MGMT_2 = 108
        
        const val ACCEL_LOW = 59
        const val ACCEL_HIGH = 64
        
        const val TEMP_LOW = 65
        const val TEMP_HIGH = 66
        
        const val GYRO_LOW = 67
        const val GYRO_HIGH = 72
        
        enum class DLPF(override val value: Byte): ByteValued {
            /**
             * Accel 260Hz bandwidth, 0.00ms delay, 1kHz freq
             * Gyro  256Hz bandwidth, 0.98ms delay, 8kHz freq
             */
            ZERO(0),
            /**
             * Accel 184Hz bandwidth, 2.0ms delay, 1kHz freq
             * Gyro  188Hz bandwidth, 1.9ms delay, 1kHz freq
             */
            ONE(1),
            /**
             * Accel 94Hz bandwidth, 3.0ms delay, 1kHz freq
             * Gyro  98Hz bandwidth, 2.8ms delay, 1kHz freq
             */
            TWO(2),
            /**
             * Accel 44Hz bandwidth, 4.9ms delay, 1kHz freq
             * Gyro  42Hz bandwidth, 4.8ms delay, 1kHz freq
             */
            THREE(3),
            /**
             * Accel 21Hz bandwidth, 8.5ms delay, 1kHz freq
             * Gyro  20Hz bandwidth, 8.3ms delay, 1kHz freq
             */
            FOUR(4),
            /**
             * Accel 10Hz bandwidth, 13.8ms delay, 1kHz freq
             * Gyro  10Hz bandwidth, 13.4ms delay, 1kHz freq
             */
            FIVE(5),
            /**
             * Accel 5Hz bandwidth, 19.0ms delay, 1kHz freq
             * Gyro  5Hz bandwidth, 18.6ms delay, 1kHz freq
             */
            SIX(6),
            /**
             * Reserved value (DO NOT USE)
             */
            SEVEN(7);
        }
        
        enum class ExtSync(override val value: Byte): ByteValued {
            DISABLED(0),
            TEMP_OUT_L0(1),
            GYRO_XOUT_L0(2),
            GYRO_YOUT_L0(3),
            GYRO_ZOUT_L0(4),
            ACCEL_XOUT_L0(5),
            ACCEL_YOUT_L0(6),
            ACCEL_ZOUT_L0(7);
        }
        
        enum class GyroRange(override val value: Byte, val scaleFactor: Double):
                ByteValued {
            /**
             * 250 degrees / second
             */
            DEG250(0, 250.0 / Short.MAX_VALUE),
            /**
             * 500 degrees / second
             */
            DEG500(1, 500.0 / Short.MAX_VALUE),
            /**
             * 1000 degrees / second
             */
            DEG1000(2, 1000.0 / Short.MAX_VALUE),
            /**
             * 2000 degrees / second
             */
            DEG2000(3, 2000.0 / Short.MAX_VALUE);
        }
        
        enum class AccelRange(override val value: Byte, val scaleFactor: Double):
                ByteValued {
            /**
             * ±2g
             */
            G2(0, 2 * GRAVITY / Short.MAX_VALUE),
            /**
             * ±4g
             */
            G4(1, 4 * GRAVITY / Short.MAX_VALUE),
            /**
             * ±8g
             */
            G8(2, 8 * GRAVITY / Short.MAX_VALUE),
            /**
             * ±16g
             */
            G16(3, 16 * GRAVITY / Short.MAX_VALUE);
        }
        
        enum class ClockMode(override val value: Byte): ByteValued {
            INTERNAL_8MHZ(0),
            PLL_X_GYRO(1),
            PLL_Y_GYRO(2),
            PLL_Z_GYRO(3),
            PLL_EXTERNAL_32KHZ(4),
            PLL_EXTERNAL_19MHZ(5),
            /**
             * Unused (DO NOT USE)
             */
            RESERVED(6),
            /**
             * Not sure what this does (DO NOT USE)
             */
            STOP_CLOCK_RESET_TIMING(7);
        }
        
        enum class WakeControl(override val value: Byte): ByteValued {
            HZ1_25(0),
            HZ5(1),
            HZ20(2),
            HZ40(3);
        }
    }
}