package com.yohannes.dev.app.ilist.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yohannes.dev.app.ilist.MainActivity;
import com.yohannes.dev.app.ilist.R;

import java.util.HashMap;

public class SignIn extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button signInButton;
    private ProgressDialog progressDialog;
    private SignInButton googleSignInButton;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailSignIn);
        password = findViewById(R.id.passwordSignIn);
        signInButton = findViewById(R.id.signInButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");

        ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }
                }
        );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        signInButton.setOnClickListener(view -> {
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                email.setError("Invalid Email");
                email.setFocusable(true);
            } else {
                loginUser(emailText, passwordText);
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(SignIn.this, "Signed in Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignIn.this, MainActivity.class));
            finish();
        } catch (ApiException e) {
            Toast.makeText(SignIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
    }

    private void loginUser(String emailText, String passwordText) {
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    String userEmail = user.getEmail();
                    String userUniqueId = user.getUid();

                    HashMap<Object, String> userData = new HashMap<>();
                    userData.put("email", userEmail);
                    userData.put("uid", userUniqueId);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = database.getReference("Users");
                    Log.e("message", "Saving inside the database reference");
                    databaseReference.child(userUniqueId).setValue(userData);

                    startActivity(new Intent(SignIn.this, MainActivity.class));
                } else {
                    progressDialog.dismiss();
                    Log.e("message", task.getException().toString());
                    Toast.makeText(SignIn.this, "Authentication failed.",
                          Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(SignIn.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}