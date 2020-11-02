package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("error")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("name")
    private String name;

    @SerializedName("ve_id")
    private String vehicleId;

    public UserResponse(boolean status, String message, String name, String vehicleId) {
        this.status = status;
        this.message = message;
        this.name = name;
        this.vehicleId = vehicleId;
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
