package com.example.mitpqsolutions;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Active extends AppCompatActivity {
    @Override
    public void onCreate(Bundle saved)
    {
      super.onCreate(saved);
      setContentView(R.layout.active);
      populateSpinner();
    }
    protected void populateSpinner()
    {
      String[] courses = {"MIT801","MIT802","MIT803","MIT821","MIT805"};
      Spinner spinner = findViewById(R.id.courses);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,courses);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(adapter);
    }
}
