package com.meivaldi.trackerapps.model;

import com.google.gson.annotations.SerializedName;

public class Coordinate {
    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("html_instructions")
    private String instructions;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getInstructions() {
        return instructions;
    }
}
