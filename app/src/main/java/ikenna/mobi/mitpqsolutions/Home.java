package ikenna.mobi.mitpqsolutions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

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
    private static final String BASE64_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkp7kFqXRAqLfDKlRROTqTw5dRzSKWg243M4fCg/D8J7H0KeDnLj9g+SO9fkf19syjmb/hFTtz8endCtFGUIb68y2mlwsIw/9vXlDAWl2c8XTyk9YXeiiK5biEQsP+0EVH3rlfKq31HPY/28ZuHC4JdMvLKOxqGFhJvp9RlK2TLgB/1NvAKTAujeirIE9cFjj4PBsn4wQ0EGtbue/4SpSFrftk3pHppUrZynon79dSmYsiv2I6twnli3JoQwGhYBsxDLD0FpSf1zarOo6TlY00ZWH+smNDhZMgfReC/YtAi/KufQxFi/PmAfIcxQYF3bZYPg89f4AGOaQyerejDlu7QIDAQAB";
    private static final byte[] SALT = new byte[] {18,76,70,69,29,14,29,41,74,02,33,37,98,89,13,90,70,19,91,53};
    private Handler mHandler;
    private LicenseChecker mChecker;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    boolean licensed;
    boolean checkingLicense;
    boolean didCheck;
    @Override
    public void onCreate(Bundle saved)
{
    super.onCreate(saved);
    setContentView(R.layout.home);
    PingNetworkStatus netstate = new PingNetworkStatus();
    boolean connected = netstate.checkConnectionState(getApplicationContext());
    if(Active.runoffline||!connected)
    {
        populateSpinnerLocal();
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
        if(didCheck)
        {
         if(licensed||Login.root.equals("mcp_ikebobs@hotmail.com")) launchActivity();
         else showDialog(0);
        }
        else
        doCheck();
    }
    protected void launchActivity()

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
    //License verification section
    protected void initLicenseCheckingParm()
    {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("Device Id", deviceId);
        mHandler = new Handler();
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mChecker = new LicenseChecker(getApplicationContext(), new ServerManagedPolicy(getApplicationContext(), new AESObfuscator(SALT, getPackageName(), deviceId)), BASE64_PUBLIC_KEY);

    }
    private void doCheck()
    {
        try {
            initLicenseCheckingParm();
            didCheck = false;
            checkingLicense = true;
            setProgressBarIndeterminateVisibility(true);
            mChecker.checkAccess(mLicenseCheckerCallback);
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private void retryCheck()
    {
        try {
            didCheck = false;
            checkingLicense = true;
            setProgressBarIndeterminateVisibility(true);
            mChecker.checkAccess(mLicenseCheckerCallback);
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            try {
                // TODO Auto-generated method stub
                if (isFinishing()) {
                    // Don't update UI if Activity is finishing.
                    return;
                }
                Log.i("License", "Accepted!");

                //You can do other things here, like saving the licensed status to a
                //SharedPreference so the app only has to check the license once.
                licensed = true;
                checkingLicense = false;
                didCheck = true;
                launchActivity();
            }
            catch(Exception ex)
            {
                Toast.makeText(getApplicationContext(),"allow",Toast.LENGTH_SHORT).show();

            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public void dontAllow(int reason) {
            try{
                // TODO Auto-generated method stub
                if (isFinishing()) {
                    // Don't update UI if Activity is finishing.
                    return;
                }
                Log.i("License","Denied!");
                Log.i("License","Reason for denial: "+reason);

                //You can do other things here, like saving the licensed status to a
                //SharedPreference so the app only has to check the license once.

                licensed = false;
                checkingLicense = false;
                didCheck = true;

                showDialog(0);}
            catch (Exception ex)
            {
                Toast.makeText(getApplicationContext(),"dontAllow",Toast.LENGTH_SHORT).show();
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public void applicationError(int reason) {
            try {
                // TODO Auto-generated method stub
                Log.i("License", "Error: " + reason);
                if (isFinishing()) {
                    // Don't update UI if Activity is finishing.
                    return;
                }
                licensed = true;
                checkingLicense = false;
                didCheck = false;

                showDialog(0);}
            catch(Exception ex)
            {
                Toast.makeText(getApplicationContext(),"appError",Toast.LENGTH_SHORT).show();
            }
        }


    }

    protected Dialog onCreateDialog(int id) {
        try {
            // We have only one dialog.
            return new AlertDialog.Builder(this)
                    .setTitle("UNLICENSED APPLICATION DIALOG TITLE")
                    .setMessage("This application is not licensed, please uninstall and buy it from the play store.")
                    .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "http://market.android.com/details?id=" + getPackageName()));
                            startActivity(marketIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                            Log.i("License", "Key Listener");
                            finish();
                            return true;
                        }
                    })
                    .create();
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    protected void populateSpinnerLocal()
    {
        ArrayList<String> codes = new ArrayList<>();
        Uri content = CachedContent.uri_courses;
        String[] projection = {CachedContent.coursecode};
        Cursor c = getApplicationContext().getContentResolver().query(content,projection,null,null,CachedContent.coursecode);
        int index0 = c.getColumnIndex(CachedContent.coursecode);
        if(c!=null && c.getCount()>0) {
            while (c.moveToNext()) {
                String courseid = c.getString(index0);
                codes.add(courseid);
            }
            Spinner spinner = findViewById(R.id.home_courses);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,codes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

    }

}



