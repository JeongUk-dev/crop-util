package com.endlessdream.testcrop;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.endlessdream.edcroputil.EDImageCrop;

import java.io.File;
import java.security.Permissions;

public class MainActivity extends AppCompatActivity {

    private EDImageCrop mImageCrop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageCrop = new EDImageCrop(this);

        configImageCrop();

        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] items = {"갤러리에서 선택", "사진 촬영"};
                AlertDialog builder = new AlertDialog.Builder(MainActivity.this).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mImageCrop.pickImage();
//                                requestWriteExternalStoragePermission();
                                break;
                            case 1:
//                                requestUseCameraPermission();
                                mImageCrop.pickCamera();
                                break;
                        }
                    }
                }).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageCrop.removeAllTempFile();
    }

    void configImageCrop() {
//        Uri outputUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tempImage.jpg"));
        Uri outputUri = Uri.fromFile(new File(getFilesDir(), "tempImage.jpg"));
        mImageCrop.setLoggingEnabled(true).setOutputMaxSize(1024, 1024).setCropRatio(1, 1).setOutputUri(outputUri).setUsingInternalStorage(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        mImageCrop.onActivityResult(requestCode, resultCode, data, new EDImageCrop.OnCropListener() {
            @Override
            public void onCropped(Uri output) {
                ((ImageView) findViewById(R.id.image)).setImageURI(null);
                ((ImageView) findViewById(R.id.image)).setImageURI(output);
            }
        });

    }

    public static final int PERMISSIONS_REQUEST_READ_EXT_STORAGE = 101;
    private void requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRationaleDialog("거부하면 크롭한 이미지 쓸 쑤 없다!", PERMISSIONS, PERMISSIONS_REQUEST_READ_EXT_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_READ_EXT_STORAGE);
            }
        } else {
            mImageCrop.pickImage();
        }
    }

    public static final int PERMISSIONS_REQUEST_CAMERA = 102;
    private void requestUseCameraPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRationaleDialog("거부하면 카메라랑 저장소 쓸 쑤 없다!", PERMISSIONS, PERMISSIONS_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            mImageCrop.pickCamera();
        }
    }

    private String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private void showRationaleDialog(String message, final String[] permission, final int REQ_CODE) {
        new AlertDialog.Builder(this)
                .setPositiveButton("허용", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        requestPermissions(permission, REQ_CODE);
                    }
                })
                .setNegativeButton("거부", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .setMessage(message)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXT_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mImageCrop.pickImage();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mImageCrop.pickCamera();
            }
        }
    }
}
