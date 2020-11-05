package com.meivaldi.trackerapps.ui.home;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.meivaldi.trackerapps.R;
import com.meivaldi.trackerapps.VehicleAdapter;
import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.DashboardResponse;
import com.meivaldi.trackerapps.model.Vehicle;
import com.meivaldi.trackerapps.model.VehicleResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList = new ArrayList<>();
    private ProgressDialog pDialog;
    private TextView jumlahMasuk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        jumlahMasuk = root.findViewById(R.id.totalMasuk);
        vehicleAdapter = new VehicleAdapter(getContext(), vehicleList);
        recyclerView = root.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(vehicleAdapter);

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Memuat...");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DashboardResponse> call = apiService.getDashboard();

        call.enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                DashboardResponse res = response.body();

                jumlahMasuk.setText(res.getTotal() + " Ton");
                vehicleList.addAll(res.getVehicleList());
                vehicleAdapter.notifyDataSetChanged();
                pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal memuat data!", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        });

        return root;
    }
}