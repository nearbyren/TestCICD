package com.test.testcicd.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.test.testcicd.CallBackAIDLInterface
import com.test.testcicd.MethodObject
import com.test.testcicd.MyAIDLInterface

class MyService : Service() {


    val aidls: MutableList<CallBackAIDLInterface> = mutableListOf()

    override fun onBind(p0: Intent?): IBinder? {
        return MyBinder()
    }

    //编写binder 执行复杂的业务场景
    inner class MyBinder : MyAIDLInterface.Stub() {
        override fun commonMethod() {
            println("aidl commonMethod")
        }

        override fun setStringText(text: String?) {
            println("aidl setStringText $text")
        }

        override fun getMethodObject(): com.test.testcicd.MethodObject? {
            println("aidl getMethodObject ")
            return MethodObject(msg = "service", time = System.nanoTime())
        }

        override fun register(aidl: CallBackAIDLInterface?) {
            println("aidl register ")
            aidl?.let {
                if (!aidls.contains(it)) {
                    println("aidl register 添加")
                    aidls.add(it)
                }
            }


        }

        override fun unregister(aidl: CallBackAIDLInterface?) {
            println("aidl unregister ")
            aidl?.let {
                if (aidls.contains(it)) {
                    println("aidl unregister 注销完毕")
                    aidls.remove(it)
                } else {
                    println("aidl unregister 注销异常")
                }
            }
        }

    }


}