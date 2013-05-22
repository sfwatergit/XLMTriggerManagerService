package com.xlabm.tmservice;

import android.os.AsyncTask;
import android.util.Xml;
import com.xlabm.tmservice.triggerdefs.Trigger;
import com.xlabm.tmservice.triggerdefs.TriggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * The type Trigger processor.
 */
public class TriggerXMLProcessor {
    private static final String ns = null;
    private static final String tUrlString = "http://www.ocf.berkeley" +
            ".edu/~saf/triggers/triggerTest.xml";
    private static String mFormID;
    private static String mFormName;
    TriggerFactory triggerFactory = new TriggerFactory();

    /**
     * Downloads and returns the triggers. Call is done asynchronously.
     *
     * @return the triggers
     */
    public HashMap<String, Trigger> getTriggers() {
        DownloadXMLTask xmlTask = new DownloadXMLTask();
        return xmlTask.doInBackground();
    }

    /**
     * Gets form iD.
     *
     * @return the form iD
     */
    //TODO#Later: Use a form object from ODK?
    public String getFormID() {
        return mFormID;
    }

    /**
     * Gets form name.
     *
     * @return the form name
     */
    public String getFormName() {
        return mFormName;
    }

    /**
     * Parse list.
     *
     * @param in the in
     * @return the list
     * @throws XmlPullParserException the xml pull parser exception
     * @throws IOException            the iO exception
     */
    public HashMap<String, Trigger> parse(InputStream in) throws
            XmlPullParserException,
            IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private HashMap<String, Trigger> readFeed(XmlPullParser parser) throws
            XmlPullParserException, IOException {
        HashMap<String, Trigger> triggers = new HashMap<String, Trigger>();
        parser.require(XmlPullParser.START_TAG, ns, "triggers");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            mFormID = parser.getAttributeValue(0);
            mFormName = parser.getAttributeValue(1);


            //Starts by looking for the trigger tag
            if (name.equals("trigger")) {

                Trigger trigger = readTrigger(parser);
                triggers.put(trigger.qid, trigger);

            } else {
                skip(parser);
            }
        }
        return triggers;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readQid(XmlPullParser parser) throws IOException, XmlPullParserException {


        String qid = null;
        //SID@XLABM null parameter indicates we are not using namespaces
        // (though we might in the future).
        qid = parser.getAttributeValue(null, "qid");
        return qid;
    }

    private Trigger readTrigger(XmlPullParser parser)
            throws
            IOException,
            XmlPullParserException {
        Trigger trigger;

        String qid = null;
        String type = null;
        HashMap<String, String> params;
        params = new HashMap<String, String>();

        parser.require(XmlPullParser.START_TAG, ns, "trigger");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            //SID@XLABM null parameter indicates we are not using namespaces
            // (though we might in the future).
            qid = readQid(parser);


            type = parser.getName();
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String k = parser.getAttributeName(i);
                String v = parser.getAttributeValue(i);
                params.put(k, v);
            }


            parser.require(XmlPullParser.END_TAG, ns, "trigger");
        }
        return triggerFactory.makeTrigger(qid, type, params);


    }

    //Subclass to download xml and return parsed triggers as string
    class DownloadXMLTask extends AsyncTask<String, Void, HashMap<String,
            Trigger>> {


        @Override
        protected HashMap<String, Trigger> doInBackground(String... urls) {

            HashMap<String, Trigger> triggers = null;
            try {
                return getXmlFromUrl(tUrlString);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return triggers;
        }

        private HashMap<String, Trigger> getXmlFromUrl(String urlString)
                throws XmlPullParserException, IOException {
            InputStream stream = null;
            HashMap<String, Trigger> triggers = null;

            try {
                stream = downloadUrl(urlString);
                triggers = parse(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return triggers;
        }

        /**
         * Download xml from url.
         *
         * @param urlString the urlString
         * @return the xml from url
         */
        public InputStream downloadUrl(String urlString) throws IOException {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /*milliseconds*/);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            return conn.getInputStream();


        }

    }
}


