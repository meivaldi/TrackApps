package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class CoordinateResponse {
    @SerializedName("error")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("lat")
    private String lat;

    @SerializedName("lon")
    private String lon;

    @SerializedName("koordinat")
    private String koordinat;

    public CoordinateResponse(boolean status, String message, String lat, String lon, String koordinat) {
        this.status = status;
        this.message = message;
        this.lat = lat;
        this.lon = lon;
        this.koordinat = koordinat;
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

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getKoordinat() {
        return koordinat;
    }

    public void setKoordinat(String koordinat) {
        this.koordinat = koordinat;
    }
}
