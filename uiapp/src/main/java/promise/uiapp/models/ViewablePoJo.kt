package promise.uiapp.models

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import promise.ui.model.ViewHolder
import promise.uiapp.R

class ViewablePoJoViewable(private val viewablePoJo: ViewablePoJo) : promise.ui.model.Viewable {

  lateinit var imageView: ImageView
  lateinit var textView: TextView
  lateinit var shimmerFrameLayout: ShimmerFrameLayout

  override fun layout(): Int = R.layout.pojo_layout

  override fun init(view: View) {
    shimmerFrameLayout = view.findViewById(R.id.shimmers)
    textView = view.findViewById(R.id.pojo_text)
    imageView = view.findViewById(R.id.action_image)
  }

  override fun bind(view: View?, args: Any?) {
    if (args is Boolean) {
      shimmerFrameLayout.startShimmer()
      return
    }
    shimmerFrameLayout.stopShimmer()
    shimmerFrameLayout.setShimmer(null)
    textView.text = viewablePoJo.text
  }
}


class ViewablePoJoViewHolder(private val viewablePoJo: ViewablePoJo) : ViewHolder {

  override fun init(view: View) {
    textView = view.findViewById(R.id.pojo_text)
  }

  override fun bind(view: View, args: Any?) {
    textView.text = viewablePoJo.text
  }

  lateinit var textView: TextView
}

//@Viewable(layoutResource = R.layout.pojo_layout, viewHolderClass = ViewablePoJoViewHolder::class)
class ViewablePoJo(val text: String)