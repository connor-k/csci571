package com.connorkerns.csci571_stocks;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
    public static String DEBUG_TAG = "Downloader";

    public static String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            String contentAsString = "";
            if (is != null && response == 200) {
                // Convert the InputStream into a string
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                contentAsString = s.hasNext() ? s.next() : "";
                s.close();
            }
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
