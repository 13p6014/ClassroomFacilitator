package com.example.aly.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Activity is responsible for:
// 1. Fetching current available courses
// 2. Fetching current user roles
// 3. Handling user sign-up process

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String role;
    private String enrolled;

    FirebaseDatabase database2 = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database2.getReference("courses"); //Firebase DB handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //To get available courses from DB

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Create HashMap to get courses DB contents

                HashMap<String, String> map = new HashMap<String, String>();
                map = (HashMap<String, String>) dataSnapshot.getValue();
                Collection<String> values = map.keySet();
                List<String> courses = new ArrayList<String>(values);

                // Log.e("Values ", courses.toString());

                //Add contents to spinner (dropdown menu)

                Spinner dropdown = findViewById(R.id.courses_spinner);
                ArrayAdapter adapter = new ArrayAdapter<String>(SignupActivity.this, android.R.layout.simple_spinner_dropdown_item, courses);
                dropdown.setAdapter(adapter);
                dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        enrolled = (String) adapterView.getItemAtPosition(i);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        //Handle firebase authentication

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //Layout components init.

        btnSignUp = (Button) findViewById(R.id.signup);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.pass);
        progressBar = (ProgressBar) findViewById(R.id.pb);

        //Create spinner (dropdown menu) for user roles

        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Student", "Teacher"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
              //  Log.v("item", (String) adapterView.getItemAtPosition(i));
                role = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Handle user signup operation

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Trim values

                final String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                //Validate values

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Error: Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Error: Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Error: Password too short, minimum of 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Create firebase user

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                DatabaseReference myRef = database.getReference("userinfo");
                                progressBar.setVisibility(View.GONE);

                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {

                                    Toast.makeText(SignupActivity.this, "Authentication success", Toast.LENGTH_SHORT).show();

                                    //Add user details in map then insert in DB

                                    Map<String, String> usrData = new HashMap<String, String>();

                                    usrData.put("role", role);
                                    usrData.put("enrolled_in", enrolled);

                                    //To form account username

                                    myRef = myRef.child(email.split("@")[0]);
                                    myRef.setValue(usrData);

                                    //Call dashboard function with the registered user

                                    Intent i = new Intent(SignupActivity.this, Dashboard.class);
                                    i.putExtra("username", email.split("@")[0]);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}