package com.meivaldi.trackerapps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.Coordinate;
import com.meivaldi.trackerapps.model.CoordinateResponse;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.TPA;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ProgressDialog pDialog;
    private ApiInterface apiService;
    private List<TPA> tpaList = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Coordinate> tracks = new ArrayList<>();
    private LatLng loc;
    private Marker marker;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_track);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tracking Kendaraan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (ActivityCompat.checkSelfPermission(DetailTrackActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(DetailTrackActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DetailTrackActivity.this, permissionsRequired[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(DetailTrackActivity.this, permissionsRequired[1])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailTrackActivity.this);
                builder.setTitle("Perizian Aplikasi");
                builder.setMessage("Aplikasi ini membutuhkan lokasi Anda!");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(DetailTrackActivity.this,
                                permissionsRequired,
                                PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(DetailTrackActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pDialog = new ProgressDialog(DetailTrackActivity.this);
        pDialog.setMessage("Memuat...");
        pDialog.setCancelable(false);
        pDialog.show();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                String ve_id = getIntent().getStringExtra("ve_id");

                apiService = ApiClient.getClient().create(ApiInterface.class);
                Call<CoordinateResponse> call = apiService.trackVehicle(ve_id);

                call.enqueue(new Callback<CoordinateResponse>() {
                    @Override
                    public void onResponse(Call<CoordinateResponse> call, Response<CoordinateResponse> response) {
                        CoordinateResponse res = response.body();

                        Log.d("TES", res.getLat() + ", " + res.getLon());
                        if (marker == null) return;

                            MarkerAnimation.animateMarkerToGB(marker, new LatLng(Double.parseDouble(res.getLat()),
                                    Double.parseDouble(res.getLon())), new LatLngInterpolator.Spherical());
                    }

                    @Override
                    public void onFailure(Call<CoordinateResponse> call, Throwable t) {

                    }
                });
            }
        };
        LocationServices.getFusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(locationRequest, callback, null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(true);

        String latitude = getIntent().getStringExtra("lat");
        String longitude = getIntent().getStringExtra("lon");
        String iconRef = getIntent().getStringExtra("icon");
        String ve_id = getIntent().getStringExtra("ve_id");

        loc = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        int icon = 0;

        if (iconRef.equals("1")) {
            icon = R.drawable.truck;
        } else if (iconRef.equals("2")) {
            icon = R.drawable.truck2;
        } else if (iconRef.equals("3")) {
            icon = R.drawable.truck3;
        } else if (iconRef.equals("4")) {
            icon = R.drawable.truck4;
        } else if (iconRef.equals("5")) {
            icon = R.drawable.truck5;
        } else if (iconRef.equals("6")) {
            icon = R.drawable.truck6;
        } else if (iconRef.equals("7")) {
            icon = R.drawable.truck7;
        } else if (iconRef.equals("8")) {
            icon = R.drawable.truck8;
        }

        marker = mMap.addMarker(new MarkerOptions()
                .position(loc)
                .title("Starter Point")
                .icon(BitmapDescriptorFactory.fromResource(icon)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));

        apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MarkerResponse> call = apiService.getAllMarker(ve_id);
        call.enqueue(new Callback<MarkerResponse>() {
            @Override
            public void onResponse(Call<MarkerResponse> call, Response<MarkerResponse> response) {
                MarkerResponse res = response.body();

                if (!res.isStatus()) {
                    tpaList.addAll(res.getTpaList());

                    for (TPA tpa: tpaList) {
                        Log.d("DATA", tpa.getLatitude() + " " + tpa.getLongitude());
                        int icon;

                        if (tpa.isInput()) {
                            icon = R.drawable.bin;
                        } else {
                            icon = R.drawable.bin_not;
                        }

                        LatLng pick = new LatLng(Float.parseFloat(tpa.getLatitude()), Float.parseFloat(tpa.getLongitude()));
                        Marker pickMarker = mMap.addMarker(new MarkerOptions()
                                .position(pick)
                                .title(tpa.getNama())
                                .icon(BitmapDescriptorFactory.fromResource(icon)));

                        markers.add(pickMarker);
                    }
                } else {
                    Log.d("DATA", new Gson().toJson(res));
                    Toast.makeText(DetailTrackActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                }

                pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<MarkerResponse> call, Throwable t) {
                Toast.makeText(DetailTrackActivity.this, "Koneksi error!", Toast.LENGTH_SHORT).show();
                Log.e("DATA", t.getMessage());
                pDialog.dismiss();
            }
        });
    }
}