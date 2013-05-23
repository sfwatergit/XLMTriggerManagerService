package com.xlabm.tmservice.tmutils;

import android.util.Log;

import java.util.HashMap;

/**
 * XLAB Mobile Version 0.1 Alpha Release
 * User: SID@XLABM
 * Date: 5/21/13
 * Time: 7:44 AM
 * Responsibility of Class:
 */
public class DefaultTrigger extends Trigger {
    String TAG = "Default Trigger";

    DefaultTrigger(String qid, HashMap<String, String> params) {
        super(qid);
    }


    @Override
    public void execute() {
        super.status = 1;

    }


    @Override
    public void debugMessage() {
        Log.d(TAG, "Boring trigger");
    }
}