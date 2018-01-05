package com.endlessdream.edcroputil;

import android.app.Activity;
import android.os.Bundle;

public class ViewImageCrop extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main_image_crop);

        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction().add(R.id.container, MainFragment.getInstance(), null).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
