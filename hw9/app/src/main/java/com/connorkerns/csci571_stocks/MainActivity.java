package com.connorkerns.csci571_stocks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String DEBUG_TAG = "MainActivity";
    private View clearButton;
    private View quoteButton;
    private View refreshButton;
    private AutoCompleteTextView textView;
    private ArrayAdapter<String> autocompleteResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Stock Market Viewer");

        // Set up click listeners for all the buttons
        clearButton = findViewById(R.id.button_clear);
        clearButton.setOnClickListener(this);
        quoteButton = findViewById(R.id.button_get_quote);
        quoteButton.setOnClickListener(this);
        refreshButton = findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(this);
        textView = (AutoCompleteTextView)findViewById(R.id.auto_complete_text_view);
        // Show suggestions for every char entered
        textView.setThreshold(1);
        // Listen for key presses for autocomplete
        TextWatcher autocompleteWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                doAutoCompleteLookup();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        textView.addTextChangedListener(autocompleteWatcher);
        // From @612, we can just display it on one line
        autocompleteResults = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        //TODO remove sample adds later
        textView.setAdapter(autocompleteResults);

        refreshFavorites();
        doGetRequest("https://inspired-photon-127022.appspot.com/stock-api.php?input=AAPL");
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

    private void doGetRequest(String url) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new AutoCompleteRequestTask().execute(url);
        } else {
            Log.d(DEBUG_TAG, "Can't do request, no internet connection.");
        }
    }

    /**
     * AsyncTask to do a get request. The code is adapted from
     * http://developer.android.com/training/basics/network-ops/connecting.html
     */
    private class AutoCompleteRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(DEBUG_TAG, "Got autocomplete json=" + result);
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(result).getAsJsonArray();
            autocompleteResults.clear();
            for (int i = 0; i < array.size(); ++i) {
                String item = gson.fromJson(array.get(i).getAsJsonObject().get("Display"),
                        String.class);
                Log.d(DEBUG_TAG, "autocomplete result: " + item);
                autocompleteResults.add(item);
            }
        }
    }

    private String downloadUrl(String myurl) throws IOException {
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

    /**
     *
     */
    private void doAutoCompleteLookup() {
        Log.d(DEBUG_TAG, "Making lookup request for autocomplete...");

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
        }
        //TODO request quote
    }

    /**
     * Refresh the quotes on the favorite list
     */
    private void refreshFavorites() {
        Log.d(DEBUG_TAG, "Refreshing Favorites...");
        //TODO
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
