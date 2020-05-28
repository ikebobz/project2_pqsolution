package ikenna.mobi.mitpqsolutions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Home extends AppCompatActivity
{
    ProgressDialog pDialog;
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";
    private static final String KEY_COURSE_ID = "code";
    private static final String KEY_COURSE_DESC = "description";
    public static ArrayList<String> course_codes,course_names;
    public static JSONArray prog_courses;
    @Override
    public void onCreate(Bundle saved)
{
    super.onCreate(saved);
    setContentView(R.layout.home);
    PingNetworkStatus netstate = new PingNetworkStatus();
    boolean connected = netstate.checkConnectionState(getApplicationContext());
    if(Active.runoffline||!connected)
    {
        loadCourses();
        return;
    }
    try
    {
     FetchCoursesTask asyncWork = new FetchCoursesTask();
     asyncWork.execute();
    }
    catch(Exception ex)
    {

    }
}
    protected void startProgress(String msg)
    {
        pDialog = new ProgressDialog(Home.this);
        pDialog.setMessage(msg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }
    private  class FetchCoursesTask extends AsyncTask<String,String,String>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //Display progress bar
            startProgress("Loading Courses....");

        }
        @Override
        protected String doInBackground(String... params)
        {
            prog_courses = new JSONArray();
            HttpRequestInitiator httpJsonParser = new HttpRequestInitiator();
            JSONObject jsonObject = httpJsonParser.startRequest(
                    BASE_URL + "getcourses.php", "GET", null);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray courses;
                if (success == 1) {
                    course_codes = new ArrayList<>();
                    course_names = new ArrayList<>();
                    courses = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate movies list
                    for (int i = 0; i < courses.length(); i++) {
                        JSONObject jsobj = new JSONObject();
                        JSONObject course = courses.getJSONObject(i);
                        String courseId = course.getString(KEY_COURSE_ID);
                        String coursename = course.getString(KEY_COURSE_DESC);
                        course_codes.add(courseId);
                        course_names.add(coursename);
                        jsobj.put(KEY_COURSE_ID,courseId);
                        jsobj.put(KEY_COURSE_DESC,coursename);
                        prog_courses.put(jsobj);
                    }

                }
            } catch (Exception e)
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
        if(course_codes==null) return;
        Spinner spinner = findViewById(R.id.home_courses);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,course_codes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Toast.makeText(getApplicationContext(),course_codes.size()+" course(s) loaded",Toast.LENGTH_SHORT).show();
    }
    private void loadCourses()
    {
        ArrayList<String> codes = new ArrayList<>();
        codes.add("MIT801");codes.add("MIT8012");codes.add("MIT803");codes.add("MIT821");codes.add("MIT805");
        codes.add("MIT804");codes.add("MIT806");codes.add("MIT813");codes.add("MIT822");
        Spinner spinner = findViewById(R.id.home_courses);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,codes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    public void hmBtnGo(View vw)
    {
        RadioButton rbtn1 = findViewById(R.id.rdbtn1);
        RadioButton rbtn2 = findViewById(R.id.rdbtn2);
        if(rbtn1.isChecked()) startActivity(new Intent("ikenna.mobi.mitpqsolutions.Active"));
        if(rbtn2.isChecked())
        {
            Spinner spinner = findViewById(R.id.home_courses);
            String selected = spinner.getSelectedItem().toString();
            startActivity(new Intent("ikenna.mobi.mitpqsolutions.FullView").putExtra("course",selected));
        }
        }
    }



