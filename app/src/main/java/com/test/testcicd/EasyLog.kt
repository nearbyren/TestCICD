package com.test.testcicd

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

object EasyLog {
    // 容量为50的 Channel
    private val channel = Channel<Any>(50)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var maxSize = 50
    private val logs = mutableListOf<Any>()
    // 冲刷job
    private var flushJob: Job? = null
    init {
        scope.launch { channel.consumeEach { innerLog(it) } }
    }

    fun log(any: Any){
        scope.launch { channel.send(any) }
    }

    private fun innerLog(any: Any){
        logs.add(any)
        flushJob?.cancel() // 取消上一次倒计时
        // 若日志数量达到阈值，则直接冲刷，否则延迟冲刷
        if (logs.size >= maxSize) {
            flush()
        } else {
            flushJob = delayFlush()
        }
    }

    // 冲刷：上传内存中堆积的批量日志
    private fun flush() {
//        uploadLogs(logs)
        logs.clear()
    }

    // 延迟冲刷
    private fun delayFlush() = scope.launch {
        delay(5000)// 延迟5秒，如果没有新日志产生，则冲刷
        flush()
    }
}
