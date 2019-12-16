package promise.ui;

import promise.commons.model.List;

public interface PaginatedAdapter<T> {

   void add(List<T> list);

   boolean hasLoader();

   void addLoadingView();

   void removeLoader();
}
