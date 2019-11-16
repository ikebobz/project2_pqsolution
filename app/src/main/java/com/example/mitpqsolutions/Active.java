package com.example.mitpqsolutions;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private JSONObject jsObject;
    private HashMap<String,String> parm;
    HashMap<String,String> qaentries = new HashMap<>();
    TextView resultVw;
    EditText qbox;
    Object[] keys;
    int counter;


    @Override
    public void onCreate(Bundle saved)
    {
      super.onCreate(saved);
      setContentView(R.layout.active);
      Button btnTab = findViewById(R.id.setable);
      btnTab.setEnabled(false);
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
         startProgress("Loading Courses....");

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      getMenuInflater().inflate(R.menu.active_menu,menu);
      return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
      switch(item.getItemId())
      {
          case R.id.mnu_signout: Intent intent = new Intent(Active.this,Login.class);
                                                 startActivity(intent);
                                                 finish();
                                                 return true;
      }
       return true;
    }
    public void search_click(View view)
    {
        counter = 0;
        qbox = findViewById(R.id.editq);
      String question = qbox.getText().toString();
       resultVw = findViewById(R.id.searchResult);
      Spinner courses = findViewById(R.id.courses);
      String selected = courses.getSelectedItem().toString();
      if(!qaentries.isEmpty()) qaentries.clear();
      //JSONObject jsObject;
      try
      {

          //test for connectivity
       PingNetworkStatus netstate = new PingNetworkStatus();
       boolean connected = netstate.checkConnectionState(getApplicationContext());
       if(connected)
       {
           parm = new HashMap<String,String>();
           parm.put("qdesc",question);
           parm.put("course",selected);
           Thread primary = new Thread(new Runnable(){
            @Override
            public void run()
            {

                HttpRequestInitiator hri = new HttpRequestInitiator();
                jsObject = hri.startRequest(BASE_URL+"getanswer.php","GET",parm);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button btnTab = findViewById(R.id.setable);
                        try
                        {
                            if(jsObject.getInt("success")==1)
                            {
                                JSONArray jarr = jsObject.getJSONArray("data");
                                if(jarr.length()==0) Toast.makeText(getApplicationContext(),"Answer not yet defined",Toast.LENGTH_LONG).show();
                                else { //array is not empty
                                    for (int i = 0; i < jarr.length(); i++) {

                                        String colnum = "", rownum = "", content = "", combined = "";
                                        JSONObject jobj = jarr.getJSONObject(i);
                                        String qid = jobj.getString("qid");
                                        String course = jobj.getString("course");
                                        String qdesc = jobj.getString("description");
                                        String answer = jobj.getString("answer");
                                        content = jobj.getString("content");
                                        if (!content.equals("X")) {
                                            colnum = jobj.getString("colnum");
                                            rownum = jobj.getString("rownum");
                                            combined = colnum + "x" +rownum + "x" + content;
                                        }

                                        qaentries.put(qdesc, answer + "#" + combined);
                                        //resultVw.setText(answer);
                                        if(!testID(qid))
                                        addItem(qid, course, qdesc, answer);
                                    }

                                    keys = qaentries.keySet().toArray();
                                    qbox.setText(keys[0].toString());
                                    String answer = qaentries.get(keys[0]);
                                    if(!answer.split("#")[1].equals("00"))
                                        btnTab.setEnabled(true);
                                    else btnTab.setEnabled(false);
                                    resultVw.setText(answer.split("#")[0]);
                                }

                            }
                            else Toast.makeText(getApplicationContext(),jsObject.getString("message"),Toast.LENGTH_LONG).show();




                    }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }
                        pDialog.dismiss();
                    }
                });
            }
        });
           startProgress("Fetching Results...");
        primary.start();
        //primary.join();
       } else
           searchLocal(question,selected);// end of if construct
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
    }
    public void addItem(String ID,String course,String que,String ans)
    {
        ContentValues values = new ContentValues();
        values.put(CachedContent.QID,ID);
        values.put(CachedContent.COURSE,course);
        values.put(CachedContent.QDESC,que);
        values.put(CachedContent.QANS,ans);
        Uri track_uri = getContentResolver().insert(CachedContent.CONTENT_URI,values);
        if(track_uri!=null)
            Toast.makeText(getBaseContext(),"Answer Cached Successfully",Toast.LENGTH_LONG).show();
        else  Toast.makeText(getBaseContext(),"Answer Caching Failed",Toast.LENGTH_LONG).show();


    }
    protected boolean searchLocal(String question,String selectCourse)
    {
        boolean ret = false;
        counter = 0;
        EditText qfield =  findViewById(R.id.editq);
        TextView resultVw = findViewById(R.id.searchResult);
        Uri content = Uri.parse(CachedContent.URL);
        String[] projection = {CachedContent.QDESC,CachedContent.QANS};
        String selClause = CachedContent.QDESC+" like ? and "+CachedContent.COURSE+"=?";
        String[] args = {question,selectCourse};
        Cursor c = getApplicationContext().getContentResolver().query(content,projection,selClause,args,CachedContent.QID);
        int index0 = c.getColumnIndex(CachedContent.QDESC);
        int index1 = c.getColumnIndex(CachedContent.QANS);
        if(c!=null && c.getCount()>0)
        {
            while (c.moveToNext())
            {
                String ques = c.getString(index0);
                String ans = c.getString(index1);
                qaentries.put(ques,ans);

            }
            keys = qaentries.keySet().toArray();
            String first = qaentries.get(keys[counter]);
            resultVw.setText(first);
            qfield.setText(keys[counter].toString());
            ret = true;
        }

        return ret;
    }
    public void Forward_click(View vw)
    {
        if(qaentries.isEmpty()) return;
        EditText qfield =  findViewById(R.id.editq);
        Button btnTab = findViewById(R.id.setable);
        TextView resultVw = findViewById(R.id.searchResult);
        counter++;
        if(counter <= keys.length-1)
      {
          String first = qaentries.get(keys[counter]);
          resultVw.setText(first.split("#")[0]);
          if(!first.split("#")[1].equals("00"))
              btnTab.setEnabled(true);
             else btnTab.setEnabled(false);
          qfield.setText(keys[counter].toString());

      }
        else
        {
            counter = 0;
            String first = qaentries.get(keys[counter]);
            resultVw.setText(first.split("#")[0]);
            if(!first.split("#")[1].equals("00"))
                btnTab.setEnabled(true);
            else btnTab.setEnabled(false);

            qfield.setText(keys[counter].toString());
        }
    }
    public void Back_click(View vw)
    {
        if(qaentries.isEmpty()) return;
        EditText qfield =  findViewById(R.id.editq);
        TextView resultVw = findViewById(R.id.searchResult);
        Button btnTab = findViewById(R.id.setable);
        if(counter==0) counter=keys.length;
        counter--;
        String first = qaentries.get(keys[counter]);
        resultVw.setText(first.split("#")[0]);
        if(!first.split("#")[1].equals("00"))
            btnTab.setEnabled(true);
        else btnTab.setEnabled(false);

        qfield.setText(keys[counter].toString());

    }
    protected void startProgress(String msg)
    {
        pDialog = new ProgressDialog(Active.this);
        pDialog.setMessage(msg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
    protected boolean testID(String qid)
    {

        boolean exist = false;
        Uri content = Uri.parse(CachedContent.URL);
        String[] projection = {CachedContent.QID};
        String selClause = CachedContent.QID+" =?";
        String[] args = {qid};
        Cursor c = getApplicationContext().getContentResolver().query(content,projection,selClause,args,CachedContent.QID);
        if(c!=null && c.getCount()>0) exist = true;
        return exist;
    }
    public void getTables(View view)
    {
      Intent intent = new Intent("com.example.mitpqsolutions.TableDisplay");
      if(qaentries.isEmpty()) return;
      String tableinfo = qaentries.get(keys[counter]);
      String colnum = tableinfo.split("x",3)[0].split("#")[1];
      String rownum = tableinfo.split("x",3)[1];
      String content = tableinfo.split("x",3)[2];
      intent.putExtra("colsize",Integer.valueOf(colnum));
      intent.putExtra("rowsize",Integer.valueOf(rownum));
      intent.putExtra("content",content);
      startActivity(intent);

    }
}
