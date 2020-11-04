package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class Vehicle {

    @SerializedName("ve_id")
    private String veId;

    @SerializedName("nama")
    private String nama;

    @SerializedName("icon")
    private String icon;

    @SerializedName("lat")
    private String startLatitude;

    @SerializedName("lon")
    private String startLongitude;

    public Vehicle(String veId, String nama, String icon, String startLatitude, String startLongitude) {
        this.veId = veId;
        this.nama = nama;
        this.icon = icon;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
    }

    public String getVeId() {
        return veId;
    }

    public void setVeId(String veId) {
        this.veId = veId;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(String startLatitude) {
        this.startLatitude = startLatitude;
    }

    public String getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(String startLongitude) {
        this.startLongitude = startLongitude;
    }
}
