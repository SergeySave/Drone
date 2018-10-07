package com.sergeysav.drone

import com.sergeysav.drone.math.Vector3
import com.sergeysav.drone.property.BooleanBitfieldProperty
import com.sergeysav.drone.property.EnumBitfieldProperty
import com.sergeysav.drone.property.I2CProperty
import kotlin.experimental.or

/**
 * Register values and formulas from
 * https://www.invensense.com/wp-content/uploads/2015/02/MPU-6000-Register-Map1.pdf
 *
 * @author sergeys
 */
class MPU6050(gpio: GPIO, address: Int) {
    
    private val i2CDevice = gpio.getI2CDevice(1, address)
    
    /**
     * Sample rate = output rate / (1 + sampleRateDivisor)
     */
    var sampleRateDivisor by I2CProperty(i2CDevice, SMPLRT_DIV)
    
    /**
     * Bits 7, 6 are unused
     * Bits 5-3 configure the external Frame Synchronization (FSYNC) pin sampling
     * Bits 2-0 configure the Digital Low Pass Filter (DLPF) setting
     */
    private var config by I2CProperty(i2CDevice, CONFIG)
    
    /**
     * Bits 7, 6, 5 are X, Y, Z gyroscope self test bits
     * Bits 4-3 configure the gyroscope range
     * Bits 2-0 are unused
     */
    private var gyroConfig by I2CProperty(i2CDevice, GYRO_CONFIG)
    
    /**
     * Bits 7, 6, 5 are X, Y, Z accelerometer self test bits
     * Bits 4-3 configure the accelerometer range
     * Bits 2-0 are unused
     */
    private var accelConfig by I2CProperty(i2CDevice, ACCEL_CONFIG)
    
    /**
     * Bits 4, 3, and 0 are used to set various interrupts
     */
    private var interruptEnable by I2CProperty(i2CDevice, INT_ENABLE)
    
    /**
     * Bit 2 - Gyroscope reset
     * Bit 1 - Accelerometer reset
     * Bit 0 - Thermometer reet
     */
    private var signalReset by I2CProperty(i2CDevice, SIG_RESET)
    
    /**
     * Bit 7 - Device reset
     * Bit 6 - Sleep
     * Bit 5 - Cycle
     * Bit 4 - Unused
     * Bit 3 - Thermometer disable
     * Bits 2-0 clock selection
     */
    private var powerManagement1 by I2CProperty(i2CDevice, PWR_MGMT_1)
    
    /**
     * Bits 7-6 - Wake control
     * Bit 5 - Accelerometer X axis standby
     * Bit 4 - Accelorometer Y axis standby
     * Bit 3 - Accelorometer Z axis standby
     * Bit 2 - Gyroscope X axis standby
     * Bit 1 - Gyroscope Y axis standby
     * Bit 0 - Gyroscope Z axis standby
     */
    private var powerManagement2 by I2CProperty(i2CDevice, PWR_MGMT_2)
    
    //Acceleration registers
    private var accelXH by I2CProperty(i2CDevice, ACCEL_XH)
    private var accelXL by I2CProperty(i2CDevice, ACCEL_XL)
    private var accelYH by I2CProperty(i2CDevice, ACCEL_YH)
    private var accelYL by I2CProperty(i2CDevice, ACCEL_YL)
    private var accelZH by I2CProperty(i2CDevice, ACCEL_ZH)
    private var accelZL by I2CProperty(i2CDevice, ACCEL_ZL)
    
    //Temperature registers
    private var tempH by I2CProperty(i2CDevice, TEMP_H)
    private var tempL by I2CProperty(i2CDevice, TEMP_L)
    
    //Gyroscope registers
    private var gyroXH by I2CProperty(i2CDevice, GYRO_XH)
    private var gyroXL by I2CProperty(i2CDevice, GYRO_XL)
    private var gyroYH by I2CProperty(i2CDevice, GYRO_YH)
    private var gyroYL by I2CProperty(i2CDevice, GYRO_YL)
    private var gyroZH by I2CProperty(i2CDevice, GYRO_ZH)
    private var gyroZL by I2CProperty(i2CDevice, GYRO_ZL)
    
    var dlpf by EnumBitfieldProperty(0, 2, ::config, DLPF.values())
    var extSync by EnumBitfieldProperty(3, 5, ::config, ExtSync.values())
    private var gyroRangeRaw by EnumBitfieldProperty(3, 4, ::gyroConfig,
                                                                                  GyroRange.values())
    private var accelRangeRaw by EnumBitfieldProperty(3, 4, ::accelConfig,
                                                                                   AccelRange.values())
    var fifoOverflowInterruptEnable by BooleanBitfieldProperty(4, ::interruptEnable)
    var i2cMasterInterruptEnable by BooleanBitfieldProperty(3, ::interruptEnable)
    var dataReadyInterruptEnable by BooleanBitfieldProperty(0, ::interruptEnable)
    private var gyroReset by BooleanBitfieldProperty(2, ::signalReset)
    private var accelReset by BooleanBitfieldProperty(1, ::signalReset)
    private var tempReset by BooleanBitfieldProperty(0, ::signalReset)
    private var reset by BooleanBitfieldProperty(7, ::powerManagement1)
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
    
    val accelX: Double
        get() = ((accelXH.toInt() shl 8).toShort() or (accelXL).toShort()) * accelRange.scaleFactor

    val accelY: Double
        get() = ((accelYH.toInt() shl 8).toShort() or (accelYL).toShort()) * accelRange.scaleFactor

    val accelZ: Double
        get() = ((accelZH.toInt() shl 8).toShort() or (accelZL).toShort()) * accelRange.scaleFactor
    
    val accelerometer: Vector3
        get() = Vector3(accelX, accelY, accelZ)
    
    val thermometer: Double = (((tempH.toInt() shl 8).toShort() or (tempL).toShort()) / 340.0) + 36.53
    
    val gyroX: Double
        get() = ((gyroXH.toInt() shl 8).toShort() or (gyroXL).toShort()) * gyroRange.scaleFactor

    val gyroY: Double
        get() = ((gyroYH.toInt() shl 8).toShort() or (gyroYL).toShort()) * gyroRange.scaleFactor

    val gyroZ: Double
        get() = ((gyroZH.toInt() shl 8).toShort() or (gyroZL).toShort()) * gyroRange.scaleFactor

    val gyroscope: Vector3
        get() = Vector3(gyroX, gyroY, gyroZ)

    companion object {
        const val GRAVITY: Double = 9.80665
        
        const val SMPLRT_DIV: Byte = 25
        const val CONFIG: Byte = 26
        const val GYRO_CONFIG: Byte = 27
        const val ACCEL_CONFIG: Byte = 28
        const val INT_ENABLE: Byte = 56
        const val SIG_RESET: Byte = 104
        const val PWR_MGMT_1: Byte = 107
        const val PWR_MGMT_2: Byte = 108
        
        const val ACCEL_XH: Byte = 59
        const val ACCEL_XL: Byte = 60
        const val ACCEL_YH: Byte = 61
        const val ACCEL_YL: Byte = 62
        const val ACCEL_ZH: Byte = 63
        const val ACCEL_ZL: Byte = 64
        
        const val TEMP_H: Byte = 65
        const val TEMP_L: Byte = 66
    
        const val GYRO_XH: Byte = 67
        const val GYRO_XL: Byte = 68
        const val GYRO_YH: Byte = 69
        const val GYRO_YL: Byte = 70
        const val GYRO_ZH: Byte = 71
        const val GYRO_ZL: Byte = 72
        
        enum class DLPF(override val value: Byte): BValued {
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
        enum class ExtSync(override val value: Byte): BValued {
            DISABLED(0),
            TEMP_OUT_L0(1),
            GYRO_XOUT_L0(2),
            GYRO_YOUT_L0(3),
            GYRO_ZOUT_L0(4),
            ACCEL_XOUT_L0(5),
            ACCEL_YOUT_L0(6),
            ACCEL_ZOUT_L0(7);
        }
        enum class GyroRange(override val value: Byte, val scaleFactor: Double): BValued {
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
        enum class AccelRange(override val value: Byte, val scaleFactor: Double): BValued {
            /**
             * ±2g
             */
            G2(0, 2 * GRAVITY / Short.MAX_VALUE),
            /**
             * ±4g
             */
            G4(1,  4 * GRAVITY / Short.MAX_VALUE),
            /**
             * ±8g
             */
            G8(2,  8 * GRAVITY / Short.MAX_VALUE),
            /**
             * ±16g
             */
            G16(3,  16 * GRAVITY / Short.MAX_VALUE);
        }
        enum class ClockMode(override val value: Byte): BValued {
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
        enum class WakeControl(override val value: Byte): BValued {
            HZ1_25(0),
            HZ5(1),
            HZ20(2),
            HZ40(3);
        }
    }
}