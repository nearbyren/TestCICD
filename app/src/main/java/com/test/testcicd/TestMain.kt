package com.test.testcicd

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.atomic.AtomicInteger

class TestMain {

    private val channel = Channel<Any>(50)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    // 线程安全的自增 Int 值
    val count = AtomicInteger(0)
    // 用于验证多线程问题的 Int 数组，初始值都是 -1
    private val testArray = Array(100) { -1 }
    val list = mutableListOf<Int>()
    var flushJob: Job? = null

    fun test(){
        // 模拟并发生产日志
        repeat(100) {
            scope.launch(Dispatchers.Default) {
                delay((50L..10_00L).random())
                channel.send(count.getAndIncrement())
            }
        }

// 模拟串行消费日志
        scope.launch {
            channel.consumeEach {
                delay((50L..200L).random())
                log(it as Int)
            }
        }
    }

    //模拟日志库入口方法
    private fun log(value: Int) {
        list.add(value)
        flushJob?.cancel()
        if (list.size >= 5) {// 5条为一批
            flush()
        } else {
            flushJob = delayFlush()
        }
    }

    // 延迟冲刷
    fun delayFlush() = scope.launch {
        delay(100)
        flush()
    }

    // 冲刷
    private fun flush() {
        // 以批量日志的值赋值测试数组
        list.forEach { testArray[it] = it }
        list.clear()
    }

}