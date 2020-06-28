package ikenna.mobi.mitpqsolutions;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Active extends AppCompatActivity {

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";
    private static final String KEY_COURSE_ID = "code";
    private static final String KEY_COURSE_DESC = "description";
    private ArrayList<String> course_codes;
    private ArrayList<HashMap<String, String>> courseList;
    private ProgressDialog pDialog;
    private JSONObject jsObject;
    private HashMap<String,String> parm;
    HashMap<String,String> qaentries = new HashMap<>();
    HashMap<String,String> imageurls = new HashMap<>();
    TextView resultVw;
    EditText qbox;
    Object[] keys;
    Button btnImage;
    int counter;
    static boolean runoffline = false;
    String rcount;


    @Override
    public void onCreate(Bundle saved)
    {
      super.onCreate(saved);
      setContentView(R.layout.active);
      Button btnTab = findViewById(R.id.setable);
      btnTab.setEnabled(false);
      btnImage = findViewById(R.id.seeimg);
      btnImage.setEnabled(false);
      try {
          if (runoffline) {
              populateSpinnerLocal();
              return;
          }
          if (new PingNetworkStatus().checkConnectionState(getApplicationContext()))
              new FetchCoursesTask().execute();
          else {
              populateSpinnerLocal();
              Toast.makeText(getApplicationContext(), "unable to establish remote connection", Toast.LENGTH_SHORT).show();
          }
      }
      catch(Exception ex)
      {
          Toast.makeText(getApplicationContext(), "Ooops something has gone wrong!!!!", Toast.LENGTH_SHORT).show();
      }

    }
    @Override protected void onSaveInstanceState(Bundle packet)
    {
        try {
            super.onSaveInstanceState(packet);
            Spinner spinner = findViewById(R.id.courses);
            TextView display = findViewById(R.id.searchResult);
            packet.putString("result", display.getText().toString());
            packet.putInt("selected", spinner.getSelectedItemPosition());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    @Override protected void onRestoreInstanceState(Bundle packet)
    {
        try {
            super.onRestoreInstanceState(packet);
            Spinner spinner = findViewById(R.id.courses);
            TextView display = findViewById(R.id.searchResult);
            display.setText(packet.getString("result"));
            spinner.setSelection(packet.getInt("selected"));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }
    private  class FetchCoursesTask extends AsyncTask<String,String,String>
    {
      int numcourses;
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

         try {

             JSONArray courses;

                 courseList = new ArrayList<>();
                 course_codes = new ArrayList<>();
                 if(Home.prog_courses.length()>0) courses = Home.prog_courses;
                 else
                     {
                         JSONObject jsonObject = httpJsonParser.startRequest(
                                 BASE_URL + "getcourses.php", "GET", null);
                         int success = jsonObject.getInt(KEY_SUCCESS);
                         if(success != 1) return "";
                     courses = jsonObject.getJSONArray(KEY_DATA);
                 }
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
                     if(!testCourseID(courseId))
                         numcourses+=addCourse(courseId,courseName);
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
                    if(numcourses>0)
                    Toast.makeText(getApplicationContext(),numcourses+" new course(s) added ",Toast.LENGTH_SHORT).show();
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
    String imageurl;
    int addeditems =0;
    public void search_click(View view)
    {
        counter = 0;

        qbox = findViewById(R.id.editq);
      String question = "%"+qbox.getText().toString()+"%";
      resultVw = findViewById(R.id.searchResult);
      final TextView txt_rescnt = findViewById(R.id.resltcnt);
      Spinner courses = findViewById(R.id.courses);
      String selected = courses.getSelectedItem().toString();
      if(!qaentries.isEmpty()) qaentries.clear();
      if(!imageurls.isEmpty()) imageurls.clear();
      //JSONObject jsObject;
      try
      {

          //test for connectivity
       PingNetworkStatus netstate = new PingNetworkStatus();
       boolean connected = netstate.checkConnectionState(getApplicationContext());
       if(connected && !runoffline)
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
                                if(jarr.length()==0) Toast.makeText(getApplicationContext(),"Answer not yet defined",Toast.LENGTH_SHORT).show();
                                else { //array is not empty
                                    for (int i = 0; i < jarr.length(); i++) {

                                        String colnum = "", rownum = "", content = "", combined = "";
                                        JSONObject jobj = jarr.getJSONObject(i);
                                        String qid = jobj.getString("qid");
                                        String course = jobj.getString("course");
                                        String qdesc = jobj.getString("description");
                                        String answer = jobj.getString("answer");
                                        imageurl = jobj.getString("imageurl");
                                        content = jobj.getString("content");
                                        if (!content.equals("X")) {
                                            colnum = jobj.getString("colnum");
                                            rownum = jobj.getString("rownum");
                                            combined = colnum + "@" +rownum + "@" + content;
                                        }

                                        qaentries.put(qdesc, answer + "#" + combined);
                                        imageurls.put(qdesc,imageurl);

                                        //resultVw.setText(answer);
                                        if(!testID(qid))
                                        {
                                            addeditems+=addItem(qid, course, qdesc, answer + "#" + combined, getLocalPath(imageurl));
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                 saveImage(imageurl);
                                                }
                                            }).start();

                                        }
                                    }

                                    keys = qaentries.keySet().toArray();
                                    qbox.setText(keys[0].toString());
                                    String answer = qaentries.get(keys[0]);
                                    if(answer.split("#").length>1)
                                        btnTab.setEnabled(true);
                                    else btnTab.setEnabled(false);
                                    if(!imageurls.get(keys[0]).equals(""))
                                        btnImage.setEnabled(true);
                                     else btnImage.setEnabled(false);
                                    resultVw.setText(answer.split("#")[0]);
                                    rcount = "Number of Search Results: "+keys.length;
                                    txt_rescnt.setText(rcount);
                                    if(addeditems>0)
                                    Toast.makeText(getApplicationContext(),addeditems+" items cached!!",Toast.LENGTH_SHORT).show();

                                }

                            }
                            else Toast.makeText(getApplicationContext(),jsObject.getString("message"),Toast.LENGTH_SHORT).show();




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
    public int addItem(String ID,String course,String que,String ans,String imageurl)
    {
        //Toast.makeText(getBaseContext(),imageurl,Toast.LENGTH_SHORT).show();
        ContentValues values = new ContentValues();
        values.put(CachedContent.QID,ID);
        values.put(CachedContent.COURSE,course);
        values.put(CachedContent.QDESC,que);
        values.put(CachedContent.QANS,ans);
        values.put(CachedContent.IMAGEURL,imageurl);
        Uri track_uri = getContentResolver().insert(CachedContent.CONTENT_URI,values);
        if(track_uri!=null) return 1;
         else return 0;
           /* Toast.makeText(getBaseContext(),"Answer Cached Successfully",Toast.LENGTH_SHORT).show();
        else  Toast.makeText(getBaseContext(),"Answer Caching Failed",Toast.LENGTH_SHORT).show();*/


    }
    public int addCourse(String courseid, String description)
    {

        try {
            ContentValues values = new ContentValues();
            values.put(CachedContent.coursecode, courseid);
            values.put(CachedContent.description, description);

            Uri track_uri = getContentResolver().insert(CachedContent.uri_courses, values);
            if (track_uri != null)
                return 1;
            else
                return 0;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }

    protected boolean searchLocal(String question,String selectCourse)
    {
        //Toast.makeText(getApplicationContext(),"Searching locally...",Toast.LENGTH_SHORT).show();
        qaentries.clear();
        imageurls.clear();
        boolean ret = false;
        counter = 0;
        EditText qfield =  findViewById(R.id.editq);
        TextView txt_rescnt = findViewById(R.id.resltcnt);
        Button btnTab = findViewById(R.id.setable);
        TextView resultVw = findViewById(R.id.searchResult);
        Uri content = Uri.parse(CachedContent.URL);
        String[] projection = {CachedContent.QDESC,CachedContent.QANS,CachedContent.IMAGEURL};
        String selClause = CachedContent.QDESC+" like ? and "+CachedContent.COURSE+"=?";
        String[] args = {question,selectCourse};
        Cursor c = getApplicationContext().getContentResolver().query(content,projection,selClause,args,CachedContent.QID);
        int index0 = c.getColumnIndex(CachedContent.QDESC);
        int index1 = c.getColumnIndex(CachedContent.QANS);
        int index2 = c.getColumnIndex(CachedContent.IMAGEURL);

        if(c!=null && c.getCount()>0)
        {
            while (c.moveToNext())
            {
                String ques = c.getString(index0);
                String ans = c.getString(index1);
                String url = c.getString(index2);
                qaentries.put(ques,ans);
                imageurls.put(ques,url);

            }
            keys = qaentries.keySet().toArray();
            String first = qaentries.get(keys[counter]);
            if(first.split("#").length>1)
                btnTab.setEnabled(true);
            else btnTab.setEnabled(false);
            if (!imageurls.get(keys[counter]).equals(""))
                btnImage.setEnabled(true);
            else btnImage.setEnabled(false);
            resultVw.setText(first.split("#")[0]);
            qfield.setText(keys[counter].toString());
            rcount = "Number of Search Results: "+keys.length;
            txt_rescnt.setText(rcount);
            ret = true;
        }

        return ret;
    }
    public void Forward_click(View vw)
    {
        if(qaentries.isEmpty()) return;
        TextView num_results = findViewById(R.id.resltcnt);
        EditText qfield =  findViewById(R.id.editq);
        Button btnTab = findViewById(R.id.setable);
        TextView resultVw = findViewById(R.id.searchResult);
        counter++;
        if(counter <= keys.length-1)
      {
          //num_results.setText(rcount+"("+String.valueOf(counter)+")");
          num_results.setText(String.format("%s(%d)",rcount,counter+1));
          String first = qaentries.get(keys[counter]);
          resultVw.setText(first.split("#")[0]);
          if(first.split("#").length>1)
              btnTab.setEnabled(true);
             else btnTab.setEnabled(false);

                 if (!imageurls.get(keys[counter]).equals(""))
                     btnImage.setEnabled(true);
                 else btnImage.setEnabled(false);

          qfield.setText(keys[counter].toString());

      }
        else
        {
            counter = 0;
            //num_results.setText(rcount+"("+String.valueOf(counter)+")");
            num_results.setText(String.format("%s(%d)",rcount,counter+1));
            String first = qaentries.get(keys[counter]);
            resultVw.setText(first.split("#")[0]);
            if(first.split("#").length>1)
                btnTab.setEnabled(true);
            else btnTab.setEnabled(false);
            if(!imageurls.get(keys[counter]).equals(""))
                btnImage.setEnabled(true);
            else btnImage.setEnabled(false);
            qfield.setText(keys[counter].toString());
        }
    }
    public void Back_click(View vw)
    {
        TextView num_results = findViewById(R.id.resltcnt);
        if(qaentries.isEmpty()) return;
        EditText qfield =  findViewById(R.id.editq);
        TextView resultVw = findViewById(R.id.searchResult);
        Button btnTab = findViewById(R.id.setable);
        if(counter==0) counter=keys.length;
        counter--;
        //num_results.setText(rcount+"("+String.valueOf(counter)+")");
        num_results.setText(String.format("%s(%d)",rcount,counter+1));
        String first = qaentries.get(keys[counter]);
        resultVw.setText(first.split("#")[0]);
        if(first.split("#").length>1)
            btnTab.setEnabled(true);
        else btnTab.setEnabled(false);
        if(!imageurls.get(keys[counter]).equals(""))
            btnImage.setEnabled(true);
        else btnImage.setEnabled(false);

        qfield.setText(keys[counter].toString());

    }
    protected void startProgress(String msg)
    {
        pDialog = new ProgressDialog(Active.this);
        pDialog.setMessage(msg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
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
    protected boolean testCourseID(String courseid)
    {
        boolean exist = false;
        try {

            Uri content = Uri.parse(CachedContent.url_courses);
            String[] projection = {CachedContent.coursecode};
            String selClause = CachedContent.coursecode + " =?";
            String[] args = {courseid};
            Cursor c = getApplicationContext().getContentResolver().query(content, projection, selClause, args, CachedContent.coursecode);
            if (c != null && c.getCount() > 0) exist = true;

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return exist;
    }
    public void getTables(View view)
    {
      try {
          Intent intent = new Intent("ikenna.mobi.mitpqsolutions.TableDisplay");
          if (qaentries.isEmpty()) return;
          String tableinfo = qaentries.get(keys[counter]);
          String colnum = tableinfo.split("@", 3)[0].split("#")[1];
          String rownum = tableinfo.split("@", 3)[1];
          String content = tableinfo.split("@", 3)[2];
          intent.putExtra("colsize", colnum);
          intent.putExtra("rowsize", Integer.valueOf(rownum));
          intent.putExtra("content", content);
          startActivity(intent);
      }
      catch(Exception ex)
      {
          Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
      }

    }
    public void btnClearClicked(View view)
    {
     EditText editText  = findViewById(R.id.editq);
     TextView answervw  = findViewById(R.id.searchResult);
     Button forTable = findViewById(R.id.setable);
     Button forImage = findViewById(R.id.seeimg);
     TextView label = findViewById(R.id.resltcnt);
     editText.getText().clear();
     answervw.setText("");
     forTable.setEnabled(false);
     forImage.setEnabled(false);
     label.setText("");

    }
    public void imgvwrClicked(View view)
    {
      Intent intent = new Intent("ikenna.mobi.mitpqsolutions.ImageViewer");
      if(imageurls.isEmpty()) return;
      intent.putExtra("imageurl",imageurls.get(keys[counter]));
      startActivity(intent);
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
         Spinner spinner = findViewById(R.id.courses);
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,codes);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
        }

    }
    protected String getLocalPath(String url)
    {
        if(url.equals("")) return "";
        String name = url.substring( url.lastIndexOf('/')+1, url.length() );
        //String appFolder = "PQDoctor";
       // ContextWrapper cw = new ContextWrapper(getApplicationContext());
       // File path = new File(Environment.getExternalStorageDirectory() + "/Download/PQDoctor/");
        File path = new File(new File(Environment.getExternalStorageDirectory(),"Download"),"PQDoctor");
        if(!path.exists())
            path.mkdir();
        File imageFile = new File(path, name);
        path.mkdirs();
        return url.split("&")[0]+"&"+imageFile.getAbsolutePath();
    }
    public void saveImage(String url )
    {
        File imageFile = null;
        try
        {
            String name = url.substring( url.lastIndexOf('/')+1, url.length() );
            URL imageurl = new URL(url.split("&")[1]);
            Bitmap bm = BitmapFactory.decodeStream(imageurl.openConnection().getInputStream());
            //String appFolder = "profile";
            //File path = new File(Environment.getExternalStorageDirectory() + "/Download/PQDoctor/");
            File path = new File(new File(Environment.getExternalStorageDirectory(),"Download"),"PQDoctor");
            if(!path.exists())
                path.mkdir();
            imageFile = new File(path, name);

            FileOutputStream out = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(getApplicationContext(),new String[] { imageFile.getAbsolutePath() }, null,new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
        //Toast.makeText(getApplicationContext(),"Images saved to "+imageFile,Toast.LENGTH_SHORT).show();
    }
    public void btnShowFull(View view)
    {

     EditText editText = findViewById(R.id.editq);
     TextView tview = findViewById(R.id.searchResult);
     if(editText.getText().toString().length()==0) return;
     Intent forward = new Intent("ikenna.mobi.mitpqsolutions.QAShow");
     forward.putExtra("question",editText.getText().toString());
     forward.putExtra("answer",tview.getText().toString());
     startActivity(forward);

    }
}
