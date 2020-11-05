package com.meivaldi.trackerapps;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.ApiResponse;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.TPA;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressDialog pDialog;

    private List<TPA> tpaList = new ArrayList<>();

    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final double MIN_DISTANCE = 0.02;

    private Marker marker;
    private List<Marker> markers = new ArrayList<>();
    private Button input, inputBtn;
    private Dialog inputDialog;
    private ApiInterface apiService;
    private LatLng currentLoc;
    private int tpsId = 0, position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);

        apiService = ApiClient.getClient().create(ApiInterface.class);
        input = findViewById(R.id.input);

        pDialog = new ProgressDialog(MapsActivity.this);
        pDialog.setMessage("Memuat...");
        pDialog.setCancelable(false);
        pDialog.show();

        inputDialog = new Dialog(MapsActivity.this);
        inputDialog.setCancelable(false);
        inputDialog.setContentView(R.layout.input_dialog);
        inputDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        inputBtn = inputDialog.findViewById(R.id.inputBtn);

        inputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.setMessage("Memroses...");
                pDialog.show();

                Call<ApiResponse> call = apiService.inputGarbage(tpsId);
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        ApiResponse res = response.body();

                        if (!res.isStatus()) {
                            markers.get(position).remove();

                            TPA tpa = tpaList.get(position);
                            LatLng loc = new LatLng(Double.parseDouble(tpa.getLatitude()),
                                    Double.parseDouble(tpa.getLongitude()));

                            mMap.addMarker(new MarkerOptions()
                                    .position(loc)
                                    .title(tpa.getNama())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bin)));

                            input.setVisibility(View.GONE);
                            tpaList.get(position).setInput(true);
                        }

                        Toast.makeText(MapsActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                        inputDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toast.makeText(MapsActivity.this, "Gagal menginput data!", Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                    }
                });
            }
        });

        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputDialog.show();
            }
        });

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, permissionsRequired[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, permissionsRequired[1])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Perizian Aplikasi");
                builder.setMessage("Aplikasi ini membutuhkan lokasi Anda!");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MapsActivity.this,
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
                ActivityCompat.requestPermissions(MapsActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(100000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("DATA", location.getLatitude() + " " + location.getLongitude());
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                        currentLoc = loc;
                        if (marker != null) marker.remove();
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(loc)
                                .title("Starter Point")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));

                        int i = 0;
                        for (TPA tpa: tpaList) {
                            double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                                    Double.parseDouble(tpa.getLongitude())), loc);

                            if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                                Log.d("DATA", "Position: " + position);
                                Log.d("DATA", "id: " + tpsId);

                                input.setVisibility(View.VISIBLE);
                                tpsId = tpa.getId();
                                position = i;

                                break;
                            } else {
                                input.setVisibility(View.GONE);
                            }

                            i++;
                            Log.d("DATA", "Distance: " + distance);
                        }
                    }
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(locationRequest, mLocationCallback, null);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton location = findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked");

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
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

                            int i = 0;
                            for (TPA tpa: tpaList) {
                                double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                                        Double.parseDouble(tpa.getLongitude())), loc);

                                if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                                    Log.d("DATA", "Position: " + position);
                                    Log.d("DATA", "id: " + tpsId);

                                    input.setVisibility(View.VISIBLE);
                                    tpsId = tpa.getId();
                                    position = i;
                                    break;
                                } else {
                                    input.setVisibility(View.GONE);
                                }

                                i++;
                                Log.d("DATA", "Distance: " + distance);
                            }

                        } else {
                            Toast.makeText(MapsActivity.this, "Lokasi tidak ada!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        FloatingActionButton up = findViewById(R.id.up);
        FloatingActionButton down = findViewById(R.id.down);
        FloatingActionButton left = findViewById(R.id.left);
        FloatingActionButton right = findViewById(R.id.right);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingUp();
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingDown();
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingLeft();
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingRight();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(true);

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .title("Starter Point")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));

                    int i = 0;
                    for (TPA tpa: tpaList) {
                        double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                                Double.parseDouble(tpa.getLongitude())), loc);

                        if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                            Log.d("DATA", "Position: " + position);
                            Log.d("DATA", "id: " + tpsId);

                            input.setVisibility(View.VISIBLE);
                            tpsId = tpa.getId();
                            position = i;

                            break;
                        } else {
                            input.setVisibility(View.GONE);
                        }

                        i++;
                        Log.d("DATA", "Distance: " + distance);
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Lokasi tidak ada!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MarkerResponse> call = apiService.getAllMarker("1");
        call.enqueue(new Callback<MarkerResponse>() {
            @Override
            public void onResponse(Call<MarkerResponse> call, Response<MarkerResponse> response) {
                MarkerResponse res = response.body();

                if (!res.isStatus()) {
                    Log.d("DATA", new Gson().toJson(res));
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

                    int i=0;
                    for (TPA tpa: tpaList) {
                        double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                                Double.parseDouble(tpa.getLongitude())), marker.getPosition());

                        if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                            Log.d("DATA", "Position: " + position);
                            Log.d("DATA", "id: " + tpsId);

                            input.setVisibility(View.VISIBLE);
                            tpsId = tpa.getId();
                            position = i;

                            break;
                        } else {
                            input.setVisibility(View.GONE);
                        }

                        i++;
                    }
                } else {
                    Log.d("DATA", new Gson().toJson(res));
                    Toast.makeText(MapsActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                }

                pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<MarkerResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Koneksi error!", Toast.LENGTH_SHORT).show();
                Log.e("DATA", t.getMessage());
                pDialog.dismiss();
            }
        });
    }

    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.parseInt(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void goingRight() {
        int Radius = 6371;

        Double new_longitude = currentLoc.longitude + (0.01 / Radius) * (180 / Math.PI) / Math.cos(currentLoc.latitude * (Math.PI/180));
        LatLng newPosition = new LatLng(currentLoc.latitude, new_longitude);
        currentLoc = newPosition;

        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .position(newPosition)
                .title("Starter Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

        int i=0;
        for (TPA tpa: tpaList) {
            double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                    Double.parseDouble(tpa.getLongitude())), marker.getPosition());

            if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                Log.d("DATA", "Position: " + position);
                Log.d("DATA", "id: " + tpsId);

                input.setVisibility(View.VISIBLE);
                tpsId = tpa.getId();
                position = i;

                break;
            } else {
                input.setVisibility(View.GONE);
            }

            i++;
        }
    }

    private void goingLeft() {
        int Radius = 6371;

        Double new_longitude = currentLoc.longitude - (0.01 / Radius) * (180 / Math.PI) / Math.cos(currentLoc.latitude * (Math.PI/180));
        LatLng newPosition = new LatLng(currentLoc.latitude, new_longitude);
        currentLoc = newPosition;

        apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ApiResponse> call = apiService.track("1", String.valueOf(newPosition.latitude), String.valueOf(newPosition.longitude));
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

        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .position(newPosition)
                .title("Starter Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

        int i=0;
        for (TPA tpa: tpaList) {
            double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                    Double.parseDouble(tpa.getLongitude())), marker.getPosition());

            if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                Log.d("DATA", "Position: " + position);
                Log.d("DATA", "id: " + tpsId);

                input.setVisibility(View.VISIBLE);
                tpsId = tpa.getId();
                position = i;

                break;
            } else {
                input.setVisibility(View.GONE);
            }

            i++;
        }
    }

    private void goingUp() {
        int Radius = 6371;

        Double new_latitude  = currentLoc.latitude  + (0.01 / Radius) * (180 / Math.PI);
        LatLng newPosition = new LatLng(new_latitude, currentLoc.longitude);
        currentLoc = newPosition;

        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .position(newPosition)
                .title("Starter Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

        int i=0;
        for (TPA tpa: tpaList) {
            double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                    Double.parseDouble(tpa.getLongitude())), marker.getPosition());

            if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                Log.d("DATA", "Position: " + position);
                Log.d("DATA", "id: " + tpsId);

                input.setVisibility(View.VISIBLE);
                tpsId = tpa.getId();
                position = i;

                break;
            } else {
                input.setVisibility(View.GONE);
            }

            i++;
        }
    }

    private void goingDown() {
        int Radius = 6371;

        Double new_latitude  = currentLoc.latitude  - (0.01 / Radius) * (180 / Math.PI);
        LatLng newPosition = new LatLng(new_latitude, currentLoc.longitude);
        currentLoc = newPosition;

        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .position(newPosition)
                .title("Starter Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

        int i=0;
        for (TPA tpa: tpaList) {
            double distance = calculationByDistance(new LatLng(Double.parseDouble(tpa.getLatitude()),
                    Double.parseDouble(tpa.getLongitude())), marker.getPosition());

            if (distance <= MIN_DISTANCE && !tpa.isInput()) {
                Log.d("DATA", "Position: " + position);
                Log.d("DATA", "id: " + tpsId);

                input.setVisibility(View.VISIBLE);
                tpsId = tpa.getId();
                position = i;

                break;
            } else {
                input.setVisibility(View.GONE);
            }

            i++;
        }
    }

}