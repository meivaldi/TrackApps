package com.meivaldi.trackerapps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    }
                }, 3000
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}