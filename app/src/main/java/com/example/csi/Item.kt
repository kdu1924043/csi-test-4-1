package com.example.csi
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Item(
    val no: Int,
    val name: String,
    val price: String,
    val photo: String
) : Serializable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )


    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}
