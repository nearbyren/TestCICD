package com.test.testcicd.notification

import android.content.Context
import android.content.Intent

class NotificationProgress(
    var progressCurrent: Int = 0,
    var progressMax: Int = 100,
)

class Builder(
    //上下文
    var context: Context,
    //标题
    var title: String? = null,
    //消息内容
    var contentText: String? = null,
    //左上角图片
    var icon: Int = 0,
    //右下角图片
    var largeIcon: Any? = null,
    //发送时间
    var time: Long = 0,
    //通知栏图标
    var smallIcon: Int = 0,
    //大图通知
    var iconMax: Int = -1,
    //通知类别
    var category : String? = null,
    //是否为后台任务
    var ongoing :Boolean = false,
    //文本通知
    var text: String? = null,
    //长文本通知
    var texts: MutableList<String> = mutableListOf(),
    //消息通知
    var msgs: MutableList<String> = mutableListOf(),
    //进度条通知
    var progress: NotificationProgress? = null,
    //点击通知栏动作
    var intent: Intent? = null,
    //滑动移除
    var delIntent:Intent?=null,
    //是否为广播动作
    var isReceiver : Boolean = false,
    //是否点击取消
    var clickCancel: Boolean = true,
    //是否锁屏显示
    var isLockScreenShow: Boolean = true,
    //是否直接启动通知
    var isStart: Boolean = false,
    //是否悬浮显示
    var isShowFrontDesk: Boolean = false,
    //通知 key
    var notifyIds: MutableList<Int> = mutableListOf(1)
)
