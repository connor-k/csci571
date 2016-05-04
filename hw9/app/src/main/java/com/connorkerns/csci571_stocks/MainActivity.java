package com.connorkerns.csci571_stocks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String SYMBOL = "symbol";
    public static String NAME = "name";
    public static String QUOTE_JSON = "quote_json";

    private static String DEBUG_TAG = "MainActivity";
    private View clearButton;
    private View quoteButton;
    private View refreshButton;
    private AutoCompleteTextView textView;
    private ArrayList<FavoriteItem> favoriteItemList;
    private DynamicListView dynamicListView;
    private FavoritesArrayAdapter adapter;
    private Boolean ignoreAutoComplete = false;
    private AsyncTask autoCompleteTask = null;
    private Lock favoritesLock;
    private int favoritesUpdateCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Stock Market Viewer");

        // Print out the debug key hash for Facebook SDK
        // TODO remove once it's all verified
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.connorkerns.csci571_stocks",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        // Set up click listeners for all the buttons
        clearButton = findViewById(R.id.button_clear);
        clearButton.setOnClickListener(this);
        quoteButton = findViewById(R.id.button_get_quote);
        quoteButton.setOnClickListener(this);
        refreshButton = findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(this);
        textView = (AutoCompleteTextView)findViewById(R.id.auto_complete_text_view);
        // Show suggestions only after >=3 chars entered
        textView.setThreshold(3);
        // Listen for key presses for autocomplete
        TextWatcher autocompleteWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // If they clicked on an item and I changed the text, don't show the popup
                if (ignoreAutoComplete) {
                    ignoreAutoComplete = false;
                    textView.dismissDropDown();
                } else if (textView.getText().length() >= 3) {
                    doAutoCompleteLookup();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        textView.addTextChangedListener(autocompleteWatcher);
        // Only take ticker from front of suggestion on click
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                //TODO this is a little sketch
                String item = p.getItemAtPosition(pos).toString();
                String ticker = item.substring(0, item.indexOf(' '));
                Log.d(DEBUG_TAG, "Clicked on list item=" + ticker);
                // Disable adapter temporarily to avoid popup
                ignoreAutoComplete = true;
                textView.setText(ticker);
                // Move cursor to end
                int position = ticker.length();
                Editable etext = textView.getEditableText();
                Selection.setSelection(etext, position);
            }
        });
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textView.showDropDown();
                return false;
            }
        });

        dynamicListView = (DynamicListView)findViewById(R.id.dynamiclistview);
        favoriteItemList = new ArrayList<FavoriteItem>();
        adapter = new FavoritesArrayAdapter(this, R.layout.list_item_favorites, favoriteItemList);
        dynamicListView.setAdapter(adapter);
        // Enable swipe to dismiss
        dynamicListView.enableSwipeToDismiss(
            new OnDismissCallback() {
                @Override
                public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                    for (final int position : reverseSortedPositions) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        //TODO real message
                        alertDialog.setMessage("Want to delete " + adapter.getItemName(position) + " from favorites?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(DEBUG_TAG, "Tried to remove item " + position);
                                //Log.d(DEBUG_TAG, "Value: " + adapter.getItem(position));
                                FavoritesManager.removeFavorite(MainActivity.this, favoriteItemList.get(position).symbol);
                                favoriteItemList.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Dismiss
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        );
        dynamicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < favoriteItemList.size()) {
                    String url = "https://inspired-photon-127022.appspot.com/stock-api.php?symbol=" + favoriteItemList.get(position).symbol;
                    new QuoteRequestTask().execute(url);
                }
            }
        });

        // Add app icon to status bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        favoritesLock = new ReentrantLock();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_clear:
                clearTextView();
                break;
            case R.id.button_get_quote:
                getQuote();
                break;
            case R.id.button_refresh:
                refreshFavorites();
                break;
            default:
                break;
        }
    }

    /**
     * Clear the stock name/symbol text
     */
    private void clearTextView() {
        textView.setText("");
    }

    /**
     * AsyncTask to do a lookup request. The code is adapted from
     * http://developer.android.com/training/basics/network-ops/connecting.html
     */
    private class AutoCompleteRequestTask extends AsyncTask<String, Void, String> {
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
            if (textView.getText().toString().isEmpty()) {
                Log.d(DEBUG_TAG, "text now empty, clearing adapter...");
                textView.setAdapter(null);
                return;
            }
            if (isCancelled()) {
                Log.d(DEBUG_TAG, "cancelling request...");
                return;
            }

            Log.d(DEBUG_TAG, "Got autocomplete json=" + result);
            // From @612, we can just display all info on one line
            try {
                ArrayAdapter<String> autocompleteResults = new ArrayAdapter<String>(
                        MainActivity.this, android.R.layout.simple_dropdown_item_1line);
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(result).getAsJsonArray();
                for (int i = 0; i < array.size(); ++i) {
                    String item = gson.fromJson(array.get(i).getAsJsonObject().get("Display"),
                            String.class);
                    Log.d(DEBUG_TAG, "autocomplete result: " + item);
                    autocompleteResults.add(item);
                }
                textView.setAdapter(autocompleteResults);
                textView.showDropDown();
                Log.d(DEBUG_TAG, "popup showing? " + textView.isPopupShowing());
            } catch (JsonParseException e) {
                Log.d(DEBUG_TAG, "autocomplete request had no results or request was cancelled");
            } catch (IllegalStateException e) {
                Log.d(DEBUG_TAG, "autocomplete request had no results or request was cancelled");
            }
        }
    }

    /**
     * AsyncTask to do a quote request. The code is adapted from
     * http://developer.android.com/training/basics/network-ops/connecting.html
     */
    private class QuoteRequestTask extends AsyncTask<String, Void, String> {
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
                if (gson.fromJson(quote.get("Status"), String.class).equals("SUCCESS")) {
                    Log.d(DEBUG_TAG, "Status was success, starting result activity.");
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    intent.putExtra(MainActivity.NAME, "TODO parse in result side");
                    intent.putExtra(MainActivity.QUOTE_JSON, result);
                    startActivity(intent);
                } else {
                    Log.d(DEBUG_TAG, "Status was NOT success, can't start result activity.");
                    makeAlert("Invalid Symbol");
                }
            } catch (JsonParseException e) {
                Log.d(DEBUG_TAG, "Quote request had no results");
                makeAlert("Invalid Symbol");
            } catch (IllegalStateException e) {
                Log.d(DEBUG_TAG, "Quote request had no results");
                makeAlert("Invalid Symbol");
            }
        }
    }

    /**
     *
     */
    private void doAutoCompleteLookup() {
        Log.d(DEBUG_TAG, "Making lookup request for autocomplete...");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String input = textView.getText().toString().trim();
        if (!input.isEmpty() && networkInfo != null && networkInfo.isConnected()) {
            if (autoCompleteTask != null) {
                autoCompleteTask.cancel(true);
            }
            String url = "https://inspired-photon-127022.appspot.com/stock-api.php?input=" +
                    Html.escapeHtml(input);
            autoCompleteTask = new AutoCompleteRequestTask().execute(url);
        } else {
            Log.d(DEBUG_TAG, "Can't do request, empty or no internet connection.");
            textView.setAdapter(null);
        }
    }

    /**
     * If it's a valid search, get the quote
     */
    private void getQuote() {
        String input = textView.getText().toString().trim();
        Log.d(DEBUG_TAG, "Get Quote clicked, input=" + input);
        // Check for blank input
        if (input.isEmpty()) {
            makeAlert("Please enter a Stock Name/Symbol");
        } else {
            String url = "https://inspired-photon-127022.appspot.com/stock-api.php?symbol=" + input;
            new QuoteRequestTask().execute(url);
        }
    }

    /**
     * Refresh the quotes on the favorite list
     */
    private void refreshFavorites() {
        Log.d(DEBUG_TAG, "Refreshing Favorites...");
        //TODO spinner animation?
        List<String> favorites = FavoritesManager.getFavorites(this);
        // Refresh data for all favorites
        favoritesLock.lock();
        Log.d(DEBUG_TAG, "Currently waiting for " + favoritesUpdateCount + " updates to finish.");
        favoritesLock.unlock();
        // Remove anything missing
        for (int i = 0; i < favoriteItemList.size(); ++i) {
            boolean found = false;
            for (int j = 0; j < favorites.size(); ++j) {
                if (favorites.get(j).equals(favoriteItemList.get(i).symbol)) {
                    favorites.remove(j);
                    found = true;
                    break;
                }
            }
            if (!found) {
                favoriteItemList.remove(i);
                --i;
            }
        }
        for (int i = 0; i < favorites.size(); ++i) {
            FavoriteItem fi = new FavoriteItem(favorites.get(i));
            favoriteItemList.add(fi);
        }
        for (int i = 0; i < favoriteItemList.size(); ++i) {
            // Asynchronously do the lookup
            //TODO only refresh price/change and not everything
            favoritesLock.lock();
            ++favoritesUpdateCount;
            favoritesLock.unlock();
            new FavoriteRefresher(this, favoriteItemList.get(i));
        }
    }

    public void notifyFavoritesChanged() {
        // 1:43 updates look atomic and on going back all are reloaded!
        favoritesLock.lock();
        // TODO do I want update to be atomic?
        if (--favoritesUpdateCount == 0) {
            adapter.notifyDataSetChanged();
        }
        Log.d(DEBUG_TAG, "Received update. Waiting for " + favoritesUpdateCount);
        favoritesLock.unlock();
    }

    /**
     * Helper to make an alert dialog with no title and an OK button
     * @param message the text to display
     */
    private void makeAlert(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Dismiss
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFavorites();
    }
}
