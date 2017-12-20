package com.example.top10;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by ^^ASHISH^^ on 16-Jul-17.
 */

public class ParseApp {
    private static final String TAG = "ParseApp";
    private ArrayList<FeedEntry> apps;

    public ParseApp() {
        this.apps = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApps() {
        return apps;
    }

    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;
        String textValue = "";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();          //take any tag name

                switch (eventType) {

                    case XmlPullParser.START_TAG:
                        //Log.d(TAG, "parse: Start tag of "+tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //Log.d(TAG, "parse: At end Tag of "+tagName);
                        if (inEntry) {
                            if ("entry".equalsIgnoreCase(tagName)) {
                                apps.add(currentRecord);
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                }
                eventType = xpp.next();

            }

            /*for(FeedEntry app:apps){
                Log.d(TAG, "===============================");
                Log.d(TAG, app.toString());
            }*/


        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }
        return status;
    }
}
