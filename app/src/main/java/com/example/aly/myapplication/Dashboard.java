package com.example.aly.myapplication;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

//Activity is responsible for:
// 1. Displaying main user Dashboard
// 2. Allowing user to perform several operations such as :
// Solve/Add Quizzes, View Grades, Logout
// 3. Handling text to speech

public class Dashboard extends AppCompatActivity {

    ImageButton solve, view_grades,logout;
    TextView usrname;
    String username;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Gets the previously created intent
        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //Layout components init.

        solve = (ImageButton)findViewById(R.id.solve);
        view_grades = (ImageButton) findViewById(R.id.view_grades);
        logout=(ImageButton) findViewById(R.id.logout);
        usrname= (TextView)findViewById(R.id.usr);
        usrname.setText(username);

        //Handle tex-to-speech operation

        tts = new TextToSpeech(Dashboard.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    } else {
                        tts.speak("Welcome "+username+" to the classroom. I'm here to help you", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                } else
                    Log.e("error", "Failed!");
            }
        });

        //Redirect to ListQuestions Activity

        solve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Dashboard.this,ListQuestions.class);
                i.putExtra("username",username);
                startActivity(i);

            }
        });
        //Redirect to ViewGrades Activity

        view_grades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = usrname.getText().toString();
                Intent i = new Intent(Dashboard.this,ViewGrades.class);
                i.putExtra("username",username);
                startActivity(i);

            }
        });
        //Logout User

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(Dashboard.this,LoginActivity.class);
                startActivity(i);
                finish();
            }
        });


    }
}
