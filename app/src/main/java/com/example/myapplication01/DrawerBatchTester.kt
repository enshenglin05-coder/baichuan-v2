package com.example.myapplication01

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * 百川 1.0 抽屜測試資料集與批次載入器 (Drawer Batch Tester)
 * 負責 Drawer 1-1 ("在地攤商 / 個人私廚") 測試卡片資料之解析、轉換與倉庫初始化
 */
object DrawerBatchTester {

    val TEST_DRAWER_1_1_JSON = """
    [
      {
        "card_id": "TEST_DRAWER_1_1_001",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】附近 5KM 想吃現炒炒飯",
        "description": "預算 120 元內，希望 15 分鐘內能取餐或送到，不要加洋蔥。",
        "contact_type": "Phone",
        "contact_id": "0912345678",
        "lat": 25.002653,
        "lng": 121.497685,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_002",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】阿婆古早味蔥油餅 現點現炸",
        "description": "加蛋 45 元，加辣更讚！位在路口紅綠燈旁，歡迎直接電話預訂不用等。",
        "contact_type": "Phone",
        "contact_id": "0987654321",
        "lat": 25.003120,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_003",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】深夜宵夜 求推薦鹹酥雞",
        "description": "預算 200 元，希望有九層塔與蒜碎，距離 3 公里內。",
        "contact_type": "LINE",
        "contact_id": "night_food_seeker",
        "lat": 25.001890,
        "lng": 121.496540,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_004",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】媽媽私廚 無菜單家常便飯",
        "description": "今日主菜：紅燒排骨便當 110 元，限量 15 份，可自取或外送。",
        "contact_type": "LINE",
        "contact_id": "mom_kitchen_88",
        "lat": 25.004100,
        "lng": 121.499200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_005",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】尋找手沖冰咖啡 / 隱藏版甜點",
        "description": "預算 150 元，下午茶時間求外送或外帶私廚甜點。",
        "contact_type": "Telegram",
        "contact_id": "@coffee_lover_tw",
        "lat": 25.002100,
        "lng": 121.497100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_006",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】巷弄手工地瓜球 大包 50 元",
        "description": "酥脆不油膩，出爐時間至晚上 9:00，歡迎附近鄰居過來買。",
        "contact_type": "Phone",
        "contact_id": "0911223344",
        "lat": 25.001500,
        "lng": 121.496000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_007",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】徵求 5 份素食粥品或炒麵",
        "description": "預算每份 80 元，希望 1 小時內送達指定地點。",
        "contact_type": "Phone",
        "contact_id": "0922334455",
        "lat": 25.003500,
        "lng": 121.498800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_008",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】深夜港式點心攤 燒賣/蝦餃",
        "description": "每籠 70 元起，熱騰騰現蒸，提供外帶與 2 公里內滿 300 外送。",
        "contact_type": "WeChat",
        "contact_id": "dimsum_night_tw",
        "lat": 25.000900,
        "lng": 121.495200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_009",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】急求低碳水健身餐盒",
        "description": "預算 180 元，雞胸肉或牛肉皆可，不要白飯。",
        "contact_type": "LINE",
        "contact_id": "fit_meal_need",
        "lat": 25.002900,
        "lng": 121.497900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_010",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】阿公麻糬 現點現做花生/芝麻",
        "description": "一盒 60 元（6 顆），傳統手工純糯米製作，售完為止。",
        "contact_type": "Phone",
        "contact_id": "0933445566",
        "lat": 25.003800,
        "lng": 121.496900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_011",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】想喝熱紅豆湯加小湯圓",
        "description": "預算 60 元，天冷想吃甜湯，附近 3 公里內佳。",
        "contact_type": "Phone",
        "contact_id": "0944556677",
        "lat": 25.001200,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_012",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】老牌碳烤香腸/米腸組",
        "description": "一組 70 元，附大蒜瓣，現場排隊約 5 分鐘，可提前電話預訂。",
        "contact_type": "Phone",
        "contact_id": "0955667788",
        "lat": 25.004500,
        "lng": 121.497300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_013",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】求推薦在地酸辣粉或螺螄粉",
        "description": "預算 140 元，要夠辣夠酸，外帶自取可。",
        "contact_type": "LINE",
        "contact_id": "spicy_food_fan",
        "lat": 25.002200,
        "lng": 121.499000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_014",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】手作巴斯克乳酪蛋糕 切片",
        "description": "原味/抹茶切片 90 元，個人私廚限量手作，濃郁順口。",
        "contact_type": "Telegram",
        "contact_id": "@cheesecake_studio",
        "lat": 25.001600,
        "lng": 121.496200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_015",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】團購午餐 訂 10 份牛肉麵",
        "description": "預算每份 160 元，需附發票或收據，中午 12:00 前送達。",
        "contact_type": "Phone",
        "contact_id": "0966778899",
        "lat": 25.003100,
        "lng": 121.495500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_016",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】現煎章魚燒 6 入 50 元",
        "description": "醬燒/海苔/芥末三種口味可選，外皮酥脆內餡飽滿。",
        "contact_type": "Phone",
        "contact_id": "0977889900",
        "lat": 25.002800,
        "lng": 121.498400,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_017",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】想吃古早味刨冰 4 種料",
        "description": "預算 70 元，要加芋圓跟黑糖，距離 2 公里內。",
        "contact_type": "LINE",
        "contact_id": "ice_cream_lover",
        "lat": 25.000500,
        "lng": 121.497500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_018",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】夜市台式大腸包小腸",
        "description": "一份 65 元，酸菜蒜頭給大方！歡迎預訂外帶。",
        "contact_type": "Phone",
        "contact_id": "0988990011",
        "lat": 25.004000,
        "lng": 121.496800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_019",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】急尋手工水餃 20 顆（生餃）",
        "description": "預算 120 元，高麗菜豬肉餡，可自取。",
        "contact_type": "Phone",
        "contact_id": "0999001122",
        "lat": 25.002400,
        "lng": 121.496700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_020",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】現榨新鮮柳橙汁 700cc",
        "description": "每杯 60 元，純天然無加糖加水，冷藏即飲超健康。",
        "contact_type": "LINE",
        "contact_id": "juice_bar_101",
        "lat": 25.001900,
        "lng": 121.498600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_1_1_021",
        "drawer_code": "1-1",
        "category_name": "在地攤商 / 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】手作鮮奶布丁 焦糖香濃",
        "description": "嚴選小農鮮乳與天然雞蛋製作，綿密滑順，每顆 45 元。",
        "contact_type": "Phone",
        "contact_id": "0912223344",
        "lat": 25.002500,
        "lng": 121.497200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      }
    ]
    """.trimIndent()

    private fun safeLogI(tag: String, msg: String) {
        try { android.util.Log.i(tag, msg) } catch (_: Throwable) {}
    }

    private fun safeLogE(tag: String, msg: String) {
        try { android.util.Log.e(tag, msg) } catch (_: Throwable) {}
    }

    /**
     * 解析任意測試抽屜 JSON 字串為 RequirementCard 列表
     */
    fun parseDrawerJson(jsonString: String): List<RequirementCard> {
        val list = mutableListOf<RequirementCard>()
        try {
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            for (element in jsonArray) {
                val obj = element.asJsonObject
                list.add(parseCardObject(obj))
            }
        } catch (e: Exception) {
            safeLogE("DrawerBatchTester", "解析測試卡片失敗: ${e.message}")
        }
        return list
    }

    private fun parseCardObject(obj: JsonObject): RequirementCard {
        val cardId = obj.get("card_id")?.asString ?: obj.get("cardId")?.asString ?: ""
        val drawerCode = obj.get("drawer_code")?.asString ?: obj.get("drawerCode")?.asString ?: obj.get("category_code")?.asString ?: "1-1"
        val categoryName = obj.get("category_name")?.asString ?: obj.get("categoryName")?.asString ?: obj.get("category_tag")?.asString ?: ""
        
        val rawType = obj.get("type")?.asString ?: obj.get("drawer_type")?.asString ?: "1A"
        val drawerType = when (rawType.uppercase()) {
            "REQUIREMENT", "1A" -> "1A"
            "SUPPLY", "1B" -> "1B"
            else -> rawType
        }

        val title = obj.get("title")?.asString ?: ""
        val detailDesc = obj.get("description")?.asString ?: ""
        val fullDescription = if (title.isNotEmpty() && !detailDesc.startsWith(title)) {
            "$title\n$detailDesc"
        } else {
            detailDesc
        }

        val contactObj = if (obj.has("contact") && obj.get("contact").isJsonObject) obj.getAsJsonObject("contact") else null

        val rawContactType = contactObj?.get("method")?.asString
            ?: contactObj?.get("contact_type")?.asString
            ?: obj.get("contact_type")?.asString
            ?: obj.get("contactType")?.asString
            ?: obj.get("contact_method")?.asString
            ?: "Phone"

        val contactMethod = when (rawContactType.uppercase()) {
            "PHONE" -> RequirementCard.ContactMethod.PHONE
            "LINE" -> RequirementCard.ContactMethod.LINE
            "TELEGRAM" -> RequirementCard.ContactMethod.TELEGRAM
            "WECHAT" -> RequirementCard.ContactMethod.WECHAT
            else -> RequirementCard.ContactMethod.LINE
        }

        val contactId = contactObj?.get("val")?.asString
            ?: contactObj?.get("contact_id")?.asString
            ?: obj.get("contact_id")?.asString
            ?: obj.get("contactId")?.asString
            ?: obj.get("contact_value")?.asString
            ?: ""

        val rawPhone = contactObj?.get("phone")
            ?: obj.get("phone_number")
            ?: obj.get("phone")

        val extractedPhone = PhoneActionHelper.extractPhoneNumber(rawPhone)
        val phoneNumber = if (contactMethod == RequirementCard.ContactMethod.PHONE) contactId.ifEmpty { extractedPhone } else extractedPhone

        val lat = if (obj.has("lat")) obj.get("lat").asDouble else (obj.get("gps_lat")?.asDouble ?: 0.0)
        val lng = if (obj.has("lng")) obj.get("lng").asDouble else (obj.get("gps_lng")?.asDouble ?: 0.0)

        val rawCreatedAt = obj.get("created_at")?.asString ?: obj.get("createdAt")?.asString
        val createdAt = if (rawCreatedAt == null || rawCreatedAt == "SYSTEM_NOW") {
            System.currentTimeMillis()
        } else {
            try {
                if (rawCreatedAt.contains("T")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        java.time.Instant.parse(rawCreatedAt).toEpochMilli()
                    } else {
                        System.currentTimeMillis()
                    }
                } else {
                    rawCreatedAt.toLong()
                }
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }

        val ttlHours = obj.get("ttl_hours")?.asInt ?: 12
        val isTestOnly = obj.get("is_test_only")?.asBoolean ?: true

        return RequirementCard(
            card_id = cardId,
            publisher_device_id = "SYSTEM_ADMIN",
            created_at = createdAt,
            category_code = drawerCode,
            drawer_type = drawerType,
            description = fullDescription,
            contact_method = contactMethod,
            contact_value = contactId,
            phone_number = phoneNumber,
            gps_lat = lat,
            gps_lng = lng,
            category_tag = categoryName,
            ttl_hours = ttlHours,
            is_test_only = isTestOnly,
        )
    }

    val TEST_DRAWER_4_1_JSON = """
    [
      {
        "card_id": "TEST_DRAWER_4_1_REQ_001",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】急件！公司文件幫送板橋到中和",
        "description": "合約急件需要在 1 小時內送到，預算 250 元，請直接電話聯繫。",
        "contact_type": "Phone",
        "contact_id": "0912000101",
        "lat": 25.002653,
        "lng": 121.497685,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_002",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】代買排隊人氣名店蛋黃酥",
        "description": "需代排約 20 分鐘，買 2 盒，車資加上代購費預算 200 元。",
        "contact_type": "LINE",
        "contact_id": "need_egg_cake",
        "lat": 25.003120,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_003",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】順風送！遺忘的手機拿回信義區",
        "description": "手機忘在朋友家，希望有順路機車幫忙帶回，報酬 180 元。",
        "contact_type": "Phone",
        "contact_id": "0912000103",
        "lat": 25.001890,
        "lng": 121.496540,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_004",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】幫送家常便當給在學校的小孩",
        "description": "12:00 前需送到國小門口警衛室，距離 2.5 公里，報酬 100 元。",
        "contact_type": "Phone",
        "contact_id": "0912000104",
        "lat": 25.004100,
        "lng": 121.499200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_005",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】急拿鑰匙！從家裡送至捷運站",
        "description": "鑰匙鎖在公司，家人幫拿出來後需送至捷運出口，預算 150 元。",
        "contact_type": "Telegram",
        "contact_id": "@key_runner",
        "lat": 25.002100,
        "lng": 121.497100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_006",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】代拿藥局慢性病處方箋藥包",
        "description": "已有預約，只需憑單號取藥並送到家，距離 1.8 公里，報酬 120 元。",
        "contact_type": "Phone",
        "contact_id": "0912000106",
        "lat": 25.001500,
        "lng": 121.496000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_007",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】順路帶送花店生日花束",
        "description": "花束已付清，需小心騎車送到指定餐廳，報酬 160 元。",
        "contact_type": "LINE",
        "contact_id": "flower_express",
        "lat": 25.003500,
        "lng": 121.498800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_008",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】急！代取超商貨件並送上樓",
        "description": "需憑取件碼代領（無付款），送到 4 樓無電梯，預算 130 元。",
        "contact_type": "Phone",
        "contact_id": "0912000108",
        "lat": 25.000900,
        "lng": 121.495200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_009",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】深夜急件！護照送到機場巴士站",
        "description": "護照遺漏在家，需要在 40 分鐘內送達轉運站，急件報酬 500 元。",
        "contact_type": "Phone",
        "contact_id": "0912000109",
        "lat": 25.002900,
        "lng": 121.497900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_010",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】代買五金行特定螺絲材料",
        "description": "需代買兩包特定規格螺絲，代購費加車資預算 150 元。",
        "contact_type": "WeChat",
        "contact_id": "hardware_need",
        "lat": 25.003800,
        "lng": 121.496900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_011",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】幫送大隻毛絨公仔給朋友",
        "description": "公仔裝在袋子裡，機車踏板可放，距離 4 公里，報酬 150 元。",
        "contact_type": "Phone",
        "contact_id": "0912000111",
        "lat": 25.001200,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_012",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】順風車帶送手作生日蛋糕",
        "description": "8 吋蛋糕需平放慢慢騎，送到松山區，報酬 250 元。",
        "contact_type": "LINE",
        "contact_id": "cake_delivery_fast",
        "lat": 25.004500,
        "lng": 121.497300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_013",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】代領郵局掛號信件（需授權書）",
        "description": "證件與委託書已準備好，順路幫取並送到附近辦公室，報酬 180 元。",
        "contact_type": "Phone",
        "contact_id": "0912000113",
        "lat": 25.002200,
        "lng": 121.499000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_014",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】幫買宵夜名店滷味送到家",
        "description": "需排隊約 10 分鐘，車資及代購費用共 160 元。",
        "contact_type": "Phone",
        "contact_id": "0912000114",
        "lat": 25.001600,
        "lng": 121.496200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_015",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】急送筆電電源線至會議中心",
        "description": "開會用到一半沒電，需要在 25 分鐘內送達，報酬 200 元。",
        "contact_type": "Telegram",
        "contact_id": "@power_adapter_sos",
        "lat": 25.003100,
        "lng": 121.495500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_016",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】幫送 3 箱礦泉水至地下一樓",
        "description": "機車載得下，需協助搬到 B1，距離 1 公里，報酬 180 元。",
        "contact_type": "Phone",
        "contact_id": "0912000116",
        "lat": 25.002800,
        "lng": 121.498400,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_017",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】二手面交商品 代取代付 500 元",
        "description": "幫忙跟賣家面交帶回物品，代墊 500 元，運費與報酬另給 150 元。",
        "contact_type": "LINE",
        "contact_id": "face_trade_helper",
        "lat": 25.000500,
        "lng": 121.497500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_018",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】雨天急送雨衣雨具給家人",
        "description": "家人被雨困在公車站，幫忙把雨衣送過去，距離 2 公里，報酬 120 元。",
        "contact_type": "Phone",
        "contact_id": "0912000118",
        "lat": 25.004000,
        "lng": 121.496800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_019",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】代繳路邊停車費與水電單",
        "description": "紙本單據代拿至超商繳費，報酬 100 元，距離 1 公里內。",
        "contact_type": "Phone",
        "contact_id": "0912000119",
        "lat": 25.002400,
        "lng": 121.496700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_REQ_020",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "REQUIREMENT",
        "title": "【需求】順路帶送寵物乾飼料一包",
        "description": "飼料重 3kg，從寵物店幫送到家，報酬 140 元。",
        "contact_type": "LINE",
        "contact_id": "pet_food_express",
        "lat": 25.001900,
        "lng": 121.498600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_001",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】專業機車跑腿 全區急件接單中",
        "description": "自備大容量外送箱，文件/小件包裹 30 分鐘內可取件，價格實惠。",
        "contact_type": "Phone",
        "contact_id": "0987000201",
        "lat": 25.002700,
        "lng": 121.497700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_002",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】順風車！中和往返三重 每日定時",
        "description": "每天早晚通勤順路可帶小型文件或便當，單趟順風價 80 元起。",
        "contact_type": "LINE",
        "contact_id": "commute_runner_tw",
        "lat": 25.003300,
        "lng": 121.498200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_003",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】夜間機車跑腿 24 小時服務",
        "description": "深夜幫買宵夜、代購超商商品、送急件鑰匙，歡迎電話聯繫。",
        "contact_type": "Phone",
        "contact_id": "0987000203",
        "lat": 25.001950,
        "lng": 121.496600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_004",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】代排隊/代送件 誠信老司機",
        "description": "熟悉在地路況，代排人氣美食或醫院掛號號碼牌，按時收費。",
        "contact_type": "LINE",
        "contact_id": "pro_queue_runner",
        "lat": 25.004200,
        "lng": 121.499300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_005",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】冷藏溫控保鮮箱 機車急速送達",
        "description": "適合送生鮮、蛋糕、手搖飲，不傾倒不碰撞，距離 5 公里內可接。",
        "contact_type": "Phone",
        "contact_id": "0987000205",
        "lat": 25.002200,
        "lng": 121.497200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_006",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】順風車！新莊往板橋方向 隨時出發",
        "description": "現在人在新莊，準備騎車回板橋，可順路代送小件物品或文件。",
        "contact_type": "Telegram",
        "contact_id": "@rider_way_home",
        "lat": 25.001600,
        "lng": 121.496100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_007",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】機車快遞 專送重要合約印章",
        "description": "安全有保障，全程追蹤定位，專人親自送達簽收。",
        "contact_type": "Phone",
        "contact_id": "0987000207",
        "lat": 25.003600,
        "lng": 121.498900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_008",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】代買代繳 兼職機車跑腿員",
        "description": "收費透明，5 公里內基礎運費 100 元，歡迎附近住戶長期合作。",
        "contact_type": "WeChat",
        "contact_id": "runner_helper_88",
        "lat": 25.001000,
        "lng": 121.495300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_009",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】重型機車 跨區長途超快速直送",
        "description": "適合跨縣市急件（如台北至桃園），安全穩健迅速抵達。",
        "contact_type": "Phone",
        "contact_id": "0987000209",
        "lat": 25.003000,
        "lng": 121.498000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_010",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】午餐時間 團訂便當順車代配送",
        "description": "中午 11:00-13:00 專門協助在地店家或公司行號跑腿送餐。",
        "contact_type": "LINE",
        "contact_id": "lunch_delivery_pro",
        "lat": 25.003900,
        "lng": 121.497000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_011",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】雨天備勤 機車急件跑腿車隊",
        "description": "雨天不加價，配有防水外送包，確保物件乾爽安全送達。",
        "contact_type": "Phone",
        "contact_id": "0987000211",
        "lat": 25.001300,
        "lng": 121.498200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_012",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】順風車！台北車站往士林方向",
        "description": "15 分鐘後出發，腳踏板有空間，可順帶 20 公斤內物品，車資便宜。",
        "contact_type": "Phone",
        "contact_id": "0987000212",
        "lat": 25.004600,
        "lng": 121.497400,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_013",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】二手商品代面交驗貨服務",
        "description": "幫忙現場測試檢查手機/電子產品，確認無誤後代付帶回。",
        "contact_type": "LINE",
        "contact_id": "check_goods_runner",
        "lat": 25.002300,
        "lng": 121.499100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_014",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】寵物用品/急用藥品 快速代買",
        "description": "熟悉在地各大獸醫院與寵物店，急用用品 20 分鐘送達。",
        "contact_type": "Phone",
        "contact_id": "0987000214",
        "lat": 25.001700,
        "lng": 121.496300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_015",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】大馬力打檔車 載重急件配送",
        "description": "後座可綁較大體積箱子（30kg 內），適合機具配件、耗材急送。",
        "contact_type": "Telegram",
        "contact_id": "@heavy_rider_tw",
        "lat": 25.003200,
        "lng": 121.495600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_016",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】在地生活通 代辦各種繁瑣雜事",
        "description": "寄件、領包裹、拿衣服去洗、買生活用品，一電話即刻接單。",
        "contact_type": "Phone",
        "contact_id": "0987000216",
        "lat": 25.002900,
        "lng": 121.498500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_017",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】順風車！中和往內湖科技園區",
        "description": "早上 8:00 準時出發，可順路帶文件或輕量包裹至內科，車資 120 元。",
        "contact_type": "LINE",
        "contact_id": "neihu_commute_express",
        "lat": 25.000600,
        "lng": 121.497600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_018",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】醫院門診 代掛號/代拿病歷藥包",
        "description": "熟練各大醫院流程，幫忙省去現場排隊時間，服務親切。",
        "contact_type": "Phone",
        "contact_id": "0987000218",
        "lat": 25.004100,
        "lng": 121.496900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_019",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】代買知名夜市美食 / 甜點名店",
        "description": "想吃排隊美食不用自己跑，跑腿費 120 元起，熱騰騰幫您送達。",
        "contact_type": "Phone",
        "contact_id": "0987000219",
        "lat": 25.002500,
        "lng": 121.496800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_1_SUP_020",
        "drawer_code": "4-1",
        "category_name": "機車跑腿 急件順風",
        "type": "SUPPLY",
        "title": "【供給】學生/小資族 機車短程小件快送",
        "description": "課餘時間兼職跑腿，3 公里內小件配送統一收費 80 元，請多支持。",
        "contact_type": "LINE",
        "contact_id": "student_runner_fast",
        "lat": 25.002000,
        "lng": 121.498700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      }
    ]
    """.trimIndent()

    val TEST_DRAWER_4_2_JSON = """
    [
      {
        "card_id": "TEST_DRAWER_4_2_REQ_001",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】明天 08:30 板橋到台大醫院 照表打八折",
        "description": "回診用車，希望司機準時，照表計費給予 8 折優惠，預算單趟約 300 元。",
        "contact_type": "Phone",
        "contact_id": "0912333001",
        "lat": 25.002653,
        "lng": 121.497685,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_002",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】急叫商務車！加 300 元叫車費 桃園機場直送",
        "description": "今天 14:00 出發去桃園機場 T2，行李 3 件，願意額外加 300 元服務費求優質新車。",
        "contact_type": "LINE",
        "contact_id": "vip_airport_taxi",
        "lat": 25.003120,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_003",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長照福祉車 需輪椅斜坡板 亞東醫院返家",
        "description": "長輩坐輪椅，11:30 出院返家，需升降斜坡輔具，無階梯梯爬，單趟預算 500 元。",
        "contact_type": "Phone",
        "contact_id": "0912333003",
        "lat": 25.001890,
        "lng": 121.496540,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_004",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】下雨急叫車！加 100 元叫車費 中和到新埔捷運",
        "description": "15 分鐘內到，雨天路邊叫不到車，車資跳表外加 100 元小費。",
        "contact_type": "Phone",
        "contact_id": "0912333004",
        "lat": 25.004100,
        "lng": 121.499200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_005",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長照接送 需上樓扶持爬梯設備 台北榮總",
        "description": "老人家住 3 樓無電梯，需要含履帶爬梯機與輪椅移位服務，單趟包套 1200 元。",
        "contact_type": "LINE",
        "contact_id": "care_stair_need",
        "lat": 25.002100,
        "lng": 121.497100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_006",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】多元高階進口車（指定一年內 Lexus/Benz）",
        "description": "接送貴賓至信義區酒店，指定 2025/2026 年新車，跳表額外 +200 元叫車費。",
        "contact_type": "Telegram",
        "contact_id": "@luxury_car_req",
        "lat": 25.001500,
        "lng": 121.496000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_007",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長程包車跨縣市 照表打八折 新北到新竹",
        "description": "今天下午 16:00 出發，跑長途要求照表 8 折優惠，車況乾淨不抽菸。",
        "contact_type": "Phone",
        "contact_id": "0912333007",
        "lat": 25.003500,
        "lng": 121.498800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_008",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】深夜醉酒安全代駕/計程車 加 200 元小費",
        "description": "凌晨 01:30 熱炒店接送回永和，態度良好不甩尾，加 200 元小費。",
        "contact_type": "Phone",
        "contact_id": "0912333008",
        "lat": 25.000900,
        "lng": 121.495200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_009",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長照復健定點接送 預約每週一三五",
        "description": "長照補助身分，往返護理之家，車輛需配備氧氣瓶固定架與低地板。",
        "contact_type": "LINE",
        "contact_id": "rehab_taxi_user",
        "lat": 25.002900,
        "lng": 121.497900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_010",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】高鐵新竹站接送 指定 Tesla Model Y",
        "description": "18:00 新竹高鐵接至科學園區，希望特斯拉電車，跳表額外加 100 元。",
        "contact_type": "Phone",
        "contact_id": "0912333010",
        "lat": 25.003800,
        "lng": 121.496900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_011",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】寵物友善計程車 帶中型犬看獸醫",
        "description": "有狗籃與推車，司機不嫌棄狗毛，照表計費加 100 元清潔補貼。",
        "contact_type": "Phone",
        "contact_id": "0912333011",
        "lat": 25.001200,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_012",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】早晨通勤定點接送 照表打八折 長期包月",
        "description": "每天 07:30 從中和到內湖辦公室，長期合作照表 8 折，按月結算。",
        "contact_type": "LINE",
        "contact_id": "monthly_taxi_need",
        "lat": 25.004500,
        "lng": 121.497300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_013",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長照多點接送 診所抽血再回家",
        "description": "需等待 20 分鐘，司機有耐性協助行動不便長輩，總預算 600 元。",
        "contact_type": "Phone",
        "contact_id": "0912333013",
        "lat": 25.002200,
        "lng": 121.499000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_014",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】九人座豪華商務車 加 300 元叫車費 家族旅遊",
        "description": "6 人加 4 個大行李箱，新北市區半日包車，希望一年內 Toyota Alphard。",
        "contact_type": "WeChat",
        "contact_id": "alphard_tour_req",
        "lat": 25.001600,
        "lng": 121.496200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_015",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】跨縣市急件人車同往 照表打八折",
        "description": "隨車送急件至台中，單趟長途要求照表 8 折，10 分鐘內出發。",
        "contact_type": "Phone",
        "contact_id": "0912333015",
        "lat": 25.003100,
        "lng": 121.495500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_016",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長照接送 需雙人扶持上車",
        "description": "長輩體重較重無腳力，需要兩位體力好的司機/助手幫忙，預算 800 元。",
        "contact_type": "Phone",
        "contact_id": "0912333016",
        "lat": 25.002800,
        "lng": 121.498400,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_017",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】深夜桃園機場接機 加 200 元叫車費",
        "description": "凌晨 02:00 抵達 T1，接送至板橋，舉牌接機加 200 元叫車補貼。",
        "contact_type": "LINE",
        "contact_id": "midnight_pickup",
        "lat": 25.000500,
        "lng": 121.497500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_018",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】短程接送國小學童放學 照表打八折",
        "description": "每天 16:30 學校接回安親班，距離 1.5 公里，長期熟客要求照表 8 折。",
        "contact_type": "Phone",
        "contact_id": "0912333018",
        "lat": 25.004000,
        "lng": 121.496800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_019",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】長照接送 附急救設備與護士隨車",
        "description": "重症長輩轉院，需有抽痰機輔具與護理人員隨車，預算 2500 元。",
        "contact_type": "Phone",
        "contact_id": "0912333019",
        "lat": 25.002400,
        "lng": 121.496700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_REQ_020",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】多元高階純電休旅 加 100 元叫車費",
        "description": "指定 2026 年最新款 Hyundai Ioniq 5，靜音舒適，加 100 元叫車費。",
        "contact_type": "LINE",
        "contact_id": "ev_taxi_fan",
        "lat": 25.001900,
        "lng": 121.498600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_001",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】個人計程車 長途專案照表打八折！",
        "description": "2025 年新車 Toyota RAV4，車況極佳無煙味，跨縣市/長途一律照表打 8 折！",
        "contact_type": "Phone",
        "contact_id": "0988333201",
        "lat": 25.002700,
        "lng": 121.497700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_002",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2026 最新款 Lexus LM500h 多元高階車（車價 500 萬級）",
        "description": "一年內極新旗艦商務車，頭等艙按摩座椅，機場接送/商務履約，車資 +300 元叫車費。",
        "contact_type": "LINE",
        "contact_id": "lexus_lm_vip",
        "lat": 25.003300,
        "lng": 121.498200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_003",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照專用福祉車 鋁合金自動升降斜坡板",
        "description": "配備輪椅四點式安全固定扣、低地板，司機持 CPR 與 BLS 照護證照，專接診所醫院。",
        "contact_type": "Phone",
        "contact_id": "0988333203",
        "lat": 25.001950,
        "lng": 121.496600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_004",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2025 年新車 Tesla Model Y 多元計程車",
        "description": "高階純電休旅（新車價 180 萬），全景天窗、科技安靜，跳表 +100 元叫車費即可預約。",
        "contact_type": "Phone",
        "contact_id": "0988333204",
        "lat": 25.004200,
        "lng": 121.499300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_005",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】特約長照接送 附履帶式爬梯機輔具",
        "description": "專治無電梯公寓！專業兩員操作履帶爬梯機，協助失能長輩安全上下樓，合法開立收據。",
        "contact_type": "LINE",
        "contact_id": "stair_stretcher_pro",
        "lat": 25.002200,
        "lng": 121.497200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_006",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2026 Mercedes-Benz EQE SUV 多元高階車",
        "description": "新車價 330 萬高級賓士電動休旅，後座尊榮空間，商務接送，跳表 +200 元叫車費。",
        "contact_type": "Telegram",
        "contact_id": "@benz_eqe_driver",
        "lat": 25.001600,
        "lng": 121.496100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_007",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】優良計程車老司機 離峰時間照表打八折",
        "description": "每日 10:00-16:00 離峰時段預約叫車，一律照表 8 折，乾淨舒適 Toyota Cross。",
        "contact_type": "Phone",
        "contact_id": "0988333207",
        "lat": 25.003600,
        "lng": 121.498900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_008",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照專用福斯 Caddy Maxi 福祉車",
        "description": "配備德國進口輪椅坡道、轉移位安全帶、可隨行 4 位家屬，政府長照特約可折抵。",
        "contact_type": "Phone",
        "contact_id": "0988333208",
        "lat": 25.001000,
        "lng": 121.495300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_009",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2025 Toyota Alphard 頂級 7 人座多元車（新車價 315 萬）",
        "description": "明星藝人指定車款，第二排 Ottoman 總裁座椅，加 300 元叫車費享受極致尊榮接送。",
        "contact_type": "WeChat",
        "contact_id": "alphard_vip_service",
        "lat": 25.003000,
        "lng": 121.498000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_010",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長程專車接送 預約往返台中高雄 照表打八折",
        "description": "跨縣市包車首選，司機雙執照，開車平穩不超速，全程優惠照表 8 折！",
        "contact_type": "Phone",
        "contact_id": "0988333210",
        "lat": 25.003900,
        "lng": 121.497000,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_011",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照高背輪椅專用車 附隨車急救防滑輔具",
        "description": "專載臥床或高背輪椅患者，懸吊軟、煞車平順，車內定期酒精臭氧消毒。",
        "contact_type": "LINE",
        "contact_id": "care_van_taiwan",
        "lat": 25.001300,
        "lng": 121.498200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_012",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2026 BMW iX1 純電多元計程車（新車價 228 萬）",
        "description": "全新上市豪華純電跨界休旅，車內極度安靜，跳表 +200 元叫車費即可體驗。",
        "contact_type": "Phone",
        "contact_id": "0988333212",
        "lat": 25.004600,
        "lng": 121.497400,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_013",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】雨天/尖峰優先派車 額外 +100 元叫車費",
        "description": "尖峰時段叫不到車？只要加 100 元叫車費，5 分鐘內快速到場接送！",
        "contact_type": "Phone",
        "contact_id": "0988333213",
        "lat": 25.002300,
        "lng": 121.499100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_014",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照接送 支援氧氣鋼瓶固定與血氧監測設備",
        "description": "專用長照接送車，車上備有緊急氧氣瓶座與急救箱，親切耐性陪伴回診。",
        "contact_type": "LINE",
        "contact_id": "medical_taxi_pro",
        "lat": 25.001700,
        "lng": 121.496300,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_015",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2025 Lexus ES300h 豪華房車（新車價 210 萬）",
        "description": "多元計程車尊榮首選，後座腿部空間巨大，適合商務接送與婚禮禮車，+200 元叫車費。",
        "contact_type": "Phone",
        "contact_id": "0988333215",
        "lat": 25.003200,
        "lng": 121.495600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_016",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】機場接送/長途叫車 預約一律照表打八折",
        "description": "提前一天預約桃園機場接送或跨縣市包車，結帳直接照表打 8 折優惠！",
        "contact_type": "Telegram",
        "contact_id": "@discount_taxi_80",
        "lat": 25.002900,
        "lng": 121.498500,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_017",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照福祉車 附折疊輪椅借用與陪伴就醫服務",
        "description": "若家中無輪椅，車上可免費借用輕量折疊輪椅，司機可全程推輪椅協助就醫。",
        "contact_type": "Phone",
        "contact_id": "0988333217",
        "lat": 25.000600,
        "lng": 121.497600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_018",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2026 Volvo XC40 Recharge 純電多元車（新車價 190 萬）",
        "description": "瑞典坦克極致安全，全車空氣淨化系統，跳表額外 +100 元叫車費派車。",
        "contact_type": "LINE",
        "contact_id": "volvo_ev_taxi",
        "lat": 25.004100,
        "lng": 121.496900,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_019",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】九人座 Hyundai Staria 多元車 加 300 元叫車費",
        "description": "星艦造型九人座（新車價 200 萬），超大行李空間，適合多人機場接送與包車。",
        "contact_type": "Phone",
        "contact_id": "0988333219",
        "lat": 25.002500,
        "lng": 121.496800,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "TEST_DRAWER_4_2_SUP_020",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】婦幼親子女用計程車 附 ISOFIX 安全座椅",
        "description": "專為孕婦、媽媽與幼童服務，車內備有兩張兒童安全座椅，跳表照實收費。",
        "contact_type": "LINE",
        "contact_id": "mom_baby_taxi",
        "lat": 25.002000,
        "lng": 121.498700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      }
    ]
    """.trimIndent()

    /**
     * 取得抽屜 1-1 ("在地攤商 / 個人私廚") 的 20 筆測試卡片
     */
    fun getTestCardsForDrawer11(): List<RequirementCard> {
        return parseDrawerJson(TEST_DRAWER_1_1_JSON)
    }

    /**
     * 取得抽屜 4-1 ("機車跑腿 急件順風") 的 40 筆測試卡片
     */
    fun getTestCardsForDrawer41(): List<RequirementCard> {
        return parseDrawerJson(TEST_DRAWER_4_1_JSON)
    }

    /**
     * 取得抽屜 4-2 ("計程叫車 長照接送") 的 40 筆測試卡片
     */
    fun getTestCardsForDrawer42(): List<RequirementCard> {
        return parseDrawerJson(TEST_DRAWER_4_2_JSON)
    }

    /**
     * 載入抽屜 1-1（在地攤商 / 個人私廚）測試卡片至記憶體，並輸出日誌
     */
    fun loadDrawer1_1TestCards(): List<RequirementCard> {
        val testCards = getTestCardsForDrawer11()
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 抽屜 1-1（在地攤商）20筆測試卡片載入成功！12小時自動清空機制運作中。"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    /**
     * 載入抽屜 4-1（機車跑腿 急件順風）測試卡片至記憶體，並輸出日誌
     */
    fun loadDrawer4_1TestCards(): List<RequirementCard> {
        val testCards = getTestCardsForDrawer41()
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 抽屜 4-1（機車跑腿 急件順風）需求 20 筆 + 供給 20 筆，共 40 筆測試卡片載入完成！12小時自動清空機制運作中。"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    val TEST_LIGHT_JSON = """
    [
      {
        "card_id": "REQ_001",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】明天 08:30 板橋到台大醫院 照表打八折",
        "description": "回診用車，希望司機準時，照表計費給予 8 折優惠。",
        "contact_type": "Phone",
        "contact_id": "0912333001",
        "lat": 25.002653,
        "lng": 121.497685,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "REQ_002",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】急叫商務車！加 300 元叫車費 桃園機場直送",
        "description": "今天 14:00 出發去桃園機場 T2，行李 3 件。",
        "contact_type": "LINE",
        "contact_id": "vip_airport_taxi",
        "lat": 25.003120,
        "lng": 121.498100,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "SUP_001",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】個人計程車 長途專案照表打八折！",
        "description": "2025 年新車 Toyota RAV4，車況極佳無煙味。",
        "contact_type": "Phone",
        "contact_id": "0988333201",
        "lat": 25.002700,
        "lng": 121.497700,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "SUP_002",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】2026 最新款 Lexus LM500h 多元高階車",
        "description": "一年內極新旗艦商務車，車資 +300 元叫車費。",
        "contact_type": "LINE",
        "contact_id": "lexus_lm_vip",
        "lat": 25.003300,
        "lng": 121.498200,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      },
      {
        "card_id": "SUP_003",
        "drawer_code": "4-2",
        "category_name": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照專用福祉車 鋁合金自動升降斜坡板",
        "description": "配備輪椅四點式安全固定扣，專接診所醫院。",
        "contact_type": "Phone",
        "contact_id": "0988333203",
        "lat": 25.001950,
        "lng": 121.496600,
        "ttl_hours": 12,
        "is_test_only": true,
        "created_at": "SYSTEM_NOW"
      }
    ]
    """.trimIndent()

    /**
     * 取得輕量版測試卡片 (5 筆)
     */
    fun getLightTestCards(): List<RequirementCard> {
        return parseDrawerJson(TEST_LIGHT_JSON)
    }

    /**
     * 載入輕量版測試卡片 (5 筆卡片)，確保極速測試與 App 順暢度
     */
    fun loadLightTestCards(): List<RequirementCard> {
        val testCards = getLightTestCards()
        BaichuanRepository.injectTestCards(testCards)
        val message = "⚡ 輕量版 5 筆測試卡片（需求 2 筆 + 供給 3 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    /**
     * 根據真實/使用者當前 GPS 座標動態擴散生成 1-1 測試卡片（需求 10 筆 + 供給 10 筆）
     */
    fun loadGpsRelativeTestCards(baseLat: Double = 25.002630, baseLng: Double = 121.497703): List<RequirementCard> {
        val relItems = listOf(
            Triple("REQ_1_1_01", "【需求】急！徵求附近宵夜鹹酥雞代購", "想吃夜市鹹酥雞，外送費+100元，希望 30 分鐘內送到。") to Pair(0.001, 0.001),
            Triple("REQ_1_1_02", "【需求】求代購地瓜球與手搖飲 2 杯", "微糖微冰大杯綠茶，跑腿費付 80 元。") to Pair(0.002, 0.001),
            Triple("REQ_1_1_03", "【需求】徵求鄰居幫忙外帶巷口牛肉麵", "紅燒牛肉麵大碗不加蔥，外送費 50 元。") to Pair(0.002, 0.003),
            Triple("REQ_1_1_04", "【需求】晚餐想吃夜市滷味 預算 300 元", "請幫忙挑選熱門品項，車資/跑腿費另計。") to Pair(0.003, 0.002),
            Triple("REQ_1_1_05", "【需求】求代買超商大杯拿鐵 2 杯", "下雨不方便出門，希望能送到 1 樓管理室。") to Pair(0.004, 0.003),
            Triple("REQ_1_1_06", "【需求】徵求早餐代購（明日 07:30）", "燒餅油條加蛋，送至社區大樓，給 60 元小費。") to Pair(0.005, 0.004),
            Triple("REQ_1_1_07", "【需求】代購排隊名店章魚燒", "需要排隊約 15 分鐘，代購費給 100 元。") to Pair(0.006, 0.005),
            Triple("REQ_1_1_08", "【需求】求買在地傳統豆花 3 碗", "花生豆花微糖，跑腿費 70 元。") to Pair(0.007, 0.006),
            Triple("REQ_1_1_09", "【需求】代買連鎖速食店套餐", "送達時現場現金結清，含外送費付 500 元。") to Pair(0.008, 0.007),
            Triple("REQ_1_1_10", "【需求】求代購夜市藥燉排骨", "天冷想喝熱湯，送到巷口，跑腿費 90 元。") to Pair(0.010, 0.008)
        ).map { (data, offset) ->
            RequirementCard(
                card_id = data.first,
                publisher_device_id = "SYSTEM_ADMIN",
                created_at = System.currentTimeMillis(),
                category_code = "1-1",
                drawer_type = "1A",
                description = "${data.second}\n${data.third}",
                contact_method = RequirementCard.ContactMethod.PHONE,
                contact_value = "0912000${data.first.takeLast(2)}",
                gps_lat = baseLat + offset.first,
                gps_lng = baseLng + offset.second,
                category_tag = "在地攤販 剛需外送",
                ttl_hours = 12,
                is_test_only = true
            )
        } + listOf(
            Triple("SUP_1_1_01", "【供給】阿婆現炸脆皮鹹酥雞 滿 200 免費外送", "在地經營 20 年，傳統口味胡椒鹽，附近 1 公里免費送。") to Pair(0.001, -0.001),
            Triple("SUP_1_1_02", "【供給】夜市人氣手工地瓜球 接受預約外送", "大包 50 元，外皮超酥脆，周邊 500m 內滿 3 包可送。") to Pair(0.002, -0.001),
            Triple("SUP_1_1_03", "【供給】老字號水餃牛肉麵 專人外送服務", "手工現包水餃，湯頭濃郁，滿 300 元親自外送。") to Pair(0.003, -0.002),
            Triple("SUP_1_1_04", "【供給】深夜秘製冷熱滷味 宵夜外送中", "獨門麻辣醬汁，營業至凌晨 1 點，歡迎電話預訂。") to Pair(0.004, -0.003),
            Triple("SUP_1_1_05", "【供給】兼職機車跑腿服務 附近代購代送", "自備保溫箱，幫買便當、手搖飲、超商用品，距離可議。") to Pair(0.004, -0.004),
            Triple("SUP_1_1_06", "【供給】無名古早味蛋餅早餐 支援社區預約", "粉漿蛋餅、大腸麵線，提前一天預約可送達樓下。") to Pair(0.005, -0.004),
            Triple("SUP_1_1_07", "【供給】日式章魚燒車 周邊外送接單中", "皮酥內軟章魚大塊，滿 5 盒免收外送服務費。") to Pair(0.006, -0.005),
            Triple("SUP_1_1_08", "【供給】傳統手工黑糖豆花 團購外送", "純手工製作無防腐劑，公司行號團購滿 10 碗直送。") to Pair(0.007, -0.006),
            Triple("SUP_1_1_09", "【供給】快腿阿傑 專打短程外送/代購", "附近 2 公里內皆可送，速度快保證熱騰騰送到手。") to Pair(0.008, -0.007),
            Triple("SUP_1_1_10", "【供給】養生藥燉排骨/當歸鴨 宵夜外送", "冬天補身首選，附贈沾醬，周邊滿 400 元送達。") to Pair(0.010, -0.008)
        ).map { (data, offset) ->
            RequirementCard(
                card_id = data.first,
                publisher_device_id = "SYSTEM_ADMIN",
                created_at = System.currentTimeMillis(),
                category_code = "1-1",
                drawer_type = "1B",
                description = "${data.second}\n${data.third}",
                contact_method = RequirementCard.ContactMethod.PHONE,
                contact_value = "0988000${data.first.takeLast(2)}",
                gps_lat = baseLat + offset.first,
                gps_lng = baseLng + offset.second,
                category_tag = "在地攤販 剛需外送",
                ttl_hours = 12,
                is_test_only = true
            )
        }

        BaichuanRepository.injectTestCards(relItems)
        val message = "✅ 根據 GPS 座標 ($baseLat, $baseLng) 動態擴散生成 1-1 測試卡片 20 筆（需求 10 筆 + 供給 10 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return relItems
    }

    /**
     * 根據真實/使用者當前 GPS 座標動態擴散生成 4-1 測試卡片（需求 10 筆 + 供給 10 筆）
     */
    fun loadGpsRelativeDrawer4_1TestCards(baseLat: Double = 25.002614, baseLng: Double = 121.497660): List<RequirementCard> {
        val relItems = listOf(
            Triple("REQ_4_1_01", "【需求】急求！今晚 6 點半兩人份健康少油減脂餐", "想吃健康舒肥雞胸便當，希望蔬菜多一點，預算 300 元送達。") to Pair(0.001, 0.001),
            Triple("REQ_4_1_02", "【需求】徵求私廚訂製「家庭四菜一湯」晚餐", "適合 3-4 人吃，家常紅燒肉、炒時蔬，希望 18:30 前外送。") to Pair(0.002, 0.001),
            Triple("REQ_4_1_03", "【需求】求附近個人工作室的手作純手工蛋黃酥", "需要 2 盒（一盒 6 入）作為送禮，希望下午 4 點前送到。") to Pair(0.002, 0.003),
            Triple("REQ_4_1_04", "【需求】徵求素食個人私廚 提供全素便當 2 份", "全素無五辛，希望能送到社區管理室，運費另付 60 元。") to Pair(0.003, 0.002),
            Triple("REQ_4_1_05", "【需求】尋找會做道地四川口水雞的個人工作室", "想吃正宗麻辣口味，預訂今天宵夜，外送費+80元。") to Pair(0.004, 0.003),
            Triple("REQ_4_1_06", "【需求】預訂明天中午公司小型會議商務餐盒 5 份", "精緻主菜搭配三樣副菜，單價預算 180-200 元。") to Pair(0.005, 0.004),
            Triple("REQ_4_1_07", "【需求】徵求媽媽味手作水餃（冷凍 100 顆）", "高麗菜豬肉與牛肉口味各半，希望專人送到府。") to Pair(0.006, 0.005),
            Triple("REQ_4_1_08", "【需求】求購低糖生酮巴斯克起司蛋糕 6 吋", "生酮飲食可用，希望無麵粉無精緻糖，下午茶送到。") to Pair(0.007, 0.006),
            Triple("REQ_4_1_09", "【需求】求代煮老薑麻油雞湯 1 大鍋", "天冷月子補身，希望能用陶瓷鍋裝送到府，含跑腿費 600 元。") to Pair(0.008, 0.007),
            Triple("REQ_4_1_10", "【需求】預約週末家庭派對指派私廚到府備餐", "5 人份義式料理，需包含前菜、主餐與甜點，預算可議。") to Pair(0.010, 0.008)
        ).map { (data, offset) ->
            RequirementCard(
                card_id = data.first,
                publisher_device_id = "SYSTEM_ADMIN",
                created_at = System.currentTimeMillis(),
                category_code = "4-1",
                drawer_type = "1A",
                description = "${data.second}\n${data.third}",
                contact_method = RequirementCard.ContactMethod.PHONE,
                contact_value = "09120041${data.first.takeLast(2)}",
                gps_lat = baseLat + offset.first,
                gps_lng = baseLng + offset.second,
                category_tag = "在地攤商 個人私廚",
                ttl_hours = 12,
                is_test_only = true
            )
        } + listOf(
            Triple("SUP_4_1_01", "【供給】小林健康私廚 健身高培舒肥雞胸便當", "每日新鮮現做，紫米飯搭配五色蔬菜，周邊 1km 滿 2 個免運。") to Pair(0.001, -0.001),
            Triple("SUP_4_1_02", "【供給】陳媽媽私房菜 三菜一湯家常溫馨外送包", "無味精健康烹調，每天更換菜色，適合上班族與小家庭。") to Pair(0.002, -0.001),
            Triple("SUP_4_1_03", "【供給】甜心手作烘焙坊 現烤流心蛋黃酥/鳳梨酥", "嚴選紐西蘭奶油與紅土鹹蛋黃，禮盒裝供應中，可外送。") to Pair(0.003, -0.002),
            Triple("SUP_4_1_04", "【供給】淨心素食個人工作室 有機五穀健康餐盒", "純素/植物肉精緻餐點，滿 300 元親自送達周邊社區。") to Pair(0.004, -0.003),
            Triple("SUP_4_1_05", "【供給】老四川私房手路菜 道地口水雞/水煮牛", "正宗四川香辣風味，宵夜時段可外送，歡迎預訂。") to Pair(0.004, -0.004),
            Triple("SUP_4_1_06", "【供給】雅緻精緻會議餐盒 提供發票/公司團購", "專為企業會議打造，色香味俱全，需提前一天預訂。") to Pair(0.005, -0.004),
            Triple("SUP_4_1_07", "【供給】阿婆手工鮮肉水餃/剝皮辣椒水餃", "皮薄餡多汁，冷凍包裝每包 50 顆，滿 2 包免費送到家。") to Pair(0.006, -0.005),
            Triple("SUP_4_1_08", "【供給】無糖樂園 生酮/低碳水無負擔手作甜點", "主打生酮巴斯克起司蛋糕、赤藻糖甜點，健康美味。") to Pair(0.007, -0.006),
            Triple("SUP_4_1_09", "【供給】養生藥膳私廚 溫補麻油雞湯/十全排骨湯", "嚴選中藥材慢火燉煮 4 小時，保溫湯罐外送，暖心暖胃。") to Pair(0.008, -0.007),
            Triple("SUP_4_1_10", "【供給】Chef Marco 到府私廚服務/派對外辦", "10 年法義料理經驗，提供精緻外燴與派對備餐，歡迎洽詢。") to Pair(0.010, -0.008)
        ).map { (data, offset) ->
            RequirementCard(
                card_id = data.first,
                publisher_device_id = "SYSTEM_ADMIN",
                created_at = System.currentTimeMillis(),
                category_code = "4-1",
                drawer_type = "1B",
                description = "${data.second}\n${data.third}",
                contact_method = RequirementCard.ContactMethod.PHONE,
                contact_value = "09880041${data.first.takeLast(2)}",
                gps_lat = baseLat + offset.first,
                gps_lng = baseLng + offset.second,
                category_tag = "在地攤商 個人私廚",
                ttl_hours = 12,
                is_test_only = true
            )
        }

        BaichuanRepository.injectTestCards(relItems)
        val message = "✅ 根據 GPS 座標 ($baseLat, $baseLng) 動態擴散生成 4-1 測試卡片 20 筆（需求 10 筆 + 供給 10 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return relItems
    }

    /**
     * 根據真實/使用者當前 GPS 座標動態擴散生成 4-2 測試卡片（需求 10 筆 + 供給 10 筆）
     */
    fun loadGpsRelativeDrawer4_2TestCards(baseLat: Double = 25.002614, baseLng: Double = 121.497660): List<RequirementCard> {
        val relItems = listOf(
            Triple("REQ_4_2_01", "【需求】急叫商務車！加 300 元叫車費 桃園機場直送", "今天 14:00 出發去桃園機場 T2，行李 3 件，乾淨無煙車加價急求。") to Pair(0.001, 0.001),
            Triple("REQ_4_2_02", "【需求】明天 08:30 板橋到台大醫院 徵照表打八折司機", "長輩每週定期回診用車，希望司機準時，長期配合照表計費打 8 折。") to Pair(0.002, 0.001),
            Triple("REQ_4_2_03", "【需求】求長照福祉車接送！輪椅上車 +200 元服務費", "需配備斜坡板/升降梯，接送至新北市立醫院，輔佐長輩上落車。") to Pair(0.002, 0.003),
            Triple("REQ_4_2_04", "【需求】跨縣市長途包車（台北到台中）徵求降價折扣", "本週五單程台中，希望有回空車或長途優惠價（預算 2200 元包車）。") to Pair(0.003, 0.002),
            Triple("REQ_4_2_05", "【需求】暴雨難叫車！加價 150 元求短程接送", "從捷運站接送至社區大樓（約 2 公里），下雨加價求車。") to Pair(0.004, 0.003),
            Triple("REQ_4_2_06", "【需求】預約夜間醉酒代駕/包車 額外小費 300 元", "今晚 23:30 聚會結束，需要代駕或九座車接送 4 位朋友。") to Pair(0.005, 0.004),
            Triple("REQ_4_2_07", "【需求】寵物友善計程車（中型犬）小費 +100 元", "帶黃金獵犬去動物醫院，自備保潔墊，求不拒載寵物的好司機。") to Pair(0.006, 0.005),
            Triple("REQ_4_2_08", "【需求】徵求早高峯離岸共乘（共乘拆帳打 7 折）", "每天早上 07:45 往台北市中心，尋求順路共乘夥伴或折扣司機。") to Pair(0.007, 0.006),
            Triple("REQ_4_2_09", "【需求】微型包車半日遊（4小時）預算 1800 元", "帶長輩北海岸散心，需要耐心司機，含停靠等待時間。") to Pair(0.008, 0.007),
            Triple("REQ_4_2_10", "【需求】高鐵站接送（深夜班次）加 200 元車資", "班機延誤改搭最後一班高鐵，到達後急需接駁回住處。") to Pair(0.010, 0.008)
        ).map { (data, offset) ->
            RequirementCard(
                card_id = data.first,
                publisher_device_id = "SYSTEM_ADMIN",
                created_at = System.currentTimeMillis(),
                category_code = "4-2",
                drawer_type = "1A",
                description = "${data.second}\n${data.third}",
                contact_method = RequirementCard.ContactMethod.PHONE,
                contact_value = "0912333${data.first.takeLast(2)}",
                gps_lat = baseLat + offset.first,
                gps_lng = baseLng + offset.second,
                category_tag = "計程叫車 長照接送",
                ttl_hours = 12,
                is_test_only = true
            )
        } + listOf(
            Triple("SUP_4_2_01", "【供給】個人計程車 長途專案照表打八折！", "2025 年新車 Toyota RAV4，車況極佳無煙味，跨縣市/長途一律 8 折優惠。") to Pair(0.001, -0.001),
            Triple("SUP_4_2_02", "【供給】最新款 Lexus LM500h 旗艦商務車（+300元）", "一年內極新尊榮黑甲商務車，提供免費礦泉水與充電線，機場/商務接送指定款。") to Pair(0.002, -0.001),
            Triple("SUP_4_2_03", "【供給】長照專用福祉車 鋁合金自動升降斜坡板", "配備輪椅四點式安全固定扣，專接診所醫院，合法長照運送服務。") to Pair(0.003, -0.002),
            Triple("SUP_4_2_04", "【供給】回空車特惠！台北往返宜蘭/新竹 降價 300 元", "每天下午定時回空車，提供超值回頭車折扣，歡迎提早預約。") to Pair(0.004, -0.003),
            Triple("SUP_4_2_05", "【供給】雨天/尖峰時段 快速派車專線（指定加價）", "自備多台合作車隊，保證 10 分鐘內到場接送，急用首選。") to Pair(0.004, -0.004),
            Triple("SUP_4_2_06", "【供給】九人座Alphard保姆車 旅遊/商務包車（高優值加價）", "高級皮椅、獨立空調，適合家庭出遊或重要貴賓接送，包車另有折扣。") to Pair(0.005, -0.004),
            Triple("SUP_4_2_07", "【供給】毛小孩專屬友善計程車 免收寵物清潔費", "司機本身也養狗，車內定期消毒，大中小型犬皆歡迎上車。") to Pair(0.006, -0.005),
            Triple("SUP_4_2_08", "【供給】離峰時段市區接送 滿 200 元折 30 元", "平日 10:00-16:00 離峰時段專屬折扣，安全省錢首選。") to Pair(0.007, -0.006),
            Triple("SUP_4_2_09", "【供給】長照爬梯機+福特高頂長照車 專業團隊", "無電梯公寓救星，專業護理級協助人員，安全陪伴上下樓梯與就醫。") to Pair(0.008, -0.007),
            Triple("SUP_4_2_10", "【供給】24H 通宵夜間接送專車 安全婦女/醉酒接送", "優良職業駕駛，全程 GPS 軌道追蹤，深夜叫車安心有保障。") to Pair(0.010, -0.008)
        ).map { (data, offset) ->
            RequirementCard(
                card_id = data.first,
                publisher_device_id = "SYSTEM_ADMIN",
                created_at = System.currentTimeMillis(),
                category_code = "4-2",
                drawer_type = "1B",
                description = "${data.second}\n${data.third}",
                contact_method = RequirementCard.ContactMethod.PHONE,
                contact_value = "0988333${data.first.takeLast(2)}",
                gps_lat = baseLat + offset.first,
                gps_lng = baseLng + offset.second,
                category_tag = "計程叫車 長照接送",
                ttl_hours = 12,
                is_test_only = true
            )
        }

        BaichuanRepository.injectTestCards(relItems)
        val message = "✅ 根據 GPS 座標 ($baseLat, $baseLng) 動態擴散生成 4-2 測試卡片 20 筆（需求 10 筆 + 供給 10 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return relItems
    }

    val TEST_CROSS_DRAWER_JSON = """
    [
      {
        "cardId": "REQ_4_1_01",
        "drawerCode": "4-1",
        "categoryName": "在地攤商 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】急求！今晚 6 點半兩人份健康少油減脂餐",
        "description": "想吃健康舒肥雞胸便當，希望蔬菜多一點，預算 300 元送達。",
        "contactType": "Phone",
        "contactId": "0912004101",
        "lat": 25.003614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "REQ_4_1_02",
        "drawerCode": "4-1",
        "categoryName": "在地攤商 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】徵求私廚訂製「家庭四菜一湯」晚餐",
        "description": "適合 3-4 人吃，家常紅燒肉、炒時蔬，希望 18:30 前外送。",
        "contactType": "LINE",
        "contactId": "private_chef_req02",
        "lat": 25.004614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "REQ_4_1_03",
        "drawerCode": "4-1",
        "categoryName": "在地攤商 個人私廚",
        "type": "REQUIREMENT",
        "title": "【需求】求附近個人工作室的手作純手工蛋黃酥",
        "description": "需要 2 盒（一盒 6 入）作為送禮，希望下午 4 點前送到。",
        "contactType": "Phone",
        "contactId": "0912004103",
        "lat": 25.004614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "SUP_4_1_01",
        "drawerCode": "4-1",
        "categoryName": "在地攤商 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】小林健康私廚 健身高培舒肥雞胸便當",
        "description": "每日新鮮現做，紫米飯搭配五色蔬菜，周邊 1km 滿 2 個免運。",
        "contactType": "Phone",
        "contactId": "0988004101",
        "lat": 25.003614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "SUP_4_1_02",
        "drawerCode": "4-1",
        "categoryName": "在地攤商 個人私廚",
        "type": "SUPPLY",
        "title": "【供給】陳媽媽私房菜 三菜一湯家常溫馨外送包",
        "description": "無味精健康烹調，每天更換菜色，適合上班族與小家庭。",
        "contactType": "LINE",
        "contactId": "mama_kitchen_sup02",
        "lat": 25.004614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "REQ_4_2_01",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】急叫商務車！加 300 元叫車費 桃園機場直送",
        "description": "今天 14:00 出發去桃園機場 T2，行李 3 件，乾淨無煙車加價急求。",
        "contactType": "LINE",
        "contactId": "vip_airport_taxi",
        "lat": 25.003614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "REQ_4_2_02",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】明天 08:30 板橋到台大醫院 徵照表打八折司機",
        "description": "長輩每週定期回診用車，希望司機準時，長期配合照表計費打 8 折。",
        "contactType": "Phone",
        "contactId": "0912333001",
        "lat": 25.004614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "SUP_4_2_01",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】個人計程車 長途專案照表打八折！",
        "description": "2025 年新車 Toyota RAV4，車況極佳無煙味，跨縣市/長途一律 8 折優惠。",
        "contactType": "Phone",
        "contactId": "0988333201",
        "lat": 25.003614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "SUP_4_2_02",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】最新款 Lexus LM500h 旗艦商務車（+300元）",
        "description": "一年內極新尊榮黑甲商務車，提供免費礦泉水與充電線，機場/商務接送指定款。",
        "contactType": "LINE",
        "contactId": "lexus_lm_vip",
        "lat": 25.004614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:15:00Z"
      },
      {
        "cardId": "SUP_4_2_03",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照專用福祉車 鋁合金自動升降斜坡板",
        "description": "配備輪椅四點式安全固定扣，專接診所醫院，合法長照運送服務。",
        "contactType": "Phone",
        "contactId": "0988333203",
        "lat": 25.005614,
        "lng": 121.495660,
        "createdAt": "2026-09-22T23:15:00Z"
      }
    ]
    """.trimIndent()

    /**
     * 載入跨抽屜 10 筆測試卡片 (跨 4-1 與 4-2)
     */
    fun loadCrossDrawerTestCards(): List<RequirementCard> {
        val testCards = parseDrawerJson(TEST_CROSS_DRAWER_JSON)
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 跨抽屜測試卡片 10 筆（4-1 需求 3/供給 2 + 4-2 需求 2/供給 3）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    val TEST_DRAWER_1_1_CAMEL_JSON = """
    [
      {
        "cardId": "REQ_1_1_01",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】急！徵求附近宵夜鹹酥雞代購",
        "description": "想吃夜市鹹酥雞，外送費+100元，希望 30 分鐘內送到。",
        "contactType": "Phone",
        "contactId": "0912000001",
        "lat": 25.003614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_02",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】求代購地瓜球與手搖飲 2 杯",
        "description": "微糖微冰大杯綠茶，跑腿費付 80 元。",
        "contactType": "LINE",
        "contactId": "food_req_02",
        "lat": 25.004614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_03",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】徵求鄰居幫忙外帶巷口牛肉麵",
        "description": "紅燒牛肉麵大碗不加蔥，外送費 50 元。",
        "contactType": "Phone",
        "contactId": "0912000003",
        "lat": 25.004614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_04",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】晚餐想吃夜市滷味 預算 300 元",
        "description": "請幫忙挑選熱門品項，車資/跑腿費另計。",
        "contactType": "LINE",
        "contactId": "night_market_04",
        "lat": 25.005614,
        "lng": 121.499660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_05",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】求代買超商大杯拿鐵 2 杯",
        "description": "下雨不方便出門，希望能送到 1 樓管理室。",
        "contactType": "Phone",
        "contactId": "0912000005",
        "lat": 25.006614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_06",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】徵求早餐代購（明日 07:30）",
        "description": "燒餅油條加蛋，送至社區大樓，給 60 元小費。",
        "contactType": "LINE",
        "contactId": "breakfast_req_06",
        "lat": 25.007614,
        "lng": 121.501660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_07",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】代購排隊名店章魚燒",
        "description": "需要排隊約 15 分鐘，代購費給 100 元。",
        "contactType": "Phone",
        "contactId": "0912000007",
        "lat": 25.008614,
        "lng": 121.502660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_08",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】求買在地傳統豆花 3 碗",
        "description": "花生豆花微糖，跑腿費 70 元。",
        "contactType": "LINE",
        "contactId": "douhua_req_08",
        "lat": 25.009614,
        "lng": 121.503660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_09",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】代買連鎖速食店套餐",
        "description": "送達時現場現金結清，含外送費付 500 元。",
        "contactType": "Phone",
        "contactId": "0912000009",
        "lat": 25.010614,
        "lng": 121.504660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "REQ_1_1_10",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "REQUIREMENT",
        "title": "【需求】求代購夜市藥燉排骨",
        "description": "天冷想喝熱湯，送到巷口，跑腿費 90 元。",
        "contactType": "LINE",
        "contactId": "soup_req_10",
        "lat": 25.012614,
        "lng": 121.505660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_01",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】阿婆現炸脆皮鹹酥雞 滿 200 免費外送",
        "description": "在地經營 20 年，傳統口味胡椒鹽，附近 1 公里免費送。",
        "contactType": "Phone",
        "contactId": "0988000001",
        "lat": 25.003614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_02",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】夜市人氣手工地瓜球 接受預約外送",
        "description": "大包 50 元，外皮超酥脆，周邊 500m 內滿 3 包可送。",
        "contactType": "LINE",
        "contactId": "sweet_potato_balls",
        "lat": 25.004614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_03",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】老字號水餃牛肉麵 專人外送服務",
        "description": "手工現包水餃，湯頭濃郁，滿 300 元親自外送。",
        "contactType": "Phone",
        "contactId": "0988000003",
        "lat": 25.005614,
        "lng": 121.495660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_04",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】深夜秘製冷熱滷味 宵夜外送中",
        "description": "獨門麻辣醬汁，營業至凌晨 1 點，歡迎電話預訂。",
        "contactType": "LINE",
        "contactId": "night_luwei_vendor",
        "lat": 25.006614,
        "lng": 121.494660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_05",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】兼職機車跑腿服務 附近代購代送",
        "description": "自備保溫箱，幫買便當、手搖飲、超商用品，距離可議。",
        "contactType": "Phone",
        "contactId": "0988000005",
        "lat": 25.006614,
        "lng": 121.493660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_06",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】無名古早味蛋餅早餐 支援社區預約",
        "description": "粉漿蛋餅、大腸麵線，提前一天預約可送達樓下。",
        "contactType": "LINE",
        "contactId": "morning_food_express",
        "lat": 25.007614,
        "lng": 121.493660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_07",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】日式章魚燒車 周邊外送接單中",
        "description": "皮酥內軟章魚大塊，滿 5 盒免收外送服務費。",
        "contactType": "Phone",
        "contactId": "0988000007",
        "lat": 25.008614,
        "lng": 121.492660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_08",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】傳統手工黑糖豆花 團購外送",
        "description": "純手工製作無防腐劑，公司行號團購滿 10 碗直送。",
        "contactType": "LINE",
        "contactId": "sweet_bean_curd",
        "lat": 25.009614,
        "lng": 121.491660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_09",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】快腿阿傑 專打短程外送/代購",
        "description": "附近 2 公里內皆可送，速度快保證熱騰騰送到手。",
        "contactType": "Phone",
        "contactId": "0988000009",
        "lat": 25.010614,
        "lng": 121.490660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_10",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】養生藥燉排骨/當歸鴨 宵夜外送",
        "description": "冬天補身首選，附贈沾醬，周邊滿 400 元送達。",
        "contactType": "LINE",
        "contactId": "herbal_soup_supp",
        "lat": 25.012614,
        "lng": 121.489660,
        "createdAt": "2026-09-22T23:30:00Z"
      },
      {
        "cardId": "SUP_1_1_11",
        "drawerCode": "1-1",
        "categoryName": "在地攤商 剛需外送",
        "type": "SUPPLY",
        "title": "【供給】手作鮮奶布丁 焦糖香濃",
        "description": "嚴選小農鮮乳與天然雞蛋製作，綿密滑順，每顆 45 元。",
        "contactType": "Phone",
        "contactId": "0988000011",
        "lat": 25.002500,
        "lng": 121.497200,
        "createdAt": "2026-09-22T23:30:00Z"
      }
    ]
    """.trimIndent()

    /**
     * 載入 1-1 camelCase 格式測試卡片 21 筆
     */
    fun loadDrawer1_1CamelTestCards(): List<RequirementCard> {
        val testCards = parseDrawerJson(TEST_DRAWER_1_1_CAMEL_JSON)
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 抽屜 1-1 camelCase 測試卡片 21 筆（需求 10 筆 + 供給 11 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    val TEST_DRAWER_4_1_ERRAND_CAMEL_JSON = """
    [
      {
        "cardId": "REQ_4_1_01",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】急！代取重要文件送至板橋車站",
        "description": "文件已封口，幫忙拿到板橋車站東門交給接應人員，急件跑腿費 200 元。",
        "contactType": "Phone",
        "contactId": "0912410001",
        "lat": 25.003614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_02",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】徵順風車/跑腿 帶小包裹至中和環球",
        "description": "約 1 公斤小衣物箱，今天 18:00 前送達即可，給 120 元跑腿費。",
        "contactType": "LINE",
        "contactId": "errand_req_02",
        "lat": 25.004614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_03",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】現場手寫託運單與排隊寄件",
        "description": "需至郵局代填託運單並排隊寄寄包裹，跑腿費付 150 元。",
        "contactType": "Phone",
        "contactId": "0912410003",
        "lat": 25.004614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_04",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】順風帶貨！台北車站到中和",
        "description": "如果有剛好要從北車回中和的朋友，幫忙載一個背包，補貼車資 100 元。",
        "contactType": "LINE",
        "contactId": "ride_share_req04",
        "lat": 25.005614,
        "lng": 121.499660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_05",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】急件！代拿鑰匙送到公司",
        "description": "出門忘記帶門禁卡，急需拿備用鑰匙送至鄰近辦公大樓，車資+小費 180 元。",
        "contactType": "Phone",
        "contactId": "0912410005",
        "lat": 25.006614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_06",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】代寫大筆出貨單與標籤貼紙",
        "description": "需要細心者協助寫單與貼標，工時約 1 小時，報酬 250 元。",
        "contactType": "LINE",
        "contactId": "label_write_req06",
        "lat": 25.007614,
        "lng": 121.501660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_07",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】順風包裹！送花束至捷運頂溪站",
        "description": "情人節花束需小心攜帶，運送距離約 2 公里，酬勞 150 元。",
        "contactType": "Phone",
        "contactId": "0912410007",
        "lat": 25.008614,
        "lng": 121.502660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_08",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】代取藥局慢性病處方簽",
        "description": "長輩行動不便，需代拿健保卡至指定藥局領藥送回，小費 100 元。",
        "contactType": "LINE",
        "contactId": "medicine_req_08",
        "lat": 25.009614,
        "lng": 121.503660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_09",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】急送合約用印檔案至事務所",
        "description": "需要在 15:00 前完成起件並送達，距離 1.5 公里，急件費 220 元。",
        "contactType": "Phone",
        "contactId": "0912410009",
        "lat": 25.010614,
        "lng": 121.504660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "REQ_4_1_10",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "REQUIREMENT",
        "title": "【需求】順風帶禮盒至新店區",
        "description": "中秋禮盒兩盒，尋找順路前往新店方向的夥伴，補貼 160 元。",
        "contactType": "LINE",
        "contactId": "gift_req_10",
        "lat": 25.012614,
        "lng": 121.505660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_01",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】機車快遞/即時起件專人服務",
        "description": "配備後箱防雨包，專跑文件、小包裹、急件代取，10 分鐘內可出發。",
        "contactType": "Phone",
        "contactId": "0988410001",
        "lat": 25.003614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_02",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】每日通勤順風車（中和↔信義區）",
        "description": "每天上下班時間可幫帶隨身小包裹或文件，順風價便宜收。",
        "contactType": "LINE",
        "contactId": "commute_express_02",
        "lat": 25.004614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_03",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】代寫託運單/出貨單/寄件代辦",
        "description": "字體端正清晰，熟練各超商與郵局寫單寄件流程，歡迎賣家長期合作。",
        "contactType": "Phone",
        "contactId": "0988410003",
        "lat": 25.005614,
        "lng": 121.495660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_04",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】在地全能跑腿服務（買物/送物/排隊）",
        "description": "周邊 3 公里內皆可服務，誠實可靠，收費公道，歡迎私訊洽詢。",
        "contactType": "LINE",
        "contactId": "local_runner_sup04",
        "lat": 25.006614,
        "lng": 121.494660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_05",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】專車起件 直送雙北指定地點",
        "description": "適合貴重的物品、蛋糕花束等不可傾倒物品專車送達。",
        "contactType": "Phone",
        "contactId": "0988410005",
        "lat": 25.006614,
        "lng": 121.493660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_06",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】順風載貨（板橋↔新莊區）",
        "description": "下午時段往返板新地區，後座可放大型包裹，費用隨意。",
        "contactType": "LINE",
        "contactId": "panchiao_express_06",
        "lat": 25.007614,
        "lng": 121.493660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_07",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】專業代寫快遞單/大量標籤服務",
        "description": "協助電商賣家代填宅配單、手寫卡片，可配合時間到府起件。",
        "contactType": "Phone",
        "contactId": "0988410007",
        "lat": 25.008614,
        "lng": 121.492660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_08",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】銀髮族/行動不便者代購代送跑腿",
        "description": "專人代跑醫院領藥、代買生活用品，貼心送上樓。",
        "contactType": "LINE",
        "contactId": "care_runner_08",
        "lat": 25.009614,
        "lng": 121.491660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_09",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】夜間急件跑腿/起件送貨",
        "description": "營業至凌晨 2 點，宵夜代購、深夜急送文件包裹皆可接單。",
        "contactType": "Phone",
        "contactId": "0988410009",
        "lat": 25.010614,
        "lng": 121.490660,
        "createdAt": "2026-09-22T23:42:00Z"
      },
      {
        "cardId": "SUP_4_1_10",
        "drawerCode": "4-1",
        "categoryName": "起件 跑腿 順風 寫單",
        "type": "SUPPLY",
        "title": "【供給】假日順風車（中和↔桃園）",
        "description": "每週六日固定往返桃園，可承接中型物品或行李順風代運。",
        "contactType": "LINE",
        "contactId": "weekend_shuttle_10",
        "lat": 25.012614,
        "lng": 121.489660,
        "createdAt": "2026-09-22T23:42:00Z"
      }
    ]
    """.trimIndent()

    /**
     * 載入 4-1 跑腿順風寫單 camelCase 格式測試卡片 20 筆
     */
    fun loadDrawer4_1ErrandCamelTestCards(): List<RequirementCard> {
        val testCards = parseDrawerJson(TEST_DRAWER_4_1_ERRAND_CAMEL_JSON)
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 抽屜 4-1 跑腿順風寫單 camelCase 測試卡片 20 筆（需求 10 筆 + 供給 10 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    val TEST_DRAWER_4_2_TAXI_CAMEL_JSON = """
    [
      {
        "cardId": "REQ_4_2_01",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】急叫商務車！暴雨加價 300 元 桃園機場直送",
        "description": "現場大雨難叫車，加價急求尊榮商務車直送 T2，行李 3 件。",
        "contactType": "LINE",
        "contactId": "vip_taxi_req01",
        "lat": 25.003614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_02",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】明日 08:30 板橋至台大醫院 徵照表八折長照車",
        "description": "輪椅長輩定期回診，尋求長期配合司機，照表計費打 8 折。",
        "contactType": "Phone",
        "contactId": "0912420002",
        "lat": 25.004614,
        "lng": 121.498660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_03",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】早高峰共乘！中和至台北信義區",
        "description": "上班時間共乘分擔車資，預計 07:40 出發，徵 1-2 位順路乘客。",
        "contactType": "LINE",
        "contactId": "carpool_req_03",
        "lat": 25.004614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_04",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】深夜醉酒代駕！加價 200 元急會合",
        "description": "熱炒店聚會結束，車輛為自用轎車，急需代駕駕駛送回新莊。",
        "contactType": "Phone",
        "contactId": "0912420004",
        "lat": 25.005614,
        "lng": 121.499660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_05",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】寵物友善計程車！帶黃金獵犬看獸醫",
        "description": "大型犬有提籠，不排斥毛孩的司機請接單，給予額外清潔補貼 100 元。",
        "contactType": "LINE",
        "contactId": "pet_taxi_req05",
        "lat": 25.006614,
        "lng": 121.500660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_06",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】微型包車半日遊！淡水老街懷舊之旅",
        "description": "陪伴兩位長者包車 4 小時，需包含腰靠與乘車輔助，預算 2500 元。",
        "contactType": "Phone",
        "contactId": "0912420006",
        "lat": 25.007614,
        "lng": 121.501660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_07",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】高鐵台中站深夜接送至南投",
        "description": "末班高鐵抵達（23:50），跨縣市包車，願支付熱熱調價溢價 20%。",
        "contactType": "LINE",
        "contactId": "night_hsr_req07",
        "lat": 25.008614,
        "lng": 121.502660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_08",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】福祉車無障礙接送（爬梯機+爬梯輔助）",
        "description": "老舊公寓無電梯，需協助輪椅長者上下樓與就醫接送，車資+服務費計。",
        "contactType": "Phone",
        "contactId": "0912420008",
        "lat": 25.009614,
        "lng": 121.503660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_09",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】跨縣市長途包車！新北至新竹科學園區",
        "description": "商務出差急件用車，要求五年內車齡無菸車，照表計費給予折扣。",
        "contactType": "LINE",
        "contactId": "intercity_req09",
        "lat": 25.010614,
        "lng": 121.504660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "REQ_4_2_10",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "REQUIREMENT",
        "title": "【需求】急叫救護車退場後續復健運送",
        "description": "出院返家接送，需平躺式救護福祉車，含護理輔助陪伴。",
        "contactType": "Phone",
        "contactId": "0912420010",
        "lat": 25.012614,
        "lng": 121.505660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_01",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】個人計程車 長途專案照表打八折！",
        "description": "2025 年新車 Toyota RAV4，車況極佳無煙味，跨縣市/長途一律 8 折優惠。",
        "contactType": "Phone",
        "contactId": "0988420001",
        "lat": 25.003614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_02",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】最新款 Lexus LM500h 旗艦商務車（+300元）",
        "description": "一年內極新尊榮黑甲商務車，提供免費礦泉水與充電線，熱熱調價頂級首選。",
        "contactType": "LINE",
        "contactId": "lexus_lm_vip",
        "lat": 25.004614,
        "lng": 121.496660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_03",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】長照專用福祉車 鋁合金自動升降斜坡板",
        "description": "配備輪椅四點式安全固定扣，專接診所醫院，合法長照專屬司機。",
        "contactType": "Phone",
        "contactId": "0988420003",
        "lat": 25.005614,
        "lng": 121.495660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_04",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】專業深夜酒後代駕團隊（平安送達）",
        "description": "具備良民證與十年駕照，熟悉各式手自排車款，雙人組快速到場。",
        "contactType": "LINE",
        "contactId": "safe_driver_sup04",
        "lat": 25.006614,
        "lng": 121.494660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_05",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】早高峰共乘車隊（中和專線）",
        "description": "固定通勤路線分擔油資，乾淨舒適 9 人座，歡迎長期預約。",
        "contactType": "Phone",
        "contactId": "0988420005",
        "lat": 25.006614,
        "lng": 121.493660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_06",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】寵物專屬友善計程車（不限體型）",
        "description": "車內備有寵物清潔墊與消毒設備，大狗小狗皆可搭乘，不收額外清潔費。",
        "contactType": "LINE",
        "contactId": "pet_friendly_taxi",
        "lat": 25.007614,
        "lng": 121.493660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_07",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】暴雨/尖峰時刻熱熱調價動態派車",
        "description": "系統根據人流自動算價，保障惡劣天候 100% 派得到車，安全優先。",
        "contactType": "Phone",
        "contactId": "0988420007",
        "lat": 25.008614,
        "lng": 121.492660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_08",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】微型包車/一日遊優質司機",
        "description": "提供 4-8 小時彈性包車，行程客製化，適合家庭旅遊與長者出遊。",
        "contactType": "LINE",
        "contactId": "tour_charter_sup08",
        "lat": 25.009614,
        "lng": 121.491660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_09",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】高鐵/台鐵深夜接送專車",
        "description": "專跑深夜班次接送，車內備有暖氣與免費手機充電，安心返家。",
        "contactType": "Phone",
        "contactId": "0988420009",
        "lat": 25.010614,
        "lng": 121.490660,
        "createdAt": "2026-09-22T23:45:00Z"
      },
      {
        "cardId": "SUP_4_2_10",
        "drawerCode": "4-2",
        "categoryName": "計程叫車 長照接送",
        "type": "SUPPLY",
        "title": "【供給】跨縣市長途包車照表折扣",
        "description": "全台跨縣市接送，長期合作享有優惠價，開車平穩不暈車。",
        "contactType": "LINE",
        "contactId": "long_distance_taxi10",
        "lat": 25.012614,
        "lng": 121.489660,
        "createdAt": "2026-09-22T23:45:00Z"
      }
    ]
    """.trimIndent()

    /**
     * 載入 4-2 計程叫車長照接送 camelCase 格式測試卡片 20 筆
     */
    fun loadDrawer4_2TaxiCamelTestCards(): List<RequirementCard> {
        val testCards = parseDrawerJson(TEST_DRAWER_4_2_TAXI_CAMEL_JSON)
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 抽屜 4-2 計程叫車長照接送 camelCase 測試卡片 20 筆（需求 10 筆 + 供給 10 筆）載入成功！"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    /**
     * 載入抽屜 4-2（計程叫車 長照接送）測試卡片至記憶體，並輸出日誌
     */
    fun loadDrawer4_2TestCards(): List<RequirementCard> {
        val testCards = getTestCardsForDrawer42()
        BaichuanRepository.injectTestCards(testCards)
        val message = "✅ 抽屜 4-2（計程叫車 長照接送）需求 20 筆 + 供給 20 筆，共 40 筆測試卡片載入完成！12小時自動清空機制運作中。"
        safeLogI("DrawerBatchTester", message)
        println(message)
        return testCards
    }

    /**
     * 將所有預設測試卡片批次注入 BaichuanRepository
     */
    fun seedRepositoryWithTestCards() {
        loadDrawer1_1TestCards()
        loadDrawer4_1TestCards()
        loadDrawer4_2TestCards()
    }
}

