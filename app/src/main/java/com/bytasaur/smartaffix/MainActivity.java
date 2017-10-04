package com.bytasaur.smartaffix;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements ValueEventListener {
    private ArrayList<Geofence> locationsOfInterest = new ArrayList<>();
    private PendingIntent pendingIntent;
    private HumidityTempFragment instance1;
    private StateListFragment instance2;
    public HashMap<String, FridgeItem> itemHashMap=new HashMap<>();
    HashMap<String, Integer> imageResIds = new HashMap<>();
    ArrayList<FridgeItem> list = new ArrayList<>();
    public static ArrayAdapter<FridgeItem> adapter;
    private String[] titles = {"Monitor", "Inventory", "Restock"};
    // Add String array for keys

    public static DatabaseReference ref;

    private int colors[] = {0xFF303F9F, 0xffcc0000};  // @color/colorPrimaryDark 0xFF303F9F
    private String states[] = {"Closed", "Open"};

    ChildEventListener itemChangeListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String tmp = dataSnapshot.getKey();
            if (itemHashMap.containsKey(tmp)) {
                //tmp2.setText(dataSnapshot.getValue()+"");
                return;
            }
            Integer i = imageResIds.get(tmp);
            if (i == null) {
                i = R.drawable.def;
            }
            adapter.add(new FridgeItem(tmp, dataSnapshot.getValue(Integer.class), i));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            itemHashMap.get(dataSnapshot.getKey()).count=dataSnapshot.getValue(Integer.class);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            adapter.remove(itemHashMap.get(dataSnapshot.getKey()));
            itemHashMap.remove(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageResIds.put("Eggs", R.drawable.egg);
        imageResIds.put("Milk", R.drawable.milksmall);
        imageResIds.put("Water Bottles", R.drawable.water);
        imageResIds.put("Oranges", R.drawable.orangesmall);

        adapter = new ArrayAdapter<FridgeItem>(this, R.layout.item_view, list) {
            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_view, null, false);
                }
                FridgeItem item = list.get(position);
                ((ImageView) convertView.findViewById(R.id.item_image)).setImageResource(item.resId);
                ((TextView) convertView.findViewById(R.id.item_label)).setText(item.name);
                ((TextView) convertView.findViewById(R.id.item_count)).setText(item.count +"");
                itemHashMap.put(item.name, item);
                return convertView;
            }
        };
        ((ViewPager) findViewById(R.id.pager)).setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference(getIntent().getStringExtra("devid"));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setGeofences();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        String key=dataSnapshot.getKey();
        switch (key) {
            case "Temperature":
                instance1.temperatureView.setText(dataSnapshot.getValue(Long.class) + "° F");
                return;
            case "DoorState": {
                Integer tmp = dataSnapshot.getValue(Integer.class);
                if (tmp == null || tmp > 1 || tmp < 0) {
                    return;
                }
                instance2.doorStateView.setTextColor(colors[tmp]);
                instance2.doorStateView.setText(states[tmp]);
                return;
            }
            case "Humidity": {
                Integer tmp = dataSnapshot.getValue(Integer.class);
                instance1.humidityView.setText(tmp + "%");
                break;
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setGeofences();
            }
            else {
                Toast.makeText(this, "App requires permission for location-based reminders", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private void setGeofences() {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);

        locationsOfInterest.add(new Geofence.Builder().setRequestId("Naheed").setCircularRegion(24.877984, 67.068688, 60).setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build());
        locationsOfInterest.add(new Geofence.Builder().setRequestId("Home").setCircularRegion(24.928692, 67.062211, 30).setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build());
        locationsOfInterest.add(new Geofence.Builder().setRequestId("University Parking").setCircularRegion(24.942157, 67.114381, 45).setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build());
        locationsOfInterest.add(new Geofence.Builder().setRequestId("University Library").setCircularRegion(24.940921, 67.115062, 35).setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build());

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(), "Geofences set", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(), "Geofences NOT set", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onRestart() {
        ref.child("Temperature").addValueEventListener(this);   // Save key strings in an array maybe
        ref.child("Humidity").addValueEventListener(this);
        ref.child("Stock").addChildEventListener(itemChangeListener);
        super.onRestart();
    }

    @Override
    protected void onStop() {
        ref.child("Temperature").removeEventListener(this);
        ref.child("Humidity").removeEventListener(this);
//        ref.child("Temperature").removeEventListener((ValueEventListener) this);
        ref.child("Stock").removeEventListener(itemChangeListener);
        super.onStop();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder=new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(locationsOfInterest);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(pendingIntent==null) {
            Intent intent=new Intent(this, GeofenceTransitionService.class);
            pendingIntent=PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.signout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SigninActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 2;
        }

        // Returns the fragment to display for a particular page.
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    instance1 = HumidityTempFragment.newInstance();
                    ref.child("Temperature").addValueEventListener(MainActivity.this);   // Save key strings in an array maybe
                    ref.child("Humidity").addValueEventListener(MainActivity.this);
                    return instance1;
                case 1:
                    instance2 = StateListFragment.newInstance();
                    ref.child("DoorState").addValueEventListener(MainActivity.this);
                    ref.child("Stock").addChildEventListener(itemChangeListener);
                    return instance2;
                case 3:
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
    private static class FridgeItem {
        String name;
        int count;
        int resId;
        public FridgeItem(String name, int count) {
            this(name, count, R.id.cast_notification_id);
        }
        FridgeItem(String name, int count, int resId) {
            this.name=name;
            this.count = count;
            this.resId=resId;
        }
    }
}
