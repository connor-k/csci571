package com.connorkerns.csci571_stocks;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesManager {
    public static String KEY_FAVORITES = "KEY_FAVORITES";
    private static String DEBUG_TAG = "FavoritesManager";

    public static void addFavorite(Context context, String symbol) {
        // Check that it's not already a favorite, append it
        if (!isFavorite(context, symbol)) {
            String data = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_FAVORITES, "");
            data += "," + symbol;
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_FAVORITES, data).commit();
            Log.d(DEBUG_TAG, "addFavorite: Set favorites string: " + data);
        }
    }

    public static void removeFavorite(Context context, String symbol) {
        // Check that it's a favorite
        if (isFavorite(context, symbol)) {
            List<String> favorites = getFavorites(context);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < favorites.size(); ++i) {
                if (favorites.get(i) != symbol) {
                    sb.append(favorites.get(i)).append(",");
                }
            }
            // Remove last comma if present and store it
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
                sb = new StringBuilder(sb.substring(0, sb.length() - 1));
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_FAVORITES, sb.toString()).commit();
            Log.d(DEBUG_TAG, "removeFavorite: Set favorites string: " + sb.toString());
        }
    }

    public static List<String> getFavorites(Context context) {
        String data = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_FAVORITES, "");
        Log.d(DEBUG_TAG, "getFavorites: Got favorites string: " + data);
        return new ArrayList<String>(Arrays.asList(data.split(",")));
    }

    public static boolean isFavorite(Context context, String symbol) {
        List<String> favorites = getFavorites(context);
        Log.d(DEBUG_TAG, "isFavorite: Favorites list: " + favorites);
        return favorites.contains(symbol);
    }
}
