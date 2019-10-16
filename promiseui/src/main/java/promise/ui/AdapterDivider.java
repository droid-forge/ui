/*
 * Copyright 2017, Solutech RMS
 * Licensed under the Apache License, Version 2.0, "Solutech Limited".
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package promise.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterDivider extends RecyclerView.ItemDecoration  {
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    private static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    private static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation;

    public AdapterDivider(Context context,
                          int orientation) {
        final TypedArray a = context.obtainStyledAttributes(getATTRS());
        setmDivider(a.getDrawable(0));
        a.recycle();
        setOrientation(orientation);
    }

    public static int[] getATTRS() {
        return ATTRS;
    }

    public static int getHorizontalList() {
        return HORIZONTAL_LIST;
    }

    public static int getVerticalList() {
        return VERTICAL_LIST;
    }

    public void setOrientation(int orientation) {
        if (orientation != getHorizontalList() &&
                orientation != getVerticalList())
            throw new IllegalArgumentException("invalid orientation");
        setmOrientation(orientation);
    }
    @Override
    public void onDrawOver(Canvas c,
                           RecyclerView parent,
                           RecyclerView.State state) {
        if (getmOrientation() == getVerticalList()) drawVertical(c,
                parent);
        else drawHorizontal(c,
                parent);
    }
    public void drawVertical(Canvas c,
                             RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child
                            .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + getmDivider().getIntrinsicHeight();
            getmDivider().setBounds(left,
                    top,
                    right,
                    bottom);
            getmDivider().draw(c);
        }
    }

    public void drawHorizontal(Canvas c,
                               RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child
                            .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + getmDivider().getIntrinsicHeight();
            getmDivider().setBounds(left,
                    top,
                    right,
                    bottom);
            getmDivider().draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect,
                               View view,
                               RecyclerView parent,
                               RecyclerView.State state) {
        if (getmOrientation() == getVerticalList()) outRect.set(0,
                0,
                0,
                getmDivider().getIntrinsicHeight());
        else outRect.set(0,
                0,
                getmDivider().getIntrinsicWidth(),
                0);
    }

    public Drawable getmDivider() {
        return mDivider;
    }

    public void setmDivider(Drawable mDivider) {
        this.mDivider = mDivider;
    }

    public int getmOrientation() {
        return mOrientation;
    }

    public void setmOrientation(int mOrientation) {
        this.mOrientation = mOrientation;
    }
}
