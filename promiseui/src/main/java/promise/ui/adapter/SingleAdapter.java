/*
 *
 *  *
 *  *  * Copyright 2017, Solutech Limited
 *  *  * Licensed under the Apache License, Version 2.0, Solutech SAT.
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *  * Unless required by applicable law or agreed to in writing,
 *  *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package promise.ui.adapter;

import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;

import eu.davidea.flexibleadapter.FlexibleAdapter;

import promise.commons.model.List;
import promise.commons.model.function.MapFunction;
import promise.commons.util.Conditions;
import promise.ui.AdapterDivider;
import promise.ui.model.Searchable;


public class SingleAdapter<T extends Searchable> {

  private MapFunction<Holder<T>, T> mapFunction = Holder::new;

  private List<T> searchables;

  private Listener<T> listener;

  private RecyclerView recyclerView;

  private FlexibleAdapter<Holder<T>> flexibleAdapter;

  private boolean withDivider = false;

  public SingleAdapter(RecyclerView recyclerView, boolean withDivider) {
    this.recyclerView = recyclerView;
    this.withDivider = withDivider;
    init();
  }

  public SingleAdapter<T> withListener(final Listener<T> listener) {
    if (flexibleAdapter == null) throw new IllegalStateException("Adapter is not initialized");
    flexibleAdapter.addListener((FlexibleAdapter.OnItemClickListener) (view, position) -> {
      listener.onClick(searchables.get(position), view.getId());
      return true;
    });
    return this;
  }

  private void init() {
    if (searchables == null) searchables = new List<>();
    flexibleAdapter = new FlexibleAdapter<>(searchables.map(mapFunction));
    if (withDivider) recyclerView.addItemDecoration(new AdapterDivider(recyclerView.getContext(),
        LinearLayout.VERTICAL));
    recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    recyclerView.setAdapter(flexibleAdapter);
  }

  public <T extends Serializable> void search(T t) {
    flexibleAdapter.setFilter(t);
    flexibleAdapter.filterItems(300);
  }

  public void add(final T t) {
    searchables.add(t);
    flexibleAdapter.addItem(mapFunction.from(Conditions.checkNotNull(t)));
  }

  public void add(final List<T> list) {
    searchables.addAll(list);
    flexibleAdapter.addItems(flexibleAdapter.getItemCount(),
        Conditions.checkNotNull(list).map(mapFunction));
  }

/*
  public void remove(final T t) {
    searchables.remove(t.index());
    flexibleAdapter.removeItem(Conditions.checkNotNull(t).index());
  }

  public void update(final T t) {
    searchables.set(t.index(), t);
    flexibleAdapter.updateItem(mapFunction.from(Conditions.checkNotNull(t)));
  }
*/

  public void clear() {
    searchables.clear();
    flexibleAdapter.clear();
  }

  public interface Listener<T> {
    void onClick(T t, @IdRes int id);
  }
}
