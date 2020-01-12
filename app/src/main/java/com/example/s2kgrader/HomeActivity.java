package com.example.s2kgrader;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    EditText subject;
    EditText marks;
    Button submit;

    String subject_string;
    String marks_string;

    DatabaseReference reff;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        subject = findViewById(R.id.subject_textView);
        marks = findViewById(R.id.marks_textView);
        submit = findViewById(R.id.submit_button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_details();
            }
        });

        reff = FirebaseDatabase.getInstance().getReference().child("TotalQuestion");



    }

    void get_details(){

        subject_string = subject.getText().toString();
        marks_string =  marks.getText().toString();

        if(subject_string.isEmpty() || marks_string.isEmpty()){
            Toast.makeText(HomeActivity.this,"Enter Text",Toast.LENGTH_LONG).show();
        }

        else {

            Intent intent = new Intent(HomeActivity.this, MainActivity.class);

            intent.putExtra("subject_string",subject_string);
            intent.putExtra("marks_string",marks_string);

            reff.child(subject_string).setValue(marks_string);

            HomeActivity.this.startActivity(intent);
            finish();


        }

        //Toast.makeText(HomeActivity.this,"Hello",Toast.LENGTH_LONG).show();




    }



}
