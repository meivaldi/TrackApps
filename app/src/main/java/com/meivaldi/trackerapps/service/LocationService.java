package com.meivaldi.trackerapps.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.ApiResponse;
import com.meivaldi.trackerapps.receiver.RestartBackgroundService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {

    String veId = "";

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        veId = intent.getStringExtra("ve_id");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        requestLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadCast = new Intent();
        broadCast.setAction("restartService");
        broadCast.setClass(this, RestartBackgroundService.class);
        this.sendBroadcast(broadCast);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    Log.d("LOCATION", location.getLatitude() + ", " + location.getLongitude());
                    if (location != null) {
                        Log.d("DATA", location.getLatitude() + " " + location.getLongitude());
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                        Call<ApiResponse> call = apiService.track(veId, String.valueOf(loc.latitude), String.valueOf(loc.longitude));
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
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(locationRequest, callback, null);
    }

}
