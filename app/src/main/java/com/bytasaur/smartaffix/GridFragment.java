package com.bytasaur.smartaffix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by ASUS on 9/27/2017.
 */

public class GridFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_grid, container, false);
        ((GridView)v.findViewById(R.id.grid)).setAdapter(MainActivity.adapter);
        return v;
    }
}
