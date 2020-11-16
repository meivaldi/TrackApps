package com.meivaldi.trackerapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.api.FetchUrl;
import com.meivaldi.trackerapps.api.TaskLoadedCallback;
import com.meivaldi.trackerapps.model.Coordinate;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.TPA;
import com.meivaldi.trackerapps.model.TrackResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailMapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private ProgressDialog pDialog;
    private ApiInterface apiService;
    private List<TPA> tpaList = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Coordinate> tracks = new ArrayList<>();
    private LatLng loc;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Histori Perjalanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pDialog = new ProgressDialog(DetailMapsActivity.this);
        pDialog.setMessage("Memuat...");
        pDialog.setCancelable(false);
        pDialog.show();
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

        mMap.addMarker(new MarkerOptions()
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
                } else {
                    Log.d("DATA", new Gson().toJson(res));
                    Toast.makeText(DetailMapsActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                }

                pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<MarkerResponse> call, Throwable t) {
                Toast.makeText(DetailMapsActivity.this, "Koneksi error!", Toast.LENGTH_SHORT).show();
                Log.e("DATA", t.getMessage());
                pDialog.dismiss();
            }
        });

        Call<TrackResponse> call2 = apiService.getTracks(ve_id);
        call2.enqueue(new Callback<TrackResponse>() {
            @Override
            public void onResponse(Call<TrackResponse> call, Response<TrackResponse> response) {
                tracks = response.body().getTracks();

                if (tracks.isEmpty()) {
                    Toast.makeText(DetailMapsActivity.this, "Belum ada riwayat perjalanan hari ini!", Toast.LENGTH_SHORT).show();
                    return;
                }

                LatLng currentPath = loc;
                LatLng pick;
                for (Coordinate coordinate: tracks) {
                    pick = new LatLng(coordinate.getLat(), coordinate.getLon());
                    new FetchUrl(DetailMapsActivity.this).execute(getUrl(currentPath, pick, "driving"), "driving");
                    currentPath = pick;
                }
            }

            @Override
            public void onFailure(Call<TrackResponse> call, Throwable t) {
                Toast.makeText(DetailMapsActivity.this, "Koneksi error!", Toast.LENGTH_SHORT).show();
                Log.e("DATA", t.getMessage());
            }
        });

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.api_key);

        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}