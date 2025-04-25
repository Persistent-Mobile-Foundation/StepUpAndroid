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
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.challengehandler.SecurityCheckChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class StepUpPinCodeChallengeHandler extends SecurityCheckChallengeHandler {
    private static String securityCheckName = "StepUpPinCode";
    private Context context;
    private LocalBroadcastManager broadcastManager;

    //********************************
    // Constructor
    //********************************
    public StepUpPinCodeChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        // Receive login requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String credentials = intent.getStringExtra("credentials");
                submitAnswer(credentials);
            }
        },new IntentFilter(Constants.ACTION_PINCODE_SUBMIT_ANSWER));

        // Cancel
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Cancel();
            }
        },new IntentFilter(Constants.ACTION_PINCODE_CANCEL));
    }

    //********************************
    // createAndRegister
    //********************************
    public static StepUpPinCodeChallengeHandler createAndRegister(){
        StepUpPinCodeChallengeHandler challengeHandler = new StepUpPinCodeChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    //********************************
    // submitAnswer
    //********************************
    public void submitAnswer(String credentials) {
        Log.d(securityCheckName, "submitAnswer");
        try {
            submitChallengeAnswer(new JSONObject().put("pin", credentials));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //********************************
    // Cancel
    //********************************
    public void Cancel() {
        Log.d(securityCheckName, "Cancel");
        cancel();
    }

    //********************************
    // handleChallenge
    //********************************
    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d(securityCheckName, "handleChallenge");
        String errorMsg = null;

        try{
            if (!jsonObject.isNull("errorMsg")){
                errorMsg = jsonObject.getString("errorMsg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_REQUIRED);
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
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_SUCCESS);
        broadcastManager.sendBroadcast(intent);
    }

    //********************************
    // handleFailure
    //********************************
    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        Log.d(securityCheckName, "handleFailure");
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_FAILURE);
        intent.putExtra("errorMsg",error.toString());
        broadcastManager.sendBroadcast(intent);
    }
}
