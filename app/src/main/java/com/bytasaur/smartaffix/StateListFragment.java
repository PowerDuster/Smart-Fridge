package com.bytasaur.smartaffix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Created by ASUS on 9/27/2017.
 */

public class StateListFragment extends android.support.v4.app.Fragment {
    public TextView doorStateView;  // Can't make static due to potential memory leaks
    public GridView gridView;

    public static StateListFragment newInstance() {
        return new StateListFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.state_item, container, false);
        doorStateView=(TextView)v.findViewById(R.id.door_state_view);
        gridView=(GridView)v.findViewById(R.id.grid);
        gridView.setAdapter(MainActivity.adapter);
        return v;
    }
}
