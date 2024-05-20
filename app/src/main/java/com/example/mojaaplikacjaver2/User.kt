package com.example.mojaaplikacjaver2

import android.os.Parcel
import android.os.Parcelable
import java.security.Timestamp

data class User(
    var dawki: List<Dawka>?=null
)
data class Dawka(
    var data:com.google. firebase.Timestamp? = null,
    var dawka: Double=0.0
): Parcelable{                                                                          // co to robi
    constructor(parcel:Parcel):this(
        parcel.readParcelable(com.google.firebase.Timestamp.javaClass.classLoader)!!,
        parcel.readDouble()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(data,flags)
        dest.writeDouble(dawka)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<Dawka>{
        override fun createFromParcel(source: Parcel): Dawka {
            return Dawka(source)


        }

        override fun newArray(size: Int): Array<Dawka?> {
            return arrayOfNulls(size)
        }
    }
}

