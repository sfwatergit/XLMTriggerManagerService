package com.xlabm.tmservice.triggerdefs;


import android.util.Log;

import java.util.HashMap;

/**
 * XLAB Mobile Version 0.1 Alpha Release
 * User: SID@XLABM
 * Date: 5/21/13
 * Time: 7:41 AM
 * Responsibility of Class:
 */
public class GeoTrigger extends Trigger {

    String TAG = "GeoTrigger";
    private String _lat;
    private String _lon;


    public GeoTrigger(String qid, HashMap<String, String> parameters) {
        super(qid);
        _lat = parameters.get("lat");
        _lon = parameters.get("lon");
    }


    @Override
    public void execute() {
        super.status = 1;


    }


    @Override
    public void debugMessage() {
        Log.d(TAG, "Has completed for Latitude:" + _lat + " and Longitude: " + _lon
        );
    }
}

