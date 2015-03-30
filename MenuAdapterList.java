package com.example.sameer.googleface;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.ArrayList;

/**
 * Created by Sameer on 2/11/2015.
 */
public class MenuAdapterList extends CardScrollAdapter
{
    ArrayList<CardBuilder> cardBuilders;
    public MenuAdapterList(ArrayList<CardBuilder> cardArray)
    {
        this.cardBuilders= cardArray;
    }

    public int getCount() {
        return cardBuilders.size();
    }

    @Override
    public Object getItem(int position) {
        return cardBuilders.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return cardBuilders.get(position).getView();
    }

    @Override
    public int getPosition(Object item) {
        int pos = 0;
        for(int i=0;i<cardBuilders.size();i++){
            if(cardBuilders.get(i)==item){
                pos = i;
                break;
            }
        }
        return pos;
    }
}
