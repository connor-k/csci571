package com.connorkerns.csci571_stocks;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ResultActivity extends AppCompatActivity {
    private static String DEBUG_TAG = "ResultActivity";
    private static String symbol;
    private static String name;
    private static String lastPrice;
    private static String changeStr;
    private static String quoteJson;
    private Menu menu;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d(DEBUG_TAG, "Facebook Dialog Callback SUCCESS");
            Log.d(DEBUG_TAG, "Post id: " + result.getPostId());
            // Display a toast with not posted if that's the case
            Toast.makeText(ResultActivity.this, "You shared this post.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Log.d(DEBUG_TAG, "Facebook Dialog Callback CANCEL");
            Toast.makeText(ResultActivity.this, "Post NOT shared", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(DEBUG_TAG, "Facebook Dialog Callback ERROR");
            Toast.makeText(ResultActivity.this, "Post NOT shared", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        quoteJson = getIntent().getStringExtra(MainActivity.QUOTE_JSON);
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject quote = parser.parse(quoteJson).getAsJsonObject();

        symbol = gson.fromJson(quote.get("Symbol"), String.class);
        name = gson.fromJson(quote.get("Name"), String.class);
        setTitle(name);

        // Initialize Facebook SDK for sharing
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    private void setFavoriteIcon() {
        Log.d(DEBUG_TAG, "Setting favorite menu icon");
        if (FavoritesManager.isFavorite(ResultActivity.this, ResultActivity.symbol)) {
            menu.getItem(0).setIcon(getResources().getDrawable(android.R.drawable.star_on, getTheme()));
        } else {
            menu.getItem(0).setIcon(getResources().getDrawable(android.R.drawable.star_off, getTheme()));
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(DEBUG_TAG, "onActivityResult data: " + requestCode + " " + resultCode + " " + data.getExtras());
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        this.menu = menu;
        setFavoriteIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            Log.d(DEBUG_TAG, "Favorite toggled");
            if (FavoritesManager.isFavorite(ResultActivity.this, ResultActivity.symbol)) {
                FavoritesManager.removeFavorite(ResultActivity.this, ResultActivity.symbol);
            } else {
                FavoritesManager.addFavorite(ResultActivity.this, ResultActivity.symbol);
                // Toast at 1:23
                Toast.makeText(ResultActivity.this, "Bookmarked " + ResultActivity.name + "!!", Toast.LENGTH_SHORT).show();
            }
            setFavoriteIcon();
            return true;
        } else if (id == R.id.action_facebook) {
            Log.d(DEBUG_TAG, "Facebook share button clicked");
            // Toast at 1:27
            Toast.makeText(ResultActivity.this, "Sharing " + ResultActivity.name + "!!", Toast.LENGTH_SHORT).show();
            shareDialog = new ShareDialog(this);
            shareDialog.registerCallback(callbackManager, shareCallback);
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("Current Stock Price of " + ResultActivity.name + " is $"
                                + ResultActivity.lastPrice)
                        .setContentDescription("Stock Information of " + ResultActivity.name + " ("
                                + ResultActivity.symbol + ")")
                        .setImageUrl(Uri.parse("https://chart.yahoo.com/t?s=" + ResultActivity.symbol
                                + "&lang=en-US&width=400&height=300"))
                        .setContentUrl(Uri.parse("https://finance.yahoo.com/q?s=" + symbol))
                        .build();
                shareDialog.setShouldFailOnDataError(true);
                shareDialog.show(linkContent, ShareDialog.Mode.FEED);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class CurrentFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public CurrentFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CurrentFragment newInstance(int sectionNumber) {
            CurrentFragment fragment = new CurrentFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void addDetailItem(LinearLayout parent, String label, String value,
                                   LayoutInflater inflater, ViewGroup container) {
            View child = inflater.inflate(R.layout.list_item_details, container, false);
            ((TextView)child.findViewById(R.id.detailsItemLabel)).setText(label);
            ((TextView)child.findViewById(R.id.detailsItemValue)).setText(value);
            parent.addView(child);
        }

        private void addDetailItem(LinearLayout parent, String label, String value,
                                   LayoutInflater inflater, ViewGroup container, boolean up) {
            View child;
            if (up) {
                child = inflater.inflate(R.layout.list_item_details_up, container, false);
            } else {
                child = inflater.inflate(R.layout.list_item_details_down, container, false);
            }
            ((TextView)child.findViewById(R.id.detailsItemLabel)).setText(label);
            ((TextView)child.findViewById(R.id.detailsItemValue)).setText(value);
            parent.addView(child);
        }

        private void addDivider(LinearLayout parent, LayoutInflater inflater, ViewGroup container) {
            View child = inflater.inflate(R.layout.divider, container, false);
            parent.addView(child);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_current, container, false);
            LinearLayout details = (LinearLayout) rootView.findViewById(R.id.stock_details_list);

            // Parse the JSON and add all the list items
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject quote = parser.parse(ResultActivity.quoteJson).getAsJsonObject();
            addDetailItem(details, "NAME", gson.fromJson(quote.get("Name"), String.class), inflater, container);
            addDivider(details, inflater, container);

            String symbol = gson.fromJson(quote.get("Symbol"), String.class);
            addDetailItem(details, "SYMBOL", symbol, inflater, container);
            addDivider(details, inflater, container);

            lastPrice = gson.fromJson(quote.get("LastPrice"), String.class);
            addDetailItem(details, "LASTPRICE", lastPrice, inflater, container);
            addDivider(details, inflater, container);

            Double value = gson.fromJson(quote.get("Change"), Double.class);
            boolean positive = value > 0.0;
            String display = String.format("%.2f", value);
            value = gson.fromJson(quote.get("ChangePercent"), Double.class);
            display += "(" + (positive ? "+" : "") + String.format("%.2f", value) + "%)";
            changeStr = display;
            addDetailItem(details, "Change", display, inflater, container, positive);
            addDivider(details, inflater, container);

            String timestamp = gson.fromJson(quote.get("Timestamp"), String.class);
            addDetailItem(details, "TIMESTAMP", timestamp, inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "MARKETCAP", gson.fromJson(quote.get("MarketCap"), String.class), inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "VOLUME", gson.fromJson(quote.get("Volume"), String.class), inflater, container);
            addDivider(details, inflater, container);

            value = gson.fromJson(quote.get("ChangeYTD"), Double.class);
            display = String.format("%.2f", value);
            value = gson.fromJson(quote.get("ChangePercentYTD"), Double.class);
            positive = value > 0.0;
            display += "(" + (positive ? "+" : "") + String.format("%.2f", value) + "%)";
            addDetailItem(details, "CHANGEYTD", display, inflater, container, positive);
            addDivider(details, inflater, container);

            addDetailItem(details, "HIGH", gson.fromJson(quote.get("High"), String.class), inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "LOW", gson.fromJson(quote.get("Low"), String.class), inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "OPEN", gson.fromJson(quote.get("Open"), String.class), inflater, container);

            // Load up the stock image
            final String url = "https://chart.yahoo.com/t?s=" + symbol + "&lang=en-US&width=400&height=300";
            ImageView imageView = (ImageView)rootView.findViewById(R.id.current_stock_image);
            Picasso.with(getActivity()).load(url).resize(1200, 900).centerInside().into(imageView);
            // Open dialog with zoomable image on click
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog builder = new Dialog(getContext());
                    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    builder.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            // Do nothing
                        }
                    });

                    ImageView imageView = new ImageView(getActivity());
                    imageView.setPadding(48, 0, 48, 0);
                    Picasso.with(getActivity()).load(url).resize(1200, 900).centerInside().into(imageView);
                    new PhotoViewAttacher(imageView);
                    builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    builder.show();
                }
            });

            return rootView;
        }
    }

    public static class HistoricalFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public HistoricalFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static HistoricalFragment newInstance(int sectionNumber) {
            HistoricalFragment fragment = new HistoricalFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_historical, container, false);
            WebView webView = (WebView) rootView.findViewById(R.id.historical_webView);
            webView.getSettings().setJavaScriptEnabled(true);
            String baseURL = "https://inspired-photon-127022.appspot.com/";
            webView.loadUrl("javascript:var symbol='" + ResultActivity.symbol + "';");
            // Use the html file in assets to render the WebView
            StringBuilder html = new StringBuilder();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(getResources().getAssets().open("HighCharts.html")));
                String line;
                while ((line = in.readLine()) != null) {
                    html.append(line);
                }
                in.close();
                webView.loadDataWithBaseURL(baseURL, html.toString(), "text/html", "utf-8", null);
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Failed to open WebView");
            }

            return rootView;
        }
    }

    public static class NewsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        // TODO not great..
        public static NewsFragment instance;
        private static LinearLayout newsList;
        private static LayoutInflater inflater;
        private static ViewGroup container;

        public NewsFragment() {}

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static NewsFragment newInstance(int sectionNumber) {
            NewsFragment fragment = new NewsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public void addDetailItem(final String url, String title, String content, String publisher, String date) {
            View child = inflater.inflate(R.layout.list_item_news, container, false);
            SpannableString titleUnderlined = new SpannableString(title);
            titleUnderlined.setSpan(new UnderlineSpan(), 0, titleUnderlined.length(), 0);
            ((TextView)child.findViewById(R.id.newsItemTitle)).setText(titleUnderlined);
            // Make the title a link to the news url
            ((TextView)child.findViewById(R.id.newsItemTitle)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
            ((TextView)child.findViewById(R.id.newsItemContent)).setText(content);
            ((TextView)child.findViewById(R.id.newsItemPublisher)).setText(publisher);
            ((TextView)child.findViewById(R.id.newsItemDate)).setText(date);
            NewsFragment.newsList.addView(child);
        }

        public void addDivider() {
            View child = inflater.inflate(R.layout.divider, NewsFragment.container, false);
            NewsFragment.newsList.addView(child);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            NewsFragment.instance = this;
            View rootView = inflater.inflate(R.layout.fragment_news, container, false);
            NewsFragment.newsList = (LinearLayout) rootView.findViewById(R.id.stock_news_list);
            NewsFragment.inflater = inflater;
            NewsFragment.container = container;
            String url = "https://inspired-photon-127022.appspot.com/stock-api.php?news="
                    + ResultActivity.symbol;
            new NewsRequestTask().execute(url);

            return rootView;
        }

        /**
         * AsyncTask to do a news feed request. The code is adapted from
         * http://developer.android.com/training/basics/network-ops/connecting.html
         */
        private class NewsRequestTask extends AsyncTask<String, Void, String> {
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
                Log.d(DEBUG_TAG, "Got news json=" + result);
                try {
                    // Check for success
                    Gson gson = new Gson();
                    JsonParser parser = new JsonParser();
                    JsonObject news = parser.parse(result).getAsJsonObject().getAsJsonObject("d");
                    JsonArray results = news.getAsJsonArray("results");

                    for (int i = 0; i < results.size(); ++i) {
                        JsonObject newsItem = results.get(i).getAsJsonObject();
                        String url = gson.fromJson(newsItem.get("Url"), String.class);
                        String title = gson.fromJson(newsItem.get("Title"), String.class);
                        String content = gson.fromJson(newsItem.get("Description"), String.class);
                        String publisher = "Publisher : " + gson.fromJson(newsItem.get("Source"), String.class);
                        String date = gson.fromJson(newsItem.get("Date"), String.class);
                        String oldFormat = "yyyy-MM-dd HH:mm:ss";
                        String newFormat = "dd MMM yyyy hh:mm:ss";
                        String time = date.replace('T', ' ').replace('Z', ' ');
                        String newTime = date;
                        DateFormat formatter = new SimpleDateFormat(oldFormat);
                        Date d = null;
                        try {
                            d = formatter.parse(time);
                            ((SimpleDateFormat) formatter).applyPattern(newFormat);
                            newTime = formatter.format(d);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        newTime = "Date : " + newTime;
                        NewsFragment.instance.addDetailItem(url, title, content, publisher, newTime);
                        if (i != results.size() - 1) {
                            NewsFragment.instance.addDivider();
                        }
                    }
                } catch (JsonParseException e) {
                    Log.d(DEBUG_TAG, "News request had no results");
                } catch (IllegalStateException e) {
                    Log.d(DEBUG_TAG, "News request had no results");
                }
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                default:
                    return CurrentFragment.newInstance(position + 1);
                case 1:
                    return HistoricalFragment.newInstance(position + 1);
                case 2:
                    return NewsFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.label_current);
                case 1:
                    return getResources().getString(R.string.label_historical);
                case 2:
                    return  getResources().getString(R.string.label_news);
            }
            return null;
        }
    }
}
