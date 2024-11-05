package com.oneleft.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oneleft.app.R;
import com.oneleft.app.utils.ApiConfig;
import com.oneleft.app.utils.App;
import com.oneleft.app.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private TextView tvAgreement;
    private TextView tvLogin;
    private Button btnSignUp;
    private EditText etEmail;
    private EditText etConfirmEmail;

    private ProgressDialog progressDialog;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tvAgreement = findViewById(R.id.tvAgreement);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        etEmail = findViewById(R.id.etEmail);
        etConfirmEmail = findViewById(R.id.etConfirmEmail);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");

        dbRef = FirebaseDatabase.getInstance().getReference();

        tvAgreement.setMovementMethod(new LinkMovementMethod());

        SpannableString spannableString = new SpannableString(getString(R.string.by_continuing_you_agree));

        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 39, 53, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 56, 72, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

            }
        }, 39, 53, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

            }
        }, 56, 72, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        tvAgreement.setText(spannableString);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String confirmEmail = etConfirmEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignUpActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                } else if (!confirmEmail.equals(email)) {
                    Toast.makeText(SignUpActivity.this, "Confirm email does not match", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.show();
                    dbRef.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Email address already exists", Toast.LENGTH_SHORT).show();
                            }else {
                                StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.SEND_OTP_URL, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        progressDialog.dismiss();
                                        Log.i("mytag", response);
                                        try {
                                            JSONObject jsonResponse = new JSONObject(response);
                                            int status = jsonResponse.getInt("status");
                                            if (status == 1) {
                                                startActivity(new Intent(SignUpActivity.this, OtpActivity.class).putExtra(Constants.INTENT_EMAIL, email));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(SignUpActivity.this, R.string.unable_to_process, Toast.LENGTH_SHORT).show();
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

    }
}
