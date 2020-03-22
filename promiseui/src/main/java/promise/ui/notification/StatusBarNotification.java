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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;

class StatusBarNotification {
  private static final String NOTIFICATION_TAG = "Promise_Message";
 private static  String GROUP_KEY = "promise.GROUP_NOTIFICATION";
  private static int id_normal;
  private static NotificationManager notificationManager;

  static void notify(final Context context,
                     String title,
                     final String message,
                     Bitmap bitmap,
                     @DrawableRes int small,
                     PendingIntent pendingIntent) {
    NotificationCompat.Builder builder;
    if (notificationManager == null)
      notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel mChannel = null;
      if (notificationManager != null)
        mChannel = notificationManager.getNotificationChannel("normal_channel_id");
      if (mChannel == null) {
        mChannel = new NotificationChannel("normal_channel_id", title, importance);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(mChannel);
      }
      builder = new NotificationCompat.Builder(context, "normal_channel_id");
      builder.setContentTitle(message)
          .setDefaults(Notification.DEFAULT_ALL)
          .setSmallIcon(small)
          .setContentTitle(title)
          .setContentText(message)
          .setPriority(NotificationCompat.PRIORITY_DEFAULT)
          .setLargeIcon(bitmap)
          .setTicker(message)
          .setGroup(GROUP_KEY)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true);
    } else builder =
        new NotificationCompat.Builder(context, "normal_channel_id")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(small)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(bitmap)
            .setGroup(GROUP_KEY)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

    notify(context, builder.build());
  }

  static void notifyWithProgress(final Context context,
                                 String title,
                                 final String message,
                                 Bitmap bitmap,
                                 @DrawableRes int small,
                                 int progress) {
    NotificationCompat.Builder builder;
    if (notificationManager == null)
      notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel mChannel = null;
      if (notificationManager != null)
        mChannel = notificationManager.getNotificationChannel("progress_channel_id");
      if (mChannel == null) {
        mChannel = new NotificationChannel("progress_channel_id", title, importance);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(mChannel);
      }
      builder = new NotificationCompat.Builder(context, "progress_channel_id");
      builder.setContentTitle(message)
          .setDefaults(Notification.DEFAULT_ALL)
          .setSmallIcon(small)
          .setContentTitle(title)
          .setContentText(message)
          .setPriority(NotificationCompat.PRIORITY_DEFAULT)
          .setLargeIcon(bitmap)
          .setTicker(message)
          .setGroup(GROUP_KEY)
          .setProgress(100, progress, false)
          .setAutoCancel(false);
    } else builder =
        new NotificationCompat.Builder(context, "progress_channel_id")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(small)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(bitmap)
            .setGroup(GROUP_KEY)
            .setProgress(100, progress, false)
            .setAutoCancel(false);
    notify(0, context, builder.build());
  }

  @TargetApi(Build.VERSION_CODES.ECLAIR)
  private static void notify(final Context context,
                             final Notification notification) {
    final NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) notificationManager.notify(NOTIFICATION_TAG,
        id_normal,
        notification);
    id_normal++;
  }

  @TargetApi(Build.VERSION_CODES.ECLAIR)
  private static void notify(int id, final Context context,
                             final Notification notification) {
    final NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) notificationManager.notify(NOTIFICATION_TAG,
        id,
        notification);
  }

}
