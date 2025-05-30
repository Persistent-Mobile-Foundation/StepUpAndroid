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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLoginResponseListener;
import com.worklight.wlclient.api.challengehandler.SecurityCheckChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;


public class StepUpUserLoginChallengeHandler extends SecurityCheckChallengeHandler {
    private static String securityCheckName = "StepUpUserLogin";
    private String errorMsg = "";
    private Context context;
    private boolean isChallenged = false;
    private LocalBroadcastManager broadcastManager;

    //********************************
    // Constructor
    //********************************
    public StepUpUserLoginChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        //Reset the current user
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREFERENCES_KEY_USER);
        editor.apply();

        // Receive login requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JSONObject credentials = new JSONObject(intent.getStringExtra("credentials"));
                    login(credentials);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new IntentFilter(Constants.ACTION_LOGIN));
    }

    //********************************
    // createAndRegister
    //********************************
    public static StepUpUserLoginChallengeHandler createAndRegister(){
        StepUpUserLoginChallengeHandler challengeHandler = new StepUpUserLoginChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    //********************************
    // login
    //********************************
    public void login(JSONObject credentials){
        if(isChallenged){
            Log.d(securityCheckName, "login-submitChallengeAnswer");
            submitChallengeAnswer(credentials);
        }
        else{
            Log.d(securityCheckName, "login-login");
            WLAuthorizationManager.getInstance().login(securityCheckName, credentials, new WLLoginResponseListener() {
                @Override
                public void onSuccess() {
                    Log.d(securityCheckName, "Login Success");
                }

                @Override
                public void onFailure(WLFailResponse wlFailResponse) {
                    Log.d(securityCheckName, "Login Failure");
                }
            });
        }
    }

    //********************************
    // handleChallenge
    //********************************
    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d(securityCheckName, "Challenge Received:\n"+ jsonObject.toString());
        isChallenged = true;
        try {
            if(jsonObject.isNull("errorMsg")){
                errorMsg = "";
            }
            else{
                errorMsg = jsonObject.getString("errorMsg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_REQUIRED);
        intent.putExtra("errorMsg", errorMsg);
        broadcastManager.sendBroadcast(intent);
    }

    //********************************
    // handleSuccess
    //********************************
    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        Log.d(securityCheckName, "handleSuccess");
        isChallenged = false;
        try {
            //Save the current user
            SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREFERENCES_KEY_USER, identity.getJSONObject("user").toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_SUCCESS);
        broadcastManager.sendBroadcast(intent);
    }

    //********************************
    // handleFailure
    //********************************
    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        Log.d(securityCheckName, "handleFailure");
        isChallenged = false;
        if(error.isNull("failure")){
            errorMsg = "Failed to login. Please try again later.";
        }
        else {
            try {
                errorMsg = error.getString("failure");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_FAILURE);
        intent.putExtra("errorMsg",errorMsg);
        broadcastManager.sendBroadcast(intent);
        Log.d(securityCheckName, "handleFailure");
    }
}
