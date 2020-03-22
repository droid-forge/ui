/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Android Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package promise.uiapp.models

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import promise.ui.Viewable
import promise.ui.adapter.Searchable
import promise.ui.adapter.ViewHolder
import promise.ui.adapter.ViewableEntity
import promise.uiapp.R

class ViewablePoJoViewable(private val viewablePoJo: ViewablePoJo) : Viewable {

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

@ViewableEntity(layoutResource = R.layout.pojo_layout, viewHolderClass = ViewablePoJoViewHolder::class)
class ViewablePoJo(val text: String): Searchable {
  @SuppressLint("DefaultLocale")
  override fun onSearch(query: String): Boolean = text.toLowerCase().contains(query.toLowerCase())
}

class ImplementingViewable(): Viewable {
  override fun layout(): Int {
    return R.layout.pojo_layout
  }

  override fun init(view: View?) {
    // initialize the views
  }

  override fun bind(view: View?, args: Any?) {
    //bind data to the views
  }
}