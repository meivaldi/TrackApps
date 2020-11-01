package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class TPA {

    @SerializedName("nama")
    private String nama;

    @SerializedName("lat")
    private String latitude;

    @SerializedName("lon")
    private String longitude;

    public TPA(String nama, String latitude, String longitude) {
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
