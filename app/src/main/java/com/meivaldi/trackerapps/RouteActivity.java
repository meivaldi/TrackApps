package com.meivaldi.trackerapps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.ApiResponse;
import com.meivaldi.trackerapps.model.Coordinate;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.RouteResponse;
import com.meivaldi.trackerapps.model.TPA;
import com.meivaldi.trackerapps.service.LocationService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    private ProgressDialog pDialog;
    private Dialog inputDialog;
    private EditText jumlahET, bbmRp, bbmLiter;
    private AppCompatSpinner satuanSp;
    private List<String> satuan;

    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private String ve_id = "";

    private FloatingActionButton start, stop;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private Boolean mRequestingLocationUpdates = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Strada");

        toolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        SharedPreferences preferences = getSharedPreferences("akun", MODE_PRIVATE);
        ve_id = preferences.getString("ve_id", "");

        inputDialog = new Dialog(RouteActivity.this);
        inputDialog.setCancelable(false);
        inputDialog.setContentView(R.layout.input_dialog);
        inputDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button inputTPA = inputDialog.findViewById(R.id.inputTPA);
        jumlahET = inputDialog.findViewById(R.id.jumlahET);
        bbmRp = inputDialog.findViewById(R.id.bbmRp);
        bbmLiter = inputDialog.findViewById(R.id.bbmLiter);
        satuanSp = inputDialog.findViewById(R.id.satuanSP);

        satuan = new ArrayList<>();
        satuan.add("Rupiah");
        satuan.add("Liter");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, satuan);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        satuanSp.setAdapter(dataAdapter);
        satuanSp.setSelection(0);

        satuanSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    bbmLiter.setVisibility(View.GONE);
                    bbmRp.setVisibility(View.VISIBLE);
                } else {
                    bbmLiter.setVisibility(View.VISIBLE);
                    bbmRp.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        inputTPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jumlah = jumlahET.getText().toString();
                String satuanText = String.valueOf(satuanSp.getSelectedItemPosition());
                String nilai = "";

                if (satuanText == "0") {
                    if (bbmRp.getText().toString().isEmpty()) {
                        Toast.makeText(RouteActivity.this, "Harap masukkan jumlah uang yang dikeluarkan untuk BBM!", Toast.LENGTH_SHORT).show();
                    } else {
                        nilai = bbmRp.getText().toString();
                    }
                } else {
                    if (bbmLiter.getText().toString().isEmpty()) {
                        Toast.makeText(RouteActivity.this, "Harap masukkan berapa liter yang dikeluarkan untuk BBM!", Toast.LENGTH_SHORT).show();
                    } else {
                        nilai = bbmLiter.getText().toString();
                    }
                }

                if (jumlah.isEmpty()) {
                    Toast.makeText(RouteActivity.this, "Harap masukkan jumlah!", Toast.LENGTH_SHORT).show();
                } else {
                    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                    Call<ApiResponse> call = apiService.inputTpa(ve_id, jumlah, satuanText, nilai);
                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            ApiResponse res = response.body();
                            Toast.makeText(RouteActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            inputDialog.dismiss();
                            bbmLiter.setText("");
                            bbmRp.setText("");
                            jumlahET.setText("");
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(RouteActivity.this, "Koneksi Error!", Toast.LENGTH_SHORT).show();
                            inputDialog.dismiss();
                        }
                    });
                }

                inputDialog.dismiss();
            }
        });

        if (ActivityCompat.checkSelfPermission(RouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RouteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RouteActivity.this, permissionsRequired[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(RouteActivity.this, permissionsRequired[1])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
                builder.setTitle("Perizian Aplikasi");
                builder.setMessage("Aplikasi ini membutuhkan lokasi Anda!");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(RouteActivity.this,
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
                ActivityCompat.requestPermissions(RouteActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
        }

        pDialog = new ProgressDialog(RouteActivity.this);
        pDialog.setMessage("Memuat...");
        pDialog.setCancelable(false);
        pDialog.show();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();

                updateLocationUI();
            }
        };

        boolean status = preferences.getBoolean("request_update", false);
        mRequestingLocationUpdates = status;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationButtonClick();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationButtonClick();
            }
        });
    }

    private void logout() {
        SharedPreferences pref = getSharedPreferences("akun", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("isLogin", false);
        editor.putString("ve_id", "");
        editor.putString("name", "");
        editor.putString("tipe", "");
        editor.putString("jenis", "");

        editor.apply();

        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }

        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .title("Starter Point")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));

            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ApiResponse> call = apiService.track(ve_id, String.valueOf(loc.latitude), String.valueOf(loc.longitude));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    Log.d("DATA", response.body().getMessage());
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("DATA", "ERROR: " + t.getMessage());
                }
            });
        }

        toggleButtons();
    }

    private void toggleButtons() {
        if (mRequestingLocationUpdates) {
            stop.setVisibility(View.VISIBLE);
            start.setVisibility(View.GONE);
        } else {
            start.setVisibility(View.VISIBLE);
            stop.setVisibility(View.GONE);
        }
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("STRADA", "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Tracking Aktif!", Toast.LENGTH_SHORT).show();

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("STRADA", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(RouteActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("STRADA", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("STRADA", errorMessage);

                                Toast.makeText(RouteActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        SharedPreferences.Editor editor = getSharedPreferences("akun", MODE_PRIVATE).edit();
                        editor.putBoolean("request_update", true);
                        editor.apply();

                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public void stopLocationButtonClick() {
        mRequestingLocationUpdates = false;
        SharedPreferences.Editor editor = getSharedPreferences("akun", MODE_PRIVATE).edit();
        editor.putBoolean("request_update", false);
        editor.apply();
        stopLocationUpdates();
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Tracking Berhenti!", Toast.LENGTH_SHORT).show();
                        toggleButtons();
                    }
                });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(RouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RouteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);

        mFusedLocationClient.getLastLocation().addOnSuccessListener(RouteActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    if (marker != null) marker.remove();
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .title("Starter Point")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));
                }
            }
        });

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RouteResponse> call = apiService.getRoutes(ve_id);
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                RouteResponse res = response.body();
                if (!res.isError()) {
                    List<Coordinate> coordinates = res.getCoordinates();
                    List<LatLng> latLng = new ArrayList<>();
                    for (Coordinate coordinate: coordinates) {
                        latLng.add(new LatLng(coordinate.getLat(), coordinate.getLon()));
                    }
                    Log.d("RUTE", new Gson().toJson(latLng));

                    PolylineOptions lineOptions = new PolylineOptions();
                    lineOptions.width(5);
                    lineOptions.color(Color.rgb(42, 125, 50));
                    lineOptions.addAll(latLng);

                    mMap.addPolyline(lineOptions);
                } else {
                    Toast.makeText(RouteActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                }

                pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(RouteActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e("STRADA", "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("STRADA", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        SharedPreferences.Editor editor = getSharedPreferences("akun", MODE_PRIVATE).edit();
                        editor.putBoolean("request_update", false);
                        editor.apply();
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.input_tpa) {
            inputDialog.show();
        } else if (id == R.id.logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        updateLocationUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
    }
}