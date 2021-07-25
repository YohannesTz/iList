package com.yohannes.dev.app.ilist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yohannes.dev.app.ilist.MainActivity;
import com.yohannes.dev.app.ilist.R;

import java.util.HashMap;


public class SignUp extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button signUpButton = findViewById(R.id.signUpButton);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        signUpButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Invalid Email");
                emailEditText.setFocusable(true);
            } else if (password.length() < 6) {
                passwordEditText.setError("Password length at least 6 characters");
                passwordEditText.setFocusable(true);
            } else {
                registerUser(email, password);
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();

        Log.e("message", "Inside the registerUser");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        progressDialog.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();

                        String userEmail = user.getEmail();
                        String userUniqueId = user.getUid();

                        HashMap<Object, String> userData = new HashMap<>();
                        userData.put("email", userEmail);
                        userData.put("uid", userUniqueId);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = database.getReference("Users");
                        Log.e("message", "Saving inside the database reference");
                        databaseReference.child(userUniqueId).setValue(userData);

                        Toast.makeText(SignUp.this, "Authentication Sucess",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUp.this, MainActivity.class));
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        Toast.makeText(SignUp.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SignUp.this, "" + e.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }
}