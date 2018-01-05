package com.endlessdream.edcroputil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;

/**
 * 참조 : <a href="https://github.com/IsseiAoki/SimpleCropView">https://github.com/IsseiAoki/SimpleCropView</a>
 */

public class EDImageCrop {
    private static final String TAG = "EDImageCrop";

    public static final int REQUEST_PICK_IMAGE = 10011;
    public static final int REQUEST_CROP_IMAGE = 10012;
    public static final int REQUEST_PICK_CAMERA = 10013;

    private String tempFileNameHeader = "tmp_ed_";

    interface Extra {
        String CROP_MODE = "CROP_MODE";
        String ASPECT_X = "aspect_x";
        String ASPECT_Y = "aspect_y";
        String MAX_X = "max_x";
        String MAX_Y = "max_y";
        String CROP_FRAME_SCALE = "crop_frame_scale";
        String MIN_CROP_SIZE = "min_crop_size";
        String COLOR = "color";
        String FORMAT = "format";
        String COMPRESS_QUALITY = "compress_quality";
        String CROP_TARGET_URI = "crop_target_uri";
        String OUT_PUT_URI = "out_put_uri";
        String LOGGING_ENABLE = "logging_enable";
    }

    /**
     * 크롭된 이미지의 Uri를 반환 한다.
     * onActivityResult에서 호출하여 사용.
     */
    public interface OnCropListener {
        void onCropped(Uri output);
    }

    /**
     * @see com.isseiaoki.simplecropview.CropImageView.CropMode
     */
    public enum CropMode {
        FIT_IMAGE(CropImageView.CropMode.FIT_IMAGE),
        RATIO_4_3(CropImageView.CropMode.RATIO_4_3),
        RATIO_3_4(CropImageView.CropMode.RATIO_3_4),
        SQUARE(CropImageView.CropMode.SQUARE),
        RATIO_16_9(CropImageView.CropMode.RATIO_16_9),
        RATIO_9_16(CropImageView.CropMode.RATIO_9_16),
        FREE(CropImageView.CropMode.FREE),
        CUSTOM(CropImageView.CropMode.CUSTOM),
        CIRCLE(CropImageView.CropMode.CIRCLE),
        CIRCLE_SQUARE(CropImageView.CropMode.CIRCLE_SQUARE);

        private CropImageView.CropMode mode;
        CropMode(CropImageView.CropMode mode) {
            this.mode = mode;
        }
        public CropImageView.CropMode getMode() {
            return mode;
        }
    }

    private Context mContext = null;
    private Intent createIntent = null;
    private Uri outputUri = null;

    public EDImageCrop(Context context) {
        this.mContext = context;
        this.createIntent = new Intent(mContext, ViewImageCrop.class);
    }

    /**
     * simplecropview 라이브러리의 로그 활성화 여부를 설정한다.
     * @param enabled
     * @return
     */
    public EDImageCrop setLoggingEnabled(boolean enabled) {
        createIntent.putExtra(Extra.LOGGING_ENABLE, enabled);
        return this;
    }

    /**
     * 커스텀 크롭 비율을 설정한다.
     * @param aspectX 가로 비율
     * @param aspectY 세로 비율
     * @return
     */
    public EDImageCrop setCropRatio(int aspectX, int aspectY) {
        createIntent.putExtra(Extra.ASPECT_X, aspectX);
        createIntent.putExtra(Extra.ASPECT_Y, aspectY);
        return this;
    }

    /**
     * 크롭 모드를 설정한다.
     * @param mode FIT_IMAGE, RATIO_4_3, RATIO_3_4, SQUARE, RATIO_16_9, RATIO_9_16, CUSTOM, FREE, CIRCLE, CIRCLE_SQUARE
     * @return
     * @see CropMode
     */
    public EDImageCrop setCropMode(CropMode mode) {
        createIntent.putExtra(Extra.CROP_MODE, mode);
        return this;
    }

    /**
     * 크롭할 이미지 경로를 설정한다.
     * pickImage() 메소드로 이미지를 선택하고 onActivityResult() 메소드를 통해 자동으로 값이 설정 된다.
     * @param cropTargetUri
     * @return
     */
    public EDImageCrop setCropTargetUri(Uri cropTargetUri) {
        createIntent.putExtra(Extra.CROP_TARGET_URI, cropTargetUri);
        return this;
    }

    /**
     * 임시로 저장되는 이미지 파일의 접두어를 설정한다.
     * @param domain ex) domain = "tmp_ed"
     * @return
     */
    public EDImageCrop setTempFileDomain(String domain) {
        tempFileNameHeader = domain;
        return this;
    }

    /**
     * 크롭한 이미지가 저장될 경로를 설정한다.
     * @param outputUri
     * @return
     */
    public EDImageCrop setOutputUri(Uri outputUri) {
        this.outputUri = outputUri;
        createIntent.putExtra(Extra.OUT_PUT_URI, outputUri);
        return this;
    }

    /**
     * 크롭한 이미지를 저장할 경로를 반환한다.
     * @return
     */
    public Uri getOutputUri() {
        return outputUri;
    }

    /**
     * 이미지 압축 퀄리티를 설정한다.
     * @param quality 0 ~ 100 (default 100)
     * @return
     */
    public EDImageCrop setCompressQuality(int quality) {
        createIntent.putExtra(Extra.COMPRESS_QUALITY, quality);
        return this;
    }

    /**
     * 압축 형식을 설정한다. (default JPEG)
     * @param format JPEG, PNG, WEBP
     * @return
     * @see android.graphics.Bitmap.CompressFormat
     */
    public EDImageCrop setCompressFormat(Bitmap.CompressFormat format) {
        createIntent.putExtra(Extra.FORMAT, format);
        return this;
    }

    /**
     * 크롭한 이미지의 최대 사이즈를 각각 설정한다.
     * @param maxX 가로 최대 사이즈.
     * @param maxY 세로 최대 사이즈.
     * @return
     */
    public EDImageCrop setOutputMaxSize(int maxX, int maxY) {
        createIntent.putExtra(Extra.MAX_X, maxX);
        createIntent.putExtra(Extra.MAX_Y, maxY);
        return this;
    }

    /**
     * 크롭할 영역 최소 사이즈를 설정한다. (단위 dp)
     * @param minSizeDp default 50
     * @return
     */
    public EDImageCrop setMinCropFrameSize(int minSizeDp) {
        createIntent.putExtra(Extra.MIN_CROP_SIZE, minSizeDp);
        return this;
    }

    /**
     * 초기 크롭 영역 사이즈를 설정한다.
     * @param scale 0.01 ~ 1.0 (default 1.0)
     * @return
     */
    public EDImageCrop setInitCropFrameScale(float scale) {
        createIntent.putExtra(Extra.CROP_FRAME_SCALE, scale);
        return this;
    }

    /**
     * 크롭뷰의 색상을 설정한다.
     * @param color (default {@link android.graphics.Color.WHITE})
     * @return
     */
    public EDImageCrop setColor(int color) {
        createIntent.putExtra(Extra.COLOR, color);
        return this;
    }

    /**
     * 크롭하기!
     */
    public void start() {
        if (createIntent.hasExtra(Extra.OUT_PUT_URI)) {
            ((Activity) mContext).startActivityForResult(createIntent, REQUEST_CROP_IMAGE);
        } else {
            Log.e(TAG, "output Uri is Null !");
        }
    }


    private boolean isUsingInternalStorage = false;

    /**
     * 카메라 촬영시 생성되는 임시 파일을 내부 저장소에 쓸지, 외부 저장소에 쓸지 설정한다. 외부 저장소를 사용할 경우 권한이 필요하다.
     *
     * @param enable true: 내부 저장소 사용.
     *               false: 외부 저장소 사용.
     *               (default false)
     */
    public EDImageCrop setUsingInternalStorage(boolean enable) {
        this.isUsingInternalStorage = enable;
        return this;
    }

    /**
     * 이미지 선택.
     */
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        ((Activity) mContext).startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private Uri cameraCaptureUri;

    /**
     *  카메라로 촬영하여 선택.
     */
    public void pickCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String tempFileName = tempFileNameHeader + String.valueOf(System.currentTimeMillis()) + ".jpg";

        File dir = isUsingInternalStorage ? mContext.getFilesDir() : Environment.getExternalStorageDirectory();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            cameraCaptureUri = Uri.fromFile(new File(dir, tempFileName));
        } else {
            cameraCaptureUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(dir, tempFileName));
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraCaptureUri);
        ((Activity) mContext).startActivityForResult(intent, REQUEST_PICK_CAMERA);
    }

    /**
     * 액티비티의 onActivityResult에서 인자값을 전달받아 사용.
     * @param requestCode
     * @param resultCode
     * @param data
     * @param listener
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data, OnCropListener listener) {
        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Activity Result Code is not OK. {requestCode:" + requestCode + ", resultCode:" + resultCode + "}");
            return ;
        }
        if (requestCode == REQUEST_PICK_IMAGE) {
            Uri targetUri = data.getData();
            setCropTargetUri(targetUri).start();
        } else if (requestCode == REQUEST_PICK_CAMERA) {
            setCropTargetUri(cameraCaptureUri).start();
        } else if (requestCode == REQUEST_CROP_IMAGE) {
            Uri output = data.getData();
            if (output == null) {
                new NullPointerException("Output Data is Null..").printStackTrace();
                return;
            }

            listener.onCropped(output);
        }

    }

    /**
     * 이미지 파일을 지운다.
     * @param targetUri
     * @param fileArray
     */
    public void removeImageFile(Uri targetUri, File... fileArray) {
        for (int i = 0; i < fileArray.length; i++) {
            if (fileArray[i].exists()) {
                fileArray[i].delete();
            }
        }
        if (targetUri != null) {
            File f = new File(targetUri.getPath());
            if (f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * 크롭에 사용했던 모든 임시 파일을 제거 한다.
     */
    public void removeAllTempFile() {
        File[] listFiles = (Environment.getExternalStorageDirectory().listFiles());
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile() && file.getName().toString().contains(tempFileNameHeader)) {
                    boolean isDeleted = file.delete();
                    Log.i(TAG, "Delete temp file name : [" + file + "], :: is deleted = " + isDeleted);
                }
            }
        }
        if (outputUri != null) {
            File f = new File(outputUri.getPath());
            if (f.exists()) {
                f.delete();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String realPath;
        // SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            realPath = getRealPathFromURI_BelowAPI11(mContext, contentUri);
        }

        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19) {
            realPath = getRealPathFromURI_API11to18(mContext, contentUri);
        }

        // SDK > 19 (Android 4.4)
        else {
            realPath = getRealPathFromURI_API19(mContext, contentUri);
        }

        return realPath;
    }

    @SuppressLint("NewApi")
    private String getRealPathFromURI_API19(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private String getDataColumn(Context context, Uri uri,
                                String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri
                .getAuthority());
    }

    @SuppressLint("NewApi")
    private String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }

        if(result == null){
            return contentUri.toString();
        }else{
            return result;
        }
    }

    private String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


}
