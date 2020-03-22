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

package promise.ui.notification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.annotation.StringRes;

public class NotificationDialog extends AlertDialog {

  private String title, message;
  private DialogButton positive, negative, neutral;
  private CListener cListener;
  private OnAnswer listener;
  private boolean close = true;

  public NotificationDialog(
      Context context,
      String title,
      String message,
      DialogButton positive,
      DialogButton negative,
      DialogButton neutral) {
    super(context);
    this.title = title;
    this.message = message;
    this.positive = positive;
    this.negative = negative;
    this.neutral = neutral;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
      requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setCanceledOnTouchOutside(true);
  }

  public NotificationDialog(
      Context context,
      String title,
      String message,
      DialogButton positive,
      DialogButton negative,
      DialogButton neutral,
      OnAnswer listener) {
    this(context, title, message, positive, negative, neutral);
    this.listener = listener;
  }

  public NotificationDialog(
      Context context,
      @StringRes int title,
      @StringRes int message,
      DialogButton positive,
      DialogButton negative,
      DialogButton neutral) {
    this(
        context,
        context.getResources().getString(title),
        context.getResources().getString(message),
        positive,
        negative,
        neutral);
  }

  public void setcListener(CListener cListener) {
    this.cListener = cListener;
  }

  public NotificationDialog close(boolean close) {
    this.close = close;
    return this;
  }

  @Override
  public void show() {
    Builder builder = new Builder(getContext());
    builder.setTitle(title);
    builder.setMessage(message);
    if (positive != null)
      builder.setPositiveButton(
          positive.getText(getContext()),
          new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              if (positive.getListener() == null) return;
              positive.getListener().onClick(null);
            }
          });
    if (negative != null)
      builder.setNegativeButton(
          negative.getText(getContext()),
          new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              if (negative.getListener() == null) return;
              negative.getListener().onClick(null);
            }
          });
    if (neutral != null)
      builder.setNeutralButton(
          neutral.getText(getContext()),
          new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              if (neutral.getListener() == null) return;
              neutral.getListener().onClick(null);
            }
          });

    if (cListener != null && listener == null)
      builder.setOnCancelListener(
          new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
              cListener.onCancel(dialogInterface);
            }
          });
    builder.setCancelable(close);
    builder.show();
  }

  public interface CListener {
    void onCancel(DialogInterface dialogInterface);
  }

  public interface OnAnswer {
    void onAnswer(String t);
  }

  public static class DialogButton {
    private String text = "";
    private int textId = 0;
    private BListener listener;

    public DialogButton(String text, BListener listener) {
      this.text = text;
      this.listener = listener;
    }
    public DialogButton(@StringRes int text, BListener listener) {
      this.textId = text;
      this.listener = listener;
    }

    public String getText(Context context) {
      if (textId > 0) return context.getString(textId);
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public BListener getListener() {
      return listener;
    }

    public void setListener(BListener listener) {
      this.listener = listener;
    }

    public interface BListener {
      void onClick(View v);
    }
  }
}
