package promise.uiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import promise.commons.Promise
import promise.commons.data.log.LogUtil
import promise.commons.model.List
import promise.commons.model.Result
import promise.ui.DataSource
import promise.ui.PromiseAdapter
import promise.ui.model.LoadingViewable
import promise.ui.model.Viewable
import promise.uiapp.models.ViewablePoJo
import promise.uiapp.models.ViewablePoJoViewable
import kotlin.reflect.KClass


inline fun <T> generate(num: Int, function: (Int) -> T): List<T> {
  val list = List<T>()
  for (i in 0 until num) {
    list.add(function(i))
  }
  return list
}

class MainActivity : AppCompatActivity(), PromiseAdapter.Listener<ViewablePoJo> {

  override fun onClick(t: ViewablePoJo, id: Int) {
    Toast.makeText(this, "Clicked ${t.text}", Toast.LENGTH_LONG).show()
    LogUtil.e("_MainActivity", "clicked the pojo", t, " id ", id)
  }

  lateinit var adapter: PromiseAdapter<ViewablePoJo>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    adapter = PromiseAdapter(ArrayMap<Class<*>, KClass<out Viewable>>().apply {
      put(ViewablePoJo::class.java, ViewablePoJoViewable::class)
    }, this)

    recycler_view.layoutManager = LinearLayoutManager(this)

    recycler_view.adapter = adapter

    adapter.add(generate(50) {
      ViewablePoJo("test $it")
    })
  }
}
