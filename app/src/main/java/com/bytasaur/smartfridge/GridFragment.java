package com.bytasaur.smartfridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Created by ASUS on 9/27/2017.
 */

public class GridFragment extends android.support.v4.app.Fragment {
    Toast toast;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_grid, container, false);
        GridView gridView=v.findViewById(R.id.grid);
        gridView.setAdapter(MainActivity.adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(toast!=null) {
                    toast.cancel();
                }
                toast= Toast.makeText(getContext(), "Long press item for menu", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        gridView.setOnItemLongClickListener(MainActivity.listener);
        return v;
    }
}
