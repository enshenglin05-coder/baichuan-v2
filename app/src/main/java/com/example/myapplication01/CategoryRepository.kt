package com.example.myapplication01

data class CategoryItem(
    val id: String,
    val displayName: String,
    val description: String = ""
)

object CategoryRepository {
    
    val foodCategoryList = createList("1", listOf(
        "在地攤商 / 個人私廚",
        "火鍋燒肉 / 牛排鐵板",
        "在地小吃 / 便當快餐",
        "熱炒合菜 / 中式餐廳",
        "異國料理 / 日韓泰法",
        "手搖飲料 / 冰品甜湯",
        "早午輕食 / 咖啡甜點",
        "宵夜炸物 / 酒吧滷味"
    ))
    val clothingCategoryList = createList("2", listOf(
        "潮流服飾",
        "飾品配件",
        "鞋類精品",
        "運動戶外",
        "內衣睡衣",
        "洗滌二手",
        "童裝孕婦",
        "綜合其它"
    ))
    val housingCategoryList = createList("3", listOf(
        "租屋資訊",
        "裝潢修繕",
        "房產買賣",
        "水電鎖印",
        "清潔家政",
        "搬家儲藏",
        "家具家電",
        "綜合其它"
    ))
    val transportCategoryList = createList("4", listOf(
        "機車跑腿 / 急件順風",
        "貨車搬運 / 載貨接單",
        "計程車叫車 / 長距接送",
        "機車維修 / 保養洗車",
        "汽車修護 / 美容輪胎",
        "租車服務 / 共享出行",
        "拖吊救援 / 代駕服務",
        "綜合其它 / 出行站點"
    ))
    val educationCategoryList = createList("5", listOf(
        "幼兒託管 / 保母家教",
        "才藝培訓 / 音樂美術",
        "補習輔導 / 升學檢定",
        "運動健身 / 私人教練",
        "語言學習 / 留學檢定",
        "駕訓考照 / 汽機車班",
        "職業技能 / 證照培訓",
        "綜合其它 / 學習資訊"
    ))
    val entertainmentCategoryList = createList("6", listOf(
        "KTV 視聽 / 酒吧夜生活",
        "影城劇院 / 展覽演出",
        "按摩 SPA / 美容美體",
        "網咖電競 / 桌遊密室",
        "親子樂園 / 戶外休閒",
        "美甲美睫 / 美髮理容",
        "運動場館 / 保齡球館",
        "綜合其它 / 休閒票券"
    ))
    val medicalCategoryList = createList("7", listOf(
        "藥局藥品 / 保健送藥",
        "看護照顧 / 居家照護",
        "診所門診 / 掛號諮詢",
        "醫美整形 / 皮膚管理",
        "健檢中心 / 醫事檢驗",
        "輔具租售 / 醫療器材",
        "復健物理 / 中醫針灸",
        "綜合其它 / 健康諮詢"
    ))
    val secondhandCategoryList = createList("8", listOf(
        "二手手機 / 數碼 3C",
        "家電傢俱 / 居家閒置",
        "汽機買賣 / 二輪四輪",
        "精品服飾 / 包款古著",
        "母嬰用品 / 玩具童車",
        "戶外裝備 / 運動器材",
        "遊戲電玩 / 動漫周邊",
        "綜合閒置 / 在地面交"
    ))
    val petCategoryList = createList("9", listOf(
        "寵物洗澡 / 美容造型",
        "獸醫門診 / 急診疫苗",
        "寵物寄養 / 日託安親",
        "寵物接送 / 專車跑腿",
        "到府餵食 / 代遛寵物",
        "走失協尋 / 送養領養",
        "寵物用品 / 主糧零食",
        "綜合其它 / 寵物生命"
    ))
    val serviceCategoryList = createList("10", listOf(
        "臨時短工 / 活動人力",
        "專業修繕 / 裝潢水電",
        "家事清潔 / 消毒除蟲",
        "代辦跑腿 / 現場排隊",
        "活動企劃 / 攝影主持",
        "緊急求助 / 在地互助",
        "專業諮詢 / 代寫翻譯",
        "綜合服務 / 其它幫忙"
    ))
    
    // 第 11 個大項目：公益關懷 (Charity Care)
    val reservedCategoryList = createList("11", listOf(
        "庇護手作 / 愛心義賣",
        "志工招募 / 義工服務",
        "物資募集 / 浪物捐贈",
        "愛心餐食 / 待用餐點",
        "浪浪救助 / 中途送養",
        "義剪義診 / 資源援助",
        "社區關懷 / 長者陪伴",
        "環境保護 / 淨灘義工"
    ))

    // 第 12 個大項目：信仰祈福 (Faith & Religion)
    val religionCategoryList = createList("12", listOf(
        "土地公 / 福德正神",
        "關聖帝君 / 恩主公",
        "天上聖母 / 媽祖",
        "基督教 / 代禱祈禱",
        "佛教感應 / 點燈祈福",
        "天主教 / 祈禱祈福",
        "心靈平安 / 普渡祈福",
        "綜合信仰 / 靈修祈福"
    ))

    private fun createList(mainId: String, names: List<String>): List<CategoryItem> {
        return names.mapIndexed { index, name ->
            CategoryItem("$mainId-${index + 1}", name)
        }
    }

    fun getSubItems(categoryIndex: String): List<CategoryItem> {
        return when (categoryIndex) {
            "1" -> foodCategoryList
            "2" -> clothingCategoryList
            "3" -> housingCategoryList
            "4" -> transportCategoryList
            "5" -> educationCategoryList
            "6" -> entertainmentCategoryList
            "7" -> medicalCategoryList
            "8" -> secondhandCategoryList
            "9" -> petCategoryList
            "10" -> serviceCategoryList
            "11" -> reservedCategoryList
            "12" -> religionCategoryList
            else -> emptyList()
        }
    }

    fun getAllCategoryIds(): List<String> {
        val list = mutableListOf("0-0") // 總覽主格
        for (main in 1..12) {
            for (sub in 1..8) {
                list.add("$main-$sub")
            }
        }
        return list
    }

    /**
     * 獲取小項目標題文字 (drawer_title)
     * 用於動態 UI 文字對齊
     */
    fun getCategoryTitle(categoryCode: String, context: android.content.Context? = null): String {
        if (categoryCode == "0-0") return "12大項 97格 總覽主格"
        val parts = categoryCode.split("-")
        if (parts.size != 2) return "未知項目"
        val main = parts[0]
        val subIndex = parts[1].toIntOrNull()?.minus(1) ?: return "未知項目"
        
        if (context != null) {
            val resName = "sub_item_${main}_${subIndex + 1}"
            val resId = context.resources.getIdentifier(resName, "string", context.packageName)
            if (resId != 0) {
                return context.getString(resId).replace("\n", " ")
            }
        }

        val list = getSubItems(main)
        return if (subIndex in list.indices) list[subIndex].displayName else "預留項目"
    }
}
