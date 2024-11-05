package com.oneleft.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oneleft.app.R;
import com.oneleft.app.utils.ApiConfig;
import com.oneleft.app.utils.App;
import com.oneleft.app.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OtpActivity extends AppCompatActivity {

    private EditText etOtp1;
    private EditText etOtp2;
    private EditText etOtp3;
    private EditText etOtp4;
    private TextView tvResend;
    private String email;

    private Button btnNext;
    private ProgressDialog progressDialog;

    //startActivity(new Intent(OtpActivity.this, ProfileCompleteActivity.class));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        btnNext = findViewById(R.id.btnNext);
        tvResend = findViewById(R.id.tvResend);

        email = getIntent().getStringExtra(Constants.INTENT_EMAIL);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");

        etOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etOtp2.requestFocus();
                }
            }
        });
        etOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etOtp3.requestFocus();
                }
            }
        });
        etOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etOtp4.requestFocus();
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getIntent().getStringExtra(Constants.INTENT_EMAIL);
                String otp = getOtp();
                if (otp.isEmpty()) {
                    return;
                }
                StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.VERIFY_OTP_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i("mytag", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int status = jsonResponse.getInt("status");
                            if (status == 1) {
                                String purpose = getIntent().getStringExtra("purpose");
                                if (purpose != null && purpose.equals("password_reset")) {
                                    startActivity(new Intent(OtpActivity.this, ProfileCompleteActivity.class));
                                    finish();
                                } else {
                                    startActivity(new Intent(OtpActivity.this, ProfileCompleteActivity.class).putExtra(Constants.INTENT_EMAIL, email));
                                }
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(OtpActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(OtpActivity.this, R.string.unable_to_process, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email);
                        params.put("code", otp);
                        return params;
                    }
                };
                App.getRequestQueue().add(request);
                progressDialog.show();
            }
        });

        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.SEND_OTP_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i("mytag", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int status = jsonResponse.getInt("status");
                            if (status == 1) {
                                Toast.makeText(OtpActivity.this, "OTP Sent to your email address", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OtpActivity.this, "Unable to send OTP", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(OtpActivity.this, R.string.unable_to_process, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email);
                        return params;
                    }
                };
                App.getRequestQueue().add(request);
                progressDialog.show();
            }
        });
    }

    private String getOtp() {
        return etOtp1.getText().toString() + etOtp2.getText().toString() + etOtp3.getText().toString() + etOtp4.getText().toString();
    }
}