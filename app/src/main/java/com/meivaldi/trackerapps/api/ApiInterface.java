package com.meivaldi.trackerapps.api;

import com.meivaldi.trackerapps.model.ApiResponse;
import com.meivaldi.trackerapps.model.MarkerResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("get_markers.php")
    Call<MarkerResponse> getAllMarker(@Field("uid") String uid);

    @FormUrlEncoded
    @POST("input_garbage.php")
    Call<ApiResponse> inputGarbage(@Field("tps_id") int tpsId);

    @FormUrlEncoded
    @POST("track_record.php")
    Call<ApiResponse> track(@Field("ve_id") String ve_id, @Field("lat") String latitude, @Field("lon") String longitude);
}
