package com.connorkerns.csci571_stocks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
        autocompleteResults = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        //TODO remove sample adds later
        autocompleteResults.add("Test");
        autocompleteResults.add("Test2");
        autocompleteResults.add("Other");
        textView.setAdapter(autocompleteResults);

        refreshFavorites();
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

    int count = 0;
    /**
     *
     */
    private void doAutoCompleteLookup() {
        Log.d(DEBUG_TAG, "Making lookup request for autocomplete...");
        if (!textView.getText().toString().isEmpty()) {
            autocompleteResults.add("Count" + count++);
        } else {
            autocompleteResults.clear();
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
