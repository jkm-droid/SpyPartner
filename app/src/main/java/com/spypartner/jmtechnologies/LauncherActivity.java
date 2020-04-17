package com.spypartner.jmtechnologies;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LauncherActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //moving to another activity
                Intent registerActivity = new Intent(LauncherActivity.this, LoginUser.class);
                startActivity(registerActivity);
                finish();
            }
        },SPLASH_TIME_OUT);

    }
}
