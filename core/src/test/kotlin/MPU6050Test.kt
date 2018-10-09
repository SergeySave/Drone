
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.sergeysav.drone.I2CDeviceService
import com.sergeysav.drone.MPU6050
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MPU6050Test {
    
    lateinit var mpu6050: MPU6050
    lateinit var bytes: ByteArray
    
    @Before
    fun init() {
        bytes = ByteArray(128)
        
        val i2cDevice = object : I2CDeviceService {
            override fun read(address: Int): Byte? = bytes[address]
        
            override fun read(address: Int, buffer: ByteArray): Int {
                System.arraycopy(bytes, address, buffer, 0, buffer.size)
                return buffer.size
            }
        
            override fun write(address: Int, value: Byte) {
                bytes[address] = value
            }
        
            override fun write(address: Int, buffer: ByteArray) {
                System.arraycopy(buffer, 0, bytes, address, buffer.size)
            }
        }
    
        mpu6050 = MPU6050(mock {
            on { getI2CDevice(anyInt(), anyInt()) } doReturn i2cDevice
        }, 0x68)
    }
    
    @Test
    fun testSetAccelRange() {
        mpu6050.accelRange = MPU6050.Companion.AccelRange.G2
        assertRegister(MPU6050.ACCEL_CONFIG, 0)
        mpu6050.accelRange = MPU6050.Companion.AccelRange.G4
        assertRegister(MPU6050.ACCEL_CONFIG, 8)
        mpu6050.accelRange = MPU6050.Companion.AccelRange.G8
        assertRegister(MPU6050.ACCEL_CONFIG, 16)
        mpu6050.accelRange = MPU6050.Companion.AccelRange.G16
        assertRegister(MPU6050.ACCEL_CONFIG, 24)
    }
    
    @Test
    fun testSetGyroRange() {
        mpu6050.gyroRange = MPU6050.Companion.GyroRange.DEG250
        assertRegister(MPU6050.GYRO_CONFIG, 0)
        mpu6050.gyroRange = MPU6050.Companion.GyroRange.DEG500
        assertRegister(MPU6050.GYRO_CONFIG, 8)
        mpu6050.gyroRange = MPU6050.Companion.GyroRange.DEG1000
        assertRegister(MPU6050.GYRO_CONFIG, 16)
        mpu6050.gyroRange = MPU6050.Companion.GyroRange.DEG2000
        assertRegister(MPU6050.GYRO_CONFIG, 24)
    }
    
    private fun assertRegister(register: Int, value: Int) {
        assertEquals(value.toByte(), bytes[register])
    }
}