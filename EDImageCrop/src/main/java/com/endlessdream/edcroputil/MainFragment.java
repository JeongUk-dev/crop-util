package com.endlessdream.edcroputil;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

public class MainFragment extends Fragment {

    private static final String PROGRESS_DIALOG = "ProgressDialog";

    // Views ///////////////////////////////////////////////////////////////////////////////////////
    private CropImageView mCropView;
    private LinearLayout mRootLayout;


    private Uri tartgetUri = null;
    private Uri outputUri = null;

    // Note: only the system can call this constructor by reflection.
    public MainFragment() {
    }

    public static MainFragment getInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_fragment_image_crop, null, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // bind Views
        bindViews(view);
        setCropOptions();

        // set bitmap to CropImageView
        showProgress();
        mCropView.startLoad(tartgetUri, mLoadCallback);
    }

    // Bind views //////////////////////////////////////////////////////////////////////////////////

    private void bindViews(View view) {
        mCropView = (CropImageView) view.findViewById(R.id.cropImageView);
        view.findViewById(R.id.buttonDone).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonRotateLeft).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonRotateRight).setOnClickListener(btnListener);
        view.findViewById(R.id.buttonCancel).setOnClickListener(btnListener);
        mRootLayout = (LinearLayout) view.findViewById(R.id.layout_root);
    }

    private void setCropOptions() {
        Intent intent = getActivity().getIntent();

        boolean logging = intent.getBooleanExtra(EDImageCrop.Extra.LOGGING_ENABLE, false);
        mCropView.setLoggingEnabled(logging);

        outputUri = intent.getParcelableExtra(EDImageCrop.Extra.OUT_PUT_URI);

        tartgetUri = intent.getParcelableExtra(EDImageCrop.Extra.CROP_TARGET_URI);

        int aspectX = intent.getIntExtra(EDImageCrop.Extra.ASPECT_X, 1);
        int aspectY = intent.getIntExtra(EDImageCrop.Extra.ASPECT_Y, 1);
        mCropView.setCustomRatio(aspectX, aspectY);

        int maxX = intent.getIntExtra(EDImageCrop.Extra.MAX_X, 0);
        int maxY = intent.getIntExtra(EDImageCrop.Extra.MAX_Y, 0);
        mCropView.setOutputMaxSize(maxX, maxY);

        float cropFrameScale = intent.getFloatExtra(EDImageCrop.Extra.CROP_FRAME_SCALE, 1.0f);
        mCropView.setInitialFrameScale(cropFrameScale);

        int minSize = intent.getIntExtra(EDImageCrop.Extra.MIN_CROP_SIZE, 50);
        mCropView.setMinFrameSizeInDp(minSize);

        int quality = intent.getIntExtra(EDImageCrop.Extra.COMPRESS_QUALITY, 100);
        mCropView.setCompressQuality(quality);

        Bitmap.CompressFormat format = (Bitmap.CompressFormat) intent.getSerializableExtra(EDImageCrop.Extra.FORMAT);
        if (format == null) {
            mCropView.setCompressFormat(Bitmap.CompressFormat.JPEG);
        } else {
            mCropView.setCompressFormat(format);
        }

        int color = intent.getIntExtra(EDImageCrop.Extra.COLOR, Color.WHITE);
        mCropView.setFrameColor(color);
        mCropView.setHandleColor(color);
        mCropView.setGuideColor(color);

        EDImageCrop.CropMode mode = (EDImageCrop.CropMode) intent.getSerializableExtra(EDImageCrop.Extra.CROP_MODE);
        if (mode == null) {
            mCropView.setCropMode(CropImageView.CropMode.CUSTOM);
        } else {
            mCropView.setCropMode(mode.getMode());
        }
    }

    public void cropImage() {
        showProgress();

        try {
            mCropView.startCrop(outputUri, mCropCallback, mSaveCallback);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void showProgress() {
        ProgressDialogFragment f = ProgressDialogFragment.getInstance();
        getFragmentManager()
                .beginTransaction()
                .add(f, PROGRESS_DIALOG)
                .commitAllowingStateLoss();
    }

    public void dismissProgress() {
        if (!isAdded()) return;
        FragmentManager manager = getFragmentManager();
        if (manager == null) return;
        ProgressDialogFragment f = (ProgressDialogFragment) manager.findFragmentByTag(PROGRESS_DIALOG);
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
        }
    }

    // Handle button event /////////////////////////////////////////////////////////////////////////

    private final View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.buttonDone) {
                cropImage();

            } else if (i == R.id.buttonRotateLeft) {
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);

            } else if (i == R.id.buttonRotateRight) {
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);

            } else if (i == R.id.buttonCancel) {
                getActivity().finish();
            }
        }
    };


    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override
        public void onSuccess() {
            dismissProgress();
        }

        @Override
        public void onError() {
            dismissProgress();
        }
    };

    private final CropCallback mCropCallback = new CropCallback() {
        @Override
        public void onSuccess(Bitmap cropped) {
            Log.v("CropCallback", "mCropCallback");
        }

        @Override
        public void onError() {
        }
    };

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override
        public void onSuccess(Uri outputUri) {
            dismissProgress();
            Intent intent = new Intent();
            intent.setData(outputUri);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }

        @Override
        public void onError() {
            dismissProgress();
        }
    };
}