package com.example.myapplication01

import kotlin.math.*

object DistanceUtils {
    const val EARTH_RADIUS_KM = 6371.0088
    const val FORMULA_ID = "HAVERSINE_SPHERICAL"
    const val FORMULA_VERSION = "1.0.0"

    /**
     * 使用 Haversine 公式計算兩點間的距離 (單位: 公里)
     * 地球半徑固定為 6371.0088 km
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = EARTH_RADIUS_KM // 固定地球半徑 6371.0088 km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
