package com.test.testcicd

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV
import java.util.*
import com.getkeepsafe.relinker.ReLinker

/**
 * @description: 本地缓存
 * @since: 1.0.0
 */
class StoreUtils private constructor() {

    companion object {
        @Volatile
        private var instance: StoreUtils? = null

        @JvmStatic
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: StoreUtils().also { instance = it }
            }

        @JvmField
        var cryptKey = "ensd-android"

        @JvmStatic
        fun initialize(context: Context) {
            // 一些 Android 设备（API level 19）在安装/更新 APK 时可能出错, 导致 libmmkv.so 找不到
            MMKV.initialize(context) { libName -> ReLinker.loadLibrary(context, libName) }
        }

        @JvmStatic
        fun initialize(context: Context, rootDir: String) {
            // 一些 Android 设备（API level 19）在安装/更新 APK 时可能出错, 导致 libmmkv.so 找不到
            MMKV.initialize(context, rootDir) { libName -> ReLinker.loadLibrary(context, libName) }
        }
    }

    var cache: MMKV?

    init {
        cache = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, cryptKey)
    }

    fun put(key: String, value: Any?): Boolean {
        if (cache == null) {
            return false
        }
        when (value) {
            is String -> return cache!!.encode(key, value)
            is Float -> return cache!!.encode(key, value)
            is Boolean -> return cache!!.encode(key, value)
            is Int -> return cache!!.encode(key, value)
            is Long -> return cache!!.encode(key, value)
            is Double -> return cache!!.encode(key, value)
            is ByteArray -> return cache!!.encode(key, value)
        }
        return false
    }

    fun <T : Parcelable> put(key: String, t: T?): Boolean {
        return cache?.encode(key, t) ?: false
    }

    fun put(key: String, sets: Set<String>?): Boolean {
        return cache?.encode(key, sets) ?: false
    }

    fun getInt(key: String,defaultValue :Int = 0): Int? {
        return cache?.decodeInt(key, defaultValue)
    }

    fun getDouble(key: String,defaultValue :Double = 0.00): Double? {
        return cache?.decodeDouble(key, defaultValue)
    }

    fun getLong(key: String,defaultValue :Long = 0L): Long? {
        return cache?.decodeLong(key, defaultValue)
    }

    fun getBoolean(key: String,defaultValue :Boolean = false): Boolean? {
        return cache?.decodeBool(key, defaultValue)
    }

    fun getFloat(key: String,defaultValue :Float = 0F): Float? {
        return cache?.decodeFloat(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String = ""): String? {
        return cache?.decodeString(key, defaultValue)
    }

    fun getBytes(key: String): ByteArray? {
        return cache?.decodeBytes(key)
    }

    fun <T : Parcelable> getParcelable(key: String, tClass: Class<T>): T? {
        return cache?.decodeParcelable(key, tClass)
    }

    fun getStringSet(key: String): Set<String>? {
        return cache?.decodeStringSet(key, Collections.emptySet())
    }

    fun removeKey(key: String) {
        cache?.removeValueForKey(key)
    }

    fun removeKeys(keys: Array<String>) {
        cache?.removeValuesForKeys(keys)
    }

    fun clearAll() {
        cache?.clearAll()
    }

    fun isKeyExisted(key: String): Boolean {
        return cache?.containsKey(key) ?: false
    }

    fun getTotalSize(): Long {
        return cache?.totalSize() ?: 0
    }

    fun encrypted(isEncrypted: Boolean) {
        cache?.let {
            if (isEncrypted) {
                // 加密
                it.reKey(cryptKey)
            } else {
                // 明文
                it.reKey(null)
            }
        }
    }
}