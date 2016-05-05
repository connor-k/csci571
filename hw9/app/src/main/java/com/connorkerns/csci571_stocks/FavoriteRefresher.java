package com.connorkerns.csci571_stocks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;

public class FavoriteRefresher {
    private static String DEBUG_TAG = "FavoriteRefresher";
    private FavoriteItem favoriteItem;
    private MainActivity mainActivity;

    FavoriteRefresher(MainActivity ma, FavoriteItem fi) {
        mainActivity = ma;
        favoriteItem = fi;
        String url = "https://inspired-photon-127022.appspot.com/stock-api.php?symbol=" + fi.symbol;
        new FavoriteRequestTask().execute(url);
    }

    /**
     * AsyncTask to do a quote request. The code is adapted from
     * http://developer.android.com/training/basics/network-ops/connecting.html
     */
    private class FavoriteRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return Downloader.downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, "Got quote json=" + result);
            try {
                // Check for success
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonObject quote = parser.parse(result).getAsJsonObject();
                String price = gson.fromJson(quote.get("LastPrice"), String.class);
                String changePercent = gson.fromJson(quote.get("ChangePercent"), String.class);
                // Always update price/change percent
                favoriteItem.price = price;
                favoriteItem.changePercent = changePercent;
                // Only update price/change if this isn't the first quote request
                if (favoriteItem.isInitialUpdate) {
                    String name = gson.fromJson(quote.get("Name"), String.class);
                    String marketCap = gson.fromJson(quote.get("MarketCap"), String.class);
                    favoriteItem.name = name;
                    favoriteItem.marketCap = marketCap;
                    favoriteItem.isInitialUpdate = false;
                    favoriteItem.initialized = true;
                }
            } catch (JsonParseException e) {
                Log.d(DEBUG_TAG, "Quote request had no results");
            } catch (IllegalStateException e) {
                Log.d(DEBUG_TAG, "Quote request had no results");
            } finally {
                mainActivity.notifyFavoritesChanged();
            }
        }
    }
}
