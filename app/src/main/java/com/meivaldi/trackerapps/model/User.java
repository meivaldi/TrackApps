package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("name")
    private String name;

    @SerializedName("vehicle_id")
    private String vehicleId;

    public User(String name, String vehicleId) {
        this.name = name;
        this.vehicleId = vehicleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}
