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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import promise.commons.data.log.LogUtil;


/** Created by yoctopus on 11/12/16. */
public class BitmapUtil {
  private static String TAG = LogUtil.makeTag(BitmapUtil.class);

  /**
   * get the bitmap object in a resource reference id, use @code <>decodeResource of the Bitmap
   * factory class</>
   *
   * @param context used to resolve id
   * @param id the id of raw resource
   * @return a bitmap at the id reference location
   */
  public static Bitmap getBitmap(@NonNull Context context, @DrawableRes int id) {
    return BitmapFactory.decodeResource(context.getResources(), id);
  }

  public static void addImageToGallery(final String filePath, final Context context) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.MediaColumns.DATA, filePath);
    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
  }

  public static String toBase64(Bitmap bitmap) {
    if (bitmap == null) return "";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
    byte[] b = baos.toByteArray();
    String temp = null;
    try {
      System.gc();
      temp = Base64.encodeToString(b, Base64.NO_WRAP);
    } catch (OutOfMemoryError e) {
      baos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
      b = baos.toByteArray();
      temp = Base64.encodeToString(b, Base64.NO_WRAP);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return temp;
  }

  public static Bitmap fromBase64(@NonNull String encoded) {
    byte[] bytes = Base64.decode(encoded, Base64.NO_WRAP);
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
  }

  public static String getPathFromGooglePhotosUri(Context context, Uri uriPhoto) {
    if (uriPhoto == null) return null;

    FileInputStream input = null;
    FileOutputStream output = null;
    try {
      ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uriPhoto, "r");
      FileDescriptor fd = pfd.getFileDescriptor();
      input = new FileInputStream(fd);

      String tempFilename = getTempFilename(context);
      output = new FileOutputStream(tempFilename);

      int read;
      byte[] bytes = new byte[4096];
      while ((read = input.read(bytes)) != -1) {
        output.write(bytes, 0, read);
      }
      return tempFilename;
    } catch (IOException ignored) {
      // Nothing we can do
    } finally {
      closeSilently(input);
      closeSilently(output);
    }
    return null;
  }

  public static void closeSilently(Closeable c) {
    if (c == null) return;
    try {
      c.close();
    } catch (Throwable t) {
      // Do nothing
    }
  }

  private static String getTempFilename(Context context) throws IOException {
    File outputDir = context.getCacheDir();
    File outputFile = File.createTempFile("image", "tmp", outputDir);
    return outputFile.getAbsolutePath();
  }

  public static byte[] byteArray(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
    bitmap.recycle();
    return stream.toByteArray();
  }

  public static Bitmap bitmap(byte[] bytes) {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
  }

  @Nullable
  public static Bitmap bitmap(File file) {
    return bitmap(file.getAbsolutePath(), 500);
  }

  @Nullable
  public static Bitmap bitmap(String imgPath, int size) {
    Bitmap b;
    File f = new File(imgPath);
    try {
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      FileInputStream fis = new FileInputStream(f);
      BitmapFactory.decodeStream(fis, null, o);
      fis.close();
      int scale = 1;
      if (o.outHeight > size || o.outWidth > size)
        scale =
            (int)
                Math.pow(
                    2,
                    (int)
                        Math.ceil(
                            Math.log(size / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = scale;
      fis = new FileInputStream(f);
      b = BitmapFactory.decodeStream(fis, null, o2);
      fis.close();
    } catch (Exception e) {
      LogUtil.e("BitmapCompressError", e);
      return null;
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    b.compress(Bitmap.CompressFormat.JPEG, 50, bos);
    return b;
  }

  public static Bitmap bitmap(String imgPath) {
    /*File cache = new File(f.getParent() + "/cache");
    Dir.make(cache.getAbsolutePath());
    Dir.copy(f.getParent(), f.getName(), cache.getPath());
    cache = Dir.getFile(cache.getPath(), f.getName());*/
    return BitmapFactory.decodeFile(imgPath);
  }

  public static String compressImage(Context context, String imageUri) {

    String filePath = getRealPathFromURI(imageUri, context);
    Bitmap scaledBitmap = null;

    BitmapFactory.Options options = new BitmapFactory.Options();

    //      by setting this field as true, the actual bitmap pixels are not loaded in the memory.
    // Just the bounds are loaded. If
    //      you try the use the bitmap here, you will get null.
    options.inJustDecodeBounds = true;
    Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

    int actualHeight = options.outHeight;
    int actualWidth = options.outWidth;

    //      max Height and width values of the compressed image is taken as 816x612

    float maxHeight = 816.0f;
    float maxWidth = 612.0f;
    float imgRatio = actualHeight != 0 ? actualWidth / actualHeight : 0;
    float maxRatio = maxWidth / maxHeight;

    //      width and height values are set maintaining the aspect ratio of the image

    if (actualHeight > maxHeight || actualWidth > maxWidth) {
      if (imgRatio < maxRatio) {
        imgRatio = maxHeight / actualHeight;
        actualWidth = (int) (imgRatio * actualWidth);
        actualHeight = (int) maxHeight;
      } else if (imgRatio > maxRatio) {
        imgRatio = maxWidth / actualWidth;
        actualHeight = (int) (imgRatio * actualHeight);
        actualWidth = (int) maxWidth;
      } else {
        actualHeight = (int) maxHeight;
        actualWidth = (int) maxWidth;
      }
    }

    //      setting inSampleSize value allows to load a scaled down version of the original image

    options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

    //      inJustDecodeBounds set to false to load the actual bitmap
    options.inJustDecodeBounds = false;

    //      this options allow android to claim the bitmap memory if it runs low on memory
    options.inPurgeable = true;
    options.inInputShareable = true;
    options.inTempStorage = new byte[16 * 1024];

    try {
      bmp = BitmapFactory.decodeFile(filePath, options);
      scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
    } catch(IllegalArgumentException e) {
      LogUtil.e(TAG, e);
      return imageUri;
    }
    catch (OutOfMemoryError exception) {
      LogUtil.e(TAG, exception);
    }

    float ratioX = actualWidth / (float) options.outWidth;
    float ratioY = actualHeight / (float) options.outHeight;
    float middleX = actualWidth / 2.0f;
    float middleY = actualHeight / 2.0f;

    Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

    Canvas canvas = new Canvas(scaledBitmap);
    canvas.setMatrix(scaleMatrix);
    canvas.drawBitmap(
        bmp,
        middleX - bmp.getWidth() / 2,
        middleY - bmp.getHeight() / 2,
        new Paint(Paint.FILTER_BITMAP_FLAG));

    //      check the rotation of the image and display it properly
    ExifInterface exif;
    try {
      exif = new ExifInterface(filePath);

      int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
      LogUtil.d(TAG, "Exif: " + orientation);
      Matrix matrix = new Matrix();
      if (orientation == 6) {
        matrix.postRotate(90);
        LogUtil.d(TAG, "Exif: " + orientation);
      } else if (orientation == 3) {
        matrix.postRotate(180);
        LogUtil.d(TAG, "Exif: " + orientation);
      } else if (orientation == 8) {
        matrix.postRotate(270);
        LogUtil.d(TAG, "Exif: " + orientation);
      }
      scaledBitmap = Bitmap.createBitmap(
              scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
    } catch (IOException e) {
      e.printStackTrace();
    }

    FileOutputStream out;
    try {
      out = new FileOutputStream(imageUri);
      //          write the compressed bitmap at the destination specified by filename.
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

    } catch (FileNotFoundException e) {
      LogUtil.e(TAG, e);
    }

    return imageUri;
  }

  private static String getRealPathFromURI(String contentURI, Context context) {
    Uri contentUri = Uri.parse(contentURI);
    if (context == null) return contentURI;
    Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
    if (cursor == null) {
      return contentUri.getPath();
    } else {
      cursor.moveToFirst();
      int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
      String path = cursor.getString(index);
      cursor.close();
      return path;
    }
  }

  public static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth, int reqHeight) {
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int heightRatio = Math.round((float) height / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }
    final float totalPixels = width * height;
    final float totalReqPixelsCap = reqWidth * reqHeight * 2;
    while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
      inSampleSize++;
    }

    return inSampleSize;
  }
}
