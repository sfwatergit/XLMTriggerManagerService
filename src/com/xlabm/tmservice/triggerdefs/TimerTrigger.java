package com.xlabm.tmservice.triggerdefs;

import android.util.Log;

import java.util.HashMap;

/**
 * XLAB Mobile Version 0.1 Alpha Release
 * User: SID@XLABM
 * Date: 5/21/13
 * Time: 7:44 AM
 * Responsibility of Class:
 */
public class TimerTrigger extends Trigger {
    private static String mtime;

    TimerTrigger(String qid, HashMap<String, String> params) {
        super(qid);
        mtime = params.get("time");
    }


    @Override
    public void execute() {
        super.status = 1;

    }


    @Override
    public void debugMessage() {
        String TAG = "TimeTrigger";
        Log.d(TAG, "Has completed for Time" + mtime);
    }
}