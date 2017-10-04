package com.bytasaur.smartaffix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HumidityTempFragment extends android.support.v4.app.Fragment {
    public TextView temperatureView;
    public TextView humidityView;

    public static HumidityTempFragment newInstance() {
        return new HumidityTempFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.hum_temp_view, container, false);
        temperatureView=(TextView)v.findViewById(R.id.temp_view);
        humidityView=(TextView)v.findViewById(R.id.humid_view);
        return v;
    }
}
