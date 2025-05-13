import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.p2papp.PageContent
import com.example.p2papp.PageType
import com.example.p2papp.R

class ViewPagerAdapter(private val pages: List<PageContent>) :
    RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title1TextView: TextView? = view.findViewById(R.id.title1TextView)
        val description1TextView: TextView? = view.findViewById(R.id.description1TextView)
        val title2TextView: TextView? = view.findViewById(R.id.title2TextView)
        val description2TextView: TextView? = view.findViewById(R.id.description2TextView)
        val layout2Bloque: LinearLayout? = view.findViewById(R.id.layout2bloque)
        val image1View: ImageView? = view.findViewById(R.id.image1View)
        val image2View: ImageView? = view.findViewById(R.id.image2View)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val layoutId = when (viewType) {
            0 -> R.layout.page_template_normal     // texto simple o doble
            1 -> R.layout.page_template_text_image // nuevo tipo
            else -> throw IllegalArgumentException("Invalid viewType")
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return PageViewHolder(view)
    }


    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]

        when (page.type) {
            PageType.NORMAL -> {
                holder.title1TextView?.text = page.title1
                holder.description1TextView?.text = page.description1
                holder.image1View?.setImageResource(page.imageRes1Id ?: R.drawable.baseline_image_not_supported_24)

                if (page.title2 != null && page.description2 != null) {
                    holder.layout2Bloque?.visibility = View.VISIBLE
                    holder.title2TextView?.text = page.title2
                    holder.description2TextView?.text = page.description2
                    holder.image2View?.setImageResource(page.imageRes2Id ?: R.drawable.baseline_image_not_supported_24)
                } else {
                    holder.layout2Bloque?.visibility = View.GONE
                }
            }

            PageType.TEXT_IMAGE -> {
                holder.title1TextView?.text = page.title1
                holder.description1TextView?.text = page.description1
                holder.image1View?.setImageResource(page.imageRes1Id ?: R.drawable.baseline_image_not_supported_24)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (pages[position].type) {
            PageType.NORMAL -> 0
            PageType.TEXT_IMAGE -> 1
        }
    }


    override fun getItemCount(): Int = pages.size
}

