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

import android.widget.Filter;
import android.widget.Filterable;

import promise.commons.data.log.LogUtil;
import promise.commons.model.List;
import promise.commons.util.Conditions;
import promise.ui.model.Searchable;


/**
 * Created by yoctopus on 11/21/17.
 */

public class SearchableAdapter<T extends Searchable> extends PromiseAdapter<T> implements Filterable {
    private String TAG = LogUtil.makeTag(SearchableAdapter.class);

    private List<T> originalList;

    public SearchableAdapter(Listener<T> listener) {
        super(listener);
        this.originalList = new List<>();
    }

    @Override
    public void add(T t) {
        super.add(t);
        originalList.add(Conditions.checkNotNull(t));
    }

    @Override
    public void add(List<T> list) {
        super.add(list);
        originalList.addAll(Conditions.checkNotNull(list));
    }

    public void search(String query) {
        getFilter().filter(query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence charSequence) {
                LogUtil.e(TAG, charSequence);
                FilterResults results = new FilterResults();
                List<T> filterData;
                if (charSequence != null)
                    filterData = originalList.filter(t -> t.onSearch(charSequence.toString()));
                else filterData = originalList;
                results.values = filterData;
                if (filterData != null) results.count = filterData.size();
                else results.count = 0;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0) {
                    LogUtil.e(TAG, filterResults);
                    setList((List<T>) filterResults.values);
                }
                else {
                    LogUtil.e(TAG, filterResults);
                    clear();
                    /*setList(originalList);*/
                }
            }
        };
    }
}
