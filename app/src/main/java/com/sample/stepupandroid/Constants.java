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
package com.sample.stepupandroid;

public class Constants {
    static final String ACTION_LOGIN = "com.sample.stepupandroid.broadcast.login";
    static final String ACTION_LOGIN_SUCCESS = "com.sample.stepupandroid.broadcast.login.success";
    static final String ACTION_LOGIN_FAILURE = "com.sample.stepupandroid.broadcast.login.failure";
    static final String ACTION_LOGIN_REQUIRED = "com.sample.stepupandroid.broadcast.login.required";

    static final String ACTION_PINCODE_REQUIRED = "com.sample.stepupandroid.broadcast.pincode.required";
    static final String ACTION_PINCODE_SUCCESS = "com.sample.stepupandroid.broadcast.pincode.success";
    static final String ACTION_PINCODE_FAILURE = "com.sample.stepupandroid.broadcast.pincode.failure";
    static final String ACTION_PINCODE_SUBMIT_ANSWER = "com.sample.stepupandroid.broadcast.pincode.submit.answer";
    static final String ACTION_PINCODE_CANCEL = "com.sample.stepupandroid.broadcast.pincode.cancel";

    static final String PREFERENCES_FILE = "com.sample.stepupandroid.preferences";
    static final String PREFERENCES_KEY_USER = "com.sample.stepupandroid.preferences.user";
}
