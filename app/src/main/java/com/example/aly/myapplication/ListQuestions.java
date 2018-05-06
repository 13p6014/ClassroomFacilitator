package com.example.aly.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//Activity is responsible for:
// 1. Getting all current available quizzes for enrolled course
// 2. Checks current user role if allowed to add new quiz

public class ListQuestions extends AppCompatActivity {
    String course_name,last_quiz,role,usrname;
    Button addQ;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef,myRef2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent myIntent = getIntent(); // gets the previously created intent
        usrname =myIntent.getStringExtra("username");

        last_quiz="";
        myRef2 = database.getReference("userinfo/"+usrname);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_questions);

        //Layout components init.

        addQ = (Button)findViewById(R.id.addQ);
        addQ.setEnabled(false);

        //DB handler for getting user info

        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> map = new HashMap<String, String>();

                map = (HashMap<String, String>) dataSnapshot.getValue();

                role = String.valueOf(map.get("role"));
                course_name =String.valueOf(map.get("enrolled_in"));

                // Log.e("role >>> ",role);

                //Enable adding quiz feature if current user role is Teacher

                if(role.equalsIgnoreCase("teacher"))
                {
                    addQ.setEnabled(true);
                }

                //DB handler for getting course info

                myRef = database.getReference("courses/"+course_name);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        HashMap<String, String> map = new HashMap<String, String>();
                        map = (HashMap<String, String>) dataSnapshot.getValue();

                        Collection<String> values = map.keySet();
                        List<String> quizzes = new ArrayList<String>(values);
                        Collections.sort(quizzes);

                        // Log.e("Values ",quizzes.toString());

                        //Init. list view of current available quizzes

                        ArrayAdapter adapter = new ArrayAdapter<String>(ListQuestions.this, R.layout.single_row, quizzes);
                        last_quiz = adapter.getItem(adapter.getCount()-1).toString();
                        ListView listView = (ListView) findViewById(R.id.listviw);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                        {
                            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                            {
                                if(role.equalsIgnoreCase("student")) {
                                    //Log.e("entered >> ",role);
                                    addQ.setEnabled(false);
                                    String quiz = ((TextView) arg1).getText().toString();
                                    Toast.makeText(getBaseContext(), quiz + " started .. ", Toast.LENGTH_LONG).show();

                                    Intent i = new Intent(ListQuestions.this, AnswerQuiz.class);
                                    i.putExtra("course_name", course_name);
                                    i.putExtra("username",usrname);
                                    i.putExtra("quiz", quiz);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Handles adding a new quiz operation

        addQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent (ListQuestions.this, AddQuiz.class);
                i.putExtra("course_name",course_name);
                i.putExtra("username",usrname);
                i.putExtra("last_quiz",String.valueOf(last_quiz.charAt(last_quiz.length()-1)));
                startActivity(i);
                finish();
            }
        });

    }
}
