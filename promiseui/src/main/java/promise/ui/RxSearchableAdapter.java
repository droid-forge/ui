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
import promise.ui.model.Searchable;

/**
 * Created by yoctopus on 11/21/17.
 */

public class RxSearchableAdapter<T extends Searchable> extends RxPromiseAdapter<T> implements Filterable {
    private String TAG = LogUtil.makeTag(RxSearchableAdapter.class);

    private List<T> sList;
    private Object options;

    public RxSearchableAdapter(T loadingView, Listener<T> listener) {
        super(loadingView, listener);
    }

    public RxSearchableAdapter filter(Object options) {
        this.options = options;
        return this;
    }

    public void search(String query, Object options) {
        this.options = options;
        search(query);
    }

    public void search(String query) {
        if (sList == null) this.sList = getList();
        getFilter().filter(query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();

                List<T> filterData;
                // Skip the autocomplete query if no constraints are given.
                // Query the autocomplete API for the (constraint) search string.
                if (charSequence != null) filterData = getAutoComplete(charSequence);
                else filterData = sList;
                results.values = filterData;
                if (filterData != null) results.count = filterData.size();
                else results.count = 0;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                // The API returned at least one result, update the data.
                // The API did not return any results, invalidate the data set.
                if (filterResults != null && filterResults.count > 0)
                    setList((List<T>) filterResults.values);
                else clear();
            }
        };
    }

    private List<T> getAutoComplete(CharSequence constraint) {
        LogUtil.i(TAG, "Starting autocomplete query for: " + constraint);
        List<T> list = new List<>();
        for (T t : getList()) if (t.onSearch(constraint.toString())) list.add(t);
        return list;
    }
}
