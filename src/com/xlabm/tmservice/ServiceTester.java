package com.xlabm.tmservice;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

/**
 * Controller to start and stop a service. The serivce will update a status bar
 * notification every 5 seconds for a minute.
 */
public class ServiceTester extends Activity {
    private static final String TAG = "TriggerManagerActivity";
    private static final int REPORT_MSG = 1;
    ITMService service;
    TriggerManagerServiceConnection connection;
    String mAnswer;
    String qid;
    //This is an example of Callback from Android Documentation
    //The external service will broadcast updateAnswer(value) to all the binded activities
    private ITMServiceCallback mCallback = new ITMServiceCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void updateAnswer(String value) {
            mHandler.sendMessage(mHandler.obtainMessage(REPORT_MSG, value));
        }
    };
    //This is an example of Callback from Android Documentation
    //Handle the message whose request code is REPORT_MSG
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPORT_MSG:
                    mAnswer = "Received from service: " + msg.obj;
                    returnClearance();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            startService(new Intent(ServiceTester.this,
                    TriggerManagerService.class));
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            stopService(new Intent(ServiceTester.this,
                    TriggerManagerService.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Get the question ID that launches this activity
        Intent intent = getIntent();
        qid = intent.getStringExtra("qid");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        TextView mainMenuMessageLabel = (TextView) findViewById(R.id.header_text_view);
        mainMenuMessageLabel.setText("Please wait till there's a notification.");
        Button button = (Button) findViewById(R.id.notifyStart);
        button.setOnClickListener(mStartListener);
        button = (Button) findViewById(R.id.notifyStop);
        button.setOnClickListener(mStopListener);
        button = (Button) findViewById(R.id.bindService);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bindTMService(getApplicationContext());

            }
        });
        button = (Button) findViewById(R.id.unBindService);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseService();
            }
        });

        //Check the trigger status associating with the current question
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (mSharedPreferences.getInt(qid, 0)) {
            //Trigger has not been set
            case 0:
                Log.d(TAG, "case:0 qid:" + qid);
                //Bind to the external TMService and set the trigger
                bindTMService(this);
                break;

            //Trigger has been set
            case 1:
                Log.d(TAG, "case:1 qid:" + qid);
                break;

            //Trigger completed
            case 2:
                Log.d(TAG, "case:2 qid:" + qid);
                mAnswer = "Case 2";
                returnClearance();
                break;
            default:
        }
    }

    private void releaseService() {
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
        Log.d(TAG, "releaseService() called");
    }

    //Change the Intent's name to the external TMService's name
    public void bindTMService(Context context) {
        connection = new TriggerManagerServiceConnection();
        Intent i = new Intent("com.xlabm.tmservice.TriggerManagerService");
        context.startService(i);
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding success? " + ret);


        //The status of the trigger is set to 1
        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(ServiceTester.this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(qid, 1);

        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseService();
    }

    //Only return when trigger completed
    private void returnClearance() {
        //The status of the trigger is set to 2
        //It should be reset to 0 after user finishes the form
        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(ServiceTester.this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(qid, 2);
        editor.commit();

        Intent intent = new Intent();
        intent.putExtra("value", mAnswer);
        setResult(RESULT_OK, intent);
        finish();
    }

    //Prevent user from going back to the current trigger question
    @Override
    public void onBackPressed() {
        Toast.makeText(ServiceTester.this, "You can't go back.  Please press " +
                "HOME and wait for notification.",
                Toast.LENGTH_LONG).show();
    }

    //A connection to bind to the service
    public class TriggerManagerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder boundService) {

            //service is an interface to interact with methods in the external service's binder object
            service = ITMService.Stub.asInterface((IBinder) boundService);
            Log.d(TAG, "onServiceConnected() called");
            Toast.makeText(ServiceTester.this, "Service connected",
                    Toast.LENGTH_LONG).show();

            //registerCallback and setTrigger are methods of the external service's binder object
            try {
                service.registerCallback(mCallback);
                service.setTrigger(qid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //unregisterCallback is a method of the external service's binder object
            try {
                service.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            service = null;
            Log.d(TAG, "onServiceDisconnected() called");
            Toast.makeText(ServiceTester.this,
                    "Service disconnected",
                    Toast.LENGTH_LONG).show();
        }
    }

}
