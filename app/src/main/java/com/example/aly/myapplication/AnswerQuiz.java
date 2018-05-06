package com.example.aly.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Locale;

//Activity is responsible for:
// 1. Fetching quiz details (Questions + Answers)
// 2. Checking if chosen answers are correct
// 3. Counting score/grade
// 4. Handling Text to speech for questions
// 5. Handling all quiz-solving related operations

public class AnswerQuiz extends AppCompatActivity {
    String course_name,quiz,username;
    Button nxt;
    TextView ques,scre,qnumtxt;
    RadioGroup grp ;
    int qnum= 0;
    int score = 0;
    String correct_ans="";
    RadioButton r1,r2,r3;
    TextToSpeech tts;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef,myRef2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Init. media players to be used in the application

        final MediaPlayer mp_correct = MediaPlayer.create(getApplicationContext(), R.raw.correct);
        final MediaPlayer mp_wrong = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
        final MediaPlayer mp_cheer = MediaPlayer.create(getApplicationContext(), R.raw.cheer);

        Intent myIntent = getIntent(); // gets the previously created intent
        course_name = myIntent.getStringExtra("course_name");
        quiz = myIntent.getStringExtra("quiz");
        username = myIntent.getStringExtra("username");

        //DB handler for courses info + user info
        myRef = database.getReference("courses/"+course_name+"/"+quiz);
        myRef2 = database.getReference("userinfo/"+username);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_quiz);

        //Layout components init.

        nxt= (Button)findViewById(R.id.nxt);
        ques=(TextView)findViewById(R.id.ques);
        scre=(TextView)findViewById(R.id.score);
        qnumtxt = (TextView)findViewById(R.id.qnumtxt);
        grp = (RadioGroup) findViewById(R.id.grp);
        r1 = (RadioButton) findViewById(R.id.first);
        r2 = (RadioButton) findViewById(R.id.second);
        r3 = (RadioButton) findViewById(R.id.third);
        ImageButton tts_btn = (ImageButton) findViewById(R.id.tts);

        //Fist question with index = 0

        goToNextQuestion(0);


        //Go to next question if "next" button is clicked

        nxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //evaluate current chosen answer
                evaluate();
            }

            private void evaluate() {

                //Input validation
                if (grp.getCheckedRadioButtonId() == -1) {

                    Toast.makeText(AnswerQuiz.this, "Please choose an answer!", Toast.LENGTH_SHORT).show();

                } else {

                    RadioButton choice = (RadioButton) findViewById(grp.getCheckedRadioButtonId());

                    if (choice.getText().toString().equalsIgnoreCase(correct_ans)) {
                        //if answer is correct, play "correct" sound, add score, then move to next question
                        Toast.makeText(AnswerQuiz.this, "Correct", Toast.LENGTH_SHORT).show();
                        qnum++;
                        score++;
                        mp_correct.start();

                        if(qnum < 5)
                        {
                            goToNextQuestion(qnum);
                        }
                        else {

                            //if questions ended; wait for couple of seconds then display final score

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable(){
                                @Override
                                public void run(){
                                    mp_cheer.start();
                                    Toast.makeText(getBaseContext(),"You scored "+score+" /5! Well Done!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(AnswerQuiz.this,ListQuestions.class);
                                    i.putExtra("course_name",course_name);
                                    i.putExtra("username",username);
                                    myRef2 = myRef2.child(quiz);
                                    myRef2.setValue(score);
                                    startActivity(i);
                                    finish();
                                }
                            }, 1600);

                        }
                    } else {
                        //if answer is wrong, play "wrong" sound, then move to next question

                        Toast.makeText(AnswerQuiz.this, "Wrong", Toast.LENGTH_SHORT).show();
                        qnum++;
                        mp_wrong.start();

                        if(qnum < 5)
                        {
                            goToNextQuestion(qnum);
                        }
                        else {

                            //if questions ended; wait for couple of seconds then display final score

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable(){
                                @Override
                                public void run(){
                                    mp_cheer.start();
                                    Toast.makeText(getBaseContext(),"You scored "+score+" /5! Well Done!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(AnswerQuiz.this,ListQuestions.class);
                                    i.putExtra("course_name",course_name);
                                    i.putExtra("username",username);
                                    myRef2 = myRef2.child(quiz);
                                    myRef2.setValue(score);
                                    startActivity(i);
                                    finish();
                                }
                            }, 1600);

                        }
                    }
                }
            }
        });


        //If tts button clicked; read the question for user

        tts_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts = new TextToSpeech(AnswerQuiz.this, new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            int result = tts.setLanguage(Locale.US);
                            if (result == TextToSpeech.LANG_MISSING_DATA ||
                                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            } else {
                                tts.speak(ques.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        } else
                            Log.e("error", "Failed!");
                    }
                });
            }
        });
    }

    private void goToNextQuestion(final int qnum) {

        qnumtxt.setText(qnum+1+"/5");

        //DB handler for getting getting current question + choices + correct answer

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String, HashMap<String, String>> map = new HashMap<>();

                map = (HashMap<String, HashMap<String,String>>) dataSnapshot.getValue();
                Collection<String> values = map.keySet();
                List<String> questions = new ArrayList<String>(values);

                String current_ques = questions.get(qnum).toString();
                HashMap<String,String> current_map = new HashMap<>();
                current_map = map.get(current_ques);

                scre.setText(score+"");
                ques.setText(current_ques);

                r1.setText(current_map.get("choice1"));
                r2.setText(current_map.get("choice2"));
                r3.setText(current_map.get("choice3"));

                grp.clearCheck();

                correct_ans = current_map.get("answer");

            }

            @Override
            public void onCancelled(DatabaseError error) {

                // Toast.makeText(getBaseContext(),"Error Catched", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
