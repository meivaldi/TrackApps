package com.meivaldi.trackerapps.ui.trace;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meivaldi.trackerapps.DetailMapsActivity;
import com.meivaldi.trackerapps.R;
import com.meivaldi.trackerapps.RecycleTouchListener;
import com.meivaldi.trackerapps.VehicleAdapter;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.Vehicle;
import com.meivaldi.trackerapps.model.VehicleResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TraceFragment extends Fragment {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList = new ArrayList<>();
    private ProgressDialog pDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trace, container, false);

        vehicleAdapter = new VehicleAdapter(getContext(), vehicleList);
        recyclerView = root.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(vehicleAdapter);

        recyclerView.addOnItemTouchListener(new RecycleTouchListener(getContext(), recyclerView, new RecycleTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Vehicle vehicle = vehicleList.get(position);

                Intent intent = new Intent(getContext(), DetailMapsActivity.class);
                intent.putExtra("lat", vehicle.getStartLatitude());
                intent.putExtra("lon", vehicle.getStartLongitude());
                intent.putExtra("icon", vehicle.getIcon());
                intent.putExtra("ve_id", vehicle.getVeId());
                getContext().startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Memuat...");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<VehicleResponse> call = apiService.getAllVehicle();

        call.enqueue(new Callback<VehicleResponse>() {
            @Override
            public void onResponse(Call<VehicleResponse> call, Response<VehicleResponse> response) {
                VehicleResponse res = response.body();

                vehicleList.addAll(res.getVehicleList());
                vehicleAdapter.notifyDataSetChanged();
                pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<VehicleResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        });

        return root;
    }
}