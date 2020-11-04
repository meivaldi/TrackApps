package com.meivaldi.trackerapps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meivaldi.trackerapps.model.Vehicle;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.MyViewHolder> {
    private Context context;
    private List<Vehicle> vehicleList;

    public VehicleAdapter(Context context, List<Vehicle> vehicleList) {
        this.context = context;
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleAdapter.MyViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);

        int resource = 0;
        if (vehicle.getIcon().equals("1")) {
            resource = R.drawable.truck_lg;
        } else if (vehicle.getIcon().equals("2")) {
            resource = R.drawable.truck2_lg;
        }

        holder.namaTV.setText(vehicle.getNama());
        holder.icon.setImageResource(resource);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView namaTV;
        ImageView icon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            namaTV = itemView.findViewById(R.id.name);
            icon = itemView.findViewById(R.id.image);
        }
    }
}
