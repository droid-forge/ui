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

import android.view.View;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;
import promise.ui.model.Searchable;

public class Holder<T extends Searchable> extends AbstractFlexibleItem<Holder<T>.MyHolder> implements IFilterable {

  private T t;

  public Holder(T t) {
    this.t = t;
  }

  @Override
  public boolean equals(Object o) {
    return this.t.equals(o);
  }

  @Override
  public int getLayoutRes() {
    return t.layout();
  }

  @Override
  public MyHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
    return new MyHolder(t, view, adapter);
  }

  @Override
  public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, MyHolder holder, int position, List<Object> payloads) {
    holder.bind();
  }

  @Override
  public boolean filter(Serializable constraint) {
    if (constraint instanceof String) return t.onSearch((String) constraint);
    return false;
  }

  class MyHolder extends FlexibleViewHolder {
    private T t;

    MyHolder(T t, View view, FlexibleAdapter adapter) {
      super(view, adapter);
      this.t = t;
      t.init(view);
    }

    void bind() {
      t.bind(itemView);
    }
  }
}
