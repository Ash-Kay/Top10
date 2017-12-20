package com.example.top10;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String saveFeedLimit = "10";
    private static final String saveFeedUrl = null;
    private ListView listapp;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private String feedCacheUrl = "Invalid";
    private int feedLimit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listapp = (ListView) findViewById(R.id.xmlListView);

        downloadUrl(String.format(feedUrl, feedLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        if (feedLimit == 10) {
            menu.findItem(R.id.mTop10).setChecked(true);
        } else {
            menu.findItem(R.id.mTop25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.mFreeApps:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;

            case R.id.mPaidApps:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;

            case R.id.mSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;

            case R.id.mTop10:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 10;
                }
                break;

            case R.id.mTop25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 25;
                }
                break;

            case R.id.mRefresh:
                feedCacheUrl = "Invalid";
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedUrl, feedLimit));
        return true;
    }

    private void downloadUrl(String feedUrl) {

        if (!feedCacheUrl.equals(feedUrl)) {
            Log.d(TAG, "downloadURL: starting AsyncTask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            Log.d(TAG, "downloadURL: after URL execute");
            feedCacheUrl = feedUrl;
        } else
            Log.d(TAG, "downloadUrl: Same URL passed NO UPDATE");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(saveFeedLimit, Integer.toString(feedLimit));
        outState.putString(saveFeedUrl, feedUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        feedLimit = Integer.valueOf(savedInstanceState.getString(saveFeedLimit));
        Log.d(TAG, "onRestoreInstanceState: feedLimit: " + feedLimit);
        feedUrl = savedInstanceState.getString(saveFeedUrl);
        downloadUrl(String.format(feedUrl, feedLimit));

    }

    public class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Log.d(TAG, "onPostExecute: parameter is"+s);
            ParseApp parseApp = new ParseApp();
            parseApp.parse(s);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, parseApp.getApps());
            listapp.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);

            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null)
                Log.e(TAG, "doInBackground: Error in downloading!!");
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlresult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: Response received " + response);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                int charRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charRead = reader.read(inputBuffer);
                    if (charRead < 0)
                        break;
                    if (charRead > 0)
                        xmlresult.append(String.copyValueOf(inputBuffer, 0, charRead));

                }
                reader.close();

                return xmlresult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL !" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO exception in reading " + e.getMessage());
            }

            return null;
        }
    }
}
