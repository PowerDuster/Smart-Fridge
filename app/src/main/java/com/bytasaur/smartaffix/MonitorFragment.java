package com.bytasaur.smartaffix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MonitorFragment extends android.support.v4.app.Fragment {
    public TextView temperatureView;
    public TextView humidityView;
    public TextView doorStateView;  // Can't make static due to potential memory leaks

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.monitor_layout, container, false);
        doorStateView=(TextView)v.findViewById(R.id.door_state_view);
        temperatureView=(TextView)v.findViewById(R.id.temp_view);
        humidityView=(TextView)v.findViewById(R.id.humid_view);
        return v;
    }
}
