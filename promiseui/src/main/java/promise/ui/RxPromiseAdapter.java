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

package promise.ui;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import promise.commons.model.Result;
import promise.ui.model.Viewable;

public class RxPromiseAdapter<T extends Viewable> extends PromiseAdapter<T> {
  private DataSource<T> dataSource;
  boolean loading = true;
  private Result.Error<Throwable> error;
  private T loadingView;

  public RxPromiseAdapter(T loadingView, @NonNull Listener<T> listener) {
    super(listener);
    this.loadingView = loadingView;
    add(loadingView);
  }

  public RxPromiseAdapter<T> withErrorCallBack(Result.Error<Throwable> error) {
    this.error = error;
    return this;
  }

  public void load(DataSource<T> dataSource) {
    this.dataSource = dataSource;
        /*this.dataSource.load(new Result<>()
        .responseCallBack(new Result.Response<List<T>, Throwable>() {
            @Override
            public void onResponse(List<T> ts) {
                clear();
                add(ts);
            }
        }).errorCallBack(new Result.Error<Throwable>() {
                    @Override
                    public void onError(Throwable throwable) {
                        if (error != null) error.onError(throwable);
                    }
                }));*/
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
    RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
      }

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
//                int visibleItemCount = layoutManager.getChildCount();
//                int totalItemCount = layoutManager.getItemCount();
//                int firstVisibleItemPosition = layoutManager.;
//
////                if (!loading) {
////                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
////                            && firstVisibleItemPosition >= 0
////                            && totalItemCount >= PAGE_SIZE) {
////                        dataSource.load(new ResponseCallBack<List<T>, Throwable>(), );
////                    }
////                }
      }
    };
    recyclerView.addOnScrollListener(listener);
    super.onAttachedToRecyclerView(recyclerView);

  }

  public abstract class PaginationListener extends RecyclerView.OnScrollListener {
    LinearLayoutManager linearLayoutManager;
  }
}
