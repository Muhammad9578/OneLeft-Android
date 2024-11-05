package com.oneleft.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oneleft.app.R;
import com.oneleft.app.utils.ApiConfig;
import com.oneleft.app.utils.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StripeAccountActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSave;
    private TextView tvMessage;
    private ProgressBar progressBar;

    private ProgressDialog progressDialog;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.stripe_account));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        tvMessage = findViewById(R.id.tvMessage);
        progressBar = findViewById(R.id.progressBar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String stripeEmailId = preferences.getString("STRIPE_EMAIL_ID", "");
        String accountId = preferences.getString("STRIPE_ACCOUNT_ID", "");

        if (!stripeEmailId.isEmpty()) {
            etEmail.setText(accountId + "(" + stripeEmailId + ")");
            etEmail.setFocusable(false);
            fetchAccountDetails();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError("Enter email");
                    etEmail.requestFocus();
                } else {
                    //progressDialog.show();
                    btnSave.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.CREATE_STRIPE_ACCOUNT_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setVisibility(View.VISIBLE);
                            Log.i("mytag", "Response: " + response);
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                int status = jsonResponse.getInt("status");
                                if (status == 0) {
                                    String message = jsonResponse.getString("message");
                                    Toast.makeText(StripeAccountActivity.this, message, Toast.LENGTH_SHORT).show();
                                } else {
                                    String accountId = jsonResponse.getString("account_id");
                                    preferences.edit()
                                            .putString("STRIPE_ACCOUNT_ID", accountId)
                                            .putString("STRIPE_EMAIL_ID", email).apply();
                                    fetchAccountDetails();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(StripeAccountActivity.this, "Unable to process server response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                            btnSave.setVisibility(View.VISIBLE);
                            Toast.makeText(StripeAccountActivity.this, "Unable to fetch data from server", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("email", "ladlaprince786@gmail.com");
                            return params;
                        }
                    };

                    App.getRequestQueue().add(request);
                }
            }
        });
    }

    private void fetchAccountDetails() {
        String accountId = preferences.getString("STRIPE_ACCOUNT_ID", "");
        if (accountId.isEmpty()) {
            return;
        }
        String stripeEmailId = preferences.getString("STRIPE_EMAIL_ID", "");
        //etEmail.setText(stripeEmailId + "(" + accountId + ")");
        etEmail.setText(accountId + "(" + stripeEmailId + ")");
        etEmail.setFocusable(false);
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.GET_STRIPE_ACCOUNT_DETAILS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
                Log.i("mytag", "Response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject accountJson = jsonResponse.getJSONObject("account");
                    boolean detailsSubmitted = accountJson.getBoolean("details_submitted");
                    if (!detailsSubmitted) {
                        String accountLink = jsonResponse.getString("account_link");
                        tvMessage.setText("Your stripe account setup is incomplete. Tap the button below to complete stripe account setup.");
                        btnSave.setText("Complete Stripe Setup");
                        btnSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(accountLink)), 123);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(StripeAccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        tvMessage.setText("Stripe account setup is complete. No further action is required here.");
                        btnSave.setVisibility(View.INVISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(StripeAccountActivity.this, "Unable to process server response", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
                Toast.makeText(StripeAccountActivity.this, "Unable to fetch data from server", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("account_id", accountId);
                return params;
            }
        };

        App.getRequestQueue().add(request);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            fetchAccountDetails();
        }
    }
}
