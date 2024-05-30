package com.example.csi
import android.os.Parcel
import android.os.Parcelable

data class Item1(
    val no: Int,
    val name: String,
    val price: String,
    val photo: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(no)
        parcel.writeString(name)
        parcel.writeString(price)
        parcel.writeString(photo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Item1> {
        override fun createFromParcel(parcel: Parcel): Item1 {
            return Item1(parcel)
        }

        override fun newArray(size: Int): Array<Item1?> {
            return arrayOfNulls(size)
        }
    }
}
