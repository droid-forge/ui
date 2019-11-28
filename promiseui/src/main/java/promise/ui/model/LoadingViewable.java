package promise.ui.model;

import android.view.View;

import androidx.annotation.LayoutRes;

public interface LoadingViewable  {
    @LayoutRes
    int layout();
    void init(View view);
}
