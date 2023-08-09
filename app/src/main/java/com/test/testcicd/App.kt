package com.test.testcicd

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.test.testcicd.viemodel.setApplicationContext
import java.io.File
import java.util.*

class App : Application(), ViewModelStoreOwner {
    lateinit var mAppViewModelStore: ViewModelStore
    var mFactory: ViewModelProvider.Factory? = null
    override fun onCreate() {
        super.onCreate()
        //此处必须在http前
        setApplicationContext(this)
        mAppViewModelStore = ViewModelStore()
        getExternalFilesDir(null)?.absolutePath?.let { StoreUtils.initialize(this, it) }
        val ss = arrayListOf<String>()
        val s = Collections.synchronizedList(ss)
    }

    override fun getViewModelStore(): ViewModelStore = mAppViewModelStore

    /**
     * 获取一个全局的ViewModel
     */
    fun getAppViewModelProvider(): ViewModelProvider {
        return ViewModelProvider(this, this.getAppFactory())
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        }
        return mFactory as ViewModelProvider.Factory
    }
}