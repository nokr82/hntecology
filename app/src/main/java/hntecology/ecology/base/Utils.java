package hntecology.ecology.base;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Utils {
    private static Bitmap noImageBitmap = null;

    public static String since(String reg_dt) {
        if (reg_dt == null || reg_dt.trim().length() == 0) {
            return "";
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.KOREA);
        try {
            Date d1 = formatter.parse(reg_dt);
            Date d2 = new Date();

            long diff = d2.getTime() - d1.getTime();

            // System.out.println("d1.getTime() : " + d1.getTime() +
            // ", d2.getTime() : " + d2.getTime());

            long oneMin = 60 * 1000;
            long oneHour = 60 * oneMin;
            long oneDay = 24 * oneHour;
            long threeDays = 3 * oneDay;
            long oneYear = oneDay * 365;


            // 3분 이내일 경우 "방금 전"
            if (diff < oneMin * 3) {
                return "방금 전";
            }

            // 3분에서 59분일 경우 "*분 전"
            if(diff < oneHour) {
                return Math.round(diff / oneMin) + "분 전";
            }

            // 1시간 후 부터는 분 단위 무시하고 시간 단위만 표시
            if(diff >= oneHour && diff <= oneHour * 3) {
                return Math.round(diff / oneHour) + "시간 전";
            }

            // 3시간 초과인 경우 10월 1일 오후 3시 30분
            if (diff < oneYear) {
                formatter = new SimpleDateFormat("M월 d일 a h시 mm분", java.util.Locale.KOREA);
                return formatter.format(d1);
            }

            // 1년 이상이면 년월일만 표시
            if (diff >= oneYear) {
                formatter = new SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.KOREA);
                return formatter.format(d1);
            }

            Calendar cal1 = Calendar.getInstance(java.util.Locale.KOREA);
            cal1.setTime(d1);

            Calendar cal2 = Calendar.getInstance(java.util.Locale.KOREA);
            cal2.setTime(d2);
            cal2.add(Calendar.DAY_OF_MONTH, -1);

            if (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) || diff > oneDay) {
                formatter = new SimpleDateFormat("MM월 dd일", java.util.Locale.KOREA);
                return formatter.format(d1);
            }

            formatter = new SimpleDateFormat("HH시 mm분", java.util.Locale.KOREA);
            return formatter.format(d1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return reg_dt;
    }

    /**
     * EXIF정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation
     *            EXIF 회전각
     * @return 실제 각도
     */
    public static int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public static int calculateInSampleSizeByWidth(BitmapFactory.Options options, int reqWidth) {
        // Raw height and width of image
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {

            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int calculateInSampleSizeByHeight(BitmapFactory.Options options, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight) {

            final int halfHeight = height / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap
     *            비트맵 이미지
     * @param degrees
     *            회전 각도
     * @return 회전된 이미지
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError e) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 이미지를 회전시킵니다.
     *
     * @param photoPath
     * @return 회전된 이미지
     */
    public static Bitmap rotate(String photoPath) {
        try {
            // 비트맵 이미지로 가져온다
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);

            // Calculate inSampleSize
            int orientation = getRotation(photoPath);
            int reqSize = 720;
            if (orientation == 90 || orientation == 270) {
                options.inSampleSize = Utils.calculateInSampleSizeByHeight(options, reqSize);
            } else if (orientation == 0 || orientation == 180) {
                options.inSampleSize = Utils.calculateInSampleSizeByWidth(options, reqSize);
            }

            // System.out.println("options.inSampleSize : " + options.inSampleSize + ", orientation : " + orientation);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
            return Utils.rotate(bm, orientation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getRotation(String photoPath) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return Utils.exifOrientationToDegrees(exifOrientation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Bitmap getImage(ContentResolver resolver, String imageIdOrPath) {
        try {
            String photoPath = null;
            int orientation = 0;

            try {
                int uid = Integer.parseInt(imageIdOrPath);
                String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };

                String selection = Images.Media._ID + " = " + uid;

                Cursor cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, proj, selection, Images.Media.DATE_ADDED + " DESC");
                if (cursor != null && cursor.moveToFirst()) {
                    photoPath = cursor.getString(cursor.getColumnIndex(proj[0]));
                    orientation = cursor.getInt(cursor.getColumnIndex(proj[1]));
                }
                cursor.close();

            } catch (NumberFormatException e) {
                photoPath = imageIdOrPath;

                // rotation
                ExifInterface exif;
                try {
                    exif = new ExifInterface(photoPath);
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    orientation = Utils.exifOrientationToDegrees(exifOrientation);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            // 비트맵 이미지로 가져온다
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);

            // Calculate inSampleSize
            int reqSize = 654;
            if (orientation == 90 || orientation == 270) {
                options.inSampleSize = Utils.calculateInSampleSizeByHeight(options, reqSize);
            } else if (orientation == 0 || orientation == 180) {
                options.inSampleSize = Utils.calculateInSampleSizeByWidth(options, reqSize);
            }

            // System.out.println("options.inSampleSize : " + options.inSampleSize + ", orientation : " + orientation);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
            return Utils.rotate(bm, orientation);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }



    public static Bitmap getImage(ContentResolver resolver, String imageIdOrPath, int reqSize) {
        try {
            String photoPath = null;
            int orientation = 0;

            try {
                int uid = Integer.parseInt(imageIdOrPath);
                String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };

                String selection = Images.Media._ID + " = " + uid;

                Cursor cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, proj, selection, Images.Media.DATE_ADDED + " DESC");
                if (cursor != null && cursor.moveToFirst()) {
                    photoPath = cursor.getString(cursor.getColumnIndex(proj[0]));
                    orientation = cursor.getInt(cursor.getColumnIndex(proj[1]));
                }
                cursor.close();

            } catch (NumberFormatException e) {
                photoPath = imageIdOrPath;

                // rotation
                ExifInterface exif;
                try {
                    exif = new ExifInterface(photoPath);
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    orientation = Utils.exifOrientationToDegrees(exifOrientation);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            // 비트맵 이미지로 가져온다
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);

            // Calculate inSampleSize
            if (orientation == 90 || orientation == 270) {
                options.inSampleSize = Utils.calculateInSampleSizeByHeight(options, reqSize);
            } else if (orientation == 0 || orientation == 180) {
                options.inSampleSize = Utils.calculateInSampleSizeByWidth(options, reqSize);
            }

            // System.out.println("options.inSampleSize : " + options.inSampleSize + ", orientation : " + orientation);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
            return Utils.rotate(bm, orientation);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap getImage(Context context, ContentResolver resolver, String imageIdOrPath, int i) {
        try {
            String photoPath = null;
            int orientation = 0;

            try {
                int uid = Integer.parseInt(imageIdOrPath);
                String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };

                String selection = Images.Media._ID + " = " + uid;

                Cursor cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, proj, selection, Images.Media.DATE_ADDED + " DESC");
                if (cursor != null && cursor.moveToFirst()) {
                    photoPath = cursor.getString(cursor.getColumnIndex(proj[0]));
                    orientation = cursor.getInt(cursor.getColumnIndex(proj[1]));
                }
                cursor.close();

            } catch (NumberFormatException e) {
                photoPath = imageIdOrPath;

                // rotation
                ExifInterface exif;
                try {
                    exif = new ExifInterface(photoPath);
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    orientation = Utils.exifOrientationToDegrees(exifOrientation);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            // 비트맵 이미지로 가져온다
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);

            // Calculate inSampleSize
            int reqSize = 654;
            if (orientation == 90 || orientation == 270) {
                options.inSampleSize = Utils.calculateInSampleSizeByHeight(options, reqSize);
            } else if (orientation == 0 || orientation == 180) {
                options.inSampleSize = Utils.calculateInSampleSizeByWidth(options, reqSize);
            }

            // System.out.println("options.inSampleSize : " + options.inSampleSize + ", orientation : " + orientation);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
            return Utils.rotate(bm, orientation);

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Toast.makeText(context, "권한이 없거나 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    public static File getFile(ContentResolver resolver, int imageId) {
        String photoPath = null;

        try {
            String[] proj = { Images.Media.DATA };

            String selection = Images.Media._ID + " = " + imageId;

            Cursor cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, proj, selection, Images.Media.DATE_ADDED + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                photoPath = cursor.getString(cursor.getColumnIndex(proj[0]));
            }
            cursor.close();

        } catch (NumberFormatException e) {
            return null;
        }

        return new File(photoPath);
    }

    public static Bitmap getThumbnailImage(ContentResolver resolver, String imageId) {
        int uid = Integer.parseInt(imageId);
        String photoPath = null;
        int orientation = 0;

        String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };

        String selection = Images.Media._ID + " = " + uid;

        Cursor cursor = Images.Media.query(resolver, Images.Media.EXTERNAL_CONTENT_URI, proj, selection, Images.Media.DATE_ADDED + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            photoPath = cursor.getString(cursor.getColumnIndex(proj[0]));
            orientation = cursor.getInt(cursor.getColumnIndex(proj[1]));
        }
        cursor.close();

        Bitmap micro = Images.Thumbnails.getThumbnail(resolver, uid, Images.Thumbnails.MICRO_KIND, null);

        if (micro != null) {
            return Utils.rotate(micro, orientation);
        } else {
            Cursor mini = Images.Thumbnails.queryMiniThumbnail(resolver, uid, Images.Thumbnails.MINI_KIND, proj);
            if (mini != null && mini.moveToFirst()) {
                photoPath = mini.getString(mini.getColumnIndex(proj[0]));
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (options.outWidth > 96) {
            int ws = options.outWidth / 96 + 1;
            if (ws > options.inSampleSize) {
                options.inSampleSize = ws;
            }
        }
        if (options.outHeight > 96) {
            int hs = options.outHeight / 96 + 1;
            if (hs > options.inSampleSize) {
                options.inSampleSize = hs;
            }
        }

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        return Utils.rotate(bitmap, orientation);
    }

    public static String saveBitmap(Context context, Bitmap bitmap) {
        try {
            String dataDir = context.getApplicationInfo().dataDir;
            dataDir = dataDir + File.separator + "download";
            File dir = new File(dataDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // list
            File f = new File(dataDir, String.valueOf(System.currentTimeMillis() + ".png"));
            f.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.close();

            return f.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Uri getLocalBitmapUri(Bitmap bmp) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


    public static String saveBytes(Context context, byte[] imgByte) {

        try {
            String dataDir = context.getApplicationInfo().dataDir;
            dataDir = dataDir + File.separator + "download";
            File dir = new File(dataDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // list
            File f = new File(dataDir, String.valueOf(System.currentTimeMillis()));
            f.createNewFile();

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bos.write(imgByte);
            bos.flush();
            bos.close();

            return f.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void cleanUpDownload(Context context) {
        String dataDir = context.getApplicationInfo().dataDir;
        dataDir = dataDir + File.separator + "download";
        File f = new File(dataDir);
        if (f.exists()) {
            String[] list = f.list();
            for (String a : list) {
                File d = new File(dataDir + File.separator + a);
                d.delete();
            }
        } else {
            f.mkdirs();
        }

    }

    public static int getScreenHeight(Context context) {
        int height = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        } else {
            height = display.getHeight(); // deprecated
        }
        return height;
    }

    public static int getStatusBarHeight(Activity activity) {
        int height = 0;

        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = activity.getResources().getDimensionPixelSize(resourceId);
        }

        return height;
    }

    public static int getScreenWidth(Context context) {
        int width = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else {
            width = display.getWidth(); // deprecated
        }
        return width;
    }

    public static void alert(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showNotification(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context context, String msg, final AlertListener alertListener) {
        if (alertListener != null && alertListener.before()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(msg).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();

                    if (alertListener != null) {
                        alertListener.after();
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public static String getipAddress() {
        Socket socket = null;
        try {
            socket = new Socket("www.google.com", 80);
            String loc = socket.getLocalAddress().toString();
            if (loc.startsWith("/")) {
                return loc.substring(1);
            }
            return loc;
        } catch (Exception e) {
            // Log.i("ERROR", e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static int rand(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static double rand(double min, double max) {
        Random r = new Random();
        double randomValue = min + (max - min) * r.nextDouble();
        return randomValue;
    }

    public static void setSwitch(Context context, boolean on) {
        // data
        ConnectivityManager dataManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Method dataMtd = null;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        dataMtd.setAccessible(true);
        try {
            dataMtd.invoke(dataManager, on);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static int clearCacheFolder(final File dir) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    // first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child);
                    }

                    if (child.delete()) {
                        deletedFiles++;
                    }
                }
            } catch (Exception e) {
                // Log.e(TAG, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }

        return deletedFiles;
    }

    public static int rand() {
        Random random = new Random();
        int pos = random.nextInt(1500);
        return pos + 100;
    }

    // 인터넷 연결 여부 확인
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static boolean isWiFi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isAvailable();
    }

    public static boolean is3G(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return mMobile.isAvailable();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static int getPixelSize(Context context, int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return Math.round(dp * metrics.density);
    }

    public static float getDpSize(Context context, long px) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return px * metrics.density;
    }

    public static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static float pxToDp(long px) {
        return px * Resources.getSystem().getDisplayMetrics().density;
    }

    public static float pxToDp(float px) {
        return px * Resources.getSystem().getDisplayMetrics().density;
    }

    public static String join(List<String> strs) {
        StringBuffer sb = new StringBuffer();

        int idx = 0;
        for (String str : strs) {
            sb.append(str);
            if(++idx < strs.size()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    public static void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static boolean availableLocationService(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;

            /*
            if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return Utils.isOnline(context);
            } else {
                return false;
            }
            */
        }

        return true;
    }

    public static String thousand(int number) {
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(number);
    }

    /**
     * String Null값 체크.
     *
     * @param str
     * @return Null이면 true
     */
    public static boolean isStrEmpty(String str) {
        if (null == str || str.isEmpty())
            return true;
        else
            return false;
    }

    public static String getDeviceToken(Context context){
        if (!isStrEmpty(getDeviceSerial())) {
            return getDeviceSerial();
        }

        if (!isStrEmpty(getDeviceIMEI(context))) {
            return getDeviceIMEI(context);
        }

        if (!isStrEmpty(getDeviceGoogleAccount(context))) {
            return getDeviceGoogleAccount(context);
        }

        return null;
    }

    private static String getDeviceSerial(){
        return Build.SERIAL;
    }

    private static String getDeviceIMEI(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    private static String getDeviceGoogleAccount(Context context){
        AccountManager ac = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        for (Account account : ac.getAccounts()) {
            if (account.type.equals("com.google"))
                return account.name;

        }
        return ac.getAccounts()[0].name;
    }

    /**
     * Enables/Disables all child views in a view group.
     *
     * @param viewGroup the view group
     * @param enabled <code>true</code> to enable, <code>false</code> to disable
     * the views.
     */
    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    public static int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    public static int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static DisplayImageOptions UILoptions = new DisplayImageOptions.Builder()
        // .displayer(new RoundedBitmapDisplayer(2))
            // .showImageOnLoading(R.mipmap.nopics2)
            // .showImageForEmptyUri(R.mipmap.nopics2)
            // .showImageOnFail(R.mipmap.nopics2)
            // .delayBeforeLoading(100)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            // .considerExifParams(true)
            // .imageScaleType(ImageScaleType.EXACTLY)
            // .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions UILoptionsProfile = new DisplayImageOptions.Builder()
            // .displayer(new RoundedBitmapDisplayer(2))
            // .showImageOnLoading(R.mipmap.profile)
            // .showImageForEmptyUri(R.mipmap.profile)
            // .showImageOnFail(R.mipmap.profile)
            // .delayBeforeLoading(100)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            // .considerExifParams(true)
            // .imageScaleType(ImageScaleType.EXACTLY)
            // .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static String fullDateTime(String created) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.KOREA);
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy년 MM월 dd일 EEE요일 a h시 mm분", java.util.Locale.KOREA);
        try {
            Date d = sdf1.parse(created);
            return sdf2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return created;
    }

    public static String todayStr() {

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA);

        Date d = new Date();
        return sdf1.format(d);

    }

    public static String timeStr() {

        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm", java.util.Locale.KOREA);

        Date d = new Date();
        return sdf1.format(d);

    }

    public static byte[] getByteArray(InputStream is) {
        try {
            int len;
            int size = 1024;
            byte[] buf;

            if (is instanceof ByteArrayInputStream) {
                size = is.available();
                buf = new byte[size];
                len = is.read(buf, 0, size);
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                buf = new byte[size];
                while ((len = is.read(buf, 0, size)) != -1) {
                    bos.write(buf, 0, len);
                }
                buf = bos.toByteArray();
            }
            return buf;
        } catch(IOException e) {

        }
        return null;
    }

    public static Bitmap resize(Bitmap bitmap, int reqWidth) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        int reqHeight = reqWidth * height / width;

        return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
    }

    public static byte[] getByteArray(Bitmap bitmap) {
        return getByteArray(bitmap, 100);
    }

    public static byte[] getByteArray(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        bitmap.compress(CompressFormat.JPEG, quality, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static int daysDiff(Date updatedDT, Date nowDT) {
        long day1 = updatedDT.getTime(); // in milliseconds.
        long day2 = nowDT.getTime(); // in milliseconds.
        long days = (day2 - day1) / 86400000;

        return (int) days;
    }

    public static boolean isNumeric(String s){
        String pattern= "^[0-9]*$";
        if(s.matches(pattern)){
            return true;
        }
        return false;
    }

    public static boolean isAlphaOrNumeric(String s){
        String pattern= "^[a-zA-Z0-9]*$";
        if(s.matches(pattern)){
            return true;
        }
        return false;
    }

    public static boolean isAlphaAndNumeric(String s){
        String numRegex   = ".*[0-9].*";
        String alphaRegex = ".*[A-Z].*";

        String pattern= "^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]+$";
        if(s.matches(pattern)){
            return true;
        }
        return false;
    }

    public static boolean containsAlpha(String s){
        String alphaRegex = ".*[a-zA-Z].*";

        if(s.matches(alphaRegex)){
            return true;
        }
        return false;
    }

    public static boolean containsNumber(String s){
        String numRegex   = ".*[0-9].*";

        if(s.matches(numRegex)){
            return true;
        }
        return false;
    }

    public static ArrayList<String> getTag(String content) {
        Pattern p = Pattern.compile("#\\{([\\w]+)\\}");
        Matcher m = p.matcher(content);

        ArrayList<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
    }

    public static Object[] convertTag(final Context context, String workingText, boolean clickable) {

        HashMap<String, Object> mSpans = new HashMap<String, Object>();
        Spannable wordtoSpan = new SpannableString(workingText);

        ArrayList<String> tags = Utils.getTag(workingText);
        if (tags.size() > 0) {
            Map<String, Integer> map = new HashMap<String, Integer>();

            for (String tag : tags) {
                String k = "#{" + tag + "}";
                int idx = workingText.indexOf(k);
                map.put(tag, idx);

                workingText = workingText.replace(k, tag);
            }

            wordtoSpan = new SpannableString(workingText);

            Iterator<String> keys = map.keySet().iterator();
            while (keys.hasNext()) {
                final String tag = keys.next();
                int idx = map.get(tag);

                Object span = new ForegroundColorSpan(Color.parseColor("#03a1fb"));
                mSpans.put(tag, span);

                wordtoSpan.setSpan(span, idx, idx + tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                if(!clickable) {
                    continue;
                }

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {

                        if("타임메신".equals(tag)) {
                            return;
                        }

                        //Intent intent = new Intent(context, TimelineActivity.class);
                        // Intent intent = new Intent(context, MainActivity.class);
                        // intent.putExtra("nickname", tag);
                        // context.startActivity(intent);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
                wordtoSpan.setSpan(clickableSpan, idx, idx + tag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


                // WordtoSpan.setSpan(new BackgroundColorSpan(Color.parseColor("#03a1fb")), idx, tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // WordtoSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), idx, tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        Object[] re = new Object[3];
        re[0] = workingText;
        re[1] = wordtoSpan;
        re[2] = mSpans;

        return re;
    }

    /**
     * 천단위로 콤마를 찍어서 리턴한다.
     * @param number
     * @return
     */
    public static String comma(String number){
        int len = number.length();
        int point = number.length() % 3;
        String str = number.substring(0, point);

        while (point < len){
            if(!"".equals(str))
                str += ",";

            str += number.substring(point, point+3);
            point += 3;
        }

        return str;
    }

    /**
     * 문자열 길이(바이트 단위)
     */
    public static int cstrlen(String s) {
        return cstrlen(s, "KSC5601");
    }

    public static int cstrlen(String s, String enc) {
        byte[] src = null;
        try {
            src = s.getBytes(enc);
            return src.length;
        } catch (java.io.UnsupportedEncodingException e) {
            return 0;
        }
    }

    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static View getViewByPosition(int pos, GridView gridView) {
        final int firstListItemPosition = gridView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + gridView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return gridView.getAdapter().getView(pos, null, gridView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return gridView.getChildAt(childIndex);
        }
    }

    // 키보드 보이기
    public static void showKeyboard(final Context context){
        InputMethodManager immhide = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
        immhide.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    // 키보드 숨기기
    public static void hideKeyboard(final Context context){
        try {
            InputMethodManager immhide = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
            immhide.hideSoftInputFromWindow(((Activity) context).getWindow().getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e){

        }

    }

    public static void clearApplicationCache(Context context, File dir){
        if(dir==null) {
            String dataDir = context.getFilesDir().getAbsolutePath();
            dir = new File(dataDir);
        }

        if(dir==null) {
            return;
        }

        java.io.File[] children = dir.listFiles();
        try{
            for(int i=0;i<children.length;i++)
                if(children[i].isDirectory())
                    clearApplicationCache(context, children[i]);
                else children[i].delete();
        }
        catch(Exception e){}
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // System.out.println("kjghf");
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        // System.out.println("totalHeight : " + totalHeight);

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static float getVisibleDistance(GoogleMap googleMap) {
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth
        );

        float[] distanceHeight = new float[2];
        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight
        );

        float distance;
        if (distanceWidth[0] < distanceHeight[0]){
            distance = distanceWidth[0];
        } else {
            distance = distanceHeight[0];
        }

        return distance;
    }

    public static int getInt(JSONObject item, String key) {
        if(item.has(key) && !item.isNull(key)) {
            try {
                return item.getInt(key);
            } catch (JSONException e) {
                return -1;
            }
        }
        return -1;
    }

    public static String getString(JSONObject item, String key) {
        if(item.has(key) && !item.isNull(key)) {
            try {
                return item.getString(key);
            } catch (JSONException e) {
                return "";
            }
        }
        return "";
    }

    public static boolean getBoolen(JSONObject json, String key) {
        if(json != null && json.has(key) && !json.isNull(key)) {
            try {
                return json.getBoolean(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getString(String str) {
        return getString(str, "");
    }

    public static String getString(EditText editText) {
        return editText.getText().toString().trim();
    }

    public static String getString(String str, String defValue) {
        return str == null ? defValue : str;
    }

    public static double getDouble(JSONObject item, String key) {
        if(item.has(key) && !item.isNull(key)) {
            try {
                return item.getDouble(key);
            } catch (JSONException e) {
                return 0.0;
            }
        }
        return 0.0d;
    }

    public static boolean isAppInstalled(final Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static String getParameter(String requestUrl, String parameter) {
        Uri uri = Uri.parse(requestUrl);
        String server = uri.getAuthority();
        String path = uri.getPath();
        String protocol = uri.getScheme();
        Set<String> args = uri.getQueryParameterNames();

        return uri.getQueryParameter(parameter);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void fixupLocale(Context ctx, Locale newLocale) {
        final Resources res = ctx.getResources();
        final Configuration config = res.getConfiguration();
        final Locale curLocale = getLocale(config);
        if (!curLocale.equals(newLocale)) {
            Locale.setDefault(newLocale);
            final Configuration conf = new Configuration(config);
            conf.setLocale(newLocale);
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }
    }

    private static Locale getLocale(Configuration config) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return config.getLocales().get(0);
        } else {
            //noinspection deprecation
            return config.locale;
        }
    }

    public static void showLoading(Activity activity) {
        Intent intent = new Intent(activity, LoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);

        // activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void hideLoading(Activity activity) {
        /*
        ProgressBar progressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleLarge);
        progressBar.setTag(943459874);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        viewGroup.addView(progressBar,params);
        progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
        progressBar.setVisibility(View.GONE);     // To Hide ProgressBar
        */

        ActivityManager am = (ActivityManager) activity.getSystemService(activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        // System.out.println("taskInfo : " + taskInfo);

        if(taskInfo != null && taskInfo.size() > 0) {
            String topClazzName = taskInfo.get(0).topActivity.getClassName();
            if("hntecology.ecology.base.LoadingActivity".equals(topClazzName)) {
                Intent intent = new Intent();
                intent.setAction("HIDE_LOADING");
                activity.sendBroadcast(intent);
            }
        }
        // Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        // ComponentName componentInfo = taskInfo.get(0).topActivity;
        // componentInfo.getPackageName();
    }

    public static Geometry polygonize(Geometry geometry) {
        List lines = LineStringExtracter.getLines(geometry);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();
        Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
        return geometry.getFactory().createGeometryCollection(polyArray);
    }

    public static Geometry splitPolygon(Geometry poly, Geometry line) {
        Geometry nodedLinework = poly.getBoundary().union(line);
        Geometry polys = polygonize(nodedLinework);

        // Only keep polygons which are inside the input
        List output = new ArrayList();
        for (int i = 0; i < polys.getNumGeometries(); i++) {
            Polygon candpoly = (Polygon) polys.getGeometryN(i);
            if (poly.contains(candpoly.getInteriorPoint())) {
                output.add(candpoly);
            }
        }
        return poly.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(output));
    }

    public static String getToday(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    public static Date getDate(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
        return new Date();
    }

    public static void processFile(Cipher ci, String inFile, String outFile)
            throws javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException,
            java.io.IOException {
        try (FileInputStream in = new FileInputStream(inFile);
             FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] ibuf = new byte[1024];
            int len;
            while ((len = in.read(ibuf)) != -1) {
                byte[] obuf = ci.update(ibuf, 0, len);
                if ( obuf != null ) out.write(obuf);
            }
            byte[] obuf = ci.doFinal();
            if ( obuf != null ) out.write(obuf);
        }
    }

    public static void encrypt(String inFile, String outFile) {
        try {
            byte[] iv = new byte[128/8];

            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecretKey skey = kgen.generateKey();




            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
            processFile(ci, inFile, outFile);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public static void decrypt(String inFile, String outFile) {
        try {
            byte[] iv = new byte[128/8];

            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecretKey skey = kgen.generateKey();

            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.DECRYPT_MODE, skey, ivspec);
            processFile(ci, inFile, outFile);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

}
