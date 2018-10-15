package com.sergeysav.drone.math

import kotlin.math.cos
import kotlin.math.sin

/**
 * @author sergeys
 *
 * @constructor Creates a new Matrix3
 */
data class Matrix3(var p00: Double = 1.0, var p01: Double = 0.0, var p02: Double = 0.0,
                   var p10: Double = 0.0, var p11: Double = 1.0, var p12: Double = 0.0,
                   var p20: Double = 0.0, var p21: Double = 0.0, var p22: Double = 1.0) {
    
    operator fun times(vec: Vector3) =
            Vector3(vec.x * p00 + vec.y * p01 + vec.z * p02,
                    vec.x * p10 + vec.y * p11 + vec.z * p12,
                    vec.x * p20 + vec.y * p21 + vec.z * p22)
    
    operator fun times(mat: Matrix3): Matrix3 =
            Matrix3(p00 * mat.p00 + p01 * mat.p10 + p02 * mat.p20,
                    p00 * mat.p01 + p01 * mat.p11 + p02 * mat.p21,
                    p00 * mat.p02 + p01 * mat.p12 + p02 * mat.p22,
                    p10 * mat.p00 + p11 * mat.p10 + p12 * mat.p20,
                    p10 * mat.p01 + p11 * mat.p11 + p12 * mat.p21,
                    p10 * mat.p02 + p11 * mat.p12 + p12 * mat.p22,
                    p20 * mat.p00 + p21 * mat.p10 + p22 * mat.p20,
                    p20 * mat.p01 + p21 * mat.p11 + p22 * mat.p21,
                    p20 * mat.p02 + p21 * mat.p12 + p22 * mat.p22)
    
    infix fun leftMultiply(mat: Matrix3) = mat * this
    
    operator fun timesAssign(mat: Matrix3) {
        val n00 = this.p00 * mat.p00 + this.p01 * mat.p10 + this.p02 * mat.p20
        val n01 = this.p00 * mat.p01 + this.p01 * mat.p11 + this.p02 * mat.p21
        val n02 = this.p00 * mat.p02 + this.p01 * mat.p12 + this.p02 * mat.p22
        val n10 = this.p10 * mat.p00 + this.p11 * mat.p10 + this.p12 * mat.p20
        val n11 = this.p10 * mat.p01 + this.p11 * mat.p11 + this.p12 * mat.p21
        val n12 = this.p10 * mat.p02 + this.p11 * mat.p12 + this.p12 * mat.p22
        val n20 = this.p20 * mat.p00 + this.p21 * mat.p10 + this.p22 * mat.p20
        val n21 = this.p20 * mat.p01 + this.p21 * mat.p11 + this.p22 * mat.p21
        val n22 = this.p20 * mat.p02 + this.p21 * mat.p12 + this.p22 * mat.p22
        this.p00 = n00
        this.p01 = n01
        this.p02 = n02
        this.p10 = n10
        this.p11 = n11
        this.p12 = n12
        this.p20 = n20
        this.p21 = n21
        this.p22 = n22
    }
    
    infix fun leftMultiplyAssign(mat: Matrix3) {
        val n00 = mat.p00 * this.p00 + mat.p01 * this.p10 + mat.p02 * this.p20
        val n01 = mat.p00 * this.p01 + mat.p01 * this.p11 + mat.p02 * this.p21
        val n02 = mat.p00 * this.p02 + mat.p01 * this.p12 + mat.p02 * this.p22
        val n10 = mat.p10 * this.p00 + mat.p11 * this.p10 + mat.p12 * this.p20
        val n11 = mat.p10 * this.p01 + mat.p11 * this.p11 + mat.p12 * this.p21
        val n12 = mat.p10 * this.p02 + mat.p11 * this.p12 + mat.p12 * this.p22
        val n20 = mat.p20 * this.p00 + mat.p21 * this.p10 + mat.p22 * this.p20
        val n21 = mat.p20 * this.p01 + mat.p21 * this.p11 + mat.p22 * this.p21
        val n22 = mat.p20 * this.p02 + mat.p21 * this.p12 + mat.p22 * this.p22
        this.p00 = n00
        this.p01 = n01
        this.p02 = n02
        this.p10 = n10
        this.p11 = n11
        this.p12 = n12
        this.p20 = n20
        this.p21 = n21
        this.p22 = n22
    }
    
    fun transpose() = Matrix3(p00, p10, p20, p01, p11, p21, p02, p12, p22)
    fun transposed() {
        var temp = p01
        p01 = p10
        p10 = temp
        temp = p02
        p02 = p20
        p20 = temp
        temp = p12
        p12 = p21
        p21 = temp
    }
    
    companion object {
        
        fun rotationX(radians: Double): Matrix3 {
            val sin = sin(radians)
            val cos = cos(radians)
            return Matrix3(p11 = cos, p22 = cos, p12 = -sin, p21 = sin)
        }
        
        fun rotationY(radians: Double): Matrix3 {
            val sin = sin(radians)
            val cos = cos(radians)
            return Matrix3(p00 = cos, p22 = cos, p20 = -sin, p02 = sin)
        }
        
        fun rotationZ(radians: Double): Matrix3 {
            val sin = sin(radians)
            val cos = cos(radians)
            return Matrix3(p00 = cos, p11 = cos, p01 = -sin, p10 = sin)
        }
        
        fun rotation(vec: Vector3, radians: Double): Matrix3 {
            val length = vec.length
            if (length < 0.01) {
                return Matrix3()
            }
            val u = vec / length
            val sin = sin(radians)
            val cos = cos(radians)
            return Matrix3(cos + u.x * u.x * (1 - cos),
                           u.x * u.y * (1 - cos) - u.z * sin,
                           u.x * u.z * (1 - cos) + u.y * sin,
                           u.x * u.y * (1 - cos) + u.z * sin,
                           cos + u.y * u.y * (1 - cos),
                           u.y * u.z * (1 - cos) - u.x * sin,
                           u.z * u.x * (1 - cos) - u.y * sin,
                           u.y * u.z * (1 - cos) + u.x * sin,
                           cos + u.z * u.z * (1 - cos))
        }
        
        fun rotation(vec: Vector3): Matrix3 {
            val rotation = rotationZ(vec.x)
            rotation *= rotationY(vec.y)
            rotation *= rotationX(vec.z)
            return rotation
        }
    }
}


operator fun Vector3.timesAssign(mat: Matrix3) {
    val x1 = x
    val y1 = y
    val z1 = z
    x = x1 * mat.p00 + y1 * mat.p01 + z1 * mat.p02
    y = x1 * mat.p10 + y1 * mat.p11 + z1 * mat.p12
    z = x1 * mat.p20 + y1 * mat.p21 + z1 * mat.p22
}
