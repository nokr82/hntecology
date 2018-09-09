package hntecology.ecology.base;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Images;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dev1 on 2018-02-08.
 */

public class ImageUtils {
    private static Bitmap noImageBitmap = null;
    private static final int FILE_LIMIT_SIZE = 2048 * 1024;



    public static String since(String reg_dt) {
        if (reg_dt == null || reg_dt.trim().length() == 0) {
            return "";
        }

        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.KOREA);
        try {
            Date d1 = formatter.parse(reg_dt);
            Date d2 = new Date();

            long diff = d2.getTime() - d1.getTime();

            // // //System.out.println("d1.getTime() : " + d1.getTime() +
            // ", d2.getTime() : " + d2.getTime());

            long oneMin = 60 * 1000;
            long oneHour = 60 * oneMin;
            long oneDay = 24 * oneHour;
            long threeDays = 3 * oneDay;
            long oneYear = oneDay * 365;

            if (diff > oneYear) {
                formatter = new java.text.SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.KOREA);
                return formatter.format(d1);
            }

            Calendar cal1 = Calendar.getInstance(java.util.Locale.KOREA);
            cal1.setTime(d1);

            Calendar cal2 = Calendar.getInstance(java.util.Locale.KOREA);
            cal2.setTime(d2);
            cal2.add(Calendar.DAY_OF_MONTH, -1);

            if (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) || diff > oneDay) {
                formatter = new java.text.SimpleDateFormat("MM월 dd일", java.util.Locale.KOREA);
                return formatter.format(d1);
            }

            formatter = new java.text.SimpleDateFormat("HH시 mm분", java.util.Locale.KOREA);
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
     *            비트맵 이미지
     *            회전 각도
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

            // //System.out.println("options.inSampleSize : " + options.inSampleSize + ", orientation : " + orientation);

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
            int reqSize = 720;
            if (orientation == 90 || orientation == 270) {
                options.inSampleSize = Utils.calculateInSampleSizeByHeight(options, reqSize);
            } else if (orientation == 0 || orientation == 180) {
                options.inSampleSize = Utils.calculateInSampleSizeByWidth(options, reqSize);
            }

            // //System.out.println("options.inSampleSize : " + options.inSampleSize + ", orientation : " + orientation);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            Bitmap bm = null;
            try {
                bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
            } catch (OutOfMemoryError e) {
                try {
                    options.inSampleSize = options.inSampleSize * 2;
                    bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
                } catch (OutOfMemoryError e2) {
                    options.inSampleSize = options.inSampleSize * 2;
                    bm = BitmapFactory.decodeStream(new FileInputStream(photoPath), null, options);
                }

            }

            if (bm != null) {
                return Utils.rotate(bm, orientation);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

    public static void evalApp(final Context context) {
        if (!PrefUtils.getBooleanPreference(context, "isEvaluated")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("앱평가를 부탁드립니다.");
            builder.setPositiveButton("앱평가하기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PrefUtils.setPreference(context, "isEvaluated", Boolean.TRUE);

                    ApplicationInfo app = context.getApplicationInfo();
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse("market://details?id=" + app.packageName));
                    marketLaunch.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    context.startActivity(marketLaunch);
                }
            });

            builder.setNegativeButton("나중에하기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static String visitTypeStr(int visit_type) {
        if (visit_type == 0) {
            return "상품열람";
        }
        return "상품열람";
    }

    public static int calculateHeight(int width, int height, int w) {
        return w * height / width;
    }

    public static byte[] getByteArray(Bitmap bm) {

        // check file size
        double size = 0;
        if (Build.VERSION.SDK_INT >= 12) {
            size = bm.getByteCount() / 1024;
        } else {
            size = (bm.getRowBytes() * bm.getHeight()) / 1024;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (size > FILE_LIMIT_SIZE) {
            double quality = 0.0;
            quality = FILE_LIMIT_SIZE / size * 100.0;
            bm.compress(CompressFormat.PNG, (int) quality, stream);
        } else {
            bm.compress(CompressFormat.PNG, 100, stream);
        }

        return stream.toByteArray();
    }



    public static Bitmap resize(Bitmap bitmap, int reqWidth) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        int reqHeight = reqWidth * height / width;

        return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
    }
}
