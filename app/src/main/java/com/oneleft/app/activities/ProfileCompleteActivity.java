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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.oneleft.app.R;
import com.oneleft.app.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProfileCompleteActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvAgreement;
    private Button btnComplete;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_complete);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvAgreement = findViewById(R.id.tvAgreement);
        btnComplete = findViewById(R.id.btnComplete);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");

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

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getIntent().getStringExtra(Constants.INTENT_EMAIL);
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (username.isEmpty()) {
                    Toast.makeText(ProfileCompleteActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(ProfileCompleteActivity.this, "Password length must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else if (!confirmPassword.equals(password)) {
                    Toast.makeText(ProfileCompleteActivity.this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = task.getResult().getUser();
                                        Map<String, String> map = new HashMap<>();
                                        map.put("id", firebaseUser.getUid());
                                        map.put("email", firebaseUser.getEmail());
                                        map.put("username", username);
                                        dbRef.child("Users")
                                                .child(firebaseUser.getUid())
                                                .setValue(map)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        progressDialog.dismiss();
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(ProfileCompleteActivity.this, TermsOfServiceActivity.class));
                                                            finish();
                                                        } else {
                                                            Toast.makeText(ProfileCompleteActivity.this, "Unable to save user metadata", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        progressDialog.dismiss();
                                        Exception exception = task.getException();
                                        if (exception instanceof FirebaseNetworkException) {
                                            Toast.makeText(ProfileCompleteActivity.this, "Network problem. Check your internet connection.", Toast.LENGTH_SHORT).show();
                                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                                            Toast.makeText(ProfileCompleteActivity.this, "Email is already registered with an account.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ProfileCompleteActivity.this, "Unable to register user", Toast.LENGTH_SHORT).show();
                                            if (exception != null)
                                                exception.printStackTrace();
                                        }
                                    }
                                }
                            });
                }


            }
        });
    }
}
