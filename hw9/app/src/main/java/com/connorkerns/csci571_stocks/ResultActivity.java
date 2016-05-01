package com.connorkerns.csci571_stocks;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ResultActivity extends AppCompatActivity {
    private String symbol;
    private String name;
    private static String quoteJson;
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

        //TODO may need to disallow swiping between tabs (see Piazza)
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        quoteJson = getIntent().getStringExtra(MainActivity.QUOTE_JSON);
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject quote = parser.parse(quoteJson).getAsJsonObject();

        name = gson.fromJson(quote.get("Name"), String.class);
        setTitle(name);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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

        private void addDivider(LinearLayout parent, LayoutInflater inflater, ViewGroup container) {
            View child = inflater.inflate(R.layout.divider, container, false);
            parent.addView(child);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_current, container, false);
            LinearLayout details = (LinearLayout) rootView.findViewById(R.id.stock_details_list);
            View child = inflater.inflate(R.layout.list_item_details, container, false);

            // Parse the JSON and add all the list items
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject quote = parser.parse(ResultActivity.quoteJson).getAsJsonObject();
            addDetailItem(details, "NAME", gson.fromJson(quote.get("Name"), String.class), inflater, container);
            addDivider(details, inflater, container);

            String symbol = gson.fromJson(quote.get("Symbol"), String.class);
            addDetailItem(details, "SYMBOL", symbol, inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "LASTPRICE", gson.fromJson(quote.get("LastPrice"), String.class), inflater, container);
            addDivider(details, inflater, container);

            Double value = gson.fromJson(quote.get("Change"), Double.class);
            boolean positive = value > 0.0;
            String display = String.format("%.2f", value);
            value = gson.fromJson(quote.get("ChangePercent"), Double.class);
            //TODO check +
            display += "(" + String.format("%.2f", value) + "%)";
            //TODO use positive for the arrow image
            addDetailItem(details, "Change", display, inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "TIMESTAMP", gson.fromJson(quote.get("Timestamp"), String.class), inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "MARKETCAP", gson.fromJson(quote.get("MarketCap"), String.class), inflater, container);
            addDivider(details, inflater, container);

            addDetailItem(details, "VOLUME", gson.fromJson(quote.get("Volume"), String.class), inflater, container);
            addDivider(details, inflater, container);

            value = gson.fromJson(quote.get("ChangeYTD"), Double.class);
            positive = value > 0.0;
            display = String.format("%.2f", value);
            value = gson.fromJson(quote.get("ChangePercentYTD"), Double.class);
            display += "(" + String.format("%.2f", value) + "%)";
            //TODO use positive for the arrow image
            addDetailItem(details, "CHANGEYTD", display, inflater, container);
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

                    /*
                    Dialog.Builder builder = new Dialog.Builder(getActivity());
                    final AlertDialog dialog = builder.create();
                    LinearLayout parent = new LinearLayout(getContext());
                    ImageView other = imageView;
                    parent.addView(other);
                    dialog.setView(parent);
                    dialog.show();
                    */
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
            View rootView = inflater.inflate(R.layout.fragment_result, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }

    public static class NewsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public NewsFragment() {
        }

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_result, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
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
