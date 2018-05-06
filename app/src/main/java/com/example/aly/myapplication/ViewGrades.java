package com.example.aly.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

//Activity is responsible for:
// 1. Fetching/Displaying current user quizzes grades

public class ViewGrades extends AppCompatActivity {
    String username;
    TextView crse_name,grds;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent myIntent = getIntent(); // gets the previously created intent
        username = myIntent.getStringExtra("username");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_grades);

        //Layout components int.

        crse_name = (TextView)findViewById(R.id.course_name);
        grds = (TextView)findViewById(R.id.grades);

        //Firebase handler for userinfo DB

        myRef = database.getReference("userinfo/"+username);

        //Get current user grades for each quiz taken

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> map = new HashMap<>();

                map = (HashMap<String, String>) dataSnapshot.getValue();
                Collection<String> values = map.keySet();

                //Removes values of enrolled_in & role from the map
                //Verifies current user role

                crse_name.setText(map.get("enrolled_in"));
                values.remove("enrolled_in");

                if(map.get("role").toString().equalsIgnoreCase("teacher"))
                {
                    Toast.makeText(getBaseContext(), "User is not a student!", Toast.LENGTH_LONG).show();
                    return;
                }
                values.remove("role");

                List<String> grades = new ArrayList<String>(values);
                String current_values = grades.toString();
                for (int i =0; i<grades.size();i++) {

                    //Sets grades textview with user values

                    grds.setText(grds.getText()+"\n"+grades.get(i).toString()+" Grade: " + String.valueOf(map.get(grades.get(i))));
                    // Log.e("Quiz "+i+" Grade >> ", String.valueOf(map.get(grades.get(i))));

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
