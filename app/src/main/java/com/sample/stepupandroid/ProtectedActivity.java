package com.sample.stepupandroid;
/**
 * Copyright 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.worklight.wlclient.api.WLAccessTokenListener;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;
import com.worklight.wlclient.auth.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class ProtectedActivity extends AppCompatActivity {
    private final String DEBUG_NAME = "ProtectedActivity";
    private ProtectedActivity _this;
    private TextView helloTextView, errorMsgTextView;
    private Button getBalanceButton, transferFundsButton, logoutButton;
    private URI adapterPath = null;
    private BroadcastReceiver pincodeRequiredReceiver, pincodeFailureReceiver, loginRequiredReceiver;
    private LocalBroadcastManager broadcastManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;
        Log.d(DEBUG_NAME, "onCreate");

        setContentView(R.layout.activity_protected);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_with_logout_button);
        //Initialize the UI elements
        helloTextView = (TextView) findViewById(R.id.helloTextView);
        getBalanceButton = (Button) findViewById(R.id.getBalance);
        transferFundsButton = (Button) findViewById(R.id.transferFunds);
        logoutButton = (Button) findViewById(R.id.logout);
        errorMsgTextView = (TextView) findViewById(R.id.errorMsg);

        //*****************************************
        // getBalanceButton - OnClickListener
        //*****************************************
        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "getBalanceButton clicked");
                try {
                    adapterPath = new URI("/adapters/ResourceAdapter/balance");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        updateTextView("Balance: " + wlResponse.getResponseText());
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        updateTextView("Failed to get balance: " + wlFailResponse.getErrorMsg());
                    }
                });
            }
        });

        //*****************************************
        // transferFundsButton - OnClickListener
        //*****************************************
        transferFundsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "transferFundsButton clicked");

                // obtainAccessToken to check if user is authenticated before transfering
                WLAuthorizationManager.getInstance().obtainAccessToken("StepUpUserLogin", new WLAccessTokenListener() {
                    @Override
                    public void onSuccess(AccessToken accessToken) {
                        _this.displayAmountDialog();
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d(DEBUG_NAME, "obtainAccessToken failure: " + wlFailResponse.getErrorMsg());
                    }
                });
            }
        });

        //*****************************************
        // pincodeRequired BroadcastReceiver
        //*****************************************
        pincodeRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "pincodeRequiredReceiver");
                _this.displayPinCodeDialog(intent.getStringExtra("errorMsg"));
            }
        };

        //*****************************************
        // pincodeFailure Receiver
        //*****************************************
        pincodeFailureReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "pincodeFailureReceiver");
                updateTextView("Transfer failure!");
            }
        };

        //*****************************************
        // loginRequired Receiver
        //*****************************************
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(DEBUG_NAME, "loginRequiredReceiver");
                //Open login screen
                Intent openLoginScreen = new Intent(_this, LoginActivity.class);
                _this.startActivity(openLoginScreen);
            }
        };

        //*****************************************
        // logoutButton - OnClickListener
        //*****************************************
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "logoutButton clicked");

                WLAuthorizationManager.getInstance().logout("StepUpUserLogin", new WLLogoutResponseListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(DEBUG_NAME, "StepUpUserLogin->Logout Success");
                        WLAuthorizationManager.getInstance().logout("StepUpPinCode", new WLLogoutResponseListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(DEBUG_NAME, "StepUpPinCode->Logout Success");
                                updateTextView("");
                                //Open login screen
                                Intent openLoginScreen = new Intent(_this, LoginActivity.class);
                                _this.startActivity(openLoginScreen);
                            }

                            @Override
                            public void onFailure(WLFailResponse wlFailResponse) {
                                Log.d(DEBUG_NAME, "StepUpPinCode->Logout Failure");
                            }
                        });
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d(DEBUG_NAME, "StepUpUserLogin->Logout Failure");
                    }
                });
            }
        });
    }

    //*****************************************
    // onStart
    //*****************************************
    @Override
    protected void onStart() {
        super.onStart();
        //Show the display name
        SharedPreferences preferences = _this.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        if(preferences.getString(Constants.PREFERENCES_KEY_USER,null) != null){
            try {
                JSONObject user = new JSONObject(preferences.getString(Constants.PREFERENCES_KEY_USER,null));
                helloTextView.setText(getString(R.string.hello_user, user.getString("displayName")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(DEBUG_NAME, "onStart");
        broadcastManager = LocalBroadcastManager.getInstance(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(pincodeRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_REQUIRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(pincodeFailureReceiver, new IntentFilter(Constants.ACTION_PINCODE_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
    }

    //*****************************************
    // onPause
    //*****************************************
    @Override
    protected void onPause() {
        Log.d(DEBUG_NAME, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pincodeRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pincodeFailureReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        super.onPause();
    }

    //*****************************************
    // displayAmountDialog
    //*****************************************
    public void displayAmountDialog() {
        Runnable run = new Runnable() {
            public void run() {
                Log.d(DEBUG_NAME, "displayAmountDialog");
                // Create an AlertDialog to enter transfer amount
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setTitle("Enter Amount:");
                // Add input text field to the AlertDialog
                final EditText input = new EditText(_this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                // Set up the buttons to the AlertDialog
                builder.setPositiveButton("Transfer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Make a WLResourceRequest to the adapter's transfer endpoint
                        try {
                            adapterPath = new URI("/adapters/ResourceAdapter/transfer");
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
                        // Add the amount as a formParam to the request
                        HashMap<String,String>  formParams = new HashMap<>();
                        formParams.put("amount", input.getText().toString());
                        request.send(formParams, new WLResponseListener() {
                            @Override
                            public void onSuccess(WLResponse wlResponse) {
                                Log.d(DEBUG_NAME, "Transfer Success!");
                                updateTextView("Transfer Success!");
                            }

                            @Override
                            public void onFailure(WLFailResponse wlFailResponse) {
                                Log.d(DEBUG_NAME, "Transfer Failure: " + wlFailResponse.getErrorMsg());
                                updateTextView("Transfer Failure: " + wlFailResponse.getErrorMsg());
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        updateTextView("");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };
        this.runOnUiThread(run);
    }

    //*****************************************
    // displayPinCodeDialog
    //*****************************************
    public void displayPinCodeDialog(final String errorMsg) {
        Runnable run = new Runnable() {
            public void run() {
                Log.d(DEBUG_NAME, "displayPinCodeDialog");
                // Create an AlertDialog to enter PinCode
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setTitle("Enter pincode:");
                builder.setMessage(errorMsg);
                // Add input text field to the AlertDialog
                final EditText input = new EditText(_this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                // Set up the buttons to the AlertDialog
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send broadcast to PinCode-challenge-handler with entered pincode
                        Intent intent = new Intent();
                        intent.setAction(Constants.ACTION_PINCODE_SUBMIT_ANSWER);
                        intent.putExtra("credentials", input.getText().toString());
                        broadcastManager.sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Constants.ACTION_PINCODE_CANCEL);
                        broadcastManager.sendBroadcast(intent);
                        dialog.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };
        this.runOnUiThread(run);
    }

    //*****************************************
    // updateTextView
    //*****************************************
    public void updateTextView(final String str) {
        Log.d(DEBUG_NAME, "updateTextView");

        Runnable run = new Runnable() {
            public void run() {
                errorMsgTextView.setText(str);
            }
        };
        this.runOnUiThread(run);
    }

    @Override
    public void onBackPressed() {
        Log.d(DEBUG_NAME, "onBackPressed");
    }
}
