package com.meivaldi.trackerapps.api;

import com.meivaldi.trackerapps.model.ApiResponse;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("get_markers.php")
    Call<MarkerResponse> getAllMarker(@Field("vehicle_id") String uid);

    @FormUrlEncoded
    @POST("input_garbage.php")
    Call<ApiResponse> inputGarbage(@Field("tps_id") int tpsId);

    @FormUrlEncoded
    @POST("login.php")
    Call<UserResponse> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("track_record.php")
    Call<ApiResponse> track(@Field("ve_id") String ve_id, @Field("lat") String latitude, @Field("lon") String longitude);
}
