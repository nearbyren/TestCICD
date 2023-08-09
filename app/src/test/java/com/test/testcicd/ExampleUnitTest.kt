package com.test.testcicd

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import org.junit.Test

import org.junit.Assert.*
import java.lang.Exception
import java.util.concurrent.atomic.AtomicInteger

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }




    @Test
    fun testException() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("exceptionHandler${coroutineContext[CoroutineName].toString()} 处理异常 ：$throwable")
        }
        val supervisorScope = CoroutineScope(SupervisorJob() + exceptionHandler)
        with(supervisorScope) {
            launch(CoroutineName("异常子协程")) {
                println("${Thread.currentThread().name}我要开始抛异常了")
                throw NullPointerException("空指针异常")
            }
            for (index in 0..10) {
                launch(CoroutineName("子协程$index")) {
                    println("${Thread.currentThread().name}正常执行$index")
                    if (index % 3 == 0) {
                        throw NullPointerException("子协程${index}空指针异常")
                    }
                }
            }
        }
    }


    fun catch2() :Int{
        return try {
            // 执行一些代码
            println("Finally block executed 1 ")
            1
        } catch (e: Exception) {
            // 异常处理
            println("Finally block executed 2 ")
            2
        } finally {
            System.exit(0)
            // 无论如何都会执行的代码
            println("Finally block executed 3")
        }
    }
    @Test
    fun  testCatch(){
        catch2()
    }

    @Test
    fun TestSleep() {
        task3(task1(), task2())

    }

    val task1: () -> String = {
        Thread.sleep(2000)
        "Hello".also { println("task1 finished: $it") }
    }

    val task2: () -> String = {
        Thread.sleep(2000)
        "World".also { println("task2 finished: $it") }
    }

    val task3: (String, String) -> String = { p1, p2 ->
        Thread.sleep(2000)
        "$p1 $p2".also { println("task3 finished: $it") }
    }

    @Test
    fun TestJoin() {
        lateinit var s1: String
        lateinit var s2: String

        val t1 = Thread { s1 = task1() }
        val t2 = Thread { s2 = task2() }
        t1.start()
        t2.start()

        t1.join()
        t2.join()
        task3(s1, s2)


    }

    @Test
    fun textSync() {
        lateinit var s1: String
        lateinit var s2: String

        Thread {
            synchronized(Unit) {
                s1 = task1()
            }
        }.start()
        s2 = task2()

        synchronized(Unit) {
            task3(s1, s2)
        }
    }
}