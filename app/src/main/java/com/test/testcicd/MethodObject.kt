package com.test.testcicd

import android.os.Parcel
import android.os.Parcelable

data class MethodObject(var msg: String, var time: Long? = null) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(msg)
        parcel.writeValue(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MethodObject> {
        override fun createFromParcel(parcel: Parcel): MethodObject {
            return MethodObject(parcel)
        }

        override fun newArray(size: Int): Array<MethodObject?> {
            return arrayOfNulls(size)
        }
    }
}