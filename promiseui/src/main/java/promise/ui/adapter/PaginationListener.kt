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

package promise.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import promise.commons.model.List
import promise.commons.tx.PromiseResult

class PaginationListener<T : Any>(private val adapter: PaginatedAdapter<T>,
                                  private val dataSource: DataSource<T>,
                                  private val mLayoutManager: RecyclerView.LayoutManager,
                                  private var visibleThreshold: Int = 10
) : RecyclerView.OnScrollListener() {
  // The minimum amount of items to have below your current scroll position
  // before loading more.

  // The current offset index of data you have loaded
  private var currentPage = 0
  // The total number of items in the dataset after the last load
  private var previousTotalItemCount = 0
  // True if we are still waiting for the last set of data to load.
  private var loading = true
  // Sets the starting page index
  private val startingPageIndex = 0

  private infix fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
    var maxSize = 0
    for (i in lastVisibleItemPositions.indices)
      if (i == 0) maxSize = lastVisibleItemPositions[i]
    else if (lastVisibleItemPositions[i] > maxSize)
      maxSize = lastVisibleItemPositions[i]
    return maxSize
  }

  // This happens many times a second during a scroll, so be wary of the code you place here.
// We are given a few useful parameters to help us work out if we need to load some more data,
// but first we check if we are waiting for the previous load to finish.
  override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
    var lastVisibleItemPosition = 0
    val totalItemCount = mLayoutManager.itemCount
    // This condition will useful when recyclerview has less than visibleThreshold items
    when (mLayoutManager) {
      is StaggeredGridLayoutManager -> {
        val lastVisibleItemPositions = mLayoutManager.findLastVisibleItemPositions(null)
        // get maximum element within the list
        lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
      }
      is GridLayoutManager -> lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
      is LinearLayoutManager -> lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
      // If it’s still loading, we check to see if the dataset count has
      // changed, if so we conclude it has finished loading and update the current page
      // number and total item count.
      // If it isn’t currently loading, we check to see if we have breached
      // the visibleThreshold and need to reload more data.
      // If we do need to reload some more data, we execute onLoadMore to fetch the data.
      // threshold should reflect how many total columns there are too
    }
    // If it’s still loading, we check to see if the dataset count has
// changed, if so we conclude it has finished loading and update the current page
// number and total item count.
    if (loading && totalItemCount > previousTotalItemCount) {
      loading = false
      if (adapter.hasLoader()) adapter.removeLoader()
      previousTotalItemCount = totalItemCount
    }
    // If it isn’t currently loading, we check to see if we have breached
// the visibleThreshold and need to reload more data.
// If we do need to reload some more data, we execute onLoadMore to fetch the data.
// threshold should reflect how many total columns there are too
    if (!loading && lastVisibleItemPosition +
        visibleThreshold > totalItemCount &&
        view.adapter!!.itemCount > visibleThreshold) { // This condition will useful when recyclerview has less than visibleThreshold items
      currentPage += visibleThreshold
      adapter.addLoadingView()
      dataSource.load(PromiseResult<List<T>, Throwable>()
          .withCallback {
            adapter.add(it)
          }, currentPage, visibleThreshold)
      loading = true
    }
  }

  // Call whenever performing new searches
  fun resetState() {
    currentPage = startingPageIndex
    previousTotalItemCount = 0
    loading = true
  }

  init {
    when (mLayoutManager) {
      is GridLayoutManager -> this.visibleThreshold *= mLayoutManager.spanCount
      is StaggeredGridLayoutManager -> this.visibleThreshold *= mLayoutManager.spanCount
    }
  }
}