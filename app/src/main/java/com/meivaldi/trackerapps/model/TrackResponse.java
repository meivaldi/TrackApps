package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TrackResponse {
    @SerializedName("error")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("tracks")
    private List<Coordinate> tracks;

    public TrackResponse(boolean status, String message, List<Coordinate> tracks) {
        this.status = status;
        this.message = message;
        this.tracks = tracks;
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

    public List<Coordinate> getTracks() {
        return tracks;
    }

    public void setTracks(List<Coordinate> tracks) {
        this.tracks = tracks;
    }
}
