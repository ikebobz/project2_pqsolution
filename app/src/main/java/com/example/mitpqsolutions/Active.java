package com.example.mitpqsolutions;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Active extends AppCompatActivity {

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String BASE_URL = "http://10.0.2.2:8082/pqdoctor/";
    private static final String KEY_COURSE_ID = "code";
    private static final String KEY_COURSE_DESC = "description";
    private ArrayList<String> course_codes;
    private ArrayList<HashMap<String, String>> courseList;
    private ProgressDialog pDialog;
    @Override
    public void onCreate(Bundle saved)
    {
      super.onCreate(saved);
      setContentView(R.layout.active);
      if(new PingNetworkStatus().checkConnectionState(getApplicationContext()))
      new FetchCoursesTask().execute();
      else Toast.makeText(this,"unable to establish remote connection",Toast.LENGTH_LONG).show();
    }
    private  class FetchCoursesTask extends AsyncTask<String,String,String>
    {
     @Override
     protected void onPreExecute()
     {
         super.onPreExecute();
         //Display progress bar
         pDialog = new ProgressDialog(Active.this);
         pDialog.setMessage("Loading courses. Please wait...");
         pDialog.setIndeterminate(false);
         pDialog.setCancelable(false);
         pDialog.show();
     }
     @Override
        protected String doInBackground(String... params)
     {
         HttpRequestInitiator httpJsonParser = new HttpRequestInitiator();
         JSONObject jsonObject = httpJsonParser.startRequest(
                 BASE_URL + "getcourses.php", "GET", null);
         try {
             int success = jsonObject.getInt(KEY_SUCCESS);
             JSONArray courses;
             if (success == 1) {
                 courseList = new ArrayList<>();
                 course_codes = new ArrayList<>();
                 courses = jsonObject.getJSONArray(KEY_DATA);
                 //Iterate through the response and populate movies list
                 for (int i = 0; i < courses.length(); i++) {
                     JSONObject course = courses.getJSONObject(i);
                     String courseId = course.getString(KEY_COURSE_ID);
                     String courseName = course.getString(KEY_COURSE_DESC);
                     HashMap<String, String> map = new HashMap<String, String>();
                     map.put(KEY_COURSE_ID, courseId);
                     map.put(KEY_COURSE_DESC, courseName);
                     course_codes.add(courseId);
                     courseList.add(map);
                 }
             }
         } catch (JSONException e)
         {
             e.printStackTrace();
         }
         return null;
     }//end of doInBackground operation
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    populateSpinner();
                }
            });
        }
    }

    protected void populateSpinner()
    {
      //String[] courses = {"MIT801","MIT802","MIT803","MIT821","MIT805"};
      Spinner spinner = findViewById(R.id.courses);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,course_codes);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(adapter);
    }
}
