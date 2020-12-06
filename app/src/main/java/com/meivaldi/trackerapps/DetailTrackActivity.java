package com.meivaldi.trackerapps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.api.FetchUrl;
import com.meivaldi.trackerapps.api.GetCoordinates;
import com.meivaldi.trackerapps.api.TaskLoadedCallback;
import com.meivaldi.trackerapps.model.Coordinate;
import com.meivaldi.trackerapps.model.CoordinateResponse;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.RouteResponse;
import com.meivaldi.trackerapps.model.TPA;
import com.meivaldi.trackerapps.service.LocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTrackActivity extends AppCompatActivity implements OnMapReadyCallback, Runnable {

    private GoogleMap mMap;
    private ProgressDialog pDialog;
    private ApiInterface apiService;
    private List<TPA> tpaList = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Coordinate> routes = new ArrayList<>();
    private LatLng loc;
    private Marker marker;
    private Handler handler;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    int counter = 0;
    boolean running = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback callback;

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
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                String ve_id = getIntent().getStringExtra("ve_id");

                apiService = ApiClient.getClient().create(ApiInterface.class);
                Call<RouteResponse> call = apiService.tracking(ve_id);

                call.enqueue(new Callback<RouteResponse>() {
                    @Override
                    public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                        RouteResponse res = response.body();

                        if (!res.isError() && !running) {
                            routes.addAll(res.getCoordinates());

                            handler.post(DetailTrackActivity.this);
                            running = true;
                        }
                    }

                    @Override
                    public void onFailure(Call<RouteResponse> call, Throwable t) {
                        Toast.makeText(DetailTrackActivity.this, "Koneksi Error!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mFusedLocationClient.requestLocationUpdates(locationRequest, callback, null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(true);

        final String iconRef = getIntent().getStringExtra("icon");
        String ve_id = getIntent().getStringExtra("ve_id");

        apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CoordinateResponse> apiCall = apiService.trackVehicle(ve_id);

        apiCall.enqueue(new Callback<CoordinateResponse>() {
            @Override
            public void onResponse(Call<CoordinateResponse> call, Response<CoordinateResponse> response) {
                CoordinateResponse res = response.body();

                loc = new LatLng(Double.parseDouble(res.getLat()), Double.parseDouble(res.getLon()));

                marker = mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title("Starter Point")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));
            }

            @Override
            public void onFailure(Call<CoordinateResponse> call, Throwable t) {

            }
        });

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

        handler = new Handler();
    }

    @Override
    public void run() {
        if (counter < routes.size() && running) {
            Log.d("mylog", "Poin ke: " + counter + ", routes: " + routes.size());
            Coordinate coordinate = routes.get(counter++);
            LatLng point = new LatLng(coordinate.getLat(), coordinate.getLon());

            MarkerAnimation.animateMarkerToGB(marker, new LatLng(point.latitude,
                    point.longitude), new LatLngInterpolator.Spherical());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18f));

            handler.postDelayed(this, 2000);
        } else {
            counter = 0;
            routes.clear();
            running = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        running = false;
        routes.clear();
        counter = 0;
        mFusedLocationClient.removeLocationUpdates(callback);
        Log.d("mylog", "Stopped");

        finish();
    }
}