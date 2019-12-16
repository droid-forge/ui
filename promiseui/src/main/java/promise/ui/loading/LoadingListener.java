package promise.ui.loading;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.List;

import promise.ui.model.Viewable;


public interface LoadingListener {

  void showContent();

  void showContent(List<Integer> idsOfViewsNotToShow);

  void showLoading(Viewable viewable, Object args);
  void showLoading(Object args);

  void showLoading(List<Integer> idsOfViewsNotToHide, Object args);

  void showEmpty(int icon, String title, String description);

  void showEmpty(Drawable icon, String title, String description);

  void showEmpty(int icon, String title, String description, List<Integer> idsOfViewsNotToHide);

  void showEmpty(Drawable icon, String title, String description, List<Integer> idsOfViewsNotToHide);

  void showError(int icon, String title, String description, String buttonText, View.OnClickListener buttonClickListener);

  void showError(Drawable icon, String title, String description, String buttonText, View.OnClickListener buttonClickListener);

  void showError(int icon, String title, String description, String buttonText, View.OnClickListener buttonClickListener, List<Integer> idsOfViewsNotToHide);

  void showError(Drawable icon, String title, String description, String buttonText, View.OnClickListener buttonClickListener, List<Integer> idsOfViewsNotToHide);

  String getCurrentState();

  boolean isContentCurrentState();

  boolean isLoadingCurrentState();

  boolean isEmptyCurrentState();

  boolean isErrorCurrentState();
}