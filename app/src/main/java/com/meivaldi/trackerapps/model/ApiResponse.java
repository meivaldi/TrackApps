package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("error")
    private boolean status;

    @SerializedName("message")
    private String message;

    public ApiResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
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
}
