package com.test.testcicd.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.test.testcicd.R

data class HelperUtil(var notification: Builder) {
    val SEPARATOR = "_SEPARATOR_" //分隔符
    private var NOTIFYID = 0x1079 //通知id
    private var CHANEL_ID = "com.helper.util"
    private var CHANEL_NAME = "notification"
    private var CHANEL_DESCRIPTION = "chanel_description"
    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private var notifyidMap: MutableMap<Int, Any> = mutableMapOf()

    companion object {
        @JvmStatic
        fun builder() = HelperUtilBuilder()

    }

    /****
     * 构建通知通道
     */
    private fun createNotificationChannel(): String {
        CHANEL_ID = notification.context.packageName.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 初始化NotificationChannel
            // NotificationManager.IMPORTANCE_NONE              关闭通知
            // NotificationManager.IMPORTANCE_MIN              开启通知，不会弹出，但没有提示音，状态栏中无显示
            // NotificationManager.IMPORTANCE_LOW             开启通知，不会弹出，不发出提示音，状态栏中显示
            // NotificationManager.IMPORTANCE_DEFAULT           开启通知，不会弹出，发出提示音，状态栏中显示
            // NotificationManager. IMPORTANCE_HIGH           开启通知，会弹出，发出提示音，状态栏中显示
            val notificationChannel =
                NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true) //是否在桌面icon右上角展示小红点
            notificationChannel.lightColor = Color.GREEN //小红点颜色
            notificationChannel.setShowBadge(true) //是否在久按桌面图标时显示此渠道的通知
            notificationChannel.description = CHANEL_DESCRIPTION
            // 向系统添加 NotificationChannel。试图创建现有通知
            // 通道的初始值不执行任何操作，因此可以安全地执行
            // 启动顺序
            val notificationManager =
                notification.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            return CHANEL_ID
        } else {
            return CHANEL_ID
        }
    }


    /***
     * 构建普通通知
     */
    fun createNotificationForNormal(): NotificationCompat.Builder {
        notificationManagerCompat = NotificationManagerCompat.from(notification.context)
        var bitmap: Bitmap? = null
        if (notification.largeIcon is Int) {
            bitmap =
                BitmapFactory.decodeResource(notification.context.resources, notification.largeIcon as Int)
        } else if (notification.largeIcon is Bitmap) {
            bitmap = notification.largeIcon as Bitmap
        }
        val name = createNotificationChannel()
        //创建Notification并与Channel关联
        val builder =
            NotificationCompat.Builder(notification.context, name)
        builder.setAutoCancel(notification.clickCancel)
        var pendingIntent: PendingIntent

        //非前台时启动activity
        notification.intent?.let {
            // 适配12.0及以上
            pendingIntent = if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.getActivity(notification.context, 0, it, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(notification.context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            builder.setFullScreenIntent(pendingIntent, true)
        }
        if (notification.icon <= 0) {
            notification.icon = R.color.touMing
        }
        builder.setSmallIcon(notification.icon)
        bitmap?.let { bit ->
            builder.setLargeIcon(bit)
        }
        notification.title?.let { title ->
            builder.setContentTitle(title)
        }
        notification.contentText?.let { text ->
            builder.setContentText(text)
        }
        if (notification.time > 0) {
            builder.setWhen(notification.time)
        } else {
            builder.setWhen(System.currentTimeMillis())
        }
        notification.category?.let {
            builder.setCategory(it)
        }

        builder.build()
        //设置默认的声音与默认的振动
        builder.setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.FLAG_SHOW_LIGHTS)
        notification.intent?.let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            //设置通知栏 点击跳转
            builder.setContentIntent(PendingIntent.getActivities(notification.context, 0, arrayOf(it), PendingIntent.FLAG_IMMUTABLE))
        }
//        //点击启动广播
        if (notification.isReceiver) {
//            val pendingIntent =
//                PendingIntent.getBroadcast(notification.context, 0, notification.intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            builder.addAction(R.mipmap.ic_launcher, "点击安装", pendingIntent)
        }
        if (notification.notifyIds.size > 0) {
            NOTIFYID = notification.notifyIds[0]
        }
        if (notification.isLockScreenShow) {
            // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
            builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        }
        if (notification.notifyIds.size > 0) {
            NOTIFYID = notification.notifyIds[0]
        }
        if (notification.isStart) {
            notificationManagerCompat.notify(NOTIFYID, builder.build())
        }
        return builder
    }

    /***
     * 构建进度通知
     * @param intent 通知点击动作
     */
    fun createNotificationProgress(intent: Intent? = null): NotificationCompat.Builder {
        val builder = createNotificationForNormal()

        if (notification.isShowFrontDesk) {
            notification.title?.let { title ->
                builder.setTicker(title)
            }
            intent?.let {
//             // 适配12.0及以上
                val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= 31) {
                    PendingIntent.getActivity(notification.context, 0, it, PendingIntent.FLAG_IMMUTABLE)
                } else {
                    PendingIntent.getActivity(notification.context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                builder.setFullScreenIntent(pendingIntent, true)
                /* if (notification.isReceiver) {
                     val pendingIntent =
                         PendingIntent.getBroadcast(notification.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                     builder.setContentIntent(pendingIntent)
                     builder.addAction(R.mipmap.ic_launcher, "安装", pendingIntent)
                 }*/
            }

            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            //优先级
            builder.priority = Notification.PRIORITY_HIGH

        }
        //持久性的通知,用户无法删除
        builder.setOngoing(notification.ongoing)
        builder.setProgress(notification.progress!!.progressMax, notification.progress!!.progressCurrent, false)
        return builder

    }

    /***
     * 更新进度通知
     * @param builder 启动的通知
     * @param msg 通知信息思
     * @param progressCurrent 当前进度
     * @param isHide 是否显示
     * @param endAutoCancel 进度完成自动关闭
     * @param endMsg 进度完成信息
     */
    @SuppressLint("RestrictedApi")
    fun updateNotificationProgress(
        builder: NotificationCompat.Builder, intent: Intent?,
        msg: String? = null, endMsg: String? = null, clickText: String? = null,
        progressCurrent: Int, isHide: Boolean, endAutoCancel: Boolean,
        loading: Boolean,
        loadSuccess: Boolean,
        vararg notifyIds: Int) {
        if (notifyIds.isNotEmpty()) {
            NOTIFYID = notifyIds[0]
        }
        msg?.let {
            builder.setContentText(it)
        }
        if (!loading) {
            endMsg?.let {
                builder.setContentText(endMsg)
            }
        }
        //下载进行中
        if (loading) {
            builder.setProgress(notification.progress!!.progressMax, progressCurrent, false)
        } else if (!loadSuccess) {
            print("通知栏 执行安装 下载结束成功与否 $loadSuccess")
            //下载结束
            builder.setProgress(0, 0, false)
            val pendingIntent =
                PendingIntent.getBroadcast(notification.context, 0, intent!!, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(R.mipmap.ic_launcher, clickText, pendingIntent)
            cancelNotification(NOTIFYID)
        }
        if (isHide && builder.priority != NotificationCompat.PRIORITY_DEFAULT) {
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        }
        if (loadSuccess) {
            print("通知栏 执行安装 $loadSuccess")
            val pendingIntent =
                PendingIntent.getBroadcast(notification.context, 0, intent!!, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(R.mipmap.ic_launcher, clickText, pendingIntent)
        }
        notificationManagerCompat.notify(NOTIFYID, builder.build())

    }

    /**
     * @param notifyIds 通知停止
     */
    fun cancelNotification(vararg notifyIds: Int) {
        if (notifyIds.isNotEmpty()) {
            NOTIFYID = notifyIds[0]
        }
        print("通知栏 执行安装 取消 $NOTIFYID")
        notificationManagerCompat.cancel(NOTIFYID)
    }

    /**
     * 取消所有通知
     */
    fun emptyNotification() {
        notificationManagerCompat.cancelAll()
    }

    /**
     * 大图通知
     */
    fun createNotificationImg(): NotificationCompat.Builder {
        val builder = createNotificationForNormal()
        if (notification.iconMax > 0) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(
                    BitmapFactory.decodeResource(
                        notification.context.resources, notification.iconMax
                    )
                )
            )
        }
        return builder
    }

    /**
     * 文本通知
     */
    fun createNotificationText(): NotificationCompat.Builder {
        val builder = createNotificationForNormal()
        notification.text?.let { text ->
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }
        return builder
    }

    /**
     * @param layout   展开布局
     * @param layoutBig 折叠布局
     */
    fun createNotificationFoldView(layout: Int, layoutBig: Int): NotificationCompat.Builder {
        val builder = createNotificationForNormal()
        //展开布局
        var customBig: RemoteViews? = null
        //折叠布局
        var custom: RemoteViews? = null
        if (layoutBig > 0) {
            customBig = RemoteViews(notification.context.packageName, layoutBig)
        }

        if (layout > 0) {
            custom = RemoteViews(notification.context.packageName, layout)
        }
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        //展开布局
        customBig?.let {
            builder.setCustomBigContentView(customBig)
        }
        //折叠布局
        custom?.let {
            builder.setCustomContentView(custom)
        }
        return builder
    }

    /**
     * @param notifyId   通知key
     * @param msgs  消息
     */
    fun createNotificationMsgs(notifyId: Int, vararg msgs: String): NotificationCompat.Builder {
        val builder = createNotificationForNormal()
        if (msgs.isNotEmpty()) {
            val messagingStyle = NotificationCompat.MessagingStyle("Msg")
            for (msg in msgs) {
                val split = msg.split(SEPARATOR)
                when (split.size) {
                    1 -> {
                        messagingStyle.addMessage(msg, System.currentTimeMillis(), "")
                    }

                    2 -> {
                        messagingStyle.addMessage(
                            split[1], System.currentTimeMillis(), split[0]
                        )
                    }

                    3 -> {
                        messagingStyle.addMessage(split[1], split[2].toLong(), split[0])
                    }
                }
            }
            builder.setStyle(messagingStyle)
            notifyidMap[notifyId] = messagingStyle
        }
        return builder
    }

    /**
     * 添加信息通知
     * @param builder   启动的通知
     * @param notifyId 通知 key
     * @param msgs 消息
     */
    fun addLineMsg(builder: NotificationCompat.Builder, notifyId: Int, vararg msgs: String): NotificationCompat.Builder {
        if (msgs.isNotEmpty()) {
            val messagingStyle = notifyidMap[notifyId] as NotificationCompat.MessagingStyle
            for (msg in msgs) {
                val split = msg.split(SEPARATOR)
                when (split.size) {
                    1 -> {
                        messagingStyle.addMessage(msg, System.currentTimeMillis(), "")
                    }

                    2 -> {
                        messagingStyle.addMessage(
                            split[1], System.currentTimeMillis(), split[0]
                        )
                    }

                    3 -> {
                        messagingStyle.addMessage(split[1], split[2].toLong(), split[0])
                    }
                }
            }
            builder.setStyle(messagingStyle)
        }
        NOTIFYID = notifyId
        notificationManagerCompat.notify(NOTIFYID, builder.build())
        return builder
    }

    /**
     * 文本信息通知
     *
     * @param notifyId 通知 key
     * @param texts 长文本消息
     */
    fun createNotificationTexts(notifyId: Int, vararg texts: String): NotificationCompat.Builder {
        val builder = createNotificationForNormal()
        val inboxStyle = NotificationCompat.InboxStyle()
        for (text in texts) {
            inboxStyle.addLine(text)
        }
        builder.setStyle(inboxStyle)
        notifyidMap[notifyId] = inboxStyle
        return builder
    }

    /**
     * 添加文本信息
     *
     * @param builder   启动的通知
     * @param notifyIds 通知 key
     * @param texts 长文本消息
     */
    fun addLineText(builder: NotificationCompat.Builder, notifyId: Int, vararg texts: String): NotificationCompat.Builder {
        if (texts.isNotEmpty()) {
            val inboxStyle = notifyidMap[notifyId] as NotificationCompat.InboxStyle
            for (text in texts) {
                inboxStyle.addLine(text)
            }
            builder.setStyle(inboxStyle)
        }
        NOTIFYID = notifyId
        notificationManagerCompat.notify(NOTIFYID, builder.build())
        return builder
    }

    /**
     * 启动通知
     *
     * @param builder   启动的通知
     * @param notifyIds 通知 key
     */
    fun startNotification(builder: NotificationCompat.Builder, vararg notifyIds: Int) {
        if (notifyIds.isNotEmpty()) {
            NOTIFYID = notifyIds[0]
        }
        notificationManagerCompat.notify(
            NOTIFYID, builder.build()
        )
    }
}

class HelperUtilBuilder {
    private lateinit var config: HelperUtil

    fun build(): HelperUtil = config

    fun addConfig(builder: Builder): HelperUtilBuilder {
        config = HelperUtil(builder)
        return this
    }
}