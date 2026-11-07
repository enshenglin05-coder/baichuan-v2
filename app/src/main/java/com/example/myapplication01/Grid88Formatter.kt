package com.example.myapplication01

import com.example.myapplication01.repository.core.MatchingCore

/**
 * 百川 12大項 97格獨立網格編碼與格式化管理器 (12 Major Categories 97-Grid Formatter & Manager)
 * 嚴格對應 12 個大項目 × 8 個小項目 + 1 總覽主格 = 97 格的獨立網格編碼與兩排直向排列順序。
 * 支援經緯度數學函數精準對應、A/B 交叉邏輯驗證與授權標頭對齊。
 */
object Grid88Formatter {

    const val TOTAL_MAIN_CATEGORIES = 12
    const val SUB_CATEGORIES_PER_MAIN = 8
    const val TOTAL_GRID_CELLS = 97 // 12大項 97格基礎格子與格式框架 (96子格子 + 1總覽主格)

    /**
     * 生成所有網格編碼清單 (包含 0-0 總覽主格與 1-1 到 12-8 子格子，共 97 格)
     */
    fun getAllGridCodes(): List<String> {
        val list = mutableListOf<String>()
        list.add("0-0") // 總覽主格 (第 97 格)
        for (main in 1..TOTAL_MAIN_CATEGORIES) {
            for (sub in 1..SUB_CATEGORIES_PER_MAIN) {
                list.add("$main-$sub")
            }
        }
        return list
    }

    /**
     * 驗證網格編碼格式是否符合 12 大類別與 97 格規範 (0-0 及 1-1 到 12-8)
     */
    fun validateGridCode(code: String): Boolean {
        if (code == "0-0") return true
        val regex = Regex("^([1-9]|10|11|12)-[1-8]$")
        return regex.matches(code)
    }

    /**
     * 取得手繪草圖指定的「兩排直向排列」順序
     * 將 8 個子格子分為左右兩列直向排列：
     * 左列 (Column 1): 1, 2, 3, 4
     * 右列 (Column 2): 5, 6, 7, 8
     */
    fun getTwoColumnVerticalOrder(mainCategory: Int): List<String> {
        if (mainCategory !in 1..TOTAL_MAIN_CATEGORIES) return emptyList()
        return (1..SUB_CATEGORIES_PER_MAIN).map { sub -> "$mainCategory-$sub" }
    }

    /**
     * 經緯度數學函數精準對應至 97 格網格編碼
     * 根據給定的緯度 (lat) 與經度 (lng) 以及中心點範圍，計算出最接近的網格編碼
     */
    fun mapCoordinatesToGridCell(lat: Double, lng: Double, centerLat: Double = 25.0026, centerLng: Double = 121.4977): String {
        val latDiff = lat - centerLat
        val lngDiff = lng - centerLng

        val mainIndex = ((Math.abs(latDiff) * 1000).toInt() % TOTAL_MAIN_CATEGORIES) + 1
        val subIndex = ((Math.abs(lngDiff) * 1000).toInt() % SUB_CATEGORIES_PER_MAIN) + 1

        val code = "$mainIndex-$subIndex"
        return if (validateGridCode(code)) code else "0-0"
    }

    /**
     * 公需 A/B 交叉邏輯驗證 (A送B, B送A)
     * 確保當前身分 (Demand '1A' 或 Supply '1B') 與對手目標對齊
     */
    fun getOpponentDrawerType(currentType: String): String {
        return if (currentType == MatchingCore.TYPE_A_DEMAND) {
            MatchingCore.TYPE_B_SUPPLY
        } else {
            MatchingCore.TYPE_A_DEMAND
        }
    }
}
