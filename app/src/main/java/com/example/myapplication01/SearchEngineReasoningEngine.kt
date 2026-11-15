package com.example.myapplication01

import com.example.myapplication01.RequirementCard.ContactMethod

/**
 * 搜尋引擎模擬式情境推理與自動發布引擎 (Search Engine Simulated Contextual Reasoning Engine)
 * 1. 對 12 個大分類、88 個卡片項目進行交叉預設與情境推理內容生成
 * 2. 針對「1. 食」、「2. 衣/物」、「3. 住/行」、「4. 交通出行」、「5. 教育學習」、「6. 休閒娛樂」、「7. 醫療保健」、「8. 二手交換」、「9. 寵物服務」、「10. 專業服務」類別下各 8 個子項目，精準載入各 10 筆「供給」與 10 筆「需求」預設卡片資料
 * 3. 輪流交叉使用四種通訊軟體 (LINE, Telegram, WeChat, Phone) 導引，實現「半本效果」
 * 4. 內建防禦機制 (verifyExecutionGuard)，預防人工手動強行啟動與資料篡改
 */
object SearchEngineReasoningEngine {

    private const val TAG = "SearchEngineReasoning"
    
    // 系統自動執行簽章防禦 token，預防人工手動調用
    const val SYSTEM_AUTO_EXECUTION_TOKEN = "SEARCH_ENGINE_AUTO_TOKEN_88_V12"

    // 四種通訊軟體交叉輪流佇列
    private val contactMethodsQueue = listOf(
        ContactMethod.LINE,
        ContactMethod.TELEGRAM,
        ContactMethod.WECHAT,
        ContactMethod.PHONE
    )

    private fun safeLogI(tag: String, msg: String) {
        try {
            android.util.Log.i(tag, msg)
        } catch (e: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun safeLogW(tag: String, msg: String) {
        try {
            android.util.Log.w(tag, msg)
        } catch (e: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun safeLogE(tag: String, msg: String) {
        try {
            android.util.Log.e(tag, msg)
        } catch (e: Throwable) {
            println("[$tag] $msg")
        }
    }

    /**
     * 防禦機制檢查：驗證是否為系統自動排程發起，拒絕人工未授權手動啟動
     */
    fun verifyExecutionGuard(authToken: String): Boolean {
        if (authToken != SYSTEM_AUTO_EXECUTION_TOKEN) {
            safeLogW(TAG, "🛡️ 防禦機制生效：人工手動啟動請求已被攔截並阻斷 (Token Mismatch)")
            return false
        }
        return true
    }

    /**
     * 搜尋引擎情境推理與自動發布核心入口
     * 針對 1. 食至 10. 專業服務 各 8 個子項目精準生成各 10 筆「供給」與 10 筆「需求」，並完成 88 格完整卡片初始化
     */
    fun generateAndPublish88Cards(
        authToken: String = SYSTEM_AUTO_EXECUTION_TOKEN,
        baseLat: Double = 25.0026,
        baseLng: Double = 121.4977
    ): List<RequirementCard> {

        // 1. 執行防禦機制檢查
        if (!verifyExecutionGuard(authToken)) {
            safeLogE(TAG, "⛔ 存取拒絕：未通過防禦驗證，自動發布終止")
            return emptyList()
        }

        safeLogI(TAG, "🔍 啟動搜尋引擎情境推理與自動發布邏輯... 重構載入 1. 食 至 10. 專業服務 各 8 個子項目各 10 筆供給與 10 筆需求卡片")

        val generatedCards = mutableListOf<RequirementCard>()
        var globalCardIndex = 1

        // ==========================================
        // 一、精準載入「1. 食（餐飲美食）」8 個子項目 (1-1 至 1-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val foodSubItems = listOf(
            "1-1" to "在地攤商 / 個人私廚",
            "1-2" to "火鍋燒肉 / 牛排鐵板",
            "1-3" to "在地小吃 / 便當快餐",
            "1-4" to "熱炒合菜 / 中式餐廳",
            "1-5" to "異國料理 / 日韓泰法",
            "1-6" to "手搖飲料 / 冰品甜湯",
            "1-7" to "早午輕食 / 咖啡甜點",
            "1-8" to "宵夜炸物 / 酒吧滷味"
        )

        val foodDemandTitles = mapOf(
            "1-1" to listOf("尋找巷弄私廚無菜單便當", "求購手作水餃 20 顆", "尋找在地手作蘿蔔糕", "預約私廚手作便當", "求購手工肉乾禮盒", "尋找媽媽手路菜", "預約手作精緻點心", "求購在地小農新鮮蔬果", "尋找手工素食便當", "預約私廚早午餐外送"),
            "1-2" to listOf("求購日式和牛燒肉便當", "尋找麻辣鴛鴦鍋外帶組", "預約鐵板燒雙人套餐", "求購石頭火鍋外帶湯底", "尋找厚切牛排便當", "預約韓式烤肉組合", "求購酸菜白肉鍋外帶", "尋找平價涮涮鍋外帶", "預約炭火燒肉便當", "求購個人精緻小火鍋"),
            "1-3" to listOf("尋找古早味魯肉飯便當", "求購酥炸排骨飯加滷蛋", "尋找雞肉飯便當特餐", "預約台南鍋燒意麵", "求購香菇肉羹麵外帶", "尋找乾拌麵與餛飩湯", "預約招牌蛋炒飯炒麵", "求購入口即化控肉飯", "尋找新鮮蚵仔煎小吃", "預約手工高麗菜水餃"),
            "1-4" to listOf("尋找臺式熱炒百元快炒", "求購宮保雞丁合菜", "尋找特製三杯雞熱炒", "預約下飯五更腸旺合菜", "求購道地客家小炒", "尋找中式聚餐年菜合菜", "預約酸香薑絲大腸", "求購清蒸石斑魚合菜", "尋找經典糖醋排骨", "預約豪華台式海鮮合菜"),
            "1-5" to listOf("尋找正宗日式豚骨拉麵", "求購韓式石鍋拌飯", "尋找酸辣泰式綠咖哩", "預約拿坡里手工披薩", "求購法式奶油可頌早午餐", "尋找越南牛肉河粉", "預約印度咖哩烤餅", "求購多汁美式手作漢堡", "尋找西班牙海鮮飯", "預約日式握壽司便當"),
            "1-6" to listOf("尋找濃醇黑糖珍珠鮮奶", "求購清香四季春青茶", "尋找夏日芒果冰沙", "預約暖心紅豆芋頭甜湯", "求購滑嫩仙草凍奶茶", "尋找古早味黑糖刨冰", "預約手搖鮮榨水果茶", "求購香濃芋頭西米露", "尋找手洗野生檸檬愛玉", "預約暖胃手工燒仙草"),
            "1-7" to listOf("尋找燻雞起司熱壓三明治", "求購精品手沖拿鐵咖啡", "尋找健康水果沙拉餐盒", "預約手作抹醬貝果早午餐", "求購經典法式吐司", "尋找千層蛋糕下午茶", "預約希臘優格輕食杯", "求購手作香濃肉桂捲", "尋找義式帕尼尼吐司", "預約英式手工司康"),
            "1-8" to listOf("尋找金黃酥脆鹹酥雞", "求購獨家醬燒炭烤雞排", "尋找深夜招牌滷味拼盤", "預約日式居酒屋串燒", "求購美式酥炸炸雞桶", "尋找麻辣鴨血臭豆腐", "預約精釀啤酒下酒菜", "求購現炸薯條洋蔥圈", "尋找炭烤新鮮大魷魚", "預約深夜現烤肉串組合")
        )

        val foodSupplyTitles = mapOf(
            "1-1" to listOf("阿婆古早味蔥油餅現點現炸", "媽媽私廚無菜單家常便飯", "巷弄手工地瓜球大包", "深夜港式點心攤燒賣", "阿公麻糬現點現做花生", "老牌碳烤香腸米腸組", "手作巴斯克乳酪蛋糕", "手作古早味碗粿", "手作紅燒牛肉麵", "鮮榨新鮮柳橙汁 700cc"),
            "1-2" to listOf("極品 A5 和牛燒肉便當", "川味麻辣鴛鴦鍋外帶組", "頂級菲力鐵板燒套餐", "道地東北酸菜白肉鍋", "厚切沙朗牛排便當", "韓式三層肉烤肉組合", "個人精緻小火鍋外帶", "海陸雙拼鐵板燒套餐", "特製香辣麻辣小火鍋", "炭火現烤安格斯牛小排飯"),
            "1-3" to listOf("金牌古早味魯肉飯便當", "酥炸排骨飯特餐現炸", "嘉義正宗火雞肉飯便當", "台南特製鍋燒意麵", "香菇肉羹麵現做熱騰騰", "招牌鮮蝦餛飩湯", "大份量蛋炒飯加蛋", "入口即化特製控肉飯", "新鮮蚵仔煎現煎美味", "手工高麗菜豬肉水餃"),
            "1-4" to listOf("招牌百元熱炒現點現做", "香辣宮保雞丁中式合菜", "特製三杯雞快炒料理", "下飯五更腸旺特餐", "道地客家小炒風味餐", "精選中式聚餐澎湃合菜", "酸香薑絲大腸快炒", "新鮮清蒸石斑魚合菜", "經典糖醋排骨料理", "豪華台式海鮮總匯合菜"),
            "1-5" to listOf("濃郁豚骨日式拉麵", "熱騰騰韓式石鍋拌飯", "酸辣夠勁泰式綠咖哩", "拿坡里手工窯烤披薩", "法式奶油可頌早午餐", "清香越南牛肉河粉", "道地印度烤餅與咖哩", "多汁美式手作漢堡薯條", "西班牙番紅花海鮮飯", "新鮮握壽司精緻便當"),
            "1-6" to listOf("濃醇黑糖珍珠鮮奶", "清香回甘四季春青茶", "夏日限定芒果冰沙", "暖心紅豆芋頭甜湯", "滑嫩仙草凍奶茶", "傳統古早味黑糖刨冰", "鮮榨清涼水果茶", "香濃芋頭西米露甜品", "手洗野生檸檬愛玉", "暖胃手工燒仙草"),
            "1-7" to listOf("燻雞起司熱壓三明治", "精品手沖拿鐵咖啡", "健康低卡水果沙拉餐盒", "手作抹醬貝果早午餐", "經典法式吐司早午餐", "千層蛋糕下午茶組合", "希臘優格輕食杯", "手作香濃肉桂捲", "義式帕尼尼吐司", "英式手工司康點心"),
            "1-8" to listOf("金黃酥脆鹹酥雞加九層塔", "獨家醬燒炭烤雞排", "入味深夜招牌滷味拼盤", "日式炭火居酒屋串燒", "美式酥炸炸雞桶", "麻辣鴨血臭豆腐", "精釀啤酒與美味下酒菜", "現炸黃金薯條洋蔥圈", "炭烤新鮮大魷魚", "深夜現烤肉串綜合組合")
        )

        for ((subCode, subName) in foodSubItems) {
            val demandList = foodDemandTitles[subCode] ?: emptyList()
            val supplyList = foodSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("food", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "FOOD_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 透過搜尋引擎情境智能匹配，急需優質餐飲服務，預算合理，歡迎與我聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("food", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "FOOD_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 在地嚴選熱門商家，食材新鮮、品質保證，歡迎隨時預約或直接聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 二、精準載入「2. 衣/物（生活百貨）」8 個子項目 (2-1 至 2-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val clothingSubItems = listOf(
            "2-1" to "潮流服飾",
            "2-2" to "飾品配件",
            "2-3" to "鞋類精品",
            "2-4" to "運動戶外",
            "2-5" to "內衣睡衣",
            "2-6" to "洗滌二手",
            "2-7" to "童裝孕婦",
            "2-8" to "綜合其它"
        )

        val clothingDemandTitles = mapOf(
            "2-1" to listOf("尋找當季潮流寬鬆帽T", "求購美式復古短T", "尋找設計款工裝褲", "預約質感針織外套", "求購韓系 Oversize 襯衫", "尋找防風機能外套", "預約潮流連帽夾克", "求購素色百搭大學T", "尋找美式街頭短褲", "預約秋冬羊毛大衣"),
            "2-2" to listOf("尋找純銀簡約項鍊", "求購鈦鋼手環配件", "尋找復古金屬戒指", "預約防紫外線太陽眼鏡", "求購韓系髮夾髮帶", "尋找真皮質感皮帶", "預約潮流棒球帽", "求購純銀耳環耳釘", "尋找簡約帆布托特包", "預約精緻鑰匙圈"),
            "2-3" to listOf("尋找經典復古慢跑鞋", "求購潮流滑板鞋", "尋找輕量透氣運動鞋", "預約真皮休閒皮鞋", "求購增高厚底帆布鞋", "尋找防水登山靴", "預約舒適懶人便鞋", "求購保暖雪地靴", "尋找時尚短筒馬靴", "預約透氣網面跑鞋"),
            "2-4" to listOf("尋找專業吸濕排汗運動衫", "求購高彈力瑜珈褲", "尋找戶外防風防水衝鋒衣", "預約輕量登山健行背包", "求購速乾運動短褲", "尋找露營摺疊椅", "預約專業跑步腰包", "求購防滑運動水壺", "尋找輕量露營帳篷", "預約戶外遮陽漁夫帽"),
            "2-5" to listOf("尋找無鋼圈舒適內衣", "求購純棉透氣內褲", "尋找絲滑居家睡衣套裝", "預約無痕親膚內衣", "求購純棉居家休閒褲", "尋找性感蕾絲睡裙", "預約吸濕排汗背心", "求購保暖發熱衣", "尋找純棉浴袍", "預約記憶棉居家拖鞋"),
            "2-6" to listOf("尋找二手九成新羽絨衣", "求購平價洗滌衣物籃", "尋找二手精品皮夾", "預約專業衣物乾洗服務", "求購二手品牌牛仔褲", "尋找環保洗衣精補充包", "預約二手衣物回收箱", "求購二手休閒背包", "尋找衣物防塵套", "預約洗鞋清潔服務"),
            "2-7" to listOf("尋找純棉寶寶包屁衣", "求購孕婦托腹褲", "尋找兒童防踢睡袋", "預約哺乳期友善洋裝", "求購童裝休閒套裝", "尋找孕婦哺乳內衣", "預約兒童防滑學步鞋", "求購孕婦防輻射衣", "尋找可愛造型童帽", "預約純棉寶寶浴巾"),
            "2-8" to listOf("尋找多功能收納箱", "求購行李箱束帶", "尋找防塵鞋盒", "預約雨具雨傘", "求購衣架曬衣夾", "尋找旅行過夜包", "預約縫衣線針組", "求購隨身鏡子", "尋找防蚊貼片", "預約綜合生活雜物")
        )

        val clothingSupplyTitles = mapOf(
            "2-1" to listOf("歐美潮流寬鬆帽T現貨", "美式復古重磅短T", "日系質感工裝長褲", "韓系秋冬針織毛衣外套", "Oversize 百搭襯衫", "防風防水機能外套", "潮流個性連帽夾克", "純棉素面大學T", "街頭風休閒短褲", "精選保暖羊毛大衣"),
            "2-2" to listOf("925 純銀簡約項鍊", "抗過敏鈦鋼手環", "復古質感金屬戒指", "抗 UV 潮流太陽眼鏡", "韓系珍珠髮夾髮帶", "頭層真皮質感皮帶", "刺繡潮流棒球帽", "純銀抗敏耳環耳釘", "大容量簡約帆布包", "精緻金屬鑰匙圈"),
            "2-3" to listOf("經典復古潮流慢跑鞋", "街頭滑板休閒鞋", "輕量透氣專業運動鞋", "英倫真皮休閒皮鞋", "增高厚底帆布鞋", "戶外防水登山靴", "一腳蹬舒適懶人鞋", "冬季保暖雪地短靴", "時尚皮革短筒馬靴", "透氣網面跑步鞋"),
            "2-4" to listOf("專業吸濕排汗運動上衣", "高彈力透氣瑜珈緊身褲", "戶外防風防水衝鋒外套", "輕量多功能登山背包", "透氣速乾運動短褲", "便攜露營摺疊椅", "反光專業跑步腰包", "大容量防滑運動水壺", "輕量雙人露營帳篷", "戶外防曬遮陽漁夫帽"),
            "2-5" to listOf("無鋼圈極致舒適內衣", "純棉親膚透氣內褲", "絲滑柔軟居家睡衣組", "無痕無感親膚內衣", "純棉寬鬆居家休閒褲", "浪漫蕾絲居家睡衣裙", "吸濕排汗透氣背心", "秋冬保暖發熱衣褲", "吸水純棉居家浴袍", "柔軟記憶棉拖鞋"),
            "2-6" to listOf("二手九成新保暖羽絨外套", "多功能家用洗衣籃", "二手質感真皮短夾", "專業精緻衣物乾洗服務", "二手日系品牌牛仔褲", "環保濃縮洗衣精", "二手衣物收納箱", "二手輕量休閒雙肩包", "透明衣物防塵套", "專業球鞋洗滌護理"),
            "2-7" to listOf("純棉舒適寶寶包屁衣", "透氣舒適孕婦托腹褲", "四季通用兒童防踢被", "哺乳友善孕婦連身裙", "小童純棉休閒套裝", "孕婦專用無鋼圈哺乳內衣", "兒童透氣防滑學步鞋", "孕婦專用防輻射服", "可愛動物造型童帽", "純棉吸水寶寶浴巾"),
            "2-8" to listOf("多功能透明衣物收納箱", "行李箱固定束帶", "透明防塵疊加鞋盒", "抗風堅固自動雨傘", "防風衣架與曬衣夾", "輕便旅行過夜盥洗包", "家用縫衣線針組盒", "隨身攜帶折疊化妝鏡", "天然植物精油防蚊貼", "精選生活日常雜物")
        )

        for ((subCode, subName) in clothingSubItems) {
            val demandList = clothingDemandTitles[subCode] ?: emptyList()
            val supplyList = clothingSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("cloth", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "CLOTH_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質生活百貨與潮流服飾，品質優良，誠心求購。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("cloth", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "CLOTH_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 精選生活百貨與服飾配件，現貨供應，品質保證，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 三、精準載入「3. 住/行（跑腿修繕）」8 個子項目 (3-1 至 3-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val housingSubItems = listOf(
            "3-1" to "租屋資訊",
            "3-2" to "裝潢修繕",
            "3-3" to "房產買賣",
            "3-4" to "水電鎖印",
            "3-5" to "清潔家政",
            "3-6" to "搬家儲藏",
            "3-7" to "家具家電",
            "3-8" to "綜合其它"
        )

        val housingDemandTitles = mapOf(
            "3-1" to listOf("尋找獨立套房近捷運", "求購合租雅房室友", "尋找帶電梯兩房一廳", "預約短期租屋三個月", "求購寵物友善租屋", "尋找商辦工作室租賃", "預約整層住家出租", "求購學生優惠套房", "尋找車位出租", "預約頂樓加蓋雅房"),
            "3-2" to listOf("尋找室內木工裝潢統包", "求購老屋翻新設計", "尋找浴室防水工程", "預約油漆粉刷施工", "求購系統櫃訂製安裝", "尋找壁癌處理工程", "預約裝潢拆除清運", "求購木地板鋪設", "尋找陽台鐵窗安裝", "預約輕隔間工程"),
            "3-3" to listOf("尋找三房兩廳電梯大樓", "求購商用店面置產", "尋找中古公寓首購", "預約重劃區透天別墅", "求購景觀華廈", "尋找投資收租套房", "預約預售屋轉讓", "求購工業廠房用地", "尋找農地農舍買賣", "預約法拍屋代標諮詢"),
            "3-4" to listOf("尋找緊急馬桶疏通", "求購門鎖更換開鎖", "尋找電燈開關維修", "預約電熱水器安裝", "求購水管漏水維修", "尋找加壓馬達安裝", "預約電子密碼鎖安裝", "求購跳電檢修服務", "尋找抽油煙機修理", "預約綜合水電維修"),
            "3-5" to listOf("尋找全室深度大掃除", "求購冷氣清洗保養", "尋找洗衣機拆洗清潔", "預約居家定期打掃", "求購辦公室清潔維護", "尋找裝潢後細部清潔", "預約除塵蟎沙發清洗", "求購地板打蠟拋光", "尋找水塔清洗服務", "預約退租徹底清潔"),
            "3-6" to listOf("尋找精緻個人小資搬家", "求購大型家具搬運", "尋找長途跨縣市搬家", "預約迷你倉儲出租", "求購工廠企業搬遷", "尋找學生宿舍搬家", "預約廢棄家具清運", "求購鋼琴專業搬運", "尋找自助租車搬家", "預約打包裝箱服務"),
            "3-7" to listOf("尋找二手雙人彈簧床墊", "求購北歐風布藝沙發", "尋找二手中小型冰箱", "預約變頻冷氣機買賣", "求購實木餐桌椅組", "尋找滾筒洗衣機買賣", "預約電視櫃茶几組", "求購二手液晶電視", "尋找衣櫃收納櫃", "預約二手微波爐烤箱"),
            "3-8" to listOf("尋找房屋漏水鑑定", "求購居家風水諮詢", "尋找社區公設維修", "預約郵件代收服務", "求購鄰里互助幫忙", "尋找社區團購團主", "預約居家安全檢測", "求購房屋合約審閱", "尋找生活雜物代買", "預約綜合住行諮詢")
        )

        val housingSupplyTitles = mapOf(
            "3-1" to listOf("近捷運站溫馨獨立套房", "優質雅房尋求好室友", "全新電梯兩房一廳", "彈性短期商務租屋", "可養寵物友善溫馨套房", "市中心商辦工作室出租", "採光極佳整層住家出租", "學生專屬優惠省錢套房", "平面汽車停車位出租", "高樓層景觀頂樓套房"),
            "3-2" to listOf("專業室內木工裝潢統包", "老屋全室翻新設計規劃", "浴室防水抓漏工程", "環保無毒油漆粉刷", "歐式系統櫃量身訂製", "專業牆面壁癌徹底根治", "室內裝潢拆除清運", "超耐磨木地板專業鋪設", "白鐵防盜陽台鐵窗安裝", "辦公室輕隔間工程"),
            "3-3" to listOf("市中心三房兩廳電梯大樓", "精華商圈黃金店面置產", "低總價中古公寓首購首選", "重劃區全新透天別墅", "高樓層視野景觀華廈", "穩定收租高投報套房", "熱門預售屋合約轉讓", "大坪數工業廠房土地", "郊區休閒農地農舍買賣", "專業法拍屋代標與諮詢"),
            "3-4" to listOf("24小時緊急馬桶水管疏通", "專業住宅門鎖更換開鎖", "老舊電燈開關插座維修", "儲熱式電熱水器專業安裝", "浴室廚房水管漏水維修", "加壓馬達幫浦安裝修理", "智慧電子密碼鎖到府安裝", "居家無預警跳電專業檢修", "排油煙機與瓦斯爐修理", "居家綜合水電大小維修"),
            "3-5" to listOf("專業全室深度大打掃服務", "冷氣機分體式清洗保養", "直立/滾筒洗衣機拆洗清潔", "居家定期鐘點家政清潔", "商辦大樓定期清潔維護", "新屋裝潢後細部細緻清潔", "高溫除塵蟎與沙發床墊清洗", "木質地板打蠟與拋光護理", "住宅水塔清洗與殺菌", "租屋退租交屋徹底清潔"),
            "3-6" to listOf("經濟實惠精緻小資搬家", "大型沙發床墊專業搬運", "長途跨縣市安心搬家服務", "24小時安全迷你倉儲出租", "工廠企業與辦公室大搬遷", "學生開學宿舍專屬搬家", "大型廢棄家具清運回收", "平台鋼琴與重物專業搬運", "自助貨車租賃與搬家", "專業物品分類打包裝箱"),
            "3-7" to listOf("9成新二手雙人彈簧床墊", "時尚北歐風舒適布藝沙發", "二手中小型雙門省電冰箱", "一級能效變頻冷氣機販售", "實木質感餐桌椅組合", "日系滾筒洗脫烘洗衣機", "簡約電視櫃與茶几套組", "二手高畫質液晶電視", "多功能木質衣櫃收納櫃", "二手微波爐與小烤箱"),
            "3-8" to listOf("專業房屋漏水原因鑑定", "居家風水格局專業諮詢", "社區大樓公設定期維修", "信件包裹代收與管理", "鄰里互助與緊急支援", "社區好物團購主揪服務", "居家安全與消防檢測", "房屋租賃買賣合約審閱", "日常生活雜物代買代辦", "綜合住行疑難雜症諮詢")
        )

        for ((subCode, subName) in housingSubItems) {
            val demandList = housingDemandTitles[subCode] ?: emptyList()
            val supplyList = housingSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("house", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "HOUSE_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質住屋、修繕與居家生活服務，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("house", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "HOUSE_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業住屋租賃、裝潢修繕與居家清潔服務，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 四、精準載入「4. 交通出行」8 個子項目 (4-1 至 4-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val transportSubItems = listOf(
            "4-1" to "機車跑腿 / 急件順風",
            "4-2" to "貨車搬運 / 載貨接單",
            "4-3" to "計程車叫車 / 長距接送",
            "4-4" to "機車維修 / 保養洗車",
            "4-5" to "汽車修護 / 美容輪胎",
            "4-6" to "租車服務 / 共享出行",
            "4-7" to "拖吊救援 / 代駕服務",
            "4-8" to "綜合其它 / 出行站點"
        )

        val transportDemandTitles = mapOf(
            "4-1" to listOf("急件公司文件幫送", "代買排隊人氣美食", "遺忘物品順風帶回", "幫送便當到學校", "急拿鑰匙送到捷運", "代取處方箋藥包", "順路送生日花束", "代取超商貨件上樓", "深夜護照急送到車站", "代買五金螺絲材料"),
            "4-2" to listOf("尋找 3.5 噸貨車搬家", "求購大型貨物載運", "尋找商行批發送貨", "預約家具家電運送", "求購建材載送服務", "尋找活動器材搬運", "預約跨縣市貨運", "求購展覽展位搬遷", "尋找工廠貨品運送", "預約大型機器搬運"),
            "4-3" to listOf("尋找機場接送專車", "求購跨縣市長途包車", "尋找商務行程接送", "預約醫院回診接送", "求購夜間急用計程車", "尋找包車一日遊", "預約孕婦產檢專車", "求購多人小巴接送", "尋找寵物友善計程車", "預約無障礙福祉車"),
            "4-4" to listOf("尋找機車定期保養換機油", "求購機車道路救援補胎", "尋找機車精緻洗車", "預約機車輪胎更換", "求購機車傳動系統修理", "尋找機車煞車皮更換", "預約機車電瓶檢測更換", "求購機車排氣管維修", "尋找機車大保養套餐", "預約機車改裝精品安裝"),
            "4-5" to listOf("尋找汽車定期保養廠", "求購汽車精緻美容鍍膜", "尋找汽車輪胎定位對調", "預約汽車冷氣維修保養", "求購汽車引擎故障檢修", "尋找汽車板金烤漆", "預約汽車玻璃修補", "求購汽車電瓶更換", "尋找汽車煞車系統升級", "預約汽車大燈拋光"),
            "4-6" to listOf("尋找短租自駕汽車", "求購長期租車優惠", "尋找商務七人座租車", "預約機車日租服務", "求購電動機車共享", "尋找結婚禮車出租", "預約露營車租賃", "求購保姆車包車租賃", "尋找跑車體驗租車", "預約貨車自助租賃"),
            "4-7" to listOf("尋找高速公路道路救援", "求購深夜酒後代駕", "尋找車輛沒電救援搭電", "預約爆胎更換備胎", "求購地下室拖吊救援", "尋找汽車困沙坑救援", "預約長途車輛託運", "求購事故車輛拖吊", "尋找鑰匙反鎖開車門", "預約緊急送油服務"),
            "4-8" to listOf("尋找共享單車停放點", "求購電動車充電樁查詢", "尋找停車場月租車位", "預約長途客運訂票", "求購共乘資訊詢問", "尋找交通卡加值點", "預約接駁車時刻查詢", "求購加油站位置諮詢", "尋找出行路線規劃", "預約綜合交通諮詢")
        )

        val transportSupplyTitles = mapOf(
            "4-1" to listOf("專業機車跑腿全區急件", "中和往三重順風車", "24小時夜間機車跑腿", "代排隊代送件老司機", "冷藏溫控保鮮箱速送", "新莊往板橋順風快遞", "重機長途超快速直送", "午餐時段團訂便當配送", "急件文件專人專送", "彈性跑腿代辦服務"),
            "4-2" to listOf("3.5 噸箱式貨車專業搬運", "大型貨物與建材載送", "商行批發與零售送貨", "家具家電專車配送", "活動器材與展場搬運", "跨縣市安心長途貨運", "工廠貨品與原物料運送", "大型精密機械搬運", "小額載貨與跑腿接單", "貨車隨車搬運助手"),
            "4-3" to listOf("24小時機場專車接送", "跨縣市長途安心包車", "商務行程專屬接送", "醫院回診與長照接送", "夜間安心計程車叫車", "觀光旅遊包車一日遊", "孕婦產檢專屬接送", "多人團體小巴接送", "寵物友善專車接送", "無障礙福祉車接送"),
            "4-4" to listOf("機車定期保養與更換機油", "機車道路救援與現場補胎", "機車精緻手工洗車打蠟", "機車正新/馬吉斯輪胎更換", "機車傳動皮帶普利珠維修", "機車煞車皮與碟盤更換", "機車電瓶檢測與新品更換", "機車排氣管焊接與維修", "機車 20000km 大保養", "機車燈具與改裝精品安裝"),
            "4-5" to listOf("專業汽車定期保養與換油", "汽車精緻美容與陶瓷鍍膜", "四輪定位與高性能輪胎", "汽車冷氣系統檢修保養", "引擎電腦診斷與故障排除", "汽車原廠級板金烤漆", "前擋風玻璃裂痕修補", "汽車免加水電瓶到府更換", "高性能煞車系統升級", "汽車氧化大燈拋光還原"),
            "4-6" to listOf("經濟實惠短租自駕汽車", "長期租車優惠方案", "商務出差七人座休旅車", "機車日租與週租方案", "電動機車便利租賃", "豪華婚禮禮車車隊出租", "戶外休閒露營車租賃", "明星保姆車包車租賃", "超跑與性能車體驗租車", "貨車自助租賃搬家"),
            "4-7" to listOf("24小時高速公路道路救援", "專業安全深夜酒後代駕", "汽車沒電現場搭電救援", "爆胎現場更換備胎服務", "地下室低底盤拖吊救援", "困泥巴坑/沙坑脫困救援", "長途車輛安全託運服務", "事故車輛專業拖吊處理", "車門反鎖專業無損開鎖", "緊急送汽油/柴油服務"),
            "4-8" to listOf("社區共享單車站點維護", "電動車充電樁位置指引", "室內平面停車場月租車位", "長途客運時刻與訂票諮詢", "跨區長途共乘資訊配對", "悠遊卡/一卡通加值點引導", "接駁專車時刻表與查詢", "鄰近加油站與中油直營點", "智能出行路線規劃指引", "綜合交通出行疑難解答")
        )

        for ((subCode, subName) in transportSubItems) {
            val demandList = transportDemandTitles[subCode] ?: emptyList()
            val supplyList = transportSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("trans", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "TRANS_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質交通出行與車輛服務，安全可靠，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("trans", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "TRANS_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業交通接送、車輛維修與出行服務，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 五、精準載入「5. 教育學習」8 個子項目 (5-1 至 5-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val educationSubItems = listOf(
            "5-1" to "幼兒託管 / 保母家教",
            "5-2" to "才藝培訓 / 音樂美術",
            "5-3" to "補習輔導 / 升學檢定",
            "5-4" to "運動健身 / 私人教練",
            "5-5" to "語言學習 / 留學檢定",
            "5-6" to "駕訓考照 / 汽機車班",
            "5-7" to "職業技能 / 證照培訓",
            "5-8" to "綜合其它 / 學習資訊"
        )

        val educationDemandTitles = mapOf(
            "5-1" to listOf("尋找專業到府保母", "求購幼兒全日託管", "尋找國小課後陪伴保母", "預約臨托育兒服務", "求購嬰幼兒啟蒙家教", "尋找假日短期保母", "預約雙胞胎育兒幫手", "求購夜間安心托育", "尋找幼兒園接送保母", "預約到府陪玩姊姊"),
            "5-2" to listOf("尋找兒童鋼琴家教", "求購小提琴啟蒙教學", "尋找兒童美術繪畫班", "預約吉他烏克麗麗教學", "求購少兒舞蹈律動課", "尋找書法水墨畫教學", "預約陶藝手作培訓", "求購長笛管樂家教", "尋找流行歌唱訓練", "預約兒童積木創客課"),
            "5-3" to listOf("尋找國中數學理化家教", "求購高中英文文法補習", "尋找國小全科伴讀家教", "預約學測指考衝刺班", "求購國中會考總複習", "尋找高職統測升學輔導", "預約私中入學考試特訓", "求購自然科學實驗班", "尋找作文閱讀理解課", "預約全科一對一輔導"),
            "5-4" to listOf("尋找一對一私人健身教練", "求購產後體態恢復訓練", "尋找兒童體能專長訓練", "預約瑜珈皮拉提斯課程", "求購核心肌群強化教練", "尋找減脂增肌指導員", "預約TRX懸吊訓練課", "求購拳擊有氧教練", "尋找銀髮族健康體適能", "預約戶外路跑訓練指導"),
            "5-5" to listOf("尋找外籍母語英文家教", "求購日語檢定 N1/N2 輔導", "尋找韓語會話基礎班", "預約雅思 IELTS 衝刺班", "求購多益 TOEIC 高分班", "尋找法語德語歐語家教", "預約留學文件潤飾代辦", "求購兒童美語互動班", "尋找商務英語會話課", "預約中文對外華語家教"),
            "5-6" to listOf("尋找汽車駕訓班道路實戰", "求購機車考照模擬訓練", "尋找手排車駕照保證班", "預約道路安駕駕駛培訓", "求購女子防禦駕駛特訓", "尋找大型重機考照班", "預約高速公路駕駛特訓", "求購夜間駕駛技巧指導", "尋找高齡換照認知測驗", "預約汽車路邊停車特訓"),
            "5-7" to listOf("尋找 Python 程式語言家教", "求購 AI 應用與數據分析課", "尋找室內設計軟體培訓", "預約烘焙乙丙級證照班", "求購中餐廚藝證照特訓", "尋找數位行銷廣告投放", "預約保母人員技術士班", "求購美甲美睫創業班", "尋找財務會計實務班", "預約照顧服務員訓練"),
            "5-8" to listOf("尋找升學資訊諮詢顧問", "求購讀書會夥伴招募", "尋找圖書館自修室揪團", "預約生涯規劃諮詢師", "求購二手教科書買賣", "尋找線上課程心得交流", "預約教育補助諮詢", "求購論文文獻檢索幫手", "尋找學習筆記交換", "預約綜合學習諮詢")
        )

        val educationSupplyTitles = mapOf(
            "5-1" to listOf("專業到府保母與育兒指導", "合格證照全日幼兒託管", "國小放學後課後陪伴保母", "彈性預約臨托育兒服務", "嬰幼兒早期啟蒙教導", "假日彈性短期保母支援", "雙胞胎與多胞胎育兒幫手", "夜間安心過夜托育服務", "幼兒園上下學安心接送", "經驗豐富到府陪玩姊姊"),
            "5-2" to listOf("專業兒童鋼琴一對一家教", "小提琴啟蒙與基礎教學", "兒童創意美術與繪畫班", "吉他與烏克麗麗彈唱教學", "少兒律動與肢體開發課", "傳統書法與水墨畫教學", "陶藝創作與手作培訓", "長笛與管樂專業家教", "流行歌唱技巧發聲訓練", "兒童積木創客與邏輯課"),
            "5-3" to listOf("國中數學理化一對一家教", "高中英文文法與閱讀特訓", "國小全科課業伴讀家教", "大學學測與指考衝刺輔導", "國中會考總複習衝刺班", "高職統測專業科目升學輔導", "私中入學考試特訓班", "少兒自然科學實驗班", "國高中作文與閱讀理解", "全方位全科一對一家教"),
            "5-4" to listOf("專業一對一私人健身教練", "產後媽媽體態恢復訓練", "兒童感覺統合與體能訓練", "專業瑜珈與皮拉提斯教學", "核心肌群強化與體態雕塑", "科學化減脂增肌指導", "TRX懸吊與功能性訓練", "燃脂拳擊有氧教練課", "銀髮族健康體適能指導", "戶外專業路跑訓練指導"),
            "5-5" to listOf("專業外籍母語英文家教", "日語檢定 N1/N2 衝刺輔導", "韓語基礎會話與正音班", "雅思 IELTS 應試衝刺班", "多益 TOEIC 高分保證班", "法德西歐語系專業家教", "留學申請文件潤飾與面試", "兒童互動美語啟蒙班", "職場商務英語會話訓練", "對外華語文教學專業家教"),
            "5-6" to listOf("汽車駕訓班教練道路實戰", "機車考照筆試與場考模擬", "手排車駕照考照保證班", "道路安全駕駛與防禦駕駛", "女性專屬防禦駕駛特訓", "大型重機駕訓考照輔導", "高速公路與山路駕駛特訓", "夜間與雨天駕駛技巧指導", "高齡駕駛換照認知測驗輔導", "汽車路邊停車與倒車入庫"),
            "5-7" to listOf("Python 程式設計一對一家教", "AI 生成式工具與數據分析", "AutoCAD/3D 空間設計培訓", "烘焙西點乙丙級證照班", "中餐葷素廚藝證照特訓", "數位行銷與 FB/Google 廣告", "國家保母人員技術士培訓", "美甲美睫創業全科班", "財務會計與稅務實務培訓", "照顧服務員結業證書班"),
            "5-8" to listOf("升學與志願選填諮詢顧問", "K書中心讀書會夥伴招募", "圖書館自修與專注揪團", "個人生涯規劃與職涯諮詢", "二手升學教科書與講義", "線上課程學習心得交流", "政府教育補助與獎學金諮詢", "學術論文文獻檢索與排版", "高效學習筆記與方法交換", "綜合教育學習疑難解答")
        )

        for ((subCode, subName) in educationSubItems) {
            val demandList = educationDemandTitles[subCode] ?: emptyList()
            val supplyList = educationSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("edu", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "EDU_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質教育學習與家教培訓服務，認真負責，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("edu", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "EDU_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業教育學習、才藝培訓與補習輔導服務，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 六、精準載入「6. 休閒娛樂」8 個子項目 (6-1 至 6-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val entertainmentSubItems = listOf(
            "6-1" to "KTV 視聽 / 酒吧夜生活",
            "6-2" to "影城劇院 / 展覽演出",
            "6-3" to "按摩 SPA / 美容美體",
            "6-4" to "網咖電競 / 桌遊密室",
            "6-5" to "親子樂園 / 戶外休閒",
            "6-6" to "美甲美睫 / 美髮理容",
            "6-7" to "運動場館 / 保齡球館",
            "6-8" to "綜合其它 / 休閒票券"
        )

        val entertainmentDemandTitles = mapOf(
            "6-1" to listOf("尋找週末 KTV 歡唱包廂", "求購精釀啤酒吧訂位", "尋找深夜音樂酒吧", "預約私人影音視聽室", "求購暢飲派對包廂", "尋找特色調酒酒吧", "預約網紅打卡餐酒館", "求購 Live House 演出門票", "尋找朋友聚會包廂", "預約深夜爵士酒吧"),
            "6-2" to listOf("尋找 IMAX 電影票代訂", "求購熱門舞台劇門票", "尋找藝術特展參觀券", "預約演唱會搖滾區門票", "求購音樂會演奏會門票", "尋找喜劇脫口秀門票", "預約電影包廳服務", "求購文創展覽早鳥票", "尋找沉浸式展覽門票", "預約魔術表演秀門票"),
            "6-3" to listOf("尋找全身經絡指壓按摩", "求購泰式古法按摩", "尋找精油芳療 SPA", "預約足底按摩泡腳", "求購臉部保養美體", "尋找頭皮舒壓按摩", "預約孕婦舒壓按摩", "求購肩頸僵硬放鬆", "尋找雙人同行 SPA", "預約深層肌肉放鬆"),
            "6-4" to listOf("尋找高階電競網咖包廂", "求購桌遊店聚會場地", "尋找密室逃脫遊戲體驗", "預約 PS5 視聽電玩包廂", "求購百人電競比賽場地", "尋找桌遊新手教學團", "預約VR虛擬實境體驗", "求購劇本殺推理館訂位", "尋找電競周邊體驗", "預約深夜電競包夜"),
            "6-5" to listOf("尋找室內親子樂園門票", "求購農場餵動物體驗", "尋找兒童攀岩遊樂區", "預約親子手作DIY課程", "求購水上樂園門票", "尋找戶外露營區預約", "預約親子農場採果樂", "求購遊樂園星光票", "尋找親子共讀繪本館", "預約動物園門票代訂"),
            "6-6" to listOf("尋找日式光療美甲設計", "求購 3D/6D 接睫毛服務", "尋找質感髮型設計剪裁", "預約韓式霧眉紋繡", "求購頭皮深層養護", "尋找染髮護髮優惠", "預約男士精緻理容", "求購手足保養去角質", "尋找新娘秘書造型", "預約霧唇美瞳線"),
            "6-7" to listOf("尋找羽球場地零打租借", "求購室內籃球場包場", "尋找保齡球館球道預約", "預約室內游泳池門票", "求購壁球室場地租借", "尋找桌球室桌位預約", "預約撞球館檯面租用", "求購攀岩場入場券", "尋找室內高爾夫練習", "預約運動場館月卡"),
            "6-8" to listOf("尋找全台遊樂園聯合票券", "求購溫泉泡湯券代訂", "尋找展覽藝文通票", "預約休閒農場通行證", "求購下午茶餐券優惠", "尋找旅遊行程規劃", "預約按摩養生券", "求購電影院通⽤優待券", "尋找綜合休閒諮詢", "預約假日休閒揪團")
        )

        val entertainmentSupplyTitles = mapOf(
            "6-1" to listOf("豪華影音 KTV 歡唱大包廂", "精釀啤酒專賣酒吧訂位", "深夜放鬆音樂餐酒館", "私人頂級影音視聽室", "歡樂派對無限暢飲包廂", "特色創意調酒酒吧", "網紅打卡氛圍餐酒館", "Live House 現場演出門票", "好友聚會歡樂大包廂", "深夜微醺爵士音樂酒吧"),
            "6-2" to listOf("IMAX 威秀影城電影票代訂", "熱門舞台劇與話劇門票", "國內外藝術特展門票", "大型演唱會搖滾區門票", "古典音樂會與演奏會門票", "現場喜劇脫口秀門票", "私人影城電影包廳服務", "文創展覽優惠早鳥票", "沉浸式藝術展覽門票", "精彩魔術表演秀門票"),
            "6-3" to listOf("專業全身經絡指壓按摩", "正宗泰式古法舒壓按摩", "芳香精油全身舒壓 SPA", "養生足底按摩與草本泡腳", "臉部深層保養與美體課程", "頭皮釋壓與肩頸放鬆按摩", "準媽媽孕婦專屬舒壓按摩", "上班族肩頸僵硬急救放鬆", "雙人同行專屬隱密 SPA", "運動員深層肌肉放鬆護理"),
            "6-4" to listOf("高階配備電競網咖 VIP 包廂", "豐富桌遊庫聚會場地租借", "沉浸式密室逃脫遊戲體驗", "PS5 4K 視聽電玩獨立包廂", "專業百人電競比賽場地", "桌遊新手帶玩與規則教學", "VR 虛擬實境沉浸式體驗", "沉浸式劇本殺推理館訂位", "熱門電競周邊設備體驗", "深夜電競玩家包夜優惠"),
            "6-5" to listOf("安全室內親子樂園暢玩門票", "親子農場可愛動物餵食體驗", "兒童安全室內攀岩遊樂區", "親子陶瓷/烘焙DIY手作課", "夏日消暑水上樂園門票", "戶外景觀露營區營位預約", "季節限定親子採果樂活動", "歡樂遊樂園優惠星光票", "溫馨親子共讀與繪本館", "動物園與自然科學館門票"),
            "6-6" to listOf("精緻日式光療美甲設計", "自然輕奢 3D/6D 美睫嫁接", "專業質感髮型設計與剪裁", "韓式自然霧眉與半永久紋繡", "頭皮深層淨化與養護課程", "流行質感染髮與深層護髮", "男士精緻理容與鬍型修剪", "手足深層保養與去角質", "新娘秘書與活動造型設計", "時尚霧唇與美瞳線紋繡"),
            "6-7" to listOf("標準羽球場地零打與零租", "室內籃球場團體熱血包場", "歡樂保齡球館球道與鞋租", "恆溫室內游泳池與 SPA 券", "專業壁球室場地租借服務", "舒適空調桌球室桌位預約", "休閒撞球館標準檯面租用", "專業室內攀岩場單次入場", "室內高爾夫模擬器練習", "多功能運動場館超值月卡"),
            "6-8" to listOf("全台各大遊樂園聯合優惠票券", "溫泉會館泡湯券與湯屋代訂", "熱門展覽與藝文演出通票", "休閒農場門票與體驗通行證", "星級飯店下午茶餐券優惠", "在地休閒旅遊行程規劃指導", "專業按摩養生館優惠券", "各大電影院通用優待電影票", "綜合休閒娛樂疑難雜症諮詢", "週末假日休閒好友揪團配對")
        )

        for ((subCode, subName) in entertainmentSubItems) {
            val demandList = entertainmentDemandTitles[subCode] ?: emptyList()
            val supplyList = entertainmentSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("ent", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "ENT_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質休閒娛樂與放鬆服務，精彩豐富，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("ent", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "ENT_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業休閒娛樂、美業美容與運動場館服務，體驗絕佳，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 七、精準載入「7. 醫療保健」8 個子項目 (7-1 至 7-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val medicalSubItems = listOf(
            "7-1" to "藥局藥品 / 保健送藥",
            "7-2" to "看護照顧 / 居家照護",
            "7-3" to "診所門診 / 掛號諮詢",
            "7-4" to "醫美整形 / 皮膚管理",
            "7-5" to "健檢中心 / 醫事檢驗",
            "7-6" to "輔具租售 / 醫療器材",
            "7-7" to "復健物理 / 中醫針灸",
            "7-8" to "綜合其它 / 健康諮詢"
        )

        val medicalDemandTitles = mapOf(
            "7-1" to listOf("尋找處方箋慢性病藥局領藥", "求購口罩酒精防疫用品", "尋找常備成藥急送", "預約保健食品營養諮詢", "求購嬰幼兒奶粉尿布", "尋找銀髮族營養品", "預約中藥材代煎服務", "求購急救醫療包", "尋找隱形眼鏡保養液", "預約藥師用藥安全諮詢"),
            "7-2" to listOf("尋找醫院全日照護看護", "求購居家半日看護幫手", "尋找術後短期照顧陪伴", "預約鼻胃管導尿管護理", "求購中風復健照顧助理", "尋找夜間安心陪病", "預約失智症專業照護", "求購外籍看護空窗期支援", "尋找安寧居家照顧", "預約假日短期照顧"),
            "7-3" to listOf("尋找牙科診所預約掛號", "求購眼科近視雷射諮詢", "尋找皮膚科門診推薦", "預約耳鼻喉科急診掛號", "求購小兒科疫苗預約", "尋找骨科復健科諮詢", "預約婦產科產檢掛號", "求購心臟內科門診", "尋找中醫內科調理", "預約牙齒美白諮詢"),
            "7-4" to listOf("尋找皮秒雷射除斑保養", "求購玻尿酸微整形諮詢", "尋找音波電波拉提", "預約韓式皮膚管理", "求購雙眼皮隆鼻整形", "尋找雷射除毛療程", "預約水飛梭毛孔清潔", "求購肉毒除皺瘦小臉", "尋找自體脂肪補脂", "預約醫美術後修護"),
            "7-5" to listOf("尋找全身高階健康檢查", "求購癌症篩檢基因檢測", "尋找抽血檢驗報告諮詢", "預約心血管健檢專案", "求購胃鏡大腸鏡檢查", "尋找婚前健康檢查", "預約勞工體檢辦理", "求購過敏原檢測套組", "尋找骨密度檢測", "預約超音波健康檢查"),
            "7-6" to listOf("尋找電動醫療床租借", "求購輕便輪椅購買", "尋找氣墊床褥瘡預防", "預約助行器拐杖租售", "求購製氧機抽痰機租賃", "尋找洗澡椅便盆椅", "預約電動代步車租借", "求購血壓計血糖機", "尋找護腰護膝護具", "預約居家無障礙改造"),
            "7-7" to listOf("尋找物理治療徒手治療", "求購中醫針灸推拿調理", "尋找中風復健物理訓練", "預約產後骨盆矯正復健", "求購五十肩物理治療", "尋找運動傷害物理治療", "預約脊椎側彎矯正", "求購中藥薰蒸調理", "尋找術後肌力恢復", "預約拔罐刮痧理療"),
            "7-8" to listOf("尋找心理諮商心理治療", "求購營養師飲食規劃", "尋找戒菸戒酒諮詢", "預約睡眠障礙調理", "求購健康管理師指導", "尋找產後心理支持", "預約用藥整合諮詢", "求購身心放鬆引導", "尋找衛生教育諮詢", "預約綜合健康諮詢")
        )

        val medicalSupplyTitles = mapOf(
            "7-1" to listOf("專業藥局處方箋慢性病送藥", "口罩酒精與防疫物資現貨", "常備成藥與急用藥品快送", "專業藥師保健食品營養諮詢", "嬰幼兒奶粉與紙尿布專賣", "銀髮族專用營養補充品", "中藥材代煎與真空包裝配送", "居家急救醫療包與護具", "隱形眼鏡藥水與保養品", "24小時藥師用藥安全諮詢"),
            "7-2" to listOf("專業醫院全日與半日照護看護", "居家貼身照顧與生活協助", "術後短期專業照顧與陪伴", "鼻胃管與導尿管居家護理", "中風患者復健照顧助理", "夜間安心陪病與翻身拍背", "失智症專業照顧服務", "外籍看護空窗期臨時支援", "安寧居家療護與關懷支援", "假日短期喘息照顧服務"),
            "7-3" to listOf("牙科診所專業預約與掛號諮詢", "眼科近視雷射與視力檢查諮詢", "皮膚科門診與醫學美容掛號", "耳鼻喉科門診與快速看診", "小兒科疫苗預約與幼兒保健", "骨科與復健科門診諮詢", "婦產科產檢與女性健康門診", "心臟內科與慢性病門診掛號", "中醫內科體質調理與把脈", "冷光牙齒美白與矯正諮詢"),
            "7-4" to listOf("韓式皮秒雷射除斑與美白", "玻尿酸與微晶瓷微整形諮詢", "音波與電波拉提緊緻療程", "韓國水光針與皮膚管理", "雙眼皮與隆鼻精緻整形", "亞歷山大雷射除毛療程", "水飛梭深層毛孔清潔護理", "肉毒桿菌除皺與瘦小臉", "自體脂肪移植與面部填補", "醫美雷射術後專業修護課程"),
            "7-5" to listOf("高階全身精密健康檢查中心", "癌症早期篩檢與基因檢測", "專業醫事檢驗所抽血報告", "心血管疾病精密健檢專案", "無痛胃鏡與大腸鏡檢查服務", "青年男女婚前健康檢查", "公司行號與勞工體檢辦理", "慢性過敏原檢測與營養諮詢", "超音波骨密度與骨質檢測", "腹部與甲狀腺超音波檢查"),
            "7-6" to listOf("電動醫療照護床短期租售", "輕便折疊輪椅與特製輪椅", "預防褥瘡專用電動氣墊床", "鋁合金助行器與四腳拐杖", "製氧機與抽痰機醫療租賃", "不鏽鋼洗澡椅與便盆椅", "長者電動代步車租借服務", "家用精準血壓計與血糖機", "醫療級護腰護膝與護具", "居家無障礙空間改造評估"),
            "7-7" to listOf("專業物理治療師徒手治療", "傳統中醫針灸與推拿調理", "中風患者專屬復健物理訓練", "產後骨盆矯正與核心復健", "五十肩與關節沾黏物理治療", "運動傷害修復與物理治療", "脊椎側彎與體態矯正訓練", "中藥薰蒸與溫熱敷理療", "手術後關節肌力恢復訓練", "傳統拔罐刮痧與經絡理療"),
            "7-8" to listOf("專業心理諮商與心理治療", "資深營養師個人飲食規劃", "戒菸戒酒戒癮專業諮詢", "睡眠障礙與失眠調理輔導", "專業健康管理師一對一指導", "產後媽媽心理支持與陪伴", "慢性病患用藥整合諮詢", "身心放鬆與冥想減壓引導", "社區衛生教育與健康講座", "綜合健康保健疑難解答")
        )

        for ((subCode, subName) in medicalSubItems) {
            val demandList = medicalDemandTitles[subCode] ?: emptyList()
            val supplyList = medicalSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("med", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "MED_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質醫療保健與照護服務，專業可靠，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("med", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "MED_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業醫療照護、診所門診與健康管理服務，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 八、精準載入「8. 二手交換」8 個子項目 (8-1 至 8-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val secondhandSubItems = listOf(
            "8-1" to "二手手機 / 數碼 3C",
            "8-2" to "家電傢俱 / 居家閒置",
            "8-3" to "汽機買賣 / 二輪四輪",
            "8-4" to "精品服飾 / 包款古著",
            "8-5" to "母嬰用品 / 玩具童車",
            "8-6" to "戶外裝備 / 運動器材",
            "8-7" to "遊戲電玩 / 動漫周邊",
            "8-8" to "綜合閒置 / 在地面交"
        )

        val secondhandDemandTitles = mapOf(
            "8-1" to listOf("尋找九成新二手智慧型手機", "求購二手平板電腦", "尋找輕薄二手筆記型電腦", "預約二手數位相機", "求購二手智慧手錶", "尋找二手藍牙耳機", "預約二手遊戲掌機", "求購二手行動電源", "尋找二手電競螢幕", "預約二手空拍機"),
            "8-2" to listOf("尋找二手雙門小冰箱", "求購二手變頻洗衣機", "尋找二手雙人沙發", "預約二手實木書桌", "求購二手微波爐烤箱", "尋找二手收納衣櫃", "預約二手除濕機", "求購二手書桌椅組", "尋找二手吸塵器", "預約二手雙人床架"),
            "8-3" to listOf("尋找代步二手摩托車", "求購二手檔車跑車", "尋找省油中古代步車", "預約二手電動機車", "求購二手汽車鋁圈", "尋找中古休旅車", "預約二手機車安全帽", "求購二手行車紀錄器", "尋找汽車音響主機", "預約二手汽車千斤頂"),
            "8-4" to listOf("尋找二手名牌精品包", "求購復古古著襯衫", "尋找二手精品皮夾", "預約日系古著外套", "求購二手設計師洋裝", "尋找二手潮流皮帶", "預約復古手提包", "求購二手羊毛大衣", "尋找古著牛仔外套", "預約二手名牌絲巾"),
            "8-5" to listOf("尋找二手嬰兒推車", "求購兒童安全座椅", "尋找嬰兒木製嬰兒床", "預約兒童電動玩具車", "求購學步車餐椅", "尋找哺乳餵奶用品", "預約兒童積木玩具", "求購二手嬰兒揹帶", "尋找童書繪本套書", "預約兒童滑步車"),
            "8-6" to listOf("尋找二手露營帳篷天幕", "求購二手登山背包", "尋找二手折疊休閒椅", "預約二手登山杖", "求購二手羽球拍網球拍", "尋找二手啞鈴槓鈴", "預約二手露營冰桶", "求購二手充氣睡墊", "尋找折疊自行車", "預約戶外照明露營燈"),
            "8-7" to listOf("尋找二手 Nintendo Switch 主機", "求購 PS5 二手遊戲片", "尋找動漫絕版公仔", "預約二手 PS4 主機", "求購熱門漫畫整套", "尋找 Switch 遊戲卡匣", "預約鋼彈模型素組", "求購動漫周邊抱枕", "尋找二手 XBox 主機", "預約電玩手把控制器"),
            "8-8" to listOf("尋找在地社區二手市集", "求購搬家出清雜物", "尋找免費舊物索取", "預約面交驗貨服務", "求購二手辦公文具", "尋找閒置禮品交換", "預約二手樂器吉他", "求購二手行李箱", "尋找環保回收物品", "預約綜合二手交換")
        )

        val secondhandSupplyTitles = mapOf(
            "8-1" to listOf("9成新旗艦二手智慧手機", "高規二手平板電腦現貨", "輕薄商務二手筆記型電腦", "日系經典二手數位相機", "智慧運動手錶手環", "主動降噪二手藍牙耳機", "便攜二手遊戲掌機", "大容量快充行動電源", "27吋高刷新電競螢幕", "專業空拍機二手出清"),
            "8-2" to listOf("節能省電二手雙門小冰箱", "日系變頻滾筒洗衣機", "北歐風二手雙人布沙發", "實木質感二手辦公書桌", "微波爐與小型電烤箱", "多功能木質收納衣櫃", "日除濕量大容量除濕機", "人體工學書桌椅組", "無線手持吸塵器", "堅固耐用雙人床架"),
            "8-3" to listOf("學生代步二手 125 摩托車", "經典酷炫二手檔車跑車", "省油好開中古代步汽車", "Gogoro 二手電動機車", "改裝汽車鋁合金輪圈", "空間寬敞中古休旅車", "安全認證二手安全帽", "高清前後雙鏡頭行車紀錄器", "多媒體觸控汽車音響", "車用油壓千斤頂與工具"),
            "8-4" to listOf("經典款二手名牌精品包", "日系復古古著花襯衫", "真皮質感二手短夾", "秋冬保暖古著毛呢外套", "設計師款二手連身洋裝", "潮流真皮壓紋皮帶", "復古優雅手提包", "質感保暖羊毛大衣", "美式復古刷色牛仔外套", "精美二手名牌絲巾"),
            "8-5" to listOf("輕便折疊二手嬰兒推車", "汽車專用兒童安全座椅", "實木多功能嬰兒床", "兒童四輪電動玩具車", "多功能學步車與餐椅", "母嬰專用消毒鍋與溫奶器", "益智大顆粒積木玩具", "人體工學二手嬰兒揹帶", "啟蒙童書繪本套書組合", "兒童平衡滑步車"),
            "8-6" to listOf("防水抗風露營帳篷與天幕", "專業多功能登山健行背包", "便攜輕量折疊休閒椅", "伸縮輕量碳纖維登山杖", "碳素羽球拍與網球拍", "居家健身啞鈴與槓鈴組合", "露營專用保溫保冷冰桶", "自動充氣戶外睡眠睡墊", "通勤摺疊自行車小折", "高亮度戶外照明露營燈"),
            "8-7" to listOf("9成新 Nintendo Switch 主機", "PS5 熱門大作二手遊戲片", "精美動漫絕版限量公仔", "二手經典 PS4 主機", "熱門少年漫畫整套出租/售", "Switch 暢銷遊戲卡匣", "精緻鋼彈模型與工具", "人氣動漫周邊抱枕掛軸", "二手微軟 XBox 主機", "原廠無線電玩手把控制器"),
            "8-8" to listOf("社區在地二手跳蚤市集", "搬家出清各類居家雜物", "愛心免費舊物索取區", "安全便利面交驗貨點", "辦公室二手文具與資料夾", "各類閒置禮品交換轉贈", "二手木吉他與烏克麗麗", "耐用出國旅行行李箱", "環保回收與二手物資", "綜合二手閒置物品交換")
        )

        for ((subCode, subName) in secondhandSubItems) {
            val demandList = secondhandDemandTitles[subCode] ?: emptyList()
            val supplyList = secondhandSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("second", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "SECOND_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質二手好物與交換物品，性價比高，誠心求購。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("second", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "SECOND_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 優質二手閒置物品與 3C 家電，狀況良好，價格實惠，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 九、精準載入「9. 寵物服務」8 個子項目 (9-1 至 9-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val petSubItems = listOf(
            "9-1" to "寵物洗澡 / 美容造型",
            "9-2" to "獸醫門診 / 急診疫苗",
            "9-3" to "寵物寄養 / 日託安親",
            "9-4" to "寵物接送 / 專車跑腿",
            "9-5" to "到府餵食 / 代遛寵物",
            "9-6" to "走失協尋 / 送養領養",
            "9-7" to "寵物用品 / 主糧零食",
            "9-8" to "綜合其它 / 寵物生命"
        )

        val petDemandTitles = mapOf(
            "9-1" to listOf("尋找小型犬洗澡美容", "求購貓咪無麻醉洗澡", "尋找大型犬專業美容", "預約寵物精緻造型剪毛", "求購貓狗去結毛結護理", "尋找藥浴護膚洗澡", "預約到府寵物洗澡", "求購寵物基礎清潔", "尋找寵物深層護毛", "預約貓咪剪指甲清耳"),
            "9-2" to listOf("尋找 24 小時寵物急診", "求購貓狗預防針施打", "尋找寵物健康檢查中心", "預約動物醫院門診掛號", "求購寵物結紮手術諮詢", "尋找獸醫內科調理", "預約寵物洗牙麻醉諮詢", "求購貓狗驅蟲藥領取", "尋找外科手術評估", "預約寵物皮膚科門診"),
            "9-3" to listOf("尋找貓咪獨立安心寄養", "求購狗狗日間安親班", "尋找無籠自由寵物旅館", "預約連假寵物長期寄養", "求購老齡犬特需照護寄養", "尋找小型犬專屬安親", "預約24小時專人看顧寄養", "求購貓咪安親日託", "尋找兔鼠小型寵物寄養", "預約寵物寄養視訊回報"),
            "9-4" to listOf("尋找寵物專車就醫接送", "求購寵物美容接送服務", "尋找跨縣市寵物託運", "預約寵物寄養接送", "求購代買寵物處方飼料", "尋找寵物專車搬家", "預約深夜寵物急診接送", "求購寵物用品專人跑腿", "尋找寵物機場接送", "預約多隻寵物共乘接送"),
            "9-5" to listOf("尋找貓咪到府餵食換水", "求購狗狗外出代遛服務", "尋找過年出國到府照顧", "預約餵藥與居家清理", "求購幼犬定時上廁所陪伴", "尋找中途貓咪到府探視", "預約到府陪伴玩耍", "求購植物與寵物一起照顧", "尋找深夜到府餵食", "預約到府回報服務"),
            "9-6" to listOf("尋找寵物走失協尋找狗", "求購走失貓咪協尋指導", "尋找愛心貓咪送養資訊", "預約合法幼犬領養諮詢", "求購寵物協尋海報製作", "尋找中途之家收容評估", "預約流浪動物絕育通報", "求購尋貓尋狗紅包懸賞", "尋找愛心認養交流群", "預約寵物送養審核協助"),
            "9-7" to listOf("尋找天然無穀貓狗飼料", "求購凍乾肉塊寵物零食", "尋找貓砂豆腐砂木屑砂", "預約寵物自動餵食器", "求購寵物保健營養品", "尋找貓抓板貓跳台", "預約寵物外出背包提籠", "求購狗狗尿布墊衛生紙", "尋找寵物沐浴乳洗毛精", "預約寵物涼墊保暖窩"),
            "9-8" to listOf("尋找寵物過世火化安葬", "求購寵物骨灰罈紀念品", "尋找寵物心理溝通師", "預約寵物保險諮詢", "求購寵物墓園塔位諮詢", "尋找寵物生前契約", "預約寵物告別式辦理", "求購寵物紀念毛髮鑽石", "尋找綜合寵物諮詢", "預約寵物心理諮商")
        )

        val petSupplyTitles = mapOf(
            "9-1" to listOf("小型犬精緻洗澡與美容造型", "貓咪無麻醉溫柔洗澡護理", "大型犬專業毛髮修剪美容", "寵物時尚造型與手剪毛", "貓狗嚴重結毛結專業梳理", "皮膚病專用藥浴護膚洗澡", "專業到府寵物洗澡與吹毛", "寵物基礎清潔與日常護理", "寵物頂級護毛與 SPA 護理", "貓咪專業剪指甲與清耳朵"),
            "9-2" to listOf("24小時動物醫院寵物急診", "貓狗年度預防針與晶片施打", "寵物全方位高階健康檢查", "資深獸醫師門診親自看診", "寵物絕育結紮手術專業評估", "資深獸醫內科調理與治療", "寵物安全洗牙與口腔護理", "貓狗體內外驅蟲與預防藥", "外科手術與骨科專業評估", "寵物皮膚科專科門診諮詢"),
            "9-3" to listOf("貓咪獨立空間安心住宿寄養", "狗狗白天日間安親托育班", "無籠自由活動溫馨寵物旅館", "長假期間寵物長期安心寄養", "老齡犬與特需照顧寄養服務", "小型犬專屬溫馨安親陪伴", "24小時專人駐店安心看顧", "貓咪專屬安親日託半日班", "兔鼠與爬蟲類小型寵物寄養", "寄養期間每日照片視訊回報"),
            "9-4" to listOf("專業寵物專車醫院就醫接送", "寵物美容院府邸接送服務", "跨縣市安全舒適寵物託運", "寵物旅館寄養專車接送往返", "代買寵物處方飼料與藥品跑腿", "攜帶寵物專車搬家與出行", "深夜緊急寵物急診專車接送", "寵物日常用品代買專人跑腿", "寵物友善機場接送專車", "多隻寵物家庭共乘專車接送"),
            "9-5" to listOf("專業貓咪到府餵食與換水清貓砂", "狗狗戶外定時牽繩代遛服務", "連假出國到府安心照顧寵物", "餵藥、皮下注射與居家清理", "幼犬定時餵食與如廁陪伴", "中途愛心貓咪到府探視與餵養", "到府陪伴玩耍與互動紓壓", "居家植栽澆水與寵物一同照顧", "深夜時段到府餵食與觀察", "到府照顧即時拍照影片回報"),
            "9-6" to listOf("專業寵物走失協尋與尋狗指導", "貓咪走失黃金時間協尋策略", "健康可愛愛心貓咪送養資訊", "合法犬舍幼犬領養與諮詢", "寵物協尋海報設計與社群發布", "流浪動物中途之家收容評估", "流浪貓狗 TNR 絕育通報協助", "尋貓尋狗懸賞與搜索建議", "愛心寵物認養交流與配對", "寵物送養認養人資格審核"),
            "9-7" to listOf("優質天然無穀貓狗主食飼料", "手作純天然凍乾肉塊與零食", "貓用豆腐砂、礦砂與木屑砂", "智慧自動餵食器與飲水機", "寵物關節與腸胃保健營養品", "耐磨貓抓板與多層貓跳台", "透氣寵物外出背包與提籠", "狗狗專用尿布墊與清潔用品", "天然草本寵物沐浴乳洗毛精", "四季通用寵物涼墊與保暖窩"),
            "9-8" to listOf("專業寵物過世火化與安心安葬", "精緻寵物骨灰罈與紀念品訂製", "資深動物心靈溝通師諮詢", "寵物醫療保險方案規劃諮詢", "合法寵物墓園與塔位諮詢", "寵物生前契約與關懷服務", "溫馨寵物告別式籌辦與主持", "寵物紀念毛髮鑽石與項鍊製作", "綜合寵物飼養疑難雜症諮詢", "動物行為與心理諮商輔導")
        )

        for ((subCode, subName) in petSubItems) {
            val demandList = petDemandTitles[subCode] ?: emptyList()
            val supplyList = petSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("pet", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "PET_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質寵物服務與照顧，愛心呵護，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("pet", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "PET_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業寵物美容、獸醫醫療與寄養安親服務，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 十、精準載入「10. 專業服務」8 個子項目 (10-1 至 10-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val serviceSubItems = listOf(
            "10-1" to "臨時短工 / 活動人力",
            "10-2" to "專業修繕 / 裝潢水電",
            "10-3" to "家事清潔 / 消毒除蟲",
            "10-4" to "代辦跑腿 / 現場排隊",
            "10-5" to "活動企劃 / 攝影主持",
            "10-6" to "緊急求助 / 在地互助",
            "10-7" to "專業諮詢 / 代寫翻譯",
            "10-8" to "綜合服務 / 其它幫忙"
        )

        val serviceDemandTitles = mapOf(
            "10-1" to listOf("尋找展覽活動臨時工", "求購發放傳單工讀生", "尋找搬運臨時幫手", "預約展場接待禮賓", "求購餐廳假日洗碗工", "尋找尾牙活動場布人員", "預約市集擺攤助手", "求購臨時粗工人力", "尋找活動問卷調查員", "預約臨時包裝出貨員"),
            "10-2" to listOf("尋找居家水電維修師傅", "求購壁癌抓漏防水工程", "尋找木工裝潢統包", "預約燈具衛浴設備安裝", "求購鐵件門窗修理", "尋找房屋結構補強", "預約鋁門窗氣密窗安裝", "求購老屋水管更新", "尋找室內油漆粉刷", "預約抽油煙機維修"),
            "10-3" to listOf("尋找全室深層大掃除", "求購居家消毒除蟲服務", "尋找冷氣機深層清洗", "預約定期鐘點家政清潔", "求購除塵蟎床墊沙發", "尋找辦公室定期清潔", "預約裝潢後細部清潔", "求購水塔清洗與殺菌", "尋找地板打蠟拋光", "預約退租交屋清潔"),
            "10-4" to listOf("尋找熱門餐廳代排隊", "求購醫院門診代掛號", "尋找公家機關代辦業務", "預約限量商品代買", "求購地政事務所代跑", "尋找郵局包裹代寄", "預約演唱會門票代排", "求購文件快遞跑腿", "尋找銀行臨櫃代辦", "預約日常雜物代買"),
            "10-5" to listOf("尋找婚禮攝影師記錄", "求購商業活動主持人", "尋找尾牙春酒企劃統包", "預約平面商業攝影", "求購活動音響燈光租借", "尋找婚禮錄影團隊", "預約生日派對策劃", "求購形象影片拍攝剪輯", "尋找展場Showgirl", "預約企業年會統籌"),
            "10-6" to listOf("尋找深夜緊急開鎖服務", "求購颱風天路樹清理", "尋找社區緊急物資互助", "預約水管爆裂緊急搶修", "求購尋求在地志工協助", "尋找車輛拋錨推車幫忙", "預約緊急搬運重物", "求購社區安全巡守支援", "尋找尋找走失寵物幫手", "預約夜間緊急求助諮詢"),
            "10-7" to listOf("尋找法律顧問合約審閱", "求購商業英文文件翻譯", "尋找稅務會計諮詢服務", "預約心理諮商師面談", "求購論文英文潤飾排版", "尋找日文韓文口譯", "預約創業貸款諮詢", "求購簡報PPT設計排版", "尋找專利申請代辦", "預約職涯發展諮詢"),
            "10-8" to listOf("尋找綜合雜務代辦幫忙", "求購遺失物尋找協助", "尋找社區好物交換幫手", "預約活動場地場勘", "求購幫忙搬盆栽花卉", "尋找臨時寵物寄放", "預約幫忙排隊拿包裹", "求購綜合文書處理", "尋找臨時保母幫忙", "預約各類疑難雜症協助")
        )

        val serviceSupplyTitles = mapOf(
            "10-1" to listOf("專業展覽活動臨時工", "發放傳單與舉牌工讀生", "搬運與粗重工作臨時幫手", "專業展場接待與禮賓人員", "餐廳假日洗碗與備料幫手", "尾牙春酒活動場布人員", "市集擺攤與銷售助手", "建築與工地臨時粗工人力", "活動市調與問卷調查員", "電商倉庫臨時包裝出貨員"),
            "10-2" to listOf("專業居家水電維修與配線師傅", "壁癌抓漏與外牆防水工程", "室內木工裝潢與統包工程", "燈具、免治馬桶與衛浴安裝", "鐵件訂製與門窗修理服務", "房屋結構安全補強工程", "隔音氣密鋁門窗專業安裝", "老屋全室給排水管更新", "室內環保無毒油漆粉刷", "排油煙機與瓦斯爐修理"),
            "10-3" to listOf("專業全室深層大掃除服務", "居家環境消毒與病媒除蟲", "冷氣機分體式深層清洗保養", "定期居家鐘點家政清潔人員", "高溫除塵蟎與床墊沙發清洗", "辦公室與商場定期清潔維護", "新屋裝潢後細部細緻清潔", "住宅水塔清洗與管線殺菌", "木質地板打蠟與晶化拋光", "租屋退租交屋徹底清潔"),
            "10-4" to listOf("熱門人氣餐廳代排隊服務", "各大醫院門診現場代掛號", "公家機關與監理站代辦業務", "限量聯名商品代購服務", "地政事務所與戶政代跑", "郵局信件包裹代寄服務", "演唱會與展覽門票代排隊", "急件公文與物品快遞跑腿", "銀行臨櫃領件與代辦事項", "日常生活雜物代買代辦"),
            "10-5" to listOf("專業婚禮紀實與平面攝影師", "資深商業活動與婚禮主持人", "尾牙春酒與發表會企劃統包", "商品與空間平面商業攝影", "活動專業音響與燈光租借", "婚禮動態錄影與快剪快播", "生日派對與求婚企劃策劃", "企業形象影片拍攝與剪輯", "展場主持與專業 Showgirl", "大型企業年會活動統籌"),
            "10-6" to listOf("24小時深夜緊急開鎖換鎖", "颱風過後倒塌路樹清理支援", "社區緊急物資調度與互助", "水管爆裂與大漏水緊急搶修", "在地熱心志工與人力協助", "車輛拋錨現場推車與幫忙", "夜間緊急重物搬運支援", "社區安全巡守與守望相助", "在地尋找走失寵物義工隊", "深夜在地突發緊急求助支援"),
            "10-7" to listOf("專業律師法律顧問合約審閱", "商業英文合約與論文翻譯", "稅務會計師報稅財務諮詢", "專業心理諮商師一對一諮商", "學術論文英文潤飾與排版", "日文韓文商務口譯與筆譯", "創業貸款與政府補助諮詢", "商業簡報 PPT 專業設計排版", "智慧財產權與專利申請代辦", "個人職涯發展與履歷諮詢"),
            "10-8" to listOf("各類綜合雜務代辦與幫忙", "失物協尋與在地情報打聽", "社區好物分享與交換協助", "大型活動場地實地場勘", "庭園盆栽與花卉搬運協助", "臨時居家寵物寄放與照看", "超商大宗包裹代領與搬運", "綜合文書處理與資料輸入", "臨時家庭保母與幫手支援", "各類疑難雜症與臨時幫忙")
        )

        for ((subCode, subName) in serviceSubItems) {
            val demandList = serviceDemandTitles[subCode] ?: emptyList()
            val supplyList = serviceSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("serv", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "SERV_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 尋找優質專業服務與人力支援，高效可靠，誠心洽詢。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("serv", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "SERV_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 專業人力短工、修繕清潔與企劃諮詢服務，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 十二、精準載入「12. 信仰祈福」8 個子項目 (12-1 至 12-8)，每項各 10 筆需求 + 10 筆供給
        // ==========================================
        val religionSubItems = listOf(
            "12-1" to "土地公 / 福德正神",
            "12-2" to "關聖帝君 / 恩主公",
            "12-3" to "天上聖母 / 媽祖",
            "12-4" to "基督教 / 代禱祈禱",
            "12-5" to "佛教感應 / 點燈祈福",
            "12-6" to "天主教 / 祈禱祈福",
            "12-7" to "心靈平安 / 普渡祈福",
            "12-8" to "綜合信仰 / 靈修祈福"
        )

        val religionDemandTitles = mapOf(
            "12-1" to listOf(
                "求購土地公平安米與發財金套組", "尋找福德正神頭牙代獻花供品", "預約店家開市土地公代祭拜服務", "求購土地公廟光明燈代點服務", "尋找土地公求財符與金紙代購",
                "預約烘爐地土地公代祈福拜拜", "求購商家招財福德正神尊像供奉", "尋找土地公誕辰代獻壽桃壽麵", "預約土地公廟參拜接送與導覽", "求購土地公加持平安香火袋"
            ),
            "12-2" to listOf(
                "尋找關聖帝君代禱求事業順利", "求購恩主公契孫平安符與文具", "預約行天宮恩主公代收驚服務", "尋找關帝誕辰祝壽代祭拜服務", "求購關聖帝君武財神招財香案",
                "預約關帝廟求籤解籤諮詢", "尋找考試升遷關聖帝君文昌祈福", "求購恩主公加持正氣平安卡", "預約商家誠信結盟關帝祈福", "尋找關聖帝君過爐香火袋代購"
            ),
            "12-3" to listOf(
                "尋找大甲媽祖遶境代祈福求平安", "求購白沙屯媽祖壓轎金與勇字香符", "預約媽祖誕辰祝壽花籃代送服務", "尋找天上聖母點平安燈與光明燈", "求購媽祖加持孕婦安胎平安符",
                "預約航海出行天上聖母庇佑祈福", "尋找鹿港天后宮媽祖代參拜", "求購媽祖遶境紀念香火袋與文創", "預約家庭平安天上聖母求籤解籤", "尋找媽祖契子祈福祝壽金紙組"
            ),
            "12-4" to listOf(
                "尋找基督徒團契代禱求病患康復", "求購精裝本中文聖經與靈修日誌", "預約線上主日崇拜與家庭聚會導覽", "尋找教會長老代禱求家庭和睦", "求購十字架平安項鍊與禱告卡",
                "預約基督教婚禮祝福代禱團", "尋找考試與求職福音代禱支持", "求購讚美詩歌集與福音月刊", "預約牧師線上心靈關懷與禱告", "尋找社區福音茶會與查經班"
            ),
            "12-5" to listOf(
                "尋找佛寺代點藥師燈求身體健康", "求購觀音菩薩大悲咒加持水與念珠", "預約法會超薦祖先與冤親債主", "尋找法師誦經祈福與迴向服務", "求購檀香佛珠與精裝佛經抄經本",
                "預約禪修打坐與心靈淨化課程", "尋找佛前供花供果代辦服務", "求購地藏王菩薩本願經靈感本", "預約點萬佛燈求智慧與消災解厄", "尋找放生護生功德迴向代辦"
            ),
            "12-6" to listOf(
                "尋找天主教堂區神父代禱與祝福", "求購聖母瑪利亞玫瑰念珠與聖牌", "預約彌撒意向代獻求家人平安", "尋找天主教聖體聖事與告解諮詢", "求購聖水瓶與瓜達盧佩聖母像",
                "預約天主教追思彌撒代禱服務", "尋找病患臨終傅油聖事協助", "求購天主教聖歌本與祈禱手冊", "預約聖母月祈禱團與家庭玫瑰經", "尋找天主教青年團契與聖召交流"
            ),
            "12-7" to listOf(
                "尋找中元普渡代拜與澎湃供品包", "求購心靈平安香包與淨化淨符", "預約宮廟安太歲與太歲燈代點", "尋找消災解厄補運祈福法會", "求購艾草草本淨化噴霧與沐浴包",
                "預約收驚安魂與心靈安定服務", "尋找家庭普渡金紙與祭拜導引", "求購七月平安鹽與開運水晶", "預約轉運補財庫祈福儀式代辦", "尋找祖先祭祀與清明祭祖代拜"
            ),
            "12-8" to listOf(
                "尋找頌缽音療與心靈冥想沉靜", "求購天然鼠尾草淨化薰香束", "預約水晶能量氣場淨化與療癒", "尋找心靈塔羅牌解讀與人生指引", "求購聖木淨化香條與香爐組合",
                "預約天使數字與星盤靈性諮詢", "尋找正念冥想呼吸練習引導", "求購七脈輪天然水晶能量石", "預約大地能量靈性祝福儀式", "尋找身心靈平衡體驗與工作坊"
            )
        )

        val religionSupplyTitles = mapOf(
            "12-1" to listOf(
                "福德正神加持發財米與平安金", "烘爐地土地公代拜求財與點燈", "店家開市招財土地公供品代辦", "土地公誕辰祝壽壽桃壽麵組", "福德正神招財香火袋與平安符",
                "知名土地公廟代點光明燈財神燈", "土地公參拜供品包裝與配送", "正統土地公補財庫金紙組合", "福德正神信仰文化解說與帶領", "土地公祈福安座與開光諮詢"
            ),
            "12-2" to listOf(
                "行天宮恩主公平安符與代收驚", "關聖帝君忠義武財神開運金", "關帝誕辰祝壽獻供與平安卡", "關聖帝君事業升遷求籤解籤", "恩主公契孫祈福與文昌文具組",
                "關帝廟過爐正氣平安香火袋", "關聖帝君誠信商家祈福牌位", "恩主公文化導覽與祈福儀式", "關帝武財神補財庫祝壽供品", "關聖帝君加持車輛平安掛飾"
            ),
            "12-3" to listOf(
                "白沙屯媽祖加持壓轎金與勇字符", "大甲媽祖遶境祈福平安香火袋", "天上聖母慈悲庇佑代點光明燈", "媽祖誕辰祝壽鮮花素果代獻", "鹿港天后宮媽祖代祈福與安胎符",
                "天上聖母航海出行平安掛飾", "媽祖賜福契子平安長大祈福組", "天上聖母靈籤解籤與心靈指引", "媽祖繞境專屬祈福文創禮盒", "正統天上聖母補運祈福金紙"
            ),
            "12-4" to listOf(
                "主內兄弟姊妹愛心代禱與關懷", "基督教福音聖經與靈修書籍", "精美十字架飾品與平安經文卡", "牧師線上禱告與心靈協談服務", "家庭團契與查經聚會線上導引",
                "基督教婚禮與感恩禮拜祝福代禱", "病患關懷與平安福音代禱團隊", "讚美詩歌音響與福音文創商品", "青少年福音營隊與靈修分享", "主日學教材與社區福音小組"
            ),
            "12-5" to listOf(
                "名寺大雄寶殿代點藥師燈光明燈", "觀音菩薩大悲水與加持菩提念珠", "佛門功德法會迴向與消災祈福", "手工檀香佛珠與抄經寫經套組", "佛前獻花供果純素供品代辦",
                "禪修靜坐與心靈法語諮詢", "地藏經超薦與祖先祈福牌位", "萬佛光明燈長年祈福安置", "佛教蔬食護生與功德迴向代辦", "彌陀佛號與梵音誦經音響播放器"
            ),
            "12-6" to listOf(
                "天主教聖堂彌撒意向代獻與祈禱", "精緻聖母玫瑰念珠與耶穌聖牌", "神父線上心靈陪伴與代禱祝福", "聖水保佑瓶與天主教聖像畫卡", "家庭玫瑰經祈禱分享與靈修引導",
                "天主教追思禮儀與彌撒祈福", "病患平安傅油聖事協調與代禱", "天主教聖歌靈修音樂與書籍", "聖母月特別祈禱與聖牌過香", "天主教主日學與青年信仰分享"
            ),
            "12-7" to listOf(
                "中元普渡澎湃供品包與代拜服務", "太歲年安太歲與太歲光明燈", "草本艾草抹草淨化平安沐浴包", "正統道教消災解厄補運祈福", "傳統收驚安魂與心靈舒緩安頓",
                "心靈平安開運水晶與海鹽淨化包", "補財庫轉運金紙與祈福疏文", "家庭節慶祭祖供品準備與配送", "七月平安祈福與心靈安撫諮詢", "祖先追思普渡代獻花與拜拜"
            ),
            "12-8" to listOf(
                "西藏頌缽音療與身心靈深層放鬆", "加州白鼠尾草與秘魯聖木淨化束", "天然水晶脈輪能量淨化與療癒", "直覺塔羅心靈牌卡解讀與指引", "正念冥想導引與呼吸放鬆課程",
                "星盤解析與天使卡靈性諮詢", "七脈輪開運水晶石與療癒飾品", "大地自然靈性祝福與氣場調和", "身心靈能量舒壓與心靈探索", "高頻音療與心靈沉靜祝福體驗"
            )
        )

        for ((subCode, subName) in religionSubItems) {
            val demandList = religionDemandTitles[subCode] ?: emptyList()
            val supplyList = religionSupplyTitles[subCode] ?: emptyList()

            for (j in 0 until 10) {
                val title = demandList[j % demandList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("rel", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "RELIGION_REQ_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1A",
                    category_tag = subName,
                    description = "【需求】$title - 誠心求祈福與信仰導引，祈求平安順遂，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#FFCC00",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }

            for (j in 0 until 10) {
                val title = supplyList[j % supplyList.size]
                val contactMethod = contactMethodsQueue[globalCardIndex % contactMethodsQueue.size]
                val contactVal = getContactVal("rel", contactMethod, globalCardIndex)
                val cardLat = baseLat + ((globalCardIndex % 7) - 3) * 0.001
                val cardLng = baseLng + (((globalCardIndex * 2) % 7) - 3) * 0.001

                val card = RequirementCard(
                    card_id = "RELIGION_SUP_${subCode.replace("-", "_")}_${String.format(java.util.Locale.getDefault(), "%03d", j + 1)}",
                    publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                    created_at = System.currentTimeMillis() - (j * 60000L),
                    category_code = subCode,
                    drawer_type = "1B",
                    category_tag = subName,
                    description = "【供給】$title - 信仰祈福專區：點燈求平安、神明祝福與心靈關懷，經驗豐富，歡迎聯繫。",
                    contact_method = contactMethod,
                    contact_value = contactVal,
                    phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                    gps_lat = cardLat,
                    gps_lng = cardLng,
                    mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                    theme_color = "#00E5FF",
                    ttl_hours = 12,
                    is_test_only = true
                )
                generatedCards.add(card)
                globalCardIndex++
            }
        }

        // ==========================================
        // 十三、生成其餘大項目 (11) 的情境預設卡片，補足 88 格網格完整性
        // ==========================================
        val otherGridCodes = Grid88Formatter.getAllGridCodes().filter { !it.startsWith("1-") && !it.startsWith("2-") && !it.startsWith("3-") && !it.startsWith("4-") && !it.startsWith("5-") && !it.startsWith("6-") && !it.startsWith("7-") && !it.startsWith("8-") && !it.startsWith("9-") && !it.startsWith("10-") && !it.startsWith("12-") }
        otherGridCodes.forEachIndexed { index, gridCode ->
            val drawerTitle = CategoryRepository.getCategoryTitle(gridCode)
            val contactMethod = contactMethodsQueue[index % contactMethodsQueue.size]
            val contactVal = getContactVal("other", contactMethod, index + 1100)
            val isDemand = (index % 2 == 0)
            val drawerType = if (isDemand) "1A" else "1B"
            val typePrefix = if (isDemand) "【需求】" else "【供給】"
            val (title, desc) = generateReasonedContext(gridCode, isDemand, index + 1)

            val cardLat = baseLat + ((index % 9) - 4) * 0.0012
            val cardLng = baseLng + (((index * 3) % 9) - 4) * 0.0012

            val card = RequirementCard(
                card_id = "SEARCH_AUTO_CARD_${String.format(java.util.Locale.getDefault(), "%03d", index + 1601)}",
                publisher_device_id = "SEARCH_ENGINE_AUTO_NODE",
                created_at = System.currentTimeMillis(),
                category_code = gridCode,
                drawer_type = drawerType,
                category_tag = drawerTitle,
                description = "$typePrefix $title - $desc [半本效果導引: 請透過 $contactMethod 聯繫]",
                contact_method = contactMethod,
                contact_value = contactVal,
                phone_number = if (contactMethod == ContactMethod.PHONE) contactVal else null,
                gps_lat = cardLat,
                gps_lng = cardLng,
                mapUrl = "https://www.google.com/maps/search/?api=1&query=$cardLat,$cardLng",
                theme_color = "#4285F4",
                ttl_hours = 12,
                is_test_only = true
            )
            generatedCards.add(card)
        }

        // 注入 BaichuanRepository 分片儲存
        BaichuanRepository.injectTestCards(generatedCards)
        safeLogI(TAG, "✅ 「1. 食」至「12. 信仰祈福」類別重構完成！總計生成並發布 ${generatedCards.size} 張卡片至倉儲。")

        return generatedCards
    }

    private fun getContactVal(prefix: String, contactMethod: ContactMethod, index: Int): String {
        return when (contactMethod) {
            ContactMethod.LINE -> "line_${prefix}_${String.format(java.util.Locale.getDefault(), "%02d", index)}"
            ContactMethod.TELEGRAM -> "@tg_${prefix}_${String.format(java.util.Locale.getDefault(), "%02d", index)}"
            ContactMethod.WECHAT -> "wx_${prefix}_${String.format(java.util.Locale.getDefault(), "%02d", index)}"
            ContactMethod.PHONE -> "0988${String.format(java.util.Locale.getDefault(), "%06d", index)}"
        }
    }

    /**
     * 情境推理生成器：根據類別編號與搜尋語意自動推理情境內容 (半本效果導引)
     */
    private fun generateReasonedContext(
        gridCode: String,
        isDemand: Boolean,
        index: Int
    ): Pair<String, String> {
        val categoryMain = gridCode.split("-").firstOrNull() ?: "11"
        return when (categoryMain) {
            "11" -> if (isDemand) "徵求愛心物資與志工服務" to "社區愛心食堂徵求志工夥伴與物資募集。"
                    else "庇護手作與愛心義賣" to "愛心庇護工場手工藝品義賣，邀請您一同響應公益。"
            "12" -> if (isDemand) "祈求心靈平安與點燈祈福" to "祈求家人健康平安，線上點燈與信仰祈福求籤。"
                    else "宮廟聖母與神明祝福" to "信仰祈福專區：點燈求平安、關聖帝君與慈悲祝福。"
            else -> "熱門搜尋項 #$index" to "搜尋引擎情境推理自動匹配內容"
        }
    }
}
