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

package promise.ui.loading;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.List;

import promise.ui.Viewable;


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