/*
 *
 *  * Copyright 2017, Peter Vincent
 *  * Licensed under the Apache License, Version 2.0, Promise.
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package promise.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.IdRes
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import promise.commons.Promise
import promise.commons.data.log.LogUtil
import promise.commons.model.List
import promise.commons.model.Result
import promise.commons.util.Conditions
import promise.ui.model.LoadingViewable
import promise.ui.model.Searchable
import promise.ui.model.Viewable
import promise.ui.model.ViewableInstance
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by yoctopus on 11/6/17.
 */
open class PromiseAdapter<T : Any>(list: List<T>, var listener: Listener<T>?, var args: Any?) : RecyclerView.Adapter<PromiseAdapter<T>.Holder>(), PaginatedAdapter<T>{

  private val TAG = LogUtil.makeTag(PromiseAdapter::class.java)
  private val AdapterItems = "__adapter_items__"
  private val indexer: Indexer
  private var list: List<Any>? = null
  var longClickListener: LongClickListener<T>? = null
  private var swipeListener: Swipe<T>? = null
  private var alternatingColor = 0
  private var onAfterInitListener: OnAfterInitListener? = null

  private var dataSource: DataSource<T>? = null
  private var loadingView: LoadingViewable? = null
  private var visibleThreshold = 10

  private var originalList: List<T>? = null

  private var viewableClasses: MutableMap<String, KClass<out Viewable>>? = null

  private var searchHelper: SearchHelper? = null

  var isReverse: Boolean
    get() = indexer.reverse
    private set(reverse) = this.indexer.reverse(reverse)

  constructor(listener: Listener<T>?, args: Any?) : this(List<T>(), listener, args)

  constructor(viewableClasses: Map<Class<*>, KClass<out Viewable>>, listener: Listener<T>?, args: Any?) :
      this(List<T>(), listener, args) {
    this.viewableClasses = ArrayMap()
    for ((key, value) in viewableClasses)
      this.viewableClasses!![key.name] = value
  }

  init {
    this.list = List()
    list.forEach {
      this.list!!.add(ViewableInstance(it))
    }
    indexer = Indexer()
  }


  fun swipe(swipeListener: Swipe<T>): PromiseAdapter<T> {
    this.swipeListener = swipeListener
    return this
  }

  fun onAfterInitListener(onAfterInitListener: OnAfterInitListener): PromiseAdapter<*> {
    this.onAfterInitListener = onAfterInitListener
    return this
  }

  fun alternatingColor(color: Int): PromiseAdapter<T> {
    this.alternatingColor = color
    return this
  }

  @JvmOverloads
  fun withPagination(dataSource: DataSource<T>,
                     loadingView: LoadingViewable,
                     visibleThreshold: Int = 10): PromiseAdapter<T> {
    this.dataSource = dataSource
    this.loadingView = loadingView
    this.visibleThreshold = visibleThreshold
    return this
  }

  open infix fun add(t: T) {
    indexer.add(Conditions.checkNotNull(t))
  }

  infix fun unshift(t: T) {
    indexer.unshift(Conditions.checkNotNull(t))
  }

  override infix fun add(list: List<T>) {
    indexer.add(Conditions.checkNotNull(list))
  }

  infix fun remove(t: T) {
    indexer.remove(Conditions.checkNotNull(t))
  }

  fun updateAll() {
    indexer.updateAll()
  }

  infix fun update(viewHolder: T) {
    indexer.update(Conditions.checkNotNull(viewHolder))
  }

  fun clear() {
    indexer.clear()
  }

  override fun getItemViewType(position: Int): Int {
    var viewableInstance = list!![position]
    if (viewableInstance is LoadingViewable) return TYPE_LOADING
    viewableInstance = viewableInstance as ViewableInstance<T>
    if (viewableClasses != null) {
      val tClass = viewableInstance.t.javaClass
      if (viewableClasses!!.containsKey(tClass.name)) {
        val kClass = viewableClasses!![tClass.name]
        viewableInstance.viewClass = kClass
      }
    }
    val viewable = viewableInstance.viewable()
    val viewType = viewable.layout()
    Conditions.checkState(viewType != 0, "The layout resource for $viewable is not provided")
    return viewType
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
    if (viewType == TYPE_LOADING) {
      val view = LayoutInflater.from(parent.context).inflate(loadingView!!.layout(),
          parent, false)
      if (onAfterInitListener != null) onAfterInitListener!!.onAfterInit(view)
      return LoadingHolder(view, loadingView!!)
    }
    val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    if (onAfterInitListener != null) onAfterInitListener!!.onAfterInit(view)
    return Holder(view)
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    val manager = recyclerView.layoutManager
    if (manager is GridLayoutManager) {
      val manager1 = manager as GridLayoutManager?
      recyclerView.layoutManager = WrapContentGridLayoutManager(recyclerView.context, manager1!!.spanCount)
    } else if (manager is LinearLayoutManager) {
      val orientation = manager.orientation
      val reverse = manager.reverseLayout
      recyclerView.layoutManager = WrapContentLinearLayoutManager(recyclerView.context, orientation, reverse)
    }

    if (swipeListener != null) {
      val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean {
          return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
          if (viewHolder is PromiseAdapter<*>.Holder) {
            val response: Response = object : Response {
              override fun call() {
                /*update(holder.viewHolder.getT());*/
              }
            }
            when (direction) {
              ItemTouchHelper.RIGHT -> swipeListener!!.onSwipeRight(viewHolder.viewableInstance.t as T, response)
              ItemTouchHelper.LEFT -> swipeListener!!.onSwipeLeft(viewHolder.viewableInstance.t as T, response)
            }
          }
        }
      }
      ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView)
    }

    if (dataSource != null) {
      recyclerView.addOnScrollListener(PaginationListener(this,
          dataSource!!, recyclerView.layoutManager!!, visibleThreshold))
      addLoadingView()
      dataSource!!.load(Result<List<T>, Throwable>()
          .withCallBack {
            clear()
            this add it
          }, 0, visibleThreshold)

    }
  }

  override fun addLoadingView() {
    this.list!!.add(loadingView)
    notifyDataSetChanged()
  }

  override fun hasLoader(): Boolean = getList().last() is LoadingViewable

  override fun removeLoader() {
    getList().removeAt(getList().size - 1)
    notifyItemRemoved(itemCount)
  }

  override fun onBindViewHolder(holder: Holder, position: Int) {
    if (holder is LoadingViewable) return
    val t = list!![position]
    if (t is ViewableInstance<*>) UIJobScheduler.submitJob {
      if (alternatingColor != 0)
        if (position % 2 == 1) holder.view.setBackgroundColor(alternatingColor)
      holder.bind(t as ViewableInstance<T>, args)
    }
  }

  override fun getItemCount(): Int = indexer.size()

  open fun search(query: String, helperResult: (Boolean) -> Unit) {
    if (originalList == null) originalList = getList()
    if (searchHelper == null) searchHelper = SearchHelper(originalList!!, helperResult)
    searchHelper!!.filter.filter(query)
  }

  inner class SearchHelper(private val originalList: List<T>, private val helperResult: (Boolean) -> Unit): Filterable {
    override fun getFilter(): Filter = object : Filter() {
      override fun performFiltering(charSequence: CharSequence): FilterResults {
        val results = FilterResults()
        val filterData = originalList.filter { t: T ->
          t is Searchable &&
              t.onSearch(charSequence.toString())
        }
        results.values = filterData
        if (!filterData.isEmpty()) results.count =
            filterData.size else results.count = 0
        return results
      }

      override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) =
          when {
            charSequence.isNotEmpty() && filterResults.count == 0 -> helperResult(false)
            filterResults.count > 0 -> {
              helperResult(true)
              setList((filterResults.values as List<T>))
            }
            charSequence.isEmpty() -> {
              helperResult(true)
              setList(originalList)
            }
            else -> helperResult(true)
          }
    }
  }

  fun getList(): List<T> =
      list!!.filter { it is ViewableInstance<*> }.map { (it as ViewableInstance<T>).t }

  infix fun setList(list: List<T>) {
    this.indexer.setList(list)
  }

  fun reverse() {
    indexer.reverse(true)
  }

  infix fun reverse(reverse: Boolean) {
    indexer.reverse(reverse)
  }

  interface Listener<T> {
    fun onClick(t: T, @IdRes id: Int)
  }

  interface OnAfterInitListener {
    infix fun onAfterInit(view: View)
  }

  interface Response {
    fun call()
  }

  interface Swipe<T> {
    fun onSwipeRight(t: T, response: Response)

    fun onSwipeLeft(t: T, response: Response)
  }

  interface LongClickListener<T> {
    fun onLongClick(t: T, @IdRes id: Int)
  }

  open inner class Holder(var view: View) : RecyclerView.ViewHolder(view) {
    internal lateinit var viewableInstance: ViewableInstance<T>

    internal fun bind(viewableInstance: ViewableInstance<T>, args: Any?) {
      this.viewableInstance = viewableInstance
      this.viewableInstance.viewable().init(view)
      this.viewableInstance.viewable().bind(view, args)
      bindListener()
      bindLongClickListener()
    }

    private fun bindListener() {
      if (listener == null) return
      val kClass = viewableInstance.viewClass
      if (kClass != null) {
        val fields = listOf(*kClass.java.declaredFields)
        for (field in fields)
          try {
            val viewable = viewableInstance.viewClassObject
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnClickListener { v -> listener!!.onClick(viewableInstance.t, v.id) }
          } catch (ignored: IllegalAccessException) {
            /*LogUtil.e(TAG, "illegal access ", ignored);*/
          }

      } else {
        val fields = listOf(*viewableInstance.viewable().javaClass.declaredFields)
        for (field in fields)
          try {
            val viewable = viewableInstance.viewable()
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnClickListener { v -> listener!!.onClick(viewableInstance.t, v.id) }
          } catch (ignored: IllegalAccessException) {
            /*LogUtil.e(TAG, "illegal access ", ignored);*/
          }

      }
    }

    private fun bindLongClickListener() {
      if (longClickListener == null) return
      val kClass = viewableInstance.viewClass
      if (kClass != null) {
        val fields = listOf(*kClass.java.declaredFields)
        for (field in fields)
          try {
            val viewable = viewableInstance.viewClassObject
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnLongClickListener { v ->
                    longClickListener!!.onLongClick(viewableInstance.t, v.id)
                    true
                  }
          } catch (ignored: IllegalAccessException) {
          }

      } else {
        val fields = List(Arrays.asList(*viewableInstance.viewable().javaClass.declaredFields))
        for (field in fields)
          try {
            val viewable = viewableInstance.viewable()
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnLongClickListener { v ->
                    longClickListener!!.onLongClick(viewableInstance.t, v.id)
                    true
                  }
          } catch (ignored: IllegalAccessException) {
          }
      }
    }
  }

  inner class LoadingHolder(view1: View, loadingView: LoadingViewable) : Holder(view1) {

    init {
      loadingView.init(view1)
    }

  }

  private inner class Indexer {
    internal var reverse = false
    internal infix fun add(t: T) {
      if (list == null) list = List()
      if (!list!!.isEmpty()) {
        if (reverse) list!!.reverse()
        val instance = ViewableInstance(t)
        list!!.add(instance)
        if (reverse) list!!.reverse()
        Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
      } else {
        val instance = ViewableInstance(t)
        list!!.add(instance)
        Promise.instance().executeOnUi { notifyItemInserted(0) }
      }
    }

    internal infix fun unshift(t: T) {
      if (list == null) list = List()
      if (!list!!.isEmpty()) {
        val list1 = List<T>()
        list1.add(t)
        list1.addAll(list!!.map { (it as ViewableInstance<T>).t })
        setList(list1)
        Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
      } else {
        add(t)
      }
    }

    internal infix fun setList(list: List<T>) {
      this@PromiseAdapter.list = list.map { ViewableInstance(it) }
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal infix fun remove(t: T) {
      if (list == null) return
      val instance = list!!.find { i -> (i as ViewableInstance<T>).t === t }
      list!!.remove(instance)
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal infix fun update(viewHolder: T) {
      /* if (list == null) return;
      ViewableInstance<T> v = list.find(i -> i.getT() == viewHolder);
      if (v == null) return;
      if (v.viewable().index() >= list.size()) return;
      list.set(v.viewable().index(), v);
      notifyItemChanged(viewHolder.index());
      Promise.instance().executeOnUi(PromiseAdapter.this::notifyDataSetChanged);*/
    }

    internal fun updateAll() {
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal infix fun add(list: List<T>) {
      for (t in list) add(t)
    }

    internal fun clear() {
      if (list == null || list!!.isEmpty()) return
      list!!.clear()
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal fun size(): Int = if (list == null || list!!.isEmpty()) 0 else list!!.size

    internal infix fun reverse(reverse: Boolean) {
      this.reverse = reverse
    }
  }

  inner class WrapContentLinearLayoutManager : LinearLayoutManager {

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {}

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
      try {
        super.onLayoutChildren(recycler, state)
      } catch (e: IndexOutOfBoundsException) {
        LogUtil.e(TAG, "meet a Bug in RecyclerView")
      }

    }
  }

  inner class WrapContentGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
      try {
        super.onLayoutChildren(recycler, state)
      } catch (e: IndexOutOfBoundsException) {
        LogUtil.e(TAG, "meet a Bug in RecyclerView")
      }

    }
  }

  inner class CustomItemAnimator : DefaultItemAnimator() {
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int): Boolean {
      if (supportsChangeAnimations) {
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
      } else {
        if (oldHolder === newHolder) {
          if (oldHolder != null) {
            // if the two holders are equal, call dispatch change only once
            dispatchChangeFinished(oldHolder, /*ignored*/ true)
          }
        } else {
          // else call dispatch change once for every non-null holder
          if (oldHolder != null) {
            dispatchChangeFinished(oldHolder, true)
          }
          if (newHolder != null) {
            dispatchChangeFinished(newHolder, false)
          }
        }
        // we don'viewHolder need a call to requestPendingTransactions after this, return false.
        return false
      }
    }
  }

  companion object {
    const val TYPE_NORMAL = 1;
    const val TYPE_LOADING = 2
  }
}
