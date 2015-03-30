package com.example.sameer.googleface;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

/**
 * Created by Sameer on 2/5/2015.
 */
public class MenuAdapter extends CardScrollAdapter
{
    CardBuilder [] cardBuilders;
    public MenuAdapter(CardBuilder [] cardArray)
    {
        cardBuilders = cardArray;
    }

    public int getCount() {
        return cardBuilders.length;
    }

    @Override
    public Object getItem(int position) {
        return cardBuilders[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return cardBuilders[position].getView();
    }

    @Override
    public int getPosition(Object item) {
        int pos = 0;
        for(int i=0;i<cardBuilders.length;i++){
            if(cardBuilders[i]==item){
                pos = i;
                break;
            }
        }
        return pos;
    }
}
