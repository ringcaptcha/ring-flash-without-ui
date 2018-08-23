package com.example.yuta.ringflashwithoutuisample;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.thrivecom.ringcaptcha.ringflashsdk.RingFlashSDK;
import com.thrivecom.ringcaptcha.ringflashsdk.handler.RingFlashVerificationHandler;
import com.thrivecom.ringcaptcha.ringflashsdk.http.RingFlashAPIController;
import com.thrivecom.ringcaptcha.ringflashsdk.http.RingFlashAPIHandler;
import com.thrivecom.ringcaptcha.ringflashsdk.interceptor.RingFlashInterceptionHandler;
import com.thrivecom.ringcaptcha.ringflashsdk.model.RingFlashCredentials;
import com.thrivecom.ringcaptcha.ringflashsdk.model.RingFlashResponse;

public class WaitActivity extends AppCompatActivity {
    private RingFlashSDK _flashCallSDK = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String number_text = intent.getStringExtra(MainActivity.RINGCAPTCHA_PHONE_NUMBER);

        // Verify
        verifyWithoutUi(MainActivity.RINGCAPTCHA_APP_KEY, MainActivity.RINGCAPTCHA_SECRET_KEY, number_text);
    }

    @Override
    public void onBackPressed() {
        // Clear intercepting
        if (_flashCallSDK != null) {
            _flashCallSDK.stopCellularBroadcastIntercepting();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // Clear intercepting
                if (_flashCallSDK != null) {
                    _flashCallSDK.stopCellularBroadcastIntercepting();
                }
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verifyWithoutUi(final String app_key, final String secret_key, final String number) {
        final RingFlashCredentials credentials = new RingFlashCredentials();
        credentials.setSecretKey(secret_key);
        credentials.setAppKey(app_key);
        credentials.setPhoneNumber(number);

        RingFlashAPIHandler ringFlashAPIHandler = new RingFlashAPIHandler() {
            @Override
            public void onSuccess(RingFlashResponse response) {
                Log.i(MainActivity.TAG, response.toString());
                boolean callRequested = response.checkStatus();
                if (callRequested) {
                    RingFlashInterceptionHandler interceptionHandler = new RingFlashInterceptionHandler() {
                        @Override
                        public void onCallEnded(String callerNumber) {
                            RingFlashAPIHandler ringFlashAPIHandler1 = new RingFlashAPIHandler() {
                                @Override
                                public void onSuccess(RingFlashResponse response) {
                                    Log.i(MainActivity.TAG, response.toString());
                                    boolean is_verified = response.checkStatus();
                                    if (is_verified) {
                                        Toast toast = Toast.makeText(getApplicationContext(), "VERIFIED!", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                        toast.show();
                                        _flashCallSDK.stopCellularBroadcastIntercepting();
                                        finish();
                                    } else {
                                        String error = response.getMessage() == null ? "ERROR_DEFAULT_MESSAGE" : response.getMessage();
                                        Log.i(MainActivity.TAG, error);
                                        Toast toast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                        toast.show();
                                        _flashCallSDK.stopCellularBroadcastIntercepting();
                                        finish();
                                    }
                                }

                                @Override
                                public void onError(Exception exception) {
                                    _flashCallSDK.stopCellularBroadcastIntercepting();
                                    //TODO
                                }
                            };

                            RingFlashAPIController ringFlashAPIController = new RingFlashAPIController(getApplicationContext());
                            ringFlashAPIController.requestVerification(credentials, callerNumber, ringFlashAPIHandler1);
                        }
                    };
                    _flashCallSDK = RingFlashSDK.builder()
                            .setContext(getApplicationContext())
                            .setSecretKey(secret_key)
                            .setAppKey(app_key)
                            .setHandler(getEmptyHandler())
                            .setPhoneNumber(number)
                            .build();
                    _flashCallSDK.startCellularBroadcastIntercepting(interceptionHandler);
                } else {
                    String error = response.getMessage() == null ? "ERROR_DEFAULT_MESSAGE" : response.getMessage();
                    Log.i(MainActivity.TAG, error);
                    Toast toast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    finish();
                }
            }

            @Override
            public void onError(Exception exception) {
                //TODO
            }
        };

        RingFlashAPIController ringFlashAPIController = new RingFlashAPIController(getApplicationContext());
        ringFlashAPIController.requestVoiceCall(credentials, ringFlashAPIHandler);
    }

    private static RingFlashVerificationHandler getEmptyHandler() {

        return new RingFlashVerificationHandler() {
            @Override
            public void onVerified() {
            }

            @Override
            public void onVerificationFailed(RingFlashResponse response) {
            }

            @Override
            public void onError(Exception e) {
            }

            @Override
            public void onFlashCallRequested(RingFlashResponse response) {

            }

            @Override
            public void onFlashCallRequestedFailed(RingFlashResponse response) {

            }

            @Override
            public void onVerificationInitiated() {
            }

        };
    }
}
