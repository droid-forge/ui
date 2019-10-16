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

package promise.ui.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import promise.commons.Promise;
import promise.commons.model.List;
import promise.commons.util.ClassUtil;
import promise.commons.util.Conditions;


public class ApiHelper {
    public static boolean isGooglePlayInstalled() {
        PackageManager pm = Promise.instance().context().getPackageManager();
        boolean app_installed;
        try {
            PackageInfo info = pm.getPackageInfo(
                    "com.android.vending",
                    PackageManager.GET_ACTIVITIES);
            String label = (String)
                    info.applicationInfo.loadLabel(pm);
            app_installed = (label != null
                    && label.equals(
                    "Google Play Store"));
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static void isReadContactsAllowed(Activity context,
                                             CallBack callBack) {
        checkPermission(context, callBack, Manifest.permission.READ_CONTACTS);
    }

    public static void isSendSmsAllowed(Activity context,
                                        CallBack callBack) {
        checkPermission(context, callBack, Manifest.permission.SEND_SMS);
    }

    public static void isCameraAllowed(Activity context,
                                       CallBack callBack) {
        checkPermission(context, callBack, Manifest.permission.CAMERA);
    }

    public static void isWritStorageAllowed(Activity context,
                                            CallBack callBack) {
        checkPermission(context, callBack, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void isReadStorageAllowed(Activity context,
                                            CallBack callBack) {
        checkPermission(context, callBack, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void isLocationAllowed(Activity context,
                                         CallBack callBack) {
        if (hasLollipop()) checkPermission(context, callBack,
            Manifest.permission.ACCESS_FINE_LOCATION);
        else checkPermission(context, callBack,
            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION});
    }

    public static void checkPermission(Context context, CallBack callBack, String permission) {
        if (!hasMashMellow()) callBack.onSuccess(permission);
        else {
            if (ContextCompat.checkSelfPermission(Conditions.checkNotNull(context),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) callBack.onFailure(permission);
            else callBack.onSuccess(permission);
        }
    }

    public static int batteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        float batteryPct = level / (float) scale; return (int) (batteryPct * 100);
    }

    public static void checkPermission2(FragmentActivity context,
                                       CallBack callBack,
                                       String permission) {
        if (!hasMashMellow()) callBack.onSuccess(permission);
        else {
            if (ContextCompat.checkSelfPermission(Conditions.checkNotNull(context),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                callBack.onFailure(permission);
                requestPermission(context, callBack, permission);
            } else callBack.onSuccess(permission);
        }
    }

    public static void checkPermission(Activity context,
                                       CallBack callBack,
                                       String[] permissions) {
        List<String> passed = new List<>(),
                failed = new List<>();
        boolean checked = true;
        context = Conditions.checkNotNull(context);
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                failed.add(permission);
                checked = false;
            } else passed.add(permission);
        }
        if (checked) {
            if (!passed.isEmpty()) callBack.onSuccess(passed.get(0));
            else callBack.onSuccess(null);
        } else {
            if (!failed.isEmpty()) {
                checkPermission(context, new CallBack() {
                    @Override
                    public void onSuccess(String permission) {

                    }

                    @Override
                    public void onFailure(String permission) {

                    }
                }, failed.toArray(new String[failed.size()]), 0);
                callBack.onFailure(failed.get(0));
            } else callBack.onFailure(null);
        }
    }

    public static void checkPermission(Activity context,
                                       final CallBack callBack,
                                       String[] permissions,
                                       int request) {
        List<String> passed = new List<>(), failed = new List<>();
        boolean checked = true;
        context = Conditions.checkNotNull(context);
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                failed.add(permission);
                checked = false;
            } else passed.add(permission);
        }
        if (checked) {
            if (!passed.isEmpty()) callBack.onSuccess(passed.get(0));
            else callBack.onSuccess(null);
        } else {
            if (!failed.isEmpty()) {
                String[] fails = new String[failed.size()];
                for (int i = 0; i < failed.size(); i++) fails[i] = failed.get(i);
                requestPermission(context, fails, request);
            } else callBack.onSuccess(null);
        }
    }

    public static void requestPermission(Activity context,
                                         String[] permissions,
                                         int request) {
        if (!hasMashMellow()) {
            return;
        }
        Conditions.checkNotNull(permissions);
        Conditions.checkState(permissions.length != 0, "No permissions requested");
        ActivityCompat.requestPermissions(context,
                permissions, request);
    }

    @SuppressLint("CheckResult")
    public static void requestPermission(FragmentActivity activity,
                                         final CallBack callBack,
                                         final String permission) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(permission)
                .subscribe(aBoolean -> {
                    if (aBoolean) callBack.onSuccess(permission);
                    else callBack.onFailure(permission);
                });
    }


    @TargetApi(11)
    public static void enableStrictMode(Class classs) {
        if (hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            if (hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder
                        .setClassInstanceLimit(classs, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMashMellow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean supportsPreferenceHeaders() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public interface VERSION_CODES {
        // These value are copied from Build.VERSION_CODES
        int GINGERBREAD = Build.VERSION_CODES.GINGERBREAD;
        int GINGERBREAD_MR1 = Build.VERSION_CODES.GINGERBREAD_MR1;
        int HONEYCOMB = Build.VERSION_CODES.HONEYCOMB;
        int HONEYCOMB_MR1 = Build.VERSION_CODES.HONEYCOMB_MR1;
        int HONEYCOMB_MR2 = Build.VERSION_CODES.HONEYCOMB_MR2;
        int ICE_CREAM_SANDWICH = Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        int ICE_CREAM_SANDWICH_MR1 = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
        int JELLY_BEAN = Build.VERSION_CODES.JELLY_BEAN;
        int JELLY_BEAN_MR1 = Build.VERSION_CODES.JELLY_BEAN_MR1;
        int KITKAT = Build.VERSION_CODES.KITKAT;
        int LOLLIPOP = Build.VERSION_CODES.LOLLIPOP;
    }

    public static final boolean HAS_ACTION_BAR_HOME_BUTTON =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_ICS =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean USE_888_PIXEL_FORMAT =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean ENABLE_PHOTO_EDITOR =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE =
            ClassUtil.hasField(View.class, "SYSTEM_UI_FLAG_LAYOUT_STABLE");

    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION =
            ClassUtil.hasField(View.class, "SYSTEM_UI_FLAG_HIDE_NAVIGATION");

    public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT =
            ClassUtil.hasField(MediaStore.MediaColumns.class, "WIDTH");

    public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_FACTORY =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_SET_BEAM_PUSH_URIS =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_SET_DEFALT_BUFFER_SIZE = ClassUtil.hasMethod(
            "android.graphics.SurfaceTexture", "setDefaultBufferSize",
            int.class, int.class);

    public static final boolean HAS_RELEASE_SURFACE_TEXTURE = ClassUtil.hasMethod(
            "android.graphics.SurfaceTexture", "release");

    public static final boolean HAS_SURFACE_TEXTURE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_MTP =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;

    public static final boolean HAS_AUTO_FOCUS_MOVE_CALLBACK =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_REMOTE_VIEWS_SERVICE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_INTENT_EXTRA_LOCAL_ONLY =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_SET_SYSTEM_UI_VISIBILITY =
            ClassUtil.hasMethod(View.class, "setSystemUiVisibility", int.class);

    public static final boolean HAS_FACE_DETECTION;

    static {
        boolean hasFaceDetection = false;
        try {
            Class<?> listenerClass = Class.forName(
                    "android.hardware.Camera$FaceDetectionListener");
            hasFaceDetection =
                    ClassUtil.hasMethod(Camera.class, "setFaceDetectionListener",
                            listenerClass) &&
                            ClassUtil.hasMethod(Camera.class, "startFaceDetection") &&
                            ClassUtil.hasMethod(Camera.class, "stopFaceDetection") &&
                            ClassUtil.hasMethod(Camera.Parameters.class, "getMaxNumDetectedFaces");
        } catch (Throwable ignored) {
        }
        HAS_FACE_DETECTION = hasFaceDetection;
    }

    public static final boolean HAS_GET_CAMERA_DISABLED =
            ClassUtil.hasMethod(DevicePolicyManager.class, "getCameraDisabled", ComponentName.class);

    public static final boolean HAS_MEDIA_ACTION_SOUND =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_OLD_PANORAMA =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_TIME_LAPSE_RECORDING =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_ZOOM_WHEN_RECORDING =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_CAMERA_FOCUS_AREA =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_CAMERA_METERING_AREA =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_FINE_RESOLUTION_QUALITY_LEVELS =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_MOTION_EVENT_TRANSFORM =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_EFFECTS_RECORDING = false;

    // "Background" filter does not have "context" input port in jelly bean.
    public static final boolean HAS_EFFECTS_RECORDING_CONTEXT_INPUT =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean HAS_GET_SUPPORTED_VIDEO_SIZE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_SET_ICON_ATTRIBUTE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_MEDIA_PROVIDER_FILES_TABLE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_SURFACE_TEXTURE_RECORDING =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_ACTION_BAR =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    // Ex: View.setTranslationX.
    public static final boolean HAS_VIEW_TRANSFORM_PROPERTIES =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_CAMERA_HDR =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean HAS_OPTIONS_IN_MUTABLE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean CAN_START_PREVIEW_IN_JPEG_CALLBACK =
            Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;

    public static final boolean HAS_VIEW_PROPERTY_ANIMATOR =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;

    public static final boolean HAS_POST_ON_ANIMATION =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public interface CallBack {
        void onSuccess(String permission);
        void onFailure(String permission);
    }

    public static String getAppVersionCode() {
        try {
            PackageInfo packageInfo = Promise.instance().context()
                    .getPackageManager().getPackageInfo(Promise.instance().context().getPackageName(), 0);
            return String.valueOf(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }


}
