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

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

class NotificationToast {

    private Context context;
    private String message;
    private @StringRes
    int messageId;

    @SuppressLint("InflateParams")
    NotificationToast(Context context, String message) {
        this.context = context;
        this.message = message;
    }

    @SuppressLint("InflateParams")
    NotificationToast(Context context, @StringRes int message) {
        this.context = context;
        this.message = context.getString(message);
    }

    void show() {
        Toast.makeText(context,
                message,
                Toast.LENGTH_LONG).show();
    }
}
