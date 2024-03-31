
import android.os.Parcel
import android.os.Parcelable

data class ContentModel(
    val title: String = "",
    val content: String = "",
    val time: String = "",
    val id: String = "",
    val userId: String = "", // 사용자 아이디 추가
    var likes: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "", // 사용자 아이디 추가
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(time)
        parcel.writeString(id)
        parcel.writeString(userId) // 사용자 아이디 추가
        parcel.writeInt(likes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContentModel> {
        override fun createFromParcel(parcel: Parcel): ContentModel {
            return ContentModel(parcel)
        }

        override fun newArray(size: Int): Array<ContentModel?> {
            return arrayOfNulls(size)
        }
    }
}
