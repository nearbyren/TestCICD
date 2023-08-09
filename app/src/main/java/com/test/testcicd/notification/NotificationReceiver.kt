package com.test.testcicd.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        println("通知栏 onReceive")
        intent?.let {
            if (it.action == "ejiayou.datacenter.module.apk") {
                val url = it.getStringExtra("url") ?: ""
                //是否下载完成
                val success = it.getBooleanExtra("success", false)
                //是否正在下载
                val loading = it.getBooleanExtra("loading", true)
                println("通知栏 执行安装 $url - 下载是否完成 = $success 下载是否进行中 = $loading")
                if (!success) {
//                    context?.let { context ->
//                        if (!loading) {
//                            NotificationManagerCompat.from(context).cancel(6)
//                        }
//                    }
                    return
                }
                installApk(context, url)
            }
        }
    }

    private fun installApk(context: Context?, url: String) {
        context?.let {
            val pFile =
                File(url)
            if (!pFile.exists()) return
            val intent =
                Intent(Intent.ACTION_VIEW)
            val uri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                uri =
                    FileProvider.getUriForFile(it, "${it.packageName}.fileprovider", pFile)
            } else {
                uri =
                    Uri.fromFile(pFile)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            it.startActivity(intent)
        }
    }

}

