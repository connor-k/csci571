package com.connorkerns.csci571_stocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class FavoriteItem {
    String symbol;
    String name;
    String price;
    String changePercent;
    String marketCap;
    boolean initialized;
    boolean isInitialUpdate;
    FavoriteItem(String s, String n, String p, String cp, String m) {
        setAllData(s, n, p, cp, m);
        isInitialUpdate = true;
    }

    FavoriteItem(String s) {
        setAllData(s, "", "", "", "");
        initialized = false;
        isInitialUpdate = true;
    }

    public void setAllData(String s, String n, String p, String cp, String m) {
        symbol = s;
        name = n;
        price = p;
        changePercent = cp;
        marketCap = m;
        initialized = true;
    }
}

class FavoritesArrayAdapter extends ArrayAdapter<FavoriteItem> {

    public FavoritesArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    private List<FavoriteItem> items;

    public FavoritesArrayAdapter(Context context, int resource, List<FavoriteItem> items) {
        super(context, resource, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_favorites, null);
        }

        final FavoriteItem i = items.get(position);

        if (i != null && i.initialized) {
            TextView symbolText = (TextView)v.findViewById(R.id.favoritesSymbol);
            TextView nameText = (TextView)v.findViewById(R.id.favoritesName);
            TextView marketCapLabel = (TextView)v.findViewById(R.id.favoritesMarketCapLabel);
            TextView marketCapText = (TextView)v.findViewById(R.id.favoritesMarketCap);
            TextView priceText = (TextView)v.findViewById(R.id.favoritesPrice);
            TextView changePercentText = (TextView)v.findViewById(R.id.favoritesChangePercent);

            if (symbolText != null) {
                symbolText.setText(i.symbol);
            }
            if (nameText != null) {
                nameText.setText(i.name);
            }
            if (priceText != null) {
                priceText.setText("$ " + i.price);
            }
            if (changePercentText != null) {
                if (!i.changePercent.isEmpty()) {
                    if (Double.parseDouble(i.changePercent) > 0.0) {
                        changePercentText.setText("+" + i.changePercent + "%");
                        changePercentText.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_light));
                    } else {
                        changePercentText.setText(i.changePercent + "%");
                        changePercentText.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_red_light));
                    }
                }
            }
            if (marketCapLabel != null) {
                marketCapLabel.setVisibility(View.VISIBLE);
            }
            if (marketCapText != null) {
                marketCapText.setText(i.marketCap);
            }
        }
        return v;
    }

    public String getItemName(int position) {
        return (position < items.size() ? items.get(position).name : "");
    }
}
