package com.oneleft.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.oneleft.app.R;

public class TermsOfServiceActivity extends AppCompatActivity {

    private TextView tvPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);

        tvPolicy = findViewById(R.id.tvPolicy);

        SharedPreferences sharedpreferences = getSharedPreferences("abc", Context.MODE_PRIVATE);
        String s = sharedpreferences.getString("key","");

        tvPolicy.setText(Html.fromHtml(getString(R.string.privacy_policy_text)));

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

    }
}
