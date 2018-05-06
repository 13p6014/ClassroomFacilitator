package com.example.aly.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

//Activity is responsible for:
// 1. Adding new quiz
// 2. Ensure quiz questions consistency

public class AddQuiz extends AppCompatActivity {

    String course_name,last_quiz,usrname,correct_ans;
    EditText question,choice1,choice2,choice3,answer;
    TextView count;
    int c=1;
    Button add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent myIntent = getIntent(); // gets the previously created intent
        course_name = myIntent.getStringExtra("course_name");
        usrname = myIntent.getStringExtra("username");
        last_quiz = myIntent.getStringExtra("last_quiz");
        final int index = Integer.parseInt(last_quiz)+1;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quiz);

        //Layout components init.

        add = (Button)findViewById(R.id.add);
        question = (EditText)findViewById(R.id.ques);
        choice1 = (EditText)findViewById(R.id.ch1);
        choice2 = (EditText)findViewById(R.id.ch2);
        choice3 = (EditText)findViewById(R.id.ch3);
        answer= (EditText)findViewById(R.id.ans);
        count = (TextView)findViewById(R.id.count);

        //Add spinner (dropdown menu) for the 3 choices

        Spinner dropdown = findViewById(R.id.spinner2);
        String[] items = new String[]{"Choice 1", "Choice 2","Choice 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.v("item", (String) adapterView.getItemAtPosition(i));
                String index= ((String) adapterView.getItemAtPosition(i)).split(" ")[1];

                //Setting current choice

                switch (index)
                {
                    case "1":
                        correct_ans=choice1.getText().toString();
                        break;
                    case "2":
                        correct_ans=choice2.getText().toString();
                        break;
                    case "3":
                        correct_ans=choice3.getText().toString();
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Add values to DB

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("courses/"+course_name);

                String new_question = question.getText().toString();
                Map<String, String> qData = new HashMap<String, String>();

                //Input validation

                if (!(correct_ans.equals("")
                        || choice1.getText().toString().equals("")
                        || choice2.getText().toString().equals("")
                        || choice3.getText().toString().equals("")
                        || question.getText().toString().equals("")
                ))
                {
                    //Construct map

                    qData.put("answer", correct_ans);
                    qData.put("choice1", choice1.getText().toString());
                    qData.put("choice2", choice2.getText().toString());
                    qData.put("choice3", choice3.getText().toString());

                    myRef = myRef.child("Quiz"+index);
                    myRef = myRef.child(new_question);
                    myRef.setValue(qData);

                    Toast.makeText(getBaseContext(),"Question "+ c+"/5 " +"Added", Toast.LENGTH_SHORT).show();
                    resetAll();
                    ++c;
                    if (c == 6)
                    {
                        //Display final confirmation

                        Toast.makeText(getBaseContext(),"Quiz Added", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(AddQuiz.this,ListQuestions.class);
                        i.putExtra("course_name",course_name);
                        i.putExtra("username",usrname);
                        startActivity(i);
                        finish();
                    }
                    count.setText(c+"/5");
                }

                else {
                    Toast.makeText(getBaseContext(),"ERROR!", Toast.LENGTH_SHORT).show();
                }

            }

            //Resetting all components
            private void resetAll() {

                answer.setText("");
                choice1.setText("");
                choice2.setText("");
                choice3.setText("");
                question.setText("");

            }

        });

    }

    //To ensure consistency
    @Override
    public void onBackPressed() {
        Toast.makeText(getBaseContext(),"Please Complete All 5 Questions", Toast.LENGTH_SHORT).show();
    }
}
