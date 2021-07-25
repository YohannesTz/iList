package com.yohannes.dev.app.ilist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.yohannes.dev.app.ilist.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button signInButton = findViewById(R.id.gotoSignIn);
        Button signUpButton = findViewById(R.id.gotoSignup);

        signInButton.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, SignIn.class));
        });

        signUpButton.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, SignUp.class));
        });
    }

}