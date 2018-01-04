package com.bytasaur.smartfridge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class GridFragment extends android.support.v4.app.Fragment {
    Toast toast;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_grid, container, false);
        ((GridView)v.findViewById(R.id.grid)).setAdapter(MainActivity.adapter);
        return v;
    }
}
