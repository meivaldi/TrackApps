package com.meivaldi.trackerapps.api;

import com.meivaldi.trackerapps.model.ApiResponse;
import com.meivaldi.trackerapps.model.CoordinateResponse;
import com.meivaldi.trackerapps.model.DashboardResponse;
import com.meivaldi.trackerapps.model.MarkerResponse;
import com.meivaldi.trackerapps.model.RouteResponse;
import com.meivaldi.trackerapps.model.TrackResponse;
import com.meivaldi.trackerapps.model.UserResponse;
import com.meivaldi.trackerapps.model.VehicleResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
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

    @FormUrlEncoded
    @POST("input_tpa.php")
    Call<ApiResponse> inputTpa(@Field("ve_id") String ve_id, @Field("jumlah") String jumlah, @Field("satuan") String satuan, @Field("nilai") String nilai);

    @POST("get_all_vehicle.php")
    Call<VehicleResponse> getAllVehicle();

    @FormUrlEncoded
    @POST("get_tracks.php")
    Call<TrackResponse> getTracks(@Field("ve_id") String ve_id);

    @FormUrlEncoded
    @POST("track_vehicle.php")
    Call<CoordinateResponse> trackVehicle(@Field("ve_id") String ve_id);

    @GET("dashboard.php")
    Call<DashboardResponse> getDashboard();

    @FormUrlEncoded
    @POST("get_routes.php")
    Call<RouteResponse> getRoutes(@Field("ve_id") String ve_id);
}
