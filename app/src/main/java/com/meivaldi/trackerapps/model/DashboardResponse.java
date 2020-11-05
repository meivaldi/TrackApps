package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DashboardResponse {
    @SerializedName("total")
    private String total;

    @SerializedName("vehicles")
    private List<Vehicle> vehicleList;

    public DashboardResponse(String total, List<Vehicle> vehicleList) {
        this.total = total;
        this.vehicleList = vehicleList;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }
}
