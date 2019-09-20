package com.example.mitpqsolutions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    public void button_click1(View view)
    {
     startActivity(new Intent("com.example.mitpqsolutions.Login"));
       // EditText edit = findViewById(R.id.edittext1);
       // edit.setText("The button was just clicked");
    }
}
