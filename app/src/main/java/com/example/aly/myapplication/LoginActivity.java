package com.example.aly.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

//Activity is responsible for:
// 1. Checking current authentication status
// 2. Handling user logging-in process

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Check if current user is authenticated

        if (auth.getCurrentUser() != null) {

            //Redirect user to Dashboard activity if authenticated

            String usr = auth.getCurrentUser().getEmail().toString().split("@")[0];
            Intent i = new Intent(LoginActivity.this, Dashboard.class);
            i.putExtra("username",usr);
            startActivity(i);
            finish();
        }

        setContentView(R.layout.activity_login);

        //Layout components init.

        inputEmail = (EditText) findViewById(R.id.mail);
        inputPassword = (EditText) findViewById(R.id.pass);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        btnSignup = (Button) findViewById(R.id.signup);
        btnLogin = (Button) findViewById(R.id.signin);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Redirect to signup activity
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        //Handle login operation
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                //Validate inputs

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    //Error
                                    if (password.length() < 6) {
                                        inputPassword.setError("Less than 6");
                                    }
                                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        inputEmail.setError("Not valid email");
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, "Error: Invalid Username or Password", Toast.LENGTH_LONG).show();

                                    }
                                } else {
                                    //Successful Authentication; redirect to user Dashboard

                                    Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                                    intent.putExtra("username",email.split("@")[0]);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }
}