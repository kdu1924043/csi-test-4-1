import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.csi.R

class ImageListAdapter(private val context: Context, private val imageUris: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return imageUris.size
    }

    override fun getItem(position: Int): Any {
        return imageUris[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_image, parent, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        val imageUri = imageUris[position]

        Glide.with(context)
            .load(imageUri)
            .into(imageView)

        return view
    }
}
