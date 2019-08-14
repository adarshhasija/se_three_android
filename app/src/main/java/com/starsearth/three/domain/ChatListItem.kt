package com.starsearth.three.domain

import android.os.Parcel
import android.os.Parcelable

class ChatListItem() : Parcelable{

    var message: String? = null
    var time: String? = null
    var origin: String? = null

    constructor(parcel: Parcel) : this() {
        message = parcel.readString()
        time = parcel.readString()
        origin = parcel.readString()
    }

    constructor(message: String, time: String, origin: String) : this() {
        this.message = message
        this.time = time
        this.origin = origin
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(time)
        parcel.writeString(origin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatListItem> {
        override fun createFromParcel(parcel: Parcel): ChatListItem {
            return ChatListItem(parcel)
        }

        override fun newArray(size: Int): Array<ChatListItem?> {
            return arrayOfNulls(size)
        }
    }
}
