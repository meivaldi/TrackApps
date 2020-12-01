package com.meivaldi.trackerapps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.meivaldi.trackerapps.api.ApiClient;
import com.meivaldi.trackerapps.api.ApiInterface;
import com.meivaldi.trackerapps.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private EditText usernameET, passwordET;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Proses...");
        pDialog.setCancelable(false);

        login = findViewById(R.id.login);
        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);

        SharedPreferences preferences = getSharedPreferences("akun", MODE_PRIVATE);
        boolean isLogin = preferences.getBoolean("isLogin", false);
        String tipe = preferences.getString("tipe", "");
        String jenis = preferences.getString("jenis", "");

        if (isLogin) {
            if (tipe.equals("0")) {
                startActivity(new Intent(getApplicationContext(), AdminActivity.class));
            } else {
                if (jenis.equals("0")) {
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), RouteActivity.class));
                }
            }
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Username atau Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();

                    pDialog.dismiss();
                } else {
                    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                    Call<UserResponse> call = apiService.login(username, password);

                    call.enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                            UserResponse res = response.body();
                            if (!res.isStatus()) {
                                SharedPreferences pref = getSharedPreferences("akun", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                String tipe = res.getTipe();
                                String jenis = res.getJenis();

                                editor.putBoolean("isLogin", true);
                                editor.putString("ve_id", res.getVehicleId());
                                editor.putString("name", res.getName());
                                editor.putString("lat", res.getLat());
                                editor.putString("lng", res.getLng());
                                editor.putString("tipe", tipe);
                                editor.putString("jenis", jenis);

                                editor.apply();
                                if (tipe.equals("0")) {
                                    startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                                } else {
                                    if (jenis.equals("0")) {
                                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                                    } else {
                                        startActivity(new Intent(getApplicationContext(), RouteActivity.class));
                                    }
                                }
                            } else {
                                usernameET.setText("");
                                passwordET.setText("");
                                Toast.makeText(getApplicationContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            pDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Login Gagal!", Toast.LENGTH_SHORT).show();
                            pDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}