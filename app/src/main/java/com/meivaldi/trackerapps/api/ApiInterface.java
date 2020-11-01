package com.meivaldi.trackerapps.api;

import com.meivaldi.trackerapps.model.MarkerResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("get_markers.php")
    Call<MarkerResponse> getAllMarker(@Field("uid") String uid);

}
