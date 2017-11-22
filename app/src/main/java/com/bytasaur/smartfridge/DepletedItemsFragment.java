package com.bytasaur.smartfridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class DepletedItemsFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_depleted_items, container, false);
        ((ListView)v.findViewById(R.id.depleted_list)).setAdapter(MainActivity.adapter2);
        return v;
    }

}
