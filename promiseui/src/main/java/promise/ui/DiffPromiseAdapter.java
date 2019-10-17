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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import promise.commons.data.log.LogUtil;
import promise.commons.model.List;
import promise.commons.util.Conditions;
import promise.ui.model.Viewable;



public class DiffPromiseAdapter<T extends Viewable> extends ListAdapter<T, DiffPromiseAdapter<T>.Holder> {
  private String TAG = LogUtil.makeTag(DiffPromiseAdapter.class);
  private String AdapterItems = "__adapter_items__";
  private Indexer indexer;
  private List<T> list;
  private Listener<T> listener;
  private LongClickListener<T> longClickListener;
  private Swipe<T> swipeListener;
  private int alternatingColor = 0;
  private OnAfterInitListener onAfterInitListener;

  public DiffPromiseAdapter(@NonNull Listener<T> listener) {
    this(new List<T>(), listener);
  }

  public DiffPromiseAdapter(@NonNull List<T> list, @NonNull Listener<T> listener) {
    super(new DiffUtil.ItemCallback<T>() {

      @Override
      public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return oldItem.layout() == oldItem.layout();
      }

      @SuppressLint("DiffUtilEquals")
      @Override
      public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return oldItem == newItem;
      }
    });
    this.list = Conditions.checkNotNull(list);
    submitList(list);
    this.listener = listener;
    indexList();
  }

  public void restoreViewState(Bundle instanceState) {
    List<Parcelable> items = new List<>(instanceState.getParcelableArrayList(AdapterItems));
    if (items.isEmpty()) return;
    this.list = items.map(parcelable -> (T) parcelable);
    submitList(list);
  }

  public void backupViewState(Bundle instanceState) {
    instanceState.putParcelableArrayList(AdapterItems, new ArrayList<>(list.map(t -> (Parcelable) t)));
  }

  @Deprecated
  public void destroyViewState() {

  }

  public DiffPromiseAdapter<T> swipe(Swipe<T> swipeListener) {
    this.swipeListener = swipeListener;
    return this;
  }

  public DiffPromiseAdapter onAfterInitListener(OnAfterInitListener onAfterInitListener) {
    this.onAfterInitListener = onAfterInitListener;
    return this;
  }

  public DiffPromiseAdapter<T> alternatingColor(int color) {
    this.alternatingColor = color;
    return this;
  }

  private void indexList() {
    indexer = new Indexer();
  }

  public void add(final T t) {
    indexer.add(Conditions.checkNotNull(t));
  }

  public void unshift(final T t) {
    indexer.unshift(Conditions.checkNotNull(t));
  }

  public void add(final List<T> list) {
    indexer.add(Conditions.checkNotNull(list));
  }

/*
  public void remove(final T viewHolder) {
    indexer.remove(Conditions.checkNotNull(viewHolder));
  }
*/

  public void updateAll() {
    indexer.updateAll();
  }
/*

  public void update(final T viewHolder) {
    indexer.update(Conditions.checkNotNull(viewHolder));
  }
*/

  public void clear() {
    indexer.clear();
  }

  @Override
  public int getItemViewType(int position) {
    T t = getItem(position);
    int viewType = t.layout();
    Conditions.checkState(viewType != 0, "The layoutResource resource for " + t + " is not provided");
    return viewType;
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
    if (onAfterInitListener != null) onAfterInitListener.onAfterInit(view);
    return new Holder(view);
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
    if (manager instanceof GridLayoutManager) {
      GridLayoutManager manager1 = (GridLayoutManager) manager;
      recyclerView.setLayoutManager(new WrapContentGridLayoutManager(recyclerView.getContext(), manager1.getSpanCount()));
    } else if (manager instanceof LinearLayoutManager)
      recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(recyclerView.getContext()));
    /*if (recyclerView.getItemAnimator() != null) {
      recyclerView.setItemAnimator(new CustomItemAnimator());
    }*/
    if (swipeListener != null) {
      ItemTouchHelper.SimpleCallback simpleCallback =
          new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(
                RecyclerView recyclerView,
                RecyclerView.ViewHolder viewHolder,
                RecyclerView.ViewHolder target) {
              return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
              if (viewHolder instanceof DiffPromiseAdapter.Holder) {
                final Holder holder = (Holder) viewHolder;
                Response response =
                    () -> {
                 /* update(holder.viewHolder);*/
                    };
                switch (direction) {
                  case ItemTouchHelper.RIGHT:
                    swipeListener.onSwipeRight(holder.t, response);
                    break;
                  case ItemTouchHelper.LEFT:
                    swipeListener.onSwipeLeft(holder.t, response);
                    break;
                }
              }
            }
          };
      new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    T t = getItem(position);
    if (alternatingColor != 0)
      if (position % 2 == 1) holder.view.setBackgroundColor(alternatingColor);
    holder.bind(t);
    holder.bindListener(t);
    holder.bindLongClickListener(t);

  }

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.indexer.setList(list);
  }

  public Listener<T> getListener() {
    return listener;
  }

  public void setListener(Listener<T> listener) {
    this.listener = listener;
  }

  public boolean isReverse() {
    return indexer.reverse;
  }

  private void setReverse(boolean reverse) {
    this.indexer.reverse(reverse);
  }

  public LongClickListener<T> getLongClickListener() {
    return longClickListener;
  }

  public void setLongClickListener(LongClickListener<T> longClickListener) {
    this.longClickListener = longClickListener;
  }

  public void reverse() {
    indexer.reverse(true);
  }

  public void reverse(boolean reverse) {
    indexer.reverse(reverse);
  }

  public interface Listener<T> {
    void onClick(T t, @IdRes int id);
  }

  public interface OnAfterInitListener {
    void onAfterInit(View view);
  }

  public interface Response {
    void call();
  }

  public interface Swipe<T> {
    void onSwipeRight(T t, Response response);

    void onSwipeLeft(T t, Response response);
  }

  public interface LongClickListener<T> {
    void onLongClick(T t, @IdRes int id);
  }

  public class Holder extends RecyclerView.ViewHolder {
    public View view;
    T t;

    public Holder(View itemView) {
      super(itemView);
      view = itemView;
    }

    void bind(T t) {
      this.t = t;
      t.init(view);
      t.bind(view);
    }

    void bindListener(final T t) {
      if (listener == null) return;
      List<Field> fields = new List<>(Arrays.asList(t.getClass().getDeclaredFields()));
      for (Field field : fields)
        try {
          Object view = field.get(t);
          if (view instanceof View)
            ((View) view)
                .setOnClickListener(
                    v -> listener.onClick(t, v.getId()));
        } catch (IllegalAccessException ignored) {
          /*LogUtil.e(TAG, "illegal access ", ignored);*/
        }
    }

    void bindLongClickListener(final T t) {
      if (longClickListener == null) return;
      List<Field> fields = new List<>(Arrays.asList(t.getClass().getDeclaredFields()));
      for (Field field : fields)
        try {
          Object view = field.get(t);
          if (view instanceof View)
            ((View) view)
                .setOnLongClickListener(
                    v -> {
                      longClickListener.onLongClick(t, v.getId());
                      return true;
                    });
        } catch (IllegalAccessException ignored) {
        }
    }
  }

  private class Indexer {
    boolean reverse = false;
/*

    void index() {
      for (int i = 0; i < list.size(); i++) list.get(i).index(i);
    }
*/

    void add(T t) {
      if (list == null) list = new List<>();
      if (!list.isEmpty()) {
        if (reverse) list.reverse();
        list.add(t);
        if (reverse) list.reverse();
        submitList(list);
      } else {
        list.add(t);
        submitList(list);
      }
    }

    void unshift(T t) {
      if (list == null) list = new List<>();
      if (!list.isEmpty()) {
        List<T> list1 = new List<>();
        list1.add(t);
        list1.addAll(list);
        setList(list1);
      } else {
        list.add(t);
        submitList(list);
      }
    }

    void setList(List<T> list) {
      submitList(list);

    }

 /*   void remove(final T viewHolder) {
      if (list == null) return;
      list.remove(viewHolder.index());
      index();
      submitList(list);
    }
*/
 /*   void update(final T viewHolder) {
      if (list == null) return;
      if (viewHolder.index() >= list.size()) return;
      list.set(viewHolder.index(), viewHolder);
      submitList(list);
    }
*/
    void updateAll() {
      submitList(list);
    }

    void add(List<T> list) {
      if (DiffPromiseAdapter.this.list == null) DiffPromiseAdapter.this.list = new List<>();
      DiffPromiseAdapter.this.list.addAll(list);
      submitList(DiffPromiseAdapter.this.list);
    }

    void clear() {
      if (list == null || list.isEmpty()) return;
      list.clear();
      submitList(list);
    }

    int size() {
      return list == null || list.isEmpty() ? 0 : list.size();
    }

    void reverse(boolean reverse) {
      this.reverse = reverse;
    }
  }

  public class WrapContentLinearLayoutManager extends LinearLayoutManager {
    WrapContentLinearLayoutManager(Context context) {
      super(context);
    }

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
      super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
      try {
        super.onLayoutChildren(recycler, state);
      } catch (IndexOutOfBoundsException e) {
        LogUtil.e(TAG, "meet a Bug in RecyclerView");
      }
    }
  }

  public class WrapContentGridLayoutManager extends GridLayoutManager {

    WrapContentGridLayoutManager(Context context, int spanCount) {
      super(context, spanCount);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
      try {
        super.onLayoutChildren(recycler, state);
      } catch (IndexOutOfBoundsException e) {
        LogUtil.e(TAG, "meet a Bug in RecyclerView");
      }
    }
  }
}
