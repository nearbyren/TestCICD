package com.test.testcicd

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.leon.channel.helper.ChannelReaderUtil
import com.tencent.mmkv.MMKV
import com.test.testcicd.down.DownloadManager
import com.test.testcicd.down.DownloadStatus
import com.test.testcicd.notification.Builder
import com.test.testcicd.notification.HelperUtil
import com.test.testcicd.notification.NotificationProgress
import com.test.testcicd.notification.NotificationReceiver
import com.test.testcicd.room.RoomActivity
import com.test.testcicd.viemodel.FlowEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {

    val URL =
        "https://img.ejiayou.com/upload/2023/4/5608155b-a8b4-4cec-bc50-c3df42d053dc-1681108978538.apk"
    private val channel = Channel<Int>(50)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 线程安全的自增 Int 值
    val count = AtomicInteger(0)

    // 用于验证多线程问题的 Int 数组，初始值都是 -1
    private val testArray = Array(100) { -1 }
    val list = mutableListOf<Int>()
    var flushJob: Job? = null

    fun testChan() {
        // 模拟并发生产日志
        repeat(100) {
            scope.launch(Dispatchers.Default) {
                delay((50L..10_00L).random())
                println("channel Default")
                channel.send(count.getAndIncrement())
            }
        }

        // 模拟串行消费日志
        scope.launch(logDispatcher) {
            channel.consumeEach {
                println("channel consumeEach")
                delay((50L..200L).random())
                log(it)
            }
        }


    }

    var aidlInterface: MyAIDLInterface? = null
    
    private fun aidl() {
        val intent = Intent()
        intent.action = "com.test.testcicd.service.MyService"
        intent.setComponent(ComponentName("com.test.testcicd", "com.test.testcicd.service.MyService"))

        //启动服务 向服务发送请求 处理业务场景并获取返回结果
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                println("aidl onServiceConnected")
                aidlInterface = MyAIDLInterface.Stub.asInterface(p1)
                aidlInterface?.let {
                    it.commonMethod()
                    it.setStringText("发送")
                    it.register(callBackAIDLInterface)
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                println("aidl onServiceConnected")
                aidlInterface?.let {
                    it.unregister(callBackAIDLInterface)
                }

            }

        }, Service.BIND_AUTO_CREATE)
    }

    val callBackAIDLInterface = object : CallBackAIDLInterface.Stub() {
        override fun callBack() {
            println("aidl callBack")
        }

    }

    //模拟日志库入口方法
    private fun log(value: Int) {
        println("channel log $value")
        list.add(value)
        flushJob?.cancel()
        if (list.size >= 5) {// 5条为一批
            flush()
        } else {
            flushJob = delayFlush()
        }
    }

    // 单线程 Dispatcher
    val logDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    // 延迟冲刷
    fun delayFlush() = scope.launch(logDispatcher) {
        delay(100)
        println("channel delayFlush ")
        flush()
    }

    // 冲刷
    private fun flush() {
        // 以批量日志的值赋值测试数组
        list.forEach { testArray[it] = it }
        println("channel flush ")
        list.clear()
        val isOk = testArray.any { it == -1 }
        println("channel isOk $isOk")
    }


    fun down() {
        lifecycleScope.launchWhenCreated {

            val path = getExternalFilesDir(null)?.path
            println("下载 存储路径$path")
            val file = File(path, "aaa.apk")
            DownloadManager.download(URL, file).collect { status ->
                when (status) {
                    is DownloadStatus.Progress -> {
                        findViewById<AppCompatButton>(R.id.text8).text =
                            "${status.value} -  ${status.value}%"
                        println("下载 重 ${status.value} -  ${status.value}%")
                    }
                    is DownloadStatus.Error -> {
                        Toast.makeText(this@MainActivity, "下载错误", Toast.LENGTH_SHORT).show()
                    }
                    is DownloadStatus.Done -> {
                        println("下载 完成")
                        findViewById<AppCompatButton>(R.id.text8).text = "100 100%"
                        Toast.makeText(this@MainActivity, "下载完成", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Log.d("ning", "下载失败.")
                        println("下载 失败")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "所处渠道：${ChannelReaderUtil.getChannel(this)}", Toast.LENGTH_LONG)
                .show()


        FlowEventBus.observe<Event.ShowInit>(this, Lifecycle.State.STARTED) {
            println("我来了 ${it.msg}")
        }
        FlowEventBus.observe<Event.ShowInit2>(this, Lifecycle.State.STARTED) {
            println("我来了 ${it.msg}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pm: PackageManager = packageManager
            //pm.canRequestPackageInstalls() 返回用户是否授予了安装apk的权限
            if (pm.canRequestPackageInstalls()) {
                println("未知应用 已经授权安装")
            } else {
                println("未知应用 未授权安装")
            }
        }

        val is_login = StoreUtils.getInstance().getBoolean("is_login", false) ?: false
        val a1 = StoreUtils.getInstance().getString("a1", "")
        val a2 = StoreUtils.getInstance().getString("a2", "")
        println("测试 a1 $a1  ")
        println("测试 a2 $a2  ")
        if (!is_login) {
            println("测试 存储数据")
            StoreUtils.getInstance().put("is_login", true)
            StoreUtils.getInstance().put("is_login1", true)
            StoreUtils.getInstance().put("is_login2", true)
            StoreUtils.getInstance().put("is_login3", true)
            StoreUtils.getInstance().put("is_login4", true)
            StoreUtils.getInstance().put("is_login5", true)
            StoreUtils.getInstance().put("is_login6", true)
            StoreUtils.getInstance().put("is_login7", true)
            StoreUtils.getInstance().put("fsf", "ssfsdfsf")
            StoreUtils.getInstance().put("a1", "啊哈哈哈合适啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费")
            StoreUtils.getInstance().put("a2", "啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费")
            StoreUtils.getInstance().put("a3", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a4", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a5", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a6", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a7", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a8", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a9", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a10", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a11", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a12", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a13", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a14", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a15", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
        } else {
            println("测试 已经存储")
        }

        findViewById<AppCompatButton>(R.id.text1).setOnClickListener { putong() }
        findViewById<AppCompatButton>(R.id.text2).setOnClickListener { datu() }
        findViewById<AppCompatButton>(R.id.text3).setOnClickListener { changwenben() }
        findViewById<AppCompatButton>(R.id.text4).setOnClickListener { changwenben2() }
        findViewById<AppCompatButton>(R.id.texta).setOnClickListener { changwenben3() }
        findViewById<AppCompatButton>(R.id.text5).setOnClickListener { message2() }
        findViewById<AppCompatButton>(R.id.textb).setOnClickListener { message3() }
        findViewById<AppCompatButton>(R.id.text6).setOnClickListener { jindu() }
        findViewById<AppCompatButton>(R.id.text7).setOnClickListener { custom() }
        findViewById<AppCompatButton>(R.id.text8).setOnClickListener { deepLink() }
        findViewById<AppCompatButton>(R.id.text9).setOnClickListener { down() }
        findViewById<AppCompatButton>(R.id.text10).setOnClickListener { testChan() }
        findViewById<AppCompatButton>(R.id.text13).setOnClickListener { aidl() }
        val llcWeb = findViewById<LinearLayoutCompat>(R.id.llc_web)
        val webView = WebView(this)
        llcWeb.addView(webView)
        webView.loadUrl("file:android_asset/index.html");

        val appimage = findViewById<AppCompatImageView>(R.id.appimage)
        val text11 = findViewById<AppCompatButton>(R.id.text11)

        text11.setOnClickListener {

            // 图片压缩
            val bitmap = zipBitMap()
            appimage.setImageBitmap(bitmap)
            // Toast提示
            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show()
            // 替换原来图片进行渲染
            text11.text = "zipBitMap After"
        }
        findViewById<AppCompatButton>(R.id.text12).setOnClickListener {
            startActivity(Intent(MainActivity@ this, RoomActivity::class.java))
        }

        initNetWork()

    }

    /**
     * 这里呈现的时序图是一个完整的大块，方便演示
     */
    fun zipBitMap(): Bitmap {
        var bitmap: Bitmap
        val options = BitmapFactory.Options()
        //只得到图片的宽和高
        options.inJustDecodeBounds = true

        //设置图片的压缩比例
        options.inSampleSize = computSampleSize(options, 200, 320)
        //设置完压缩比酷之后，必须将这个属性改为false
        options.inJustDecodeBounds = false
        // 尺寸压缩1/2
        options.inSampleSize = 4

//        var i = 1
//        while (i < 200) {  // 重复500次 模拟耗时操作
//            bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_avatar, options)
//            i++
//        }

        //得到传递过来的图片的信息
        return BitmapFactory.decodeResource(resources, R.drawable.ic_avatar, options)
    }

    private fun computSampleSize(options: BitmapFactory.Options, w: Int, h: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        //图片的缩小比例，只要小于等于，就是保持原图片的大小不变
        var inSqmpleSize = 1
        if (width > w || height > h) {
            val zipSizeWidth = Math.round((width / w).toFloat())
            val zipSizeHeight = Math.round((height / h).toFloat())
            inSqmpleSize = if (zipSizeWidth < zipSizeHeight) zipSizeWidth else zipSizeHeight
        }
        return inSqmpleSize
    }

    fun putong() {
        val helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, title = "普通通知", icon = R.mipmap.ic_avatar, largeIcon = R.mipmap.ic_avatar, contentText = "我是通知内容")).build()
        helperUtil.createNotificationForNormal().let {
            helperUtil.startNotification(it, 1)
            StoreUtils.getInstance().put("a1", "啊哈哈哈合适啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费")
            StoreUtils.getInstance().put("a2", "啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费")
            StoreUtils.getInstance().put("a3", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a4", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a5", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a6", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a7", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a8", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a9", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a10", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a11", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a12", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a13", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a14", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")
            StoreUtils.getInstance().put("a15", "啊哈啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费啊哈哈哈合适的话辅导费辅导费")

        }
    }

    fun datu() {
        val helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, title = "大图通知", icon = R.mipmap.ic_avatar, largeIcon = R.mipmap.ic_avatar, contentText = "我是通知内容", iconMax = R.drawable.ic_big_pic2)).build()
        helperUtil.createNotificationImg().let {
            helperUtil.startNotification(it, 2)
        }
    }

    fun changwenben() {
        val helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, title = "文本通知", icon = R.mipmap.ic_avatar, largeIcon = R.mipmap.ic_avatar, text = "应用签名系统主要负责鸿蒙hap应用包的签名完整性校验，以及应用来源识别等功能。应用完整性校验模块给其他模块提供的接口；通过验签，保障应用包完整性，防篡改；通过匹配签名证书链与可信源列表，识别应用来源。")).build()
        helperUtil.createNotificationText().let {
            helperUtil.startNotification(it, 3)
        }
    }

    var builder: NotificationCompat.Builder? = null
    var helperUtil: HelperUtil? = null
    fun changwenben2() {
        helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, icon = R.mipmap.ic_avatar, largeIcon = R.mipmap.ic_avatar, isLockScreenShow = true, title = "长文本通知")).build()
        builder = helperUtil?.createNotificationTexts(
            4,
            "1.ChatGPT是一种由OpenAI训练的大型语言模型。它的原理是基于Transformer架构，通过预训练大量文本数据来学习如何生成人类可读的文本，然后通过接受输入并生成输出来实现对话。",
            "2.ChatGPT的用途非常广泛，可以用于自然语言处理（NLP）任务，如对话生成、问答系统、文本生成等。",
            "3.如果你想使用ChatGPT，你可以用它来构建对话机器人，回答问题，生成文本等。它的应用非常广泛，可以用于各种场景，如客服、帮助提供者、教育机构等。"
        )
        builder?.let {
            helperUtil?.startNotification(it, 4)
        }
    }

    fun changwenben3() {
        helperUtil?.addLineText(builder!!, 4, "4...", "5...", "6...")
        helperUtil?.startNotification(builder!!, 4)
    }

    fun message2() {
        helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, icon = R.mipmap.ic_avatar, largeIcon = R.mipmap.ic_avatar, title = "消息通知")).build()
        builder = helperUtil?.createNotificationMsgs(
            5,
            "王重阳" + helperUtil?.SEPARATOR + "大清早是习武最好的时间！",
            "周伯通" + helperUtil?.SEPARATOR + "大兄弟，你来啦！快来看看我这秀字的蜜蜂"
        )
        builder?.let {
            helperUtil?.startNotification(it, 5)
        }
    }

    fun message3() {
        helperUtil?.addLineMsg(
            builder!!, 5,
            "郭靖" + helperUtil?.SEPARATOR + "我不玩，华山论剑就只剩下2年了，我要继续加油。",
        )
        helperUtil?.startNotification(builder!!, 5)
    }

    fun jindu() {
        val helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, icon = R.mipmap.ic_avatar, largeIcon = R.mipmap.ic_avatar, title = "进度通知", progress = (NotificationProgress(progressCurrent = 20, progressMax = 100)))).build()
        val intent2 = Intent(this, NotificationReceiver::class.java)
        intent2.action = "ejiayou.datacenter.module.apk"
        intent2.putExtra("url", "url")
        intent2.putExtra("loading", true)
        val builder = helperUtil.createNotificationProgress(intent2)
        builder.let { helperUtil.startNotification(it, 6) }
        runOnUiThread {
            for (i in 20 until 101) {
                Thread.sleep(80)
                print("当前进度位置 $i")
                builder.let { bu ->

                    if (i == 100) {
                        intent2.putExtra("loading", false)
                        intent2.putExtra("success", true)
                        helperUtil.updateNotificationProgress(
                            builder = builder!!,
                            intent = intent2,
                            msg = "正在下载 $i %", endMsg = "下载成功", clickText = "点击安装",
                            progressCurrent = i, isHide = true,
                            endAutoCancel = false,
                            loading = false,
                            loadSuccess = true,
                            6
                        )
                    } else if (i == 130) {
                        intent2.putExtra("success", false)
                        intent2.putExtra("loading", false)
                        helperUtil.updateNotificationProgress(
                            builder = builder!!,
                            intent = intent2,
                            msg = "正在下载 $i %", endMsg = "下载出现异常", clickText = "点击取消",
                            progressCurrent = i, isHide = true,
                            endAutoCancel = false,
                            loading = false,
                            loadSuccess = false,
                            6
                        )
                        return@runOnUiThread
                    } else {
                        helperUtil.updateNotificationProgress(
                            builder = builder!!,
                            intent = intent2,
                            msg = "正在下载 $i %", endMsg = "正在下载", clickText = "",
                            progressCurrent = i, isHide = true,
                            endAutoCancel = false,
                            loading = true,
                            loadSuccess = false,
                            6
                        )
                    }


                }
            }
        }

    }

    fun custom() {
        val helperUtil =
            HelperUtil.builder().addConfig(Builder(context = this, icon = R.mipmap.ic_avatar)).build()
        val builder =
            helperUtil.createNotificationFoldView(R.layout.item_notification, R.layout.item_notification2)
        builder.let { helperUtil.startNotification(it, 7) }

    }

    private fun initNetWork() {
//        print("我是 is  initNetWork:${javaClass.name}")
//        NetworkChangedReceiver.getInstance()
//            .registerListener(object : NetworkChangedReceiver.OnNetworkStatusChangedListener {
//                override fun onConnected(networkType: NetworkUtils.NetworkType?) {
//                    Logger.d("覆盖 网络监听 onConnected ${this.javaClass}")
//                    showStateContent()//将原来内容显示处理啊 在附加旧的
//                    netWorkSuccess()
//                }
//
//                override fun onDisconnected() {
//                    Logger.d("覆盖 网络监听  onDisconnected ${this.javaClass}")
//                    netWorkFailure()
////                ToastUtils.showToast(this@BaseActivityKot, "网络异常,请重试网络")
//                }
//            })
        //获取ConnectivityManager
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //创建NetworkRequest对象，定制化监听
        val networkRequest = NetworkRequest.Builder()
                //移动网络
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                //wifi网络
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            //当网络连接可用时回调
            override fun onAvailable(network: Network) {
//                print("我是 is  onAvailable:");
//                print("我是 is  onAvailable:${javaClass.name}");

            }

            //当网络断开时回调
            override fun onLost(network: Network) {
//                print("我是 is  onLost:");
//                print("我是 is  onLost:${javaClass.name}");

            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isMobileNetwork =
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                val isWifiNetwork =
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                val notUseVPN =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
//                print("我是 is mobile network:" + isMobileNetwork + ";is wifi network:" + isWifiNetwork + "; use vpn:" + !notUseVPN);
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun deepLink() {
        FlowEventBus.post(Event.ShowInit("哈哈哈"), 6000)
        FlowEventBus.post(Event.ShowInit2("嘻嘻嘻"), 1002)

//        FlowEvent.observeEvent {
//            println("11111")
//        }
//        FlowEvent.postEvent(1)

//        val url1 = "ejiayou://?text=cccc"
//        val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url1))

//        val url2 = "ejiayou://ejiayou.com?text=123&name=ejiayou"
//        val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url2))

//        val url3 = "ejiayou://ejiayou.com/match?text=123&name=ejiayou"
//        val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url3))
//      startActivity(intent);
    }
}