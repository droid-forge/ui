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

package promise.uiapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import promise.commons.AndroidPromise
import promise.commons.data.log.LogUtil
import promise.commons.model.List
import promise.ui.adapter.PromiseAdapter
import promise.ui.Viewable
import promise.uiapp.models.ViewablePoJo
import promise.uiapp.models.ViewablePoJoViewable
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), PromiseAdapter.Listener<ViewablePoJo> {

  override fun onClick(t: ViewablePoJo, id: Int) {
    Toast.makeText(this, "Clicked ${t.text}", Toast.LENGTH_LONG).show()
    LogUtil.e("_MainActivity", "clicked the pojo", t, " id ", id)
  }

  lateinit var adapter: PromiseAdapter<ViewablePoJo>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    searchEditText.addTextChangedListener(object: TextWatcher {
      override fun afterTextChanged(s: Editable) {
        this.onTextChanged(s.toString(), 0, 0, 0)
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        adapter.search(s.toString()){
          if(!it) loading_view.showEmpty(R.drawable.ic_launcher_background,
              "Searched list is empty",
              "No item found with the query provided")
          else loading_view.showContent()
        }
      }
    })

    adapter = PromiseAdapter(ArrayMap<Class<*>, KClass<out Viewable>>().apply {
      put(ViewablePoJo::class.java, ViewablePoJoViewable::class)
    }, this, true)

    recycler_view.layoutManager = LinearLayoutManager(this)

    recycler_view.adapter = adapter

    loading_view.showLoading(null)

    loading_view.showContent()

    adapter.add(List.generate(10) {
      ViewablePoJo("test $it")
    })

    AndroidPromise.instance().executeOnUi({
      adapter.args = null

      adapter setList List.generate(50) {
        ViewablePoJo("test $it")
      }
    }, 5000)
  }
}

class LoadingViewable(private val message: String): Viewable {
  override fun layout(): Int {
    return R.layout.loading_viewable
  }

  override fun init(view: View?) {
    // init loading layout views
  }

  override fun bind(view: View?, args: Any?) {
    // bind message to the views
  }
}