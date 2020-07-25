package com.example.hikersapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    TextView altitude, latitude, longitude, address,temperature,condtion,humidity,wind;
    Button map, weather,markButton,zoombutton,refreshbutton;
    boolean Map = false, Weather = false;
    GoogleMap mmap;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {

                        LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mmap.clear();
                        mmap.addMarker(new MarkerOptions().position(mylocation).title("your location"));
                        mmap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));


                        altitude.setText("Altitude : " + location.getAltitude());
                        latitude.setText("Latitude : " + location.getLatitude());
                        longitude.setText("Longitude : " + location.getLongitude());

                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        String city="";
                        try {
                            List<Address> listaddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String finaladdress = "Could not find the address";
                            if (listaddress != null && listaddress.size() > 0) {
                                finaladdress = "";

                                if (listaddress.get(0).getSubThoroughfare() != null) {
                                    finaladdress += listaddress.get(0).getSubThoroughfare() + ", ";
                                }
                                if (listaddress.get(0).getThoroughfare() != null) {
                                    finaladdress += listaddress.get(0).getThoroughfare() + "\n ";
                                }
                                if (listaddress.get(0).getLocality() != null) {
                                    finaladdress += listaddress.get(0).getLocality() + "\n ";
                                    city += listaddress.get(0).getLocality() + ", ";
                                }
                                if (listaddress.get(0).getPostalCode() != null) {
                                    finaladdress += listaddress.get(0).getPostalCode() + "\n ";
                                }
                                if (listaddress.get(0).getSubAdminArea() != null) {
                                    finaladdress += listaddress.get(0).getSubAdminArea() + ", ";
                                    city += listaddress.get(0).getSubAdminArea() + ", ";
                                }
                                if (listaddress.get(0).getAdminArea() != null) {
                                    finaladdress += listaddress.get(0).getAdminArea() + "\n ";
                                    city+=listaddress.get(0).getAdminArea()+", ";
                                }
                                if (listaddress.get(0).getCountryName() != null) {
                                    finaladdress += listaddress.get(0).getCountryName();
                                    city += listaddress.get(0).getCountryName();
                                }

                            }
                            address.setText("Address : " + finaladdress);

                        } catch (IOException e) {

                            e.printStackTrace();
                        }

                        String lat = String.valueOf(location.getLatitude());
                        String lon = String.valueOf(location.getLongitude());
                        String api="https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=f01d88ec3fb24ea6c9f39365387d4e1b";
                        downlaod task=new downlaod();
                        task.execute(api);

                    }
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        altitude = (TextView) findViewById(R.id.textView2);
        latitude = (TextView) findViewById(R.id.textView3);
        longitude = (TextView) findViewById(R.id.textView4);
        address = (TextView) findViewById(R.id.textView5);
        map = (Button) findViewById(R.id.button);
        weather = (Button) findViewById(R.id.weather);
        markButton = (Button) findViewById(R.id.markerButton);
        zoombutton = (Button)findViewById(R.id.button4);
        refreshbutton = (Button) findViewById(R.id.refreshbutton);
        temperature = (TextView) findViewById(R.id.tem);
        condtion = (TextView) findViewById(R.id.cond);
        humidity = (TextView) findViewById(R.id.hum);
        wind = (TextView) findViewById(R.id.wind);
        markButton.setVisibility(View.GONE);
        zoombutton.setVisibility(View.GONE);
        refreshbutton.setVisibility(View.GONE);
        findViewById(R.id.map).setVisibility(View.GONE);
        findViewById(R.id.wea).setVisibility(View.GONE);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Map == false) {
                    findViewById(R.id.map).setVisibility(View.VISIBLE);
                    markButton.setVisibility(View.VISIBLE);
                    zoombutton.setVisibility(View.VISIBLE);
                    Map = true;
                } else {
                    findViewById(R.id.map).setVisibility(View.GONE);
                    markButton.setVisibility(View.GONE);
                    zoombutton.setVisibility(View.GONE);
                    Map = false;
                }
            }
        });
        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Weather == false) {
                    findViewById(R.id.wea).setVisibility(View.VISIBLE);
                    refreshbutton.setVisibility(View.VISIBLE);
                    Weather = true;
                } else {
                    findViewById(R.id.wea).setVisibility(View.GONE);
                    refreshbutton.setVisibility(View.GONE);
                    Weather = false;
                }
            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng myloc = new LatLng(location.getLatitude(),location.getLongitude());


                altitude.setText("Altitude : " + location.getAltitude());
                latitude.setText("Latitude : " + location.getLatitude());
                longitude.setText("Longitude : " + location.getLongitude());

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                String city="";
                try {
                    List<Address> listaddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String finaladdress = "Could not find the address";
                    if (listaddress != null && listaddress.size() > 0) {
                        finaladdress = "";

                        if (listaddress.get(0).getSubThoroughfare() != null) {
                            finaladdress += listaddress.get(0).getSubThoroughfare() + ", ";
                        }
                        if (listaddress.get(0).getThoroughfare() != null) {
                            finaladdress += listaddress.get(0).getThoroughfare() + "\n ";
                        }
                        if (listaddress.get(0).getLocality() != null) {
                            finaladdress += listaddress.get(0).getLocality() + "\n ";
                            city += listaddress.get(0).getLocality() + ", ";
                        }
                        if (listaddress.get(0).getPostalCode() != null) {
                            finaladdress += listaddress.get(0).getPostalCode() + "\n ";
                        }
                        if (listaddress.get(0).getSubAdminArea() != null) {
                            finaladdress += listaddress.get(0).getSubAdminArea() + ", ";
                            city += listaddress.get(0).getSubAdminArea() + ", ";
                        }
                        if (listaddress.get(0).getAdminArea() != null) {
                            finaladdress += listaddress.get(0).getAdminArea() + "\n ";
                            city+=listaddress.get(0).getAdminArea()+", ";
                        }
                        if (listaddress.get(0).getCountryName() != null) {
                            finaladdress += listaddress.get(0).getCountryName();
                            city += listaddress.get(0).getCountryName();
                        }

                    }
                    address.setText("Address : " + finaladdress);

                } catch (IOException e) {

                    e.printStackTrace();
                }
                mmap.clear();
                mmap.addMarker(new MarkerOptions().position(myloc).title(city));



            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //update location;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                String lat = String.valueOf(location.getLatitude());
                String lon = String.valueOf(location.getLongitude());
                String api="https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=f01d88ec3fb24ea6c9f39365387d4e1b";


                altitude.setText("Altitude : " + location.getAltitude());
                latitude.setText("Latitude : " + location.getLatitude());
                longitude.setText("Longitude : " + location.getLongitude());


                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listaddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String finaladdress = "Could not find the address";
                    if (listaddress != null && listaddress.size() > 0) {
                        finaladdress = "";

                        if (listaddress.get(0).getSubThoroughfare() != null) {
                            finaladdress += listaddress.get(0).getSubThoroughfare() + ", ";
                        }
                        if (listaddress.get(0).getThoroughfare() != null) {
                            finaladdress += listaddress.get(0).getThoroughfare() + "\n ";
                        }
                        if (listaddress.get(0).getLocality() != null) {
                            finaladdress += listaddress.get(0).getLocality() + "\n ";
                        }
                        if (listaddress.get(0).getPostalCode() != null) {
                            finaladdress += listaddress.get(0).getPostalCode() + "\n ";
                        }
                        if (listaddress.get(0).getSubAdminArea() != null) {
                            finaladdress += listaddress.get(0).getSubAdminArea() + ", ";
                        }
                        if (listaddress.get(0).getAdminArea() != null) {
                            finaladdress += listaddress.get(0).getAdminArea() + "\n ";
                        }
                        if (listaddress.get(0).getCountryName() != null) {
                            finaladdress += listaddress.get(0).getCountryName();
                        }

                    }
                    address.setText("Address : " + finaladdress);

                } catch (IOException e) {

                    e.printStackTrace();
                }


                downlaod task=new downlaod();
                task.execute(api);
            }
        }

    }


    public void Refresh(View view){
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            String lat = String.valueOf(location.getLatitude());
            String lon = String.valueOf(location.getLongitude());
            String api="https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=f01d88ec3fb24ea6c9f39365387d4e1b";
            downlaod task=new downlaod();
            task.execute(api);
        }
    }
    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm;
        boolean gps_enabled = false;
        boolean network_enabled = false;

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");
        alertDialog.setMessage("Your location is off");

        alertDialog.setPositiveButton(
                "ON",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canGetLocation() == false) {
            showSettingsAlert();

        }
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


        NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();


        NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();


        if (mobile == NetworkInfo.State.CONNECTED) {

        } else if (wifi == NetworkInfo.State.CONNECTED) {

        } else {
            createNetErrorDialog();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mmap = googleMap;
        mmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {

                LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                mmap.clear();
                mmap.addMarker(new MarkerOptions().position(mylocation).title("your location"));
                mmap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));

            }
        }
        else{
            showSettingsAlert();
        }


    }
    public void myLoca(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {

                LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                mmap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
            }
        } else {
            showSettingsAlert();
        }
    }
    public void zoomout(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {

                LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                mmap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 1.0f));
            }
        } else {
            showSettingsAlert();
        }
    }



    /*weather*/



    public class downlaod extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            try {


                String result="";
                URL url=new URL(urls[0]);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                InputStream in= connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data=reader.read();
                while(data!=-1){
                    char c=(char)data;
                    result+=c;
                    data=reader.read();

                }

                return result;

            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {


                JSONObject comp = new JSONObject(s);
                JSONArray arr = new JSONArray(comp.getString("weather"));
                condtion.setText(arr.getJSONObject(0).getString("main"));
                double te = (comp.getJSONObject("main").getDouble("temp"));
                double incelcius = te-273;
                temperature.setText(String.format("%.1f",incelcius)+"\u00B0"+"C");
                humidity.setText("Humidity\n"+String.valueOf(comp.getJSONObject("main").getInt("humidity")));
                wind.setText("Wind\n"+String.valueOf(comp.getJSONObject("wind").getDouble("speed"))+" "+String.valueOf(comp.getJSONObject("wind").getDouble("deg"))+"\u00B0");


            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }
}
