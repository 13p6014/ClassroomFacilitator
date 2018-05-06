package com.example.aly.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class ListCourses extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("courses");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_courses);


        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //Creating a HashMap object

                HashMap<String, String> map = new HashMap<String, String>();

                //Getting Collection of values from HashMap

                map = (HashMap<String, String>) dataSnapshot.getValue();

                Collection<String> values = map.keySet();
                List<String> courses = new ArrayList<String>(values);

               // Log.e("Values ",courses.toString());

                ArrayAdapter adapter = new ArrayAdapter<String>(ListCourses.this, R.layout.single_row, courses);

                ListView listView = (ListView) findViewById(R.id.listviw);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                    {
                        String str = ((TextView) arg1).getText().toString();
                        Toast.makeText(getBaseContext(),str, Toast.LENGTH_LONG).show();

                        Intent i = new Intent (ListCourses.this, ListQuestions.class);
                        i.putExtra("course_name",str);
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

}
