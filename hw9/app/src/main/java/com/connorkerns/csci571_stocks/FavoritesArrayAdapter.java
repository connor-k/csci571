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
    FavoriteItem(String s, String n, String p, String cp, String m) {
        symbol = s;
        name = n;
        price = p;
        changePercent = cp;
        marketCap = m;
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

        FavoriteItem i = items.get(position);

        if (i != null) {
            TextView symbolText = (TextView)v.findViewById(R.id.favoritesSymbol);
            TextView nameText = (TextView)v.findViewById(R.id.favoritesName);
            TextView priceText = (TextView)v.findViewById(R.id.favoritesPrice);
            TextView changePercentText = (TextView)v.findViewById(R.id.favoritesChangePercent);
            TextView marketCapText = (TextView)v.findViewById(R.id.favoritesMarketCap);
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
                changePercentText.setText(i.changePercent + "%");
                if (Double.parseDouble(i.changePercent) > 0.0) {
                    changePercentText.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_light));
                } else {
                    changePercentText.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_red_light));
                }
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
