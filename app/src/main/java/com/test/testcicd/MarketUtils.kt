package com.test.testcicd

import android.content.Intent

import android.text.TextUtils

import android.content.pm.PackageManager

import android.content.pm.PackageInfo

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import java.lang.Exception
import java.util.*


object MarketUtils {
    private var tools: MarketUtils? = null
    private val schemaUrl = "market://details?id="

    fun getTools(): MarketUtils? {
        if (null == tools) {
            tools = MarketUtils
        }
        return tools
    }

    /***
     * 不指定包名
     * @param mContext
     */
    fun startMarket(mContext: Context) {
        val packageName: String = mContext.packageName //得到包名
        startMarket(mContext, packageName)
    }

    /**
     * 指定包名
     *
     * @param mContext
     * @param packageName
     */
    fun startMarket(mContext: Context, packageName: String): Boolean {
        try {
            val deviceBrand = getDeviceBrand() //获得手机厂商
            //根据厂商获取对应市场的包名
            val brandName = deviceBrand.uppercase(Locale.getDefault()) //大写
            if (TextUtils.isEmpty(brandName)) {
                Log.e("MarketUtils", "没有读取到手机厂商~~")
                return false
            }
            val marketPackageName = getBrandName(brandName)
            if (null == marketPackageName || "" == marketPackageName) {
                //手机不再列表里面,去尝试寻找
                //检测百度和应用宝是否在手机上安装,如果安装，则跳转到这两个市场的其中一个
                val isExit1 = isCheckBaiduOrYYB(mContext, PACKAGE_NAME.BAIDU_PACKAGE_NAME)
                if (isExit1) {
                    startMarket(mContext, packageName, PACKAGE_NAME.BAIDU_PACKAGE_NAME)
                    return true
                }
                val isExit2 = isCheckBaiduOrYYB(mContext, PACKAGE_NAME.TENCENT_PACKAGE_NAME)
                if (isExit2) {
                    startMarket(mContext, packageName, PACKAGE_NAME.TENCENT_PACKAGE_NAME)
                    return true
                }
            }
            startMarket(mContext, packageName, marketPackageName)
            return true
        } catch (anf: ActivityNotFoundException) {
            Log.e("MarketUtils", "要跳转的应用市场不存在!")
        } catch (e: Exception) {
            Log.e("MarketUtils", "其他错误：" + e.message)
        }
        return false
    }

    /***
     * 指定包名，指定市场
     * @param mContext
     * @param packageName
     * @param marketPackageName
     */
    fun startMarket(mContext: Context, packageName: String, marketPackageName: String?) {
        try {
            openMarket(mContext, packageName, marketPackageName)
        } catch (anf: ActivityNotFoundException) {
            Log.e("MarketUtils", "要跳转的应用市场不存在!")
        } catch (e: Exception) {
            Log.e("MarketUtils", "其他错误：" + e.message)
        }
    }

    /***
     * 打开应用市场
     * @param mContext
     * @param packageName
     * @param marketPackageName
     */
    private fun openMarket(mContext: Context, packageName: String, marketPackageName: String?) {
        try {
            val uri: Uri = Uri.parse(schemaUrl + packageName)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage(marketPackageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
        } catch (anf: ActivityNotFoundException) {
            Log.e("MarketUtils", "要跳转的应用市场不存在!")
        } catch (e: Exception) {
            Log.e("MarketUtils", "其他错误：" + e.message)
        }
    }

    /***
     * 检测是否是应用宝或者是百度市场
     * @param mContext
     * @param packageName
     * @return
     */
    private fun isCheckBaiduOrYYB(mContext: Context, packageName: String): Boolean {
        return isInstalled(packageName, mContext)
    }

    /****
     * 检查APP是否安装成功
     * @param packageName
     * @param context
     * @return
     */
    private fun isInstalled(packageName: String, context: Context): Boolean {
        if ("" == packageName || packageName.length <= 0) {
            return false
        }
        val packageInfo: PackageInfo?
        packageInfo = try {
            context.getPackageManager().getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return if (packageInfo == null) {
            false
        } else {
            true
        }
    }

    private fun getBrandName(brandName: String): String? {
        if (BRAND.HUAWEI_BRAND == brandName) {
            //华为
            return PACKAGE_NAME.HUAWEI_PACKAGE_NAME
        } else if (BRAND.OPPO_BRAND == brandName) {
            //oppo
            return PACKAGE_NAME.OPPO_PACKAGE_NAME
        } else if (BRAND.VIVO_BRAND == brandName) {
            //vivo
            return PACKAGE_NAME.VIVO_PACKAGE_NAME
        } else if (BRAND.XIAOMI_BRAND == brandName) {
            //小米
            return PACKAGE_NAME.XIAOMI_PACKAGE_NAME
        } else if (BRAND.LENOVO_BRAND == brandName) {
            //联想
            return PACKAGE_NAME.LIANXIANG_PACKAGE_NAME
        } else if (BRAND.QH360_BRAND == brandName) {
            //360
            return PACKAGE_NAME.QH360_PACKAGE_NAME
        } else if (BRAND.MEIZU_BRAND == brandName) {
            //魅族
            return PACKAGE_NAME.MEIZU_PACKAGE_NAME
        } else if (BRAND.HONOR_BRAND == brandName) {
            //华为
            return PACKAGE_NAME.HUAWEI_PACKAGE_NAME
        } else if (BRAND.XIAOLAJIAO_BRAND == brandName) {
            //小辣椒
            return PACKAGE_NAME.ZHUOYI_PACKAGE_NAME
        } else if (BRAND.ZTE_BRAND == brandName) {
            //zte
            return PACKAGE_NAME.ZTE_PACKAGE_NAME
        } else if (BRAND.NIUBIA_BRAND == brandName) {
            //努比亚
            return PACKAGE_NAME.NIUBIA_PACKAGE_NAME
        } else if (BRAND.ONE_PLUS_BRAND == brandName) {
            //OnePlus
            return PACKAGE_NAME.OPPO_PACKAGE_NAME
        } else if (BRAND.MEITU_BRAND == brandName) {
            //美图
            return PACKAGE_NAME.MEITU_PACKAGE_NAME
        } else if (BRAND.SONY_BRAND == brandName) {
            //索尼
            return PACKAGE_NAME.GOOGLE_PACKAGE_NAME
        } else if (BRAND.GOOGLE_BRAND == brandName) {
            //google
            return PACKAGE_NAME.GOOGLE_PACKAGE_NAME
        }
        return ""
    }

    /**
     * 获取手机厂商
     */
    private fun getDeviceBrand(): String {
        return Build.BRAND
    }

    object BRAND {
        const val HUAWEI_BRAND = "HUAWEI" //HUAWEI_PACKAGE_NAME
        const val HONOR_BRAND = "HONOR" //HUAWEI_PACKAGE_NAME
        const val OPPO_BRAND = "OPPO" //OPPO_PACKAGE_NAME
        const val MEIZU_BRAND = "MEIZU" //MEIZU_PACKAGE_NAME
        const val VIVO_BRAND = "VIVO" //VIVO_PACKAGE_NAME
        const val XIAOMI_BRAND = "XIAOMI" //XIAOMI_PACKAGE_NAME
        const val LENOVO_BRAND = "LENOVO" //LIANXIANG_PACKAGE_NAME //Lenovo
        const val ZTE_BRAND = "ZTE" //ZTE_PACKAGE_NAME
        const val XIAOLAJIAO_BRAND = "XIAOLAJIAO" //ZHUOYI_PACKAGE_NAME
        const val QH360_BRAND = "360" //QH360_PACKAGE_NAME
        const val NIUBIA_BRAND = "NUBIA" //NIUBIA_PACKAGE_NAME
        const val ONE_PLUS_BRAND = "ONEPLUS" //OPPO_PACKAGE_NAME
        const val MEITU_BRAND = "MEITU" //MEITU_PACKAGE_NAME
        const val SONY_BRAND = "SONY" //GOOGLE_PACKAGE_NAME
        const val GOOGLE_BRAND = "GOOGLE" //GOOGLE_PACKAGE_NAME
        const val HTC_BRAND = "HTC" //未知应用商店包名
        const val ZUK_BRAND = "ZUK" //未知应用商店包名
    }

    /** Redmi*/
    /** Redmi */
    /**
     * 华为，oppo,vivo,小米，360，联想，魅族，安智，百度，阿里，应用宝，goog，豌豆荚，pp助手
     */
    object PACKAGE_NAME {
        const val OPPO_PACKAGE_NAME = "com.oppo.market" //oppo
        const val VIVO_PACKAGE_NAME = "com.bbk.appstore" //vivo
        const val HUAWEI_PACKAGE_NAME = "com.huawei.appmarket" //华为
        const val QH360_PACKAGE_NAME = "com.qihoo.appstore" //360
        const val XIAOMI_PACKAGE_NAME = "com.xiaomi.market" //小米
        const val MEIZU_PACKAGE_NAME = "com.meizu.mstore" //，魅族
        const val LIANXIANG_PACKAGE_NAME = "com.lenovo.leos.appstore" //联想
        const val ZTE_PACKAGE_NAME = "zte.com.market" //zte
        const val ZHUOYI_PACKAGE_NAME = "com.zhuoyi.market" //卓易
        const val GOOGLE_PACKAGE_NAME = "com.android.vending" //google
        const val NIUBIA_PACKAGE_NAME = "com.nubia.neostore" //努比亚
        const val MEITU_PACKAGE_NAME = "com.android.mobile.appstore" //美图
        const val BAIDU_PACKAGE_NAME = "com.baidu.appsearch" //baidu
        const val TENCENT_PACKAGE_NAME = "com.tencent.android.qqdownloader" //应用宝
        const val PPZHUSHOU_PACKAGE_NAME = "com.pp.assistant" //pp助手
        const val ANZHI_PACKAGE_NAME = "com.goapk.market" //安智市场
        const val WANDOUJIA_PACKAGE_NAME = "com.wandoujia.phonenix2" //豌豆荚
        //        public static final String SUONI_PACKAGE_NAME = "com.android.vending";//索尼
    }

    /**
     * 启动到应用商店app详情界面
     * @param appPkg    目标App的包名
     * @param marketPkg 应用商店包名 ,如果为"" 则由系统弹出应用商店
     * 列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
     */
    fun launchAppDetail(context: Context, appPkg: String, marketPkg: String?) {
        try {
            if (TextUtils.isEmpty(appPkg)) return
            val uri: Uri = Uri.parse("market://details?id=$appPkg")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}