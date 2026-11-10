package com.example.myapplication01

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationFlowManager {

    var currentLocation: Pair<Double, Double>? = null

    /**
     * 圖三發布頁面：若尚未定位，自動執行首次冷啟動讀取
     */
    fun autoFetchLocationOnFirstPublish(context: Context, onLocationRetrieved: (Pair<Double, Double>) -> Unit) {
        val lat = currentLocation?.first
        val lng = currentLocation?.second
        
        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
            // 讀取裝置路由經緯度 (GPS / Network)
            fetchDeviceRouteLocation(context) { location ->
                currentLocation = location
                onLocationRetrieved(location)
            }
        } else {
            onLocationRetrieved(currentLocation!!)
        }
    }

    /**
     * 刷新列表按鈕點擊事件：防呆檢查
     */
    fun onRefreshListClicked(context: Context, onRefreshSuccess: () -> Unit) {
        val lat = currentLocation?.first ?: GlobalState.globalLatitude
        val lng = currentLocation?.second ?: GlobalState.globalLongitude
        
        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
            // 無經緯度座標，精準提示使用者
            Toast.makeText(context, "請點擊上方『我的位置』刷新列表", Toast.LENGTH_SHORT).show()
        } else {
            if (currentLocation == null) {
                currentLocation = Pair(lat, lng)
            }
            // 已有座標，順利執行列表刷新
            onRefreshSuccess()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceRouteLocation(context: Context, callback: (Pair<Double, Double>) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val pos = Pair(location.latitude, location.longitude)
                    callback(pos)
                }
            }
    }

    /**
     * 取得有效經緯度比對座標：
     * 優先使用精確 GPS 座標，若無經緯度，就使用路由器坐標大概位置進行比對
     */
    fun getEffectiveLocation(lat: Double? = null, lng: Double? = null): Pair<Double, Double> {
        val validLat = lat?.takeIf { it != 0.0 }
            ?: GlobalState.globalLatitude?.takeIf { it != 0.0 }
            ?: currentLocation?.first?.takeIf { it != 0.0 }
            ?: HurlungchuangTestManager.TARGET_LATITUDE // 路由器坐標大概位置比對點

        val validLng = lng?.takeIf { it != 0.0 }
            ?: GlobalState.globalLongitude?.takeIf { it != 0.0 }
            ?: currentLocation?.second?.takeIf { it != 0.0 }
            ?: HurlungchuangTestManager.TARGET_LONGITUDE // 路由器坐標大概位置比對點

        return Pair(validLat, validLng)
    }
}
