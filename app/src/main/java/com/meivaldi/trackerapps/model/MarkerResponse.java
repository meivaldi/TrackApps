package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MarkerResponse {

    @SerializedName("error")
    private boolean status;

    @SerializedName("error_msg")
    private String message;

    @SerializedName("markers")
    private List<TPA> tpaList;

    public MarkerResponse(boolean status, String message, List<TPA> tpaList) {
        this.status = status;
        this.message = message;
        this.tpaList = tpaList;
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

    public List<TPA> getTpaList() {
        return tpaList;
    }

    public void setTpaList(List<TPA> tpaList) {
        this.tpaList = tpaList;
    }
}
