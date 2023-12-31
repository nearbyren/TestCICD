package com.test.testcicd.notification

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import java.lang.reflect.Field

object NotificationUtil {
    private const val CHECK_OP_NO_THROW = "checkOpNoThrow"
    private const val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
    //调用该方法获取是否开启通知栏权限
    fun isNotifyEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isEnableV26(context)
        } else {
            isEnabledV19(context)
        }
    }
    /**
     * 8.0以下判断
     *
     * @param context api19  4.4及以上判断
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun isEnabledV19(context: Context): Boolean {
        val mAppOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val appInfo = context.applicationInfo
        val pkg = context.applicationContext.packageName
        val uid = appInfo.uid
        var appOpsClass: Class<*>? = null
        try {
            appOpsClass = Class.forName(AppOpsManager::class.java.name)
            val checkOpNoThrowMethod = appOpsClass.getMethod(
                CHECK_OP_NO_THROW,
                Integer.TYPE, Integer.TYPE, String::class.java
            )
            val opPostNotificationValue: Field = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
            val value = opPostNotificationValue.get(Int::class.java) as Int
            return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int ==
                    AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    /**
     * 8.0及以上通知权限判断
     *
     * @param context
     * @return
     */
    private fun isEnableV26(context: Context): Boolean {
        val appInfo = context.applicationInfo
        val pkg = context.applicationContext.packageName
        val uid = appInfo.uid
        return try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val sServiceField = notificationManager.javaClass.getDeclaredMethod("getService")
            sServiceField.isAccessible = true
            val sService = sServiceField.invoke(notificationManager)
            val method = sService.javaClass.getDeclaredMethod(
                "areNotificationsEnabledForPackage", String::class.java, Integer.TYPE
            )
            method.isAccessible = true
            method.invoke(sService, pkg, uid) as Boolean
        } catch (e: Exception) {
            true
        }
    }
    /**
     * 通知权限申请
     * @param context
     */
    fun requestNotify(context: Context) {
        /**
         * 跳到通知栏设置界面
         * @param context
         */
        val localIntent = Intent()
        ///< 直接跳转到应用通知设置的代码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O
        ) {
            localIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            localIntent.putExtra("app_package", context.packageName)
            localIntent.putExtra("app_uid", context.applicationInfo.uid)
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            localIntent.addCategory(Intent.CATEGORY_DEFAULT)
            localIntent.data = Uri.parse("package:" + context.packageName)
        } else {
            ///< 4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                localIntent.data = Uri.fromParts("package", context.packageName, null)
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.action = Intent.ACTION_VIEW
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
            }
        }
        context.startActivity(localIntent)
    }
}
