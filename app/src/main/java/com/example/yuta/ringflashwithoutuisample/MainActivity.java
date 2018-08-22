package com.example.yuta.ringflashwithoutuisample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.thrivecom.ringcaptcha.RingcaptchaApplication;
import com.thrivecom.ringcaptcha.RingcaptchaApplicationHandler;
import com.thrivecom.ringcaptcha.RingcaptchaVerification;
import com.thrivecom.ringcaptcha.ringflashsdk.RingFlashSDK;
import com.thrivecom.ringcaptcha.ringflashsdk.handler.RingFlashVerificationHandler;
import com.thrivecom.ringcaptcha.ringflashsdk.http.RingFlashAPIController;
import com.thrivecom.ringcaptcha.ringflashsdk.http.RingFlashAPIHandler;
import com.thrivecom.ringcaptcha.ringflashsdk.interceptor.RingFlashInterceptionHandler;
import com.thrivecom.ringcaptcha.ringflashsdk.model.RingFlashCredentials;
import com.thrivecom.ringcaptcha.ringflashsdk.model.RingFlashResponse;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSIONS = 1;

    public static final String RINGCAPTCHA_APP_KEY = "YOUR_APP_KEY";
    public static final String RINGCAPTCHA_SECRET_KEY = "YOUR_SECRET_KEY";
    public static final String RINGCAPTCHA_PHONE_NUMBER = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int PERMISSION_ALL = 1;

            String[] PERMISSIONS;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS};
            } else {
                PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};
            }
            if(hasPermissions(this, PERMISSIONS)){
                // do nothing
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        } else {
            // do nothing
        }
    }

    public void verifyByBlink(View view) {
        TextView tvn = (TextView)findViewById(R.id.numberEditText);
        String number_text = tvn.getText().toString();
        //TODO Log Error when APIs return an error.
        Intent intent = new Intent(this, WaitActivity.class);
        intent.putExtra(RINGCAPTCHA_PHONE_NUMBER, number_text);

        startActivity(intent);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String   permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS:
                if (grantResults.length >= 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do nothing
                } else {
                    Toast.makeText(this, "Rejected permissions", Toast.LENGTH_LONG).show();
                }
        }
    }
}
