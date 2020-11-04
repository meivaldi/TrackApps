package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VehicleResponse {
    @SerializedName("error")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("vehicles")
    private List<Vehicle> vehicleList;

    public VehicleResponse(boolean status, String message, List<Vehicle> vehicleList) {
        this.status = status;
        this.message = message;
        this.vehicleList = vehicleList;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }
}
