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

public class FullView extends AppCompatActivity
{
    String course;
    private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";
    private ProgressDialog pDialog;
    private JSONObject jsObject;
    private HashMap<String,String> parm;
    HashMap<String,String> qaentries = new HashMap<>();
    HashMap<String,String> imageurls = new HashMap<>();
    TextView resultVw;
    Object[] keys;
    Button btnImage;
    int counter;
    static boolean runoffline = false;
    String rcount;
@Override
    public void onCreate(Bundle saved)
{
  super.onCreate(saved);
  setContentView(R.layout.fullview);
  course = getIntent().getStringExtra("course");
    Button btnTab = findViewById(R.id.fv_table);
    btnTab.setEnabled(false);
    btnImage = findViewById(R.id.fv_image);
    btnImage.setEnabled(false);
    try
    {
       getQA();
    }
    catch(Exception ex)
    {
      Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
    }


}
    @Override protected void onSaveInstanceState(Bundle packet)
    {
        try {
            super.onSaveInstanceState(packet);
            TextView display = findViewById(R.id.fv_qbody);
            packet.putString("result", display.getText().toString());
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
            TextView display = findViewById(R.id.qbody);
            display.setText(packet.getString("result"));

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    String imageurl;
    public void getQA()
    {
        counter = 0;
        resultVw = findViewById(R.id.fv_qbody);
        String question = "%%%";
        final Button txt_rescnt = findViewById(R.id.fv_resultc);
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
                parm.put("course",course);
                Thread primary = new Thread(new Runnable(){
                    @Override
                    public void run()
                    {

                        HttpRequestInitiator hri = new HttpRequestInitiator();
                        jsObject = hri.startRequest(BASE_URL+"getanswer.php","GET",parm);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button btnTab = findViewById(R.id.fv_table);
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
                                                    addItem(qid, course, qdesc, answer + "#" + combined, getLocalPath(imageurl));
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            saveImage(imageurl);
                                                        }
                                                    }).start();

                                                }
                                            }

                                            keys = qaentries.keySet().toArray();
                                           // qbox.setText(keys[0].toString());
                                            String answer = qaentries.get(keys[0]);
                                            if(answer.split("#").length>1)
                                                btnTab.setEnabled(true);
                                            else btnTab.setEnabled(false);
                                            if(!imageurls.get(keys[0]).equals(""))
                                                btnImage.setEnabled(true);
                                            else btnImage.setEnabled(false);
                                            resultVw.setText(keys[0].toString());
                                            rcount = String.valueOf(keys.length);
                                            txt_rescnt.setText(rcount);

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
                searchLocal(question,course);// end of if construct
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public void addItem(String ID,String course,String que,String ans,String imageurl)
    {
        //Toast.makeText(getBaseContext(),imageurl,Toast.LENGTH_SHORT).show();
        ContentValues values = new ContentValues();
        values.put(CachedContent.QID,ID);
        values.put(CachedContent.COURSE,course);
        values.put(CachedContent.QDESC,que);
        values.put(CachedContent.QANS,ans);
        values.put(CachedContent.IMAGEURL,imageurl);
        Uri track_uri = getContentResolver().insert(CachedContent.CONTENT_URI,values);
        if(track_uri!=null)
            Toast.makeText(getBaseContext(),"Answer Cached Successfully",Toast.LENGTH_SHORT).show();
        else  Toast.makeText(getBaseContext(),"Answer Caching Failed",Toast.LENGTH_SHORT).show();


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
        Button txt_rescnt = findViewById(R.id.fv_resultc);
        Button btnTab = findViewById(R.id.fv_table);
        TextView resultVw = findViewById(R.id.fv_qbody);
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
            rcount = String.valueOf(keys.length);
            txt_rescnt.setText(rcount);
            ret = true;
        }

        return ret;
    }
    public void fvForward_click(View vw)
    {
        if(qaentries.isEmpty()) return;
        Button num_results = findViewById(R.id.fv_resultc);
        Button btnTab = findViewById(R.id.fv_table);
        TextView resultVw = findViewById(R.id.fv_qbody);
        counter++;
        if(counter <= keys.length-1)
        {
            //num_results.setText(rcount+"("+String.valueOf(counter)+")");
            num_results.setText(String.format("%s(%d)",rcount,counter+1));
            String first = qaentries.get(keys[counter]);
            resultVw.setText(keys[counter].toString());
            if(first.split("#").length>1)
                btnTab.setEnabled(true);
            else btnTab.setEnabled(false);

            if (!imageurls.get(keys[counter]).equals(""))
                btnImage.setEnabled(true);
            else btnImage.setEnabled(false);



        }
        else
        {
            counter = 0;
            //num_results.setText(rcount+"("+String.valueOf(counter)+")");
            num_results.setText(String.format("%s(%d)",rcount,counter+1));
            String first = qaentries.get(keys[counter]);
            resultVw.setText(keys[counter].toString());
            if(first.split("#").length>1)
                btnTab.setEnabled(true);
            else btnTab.setEnabled(false);
            if(!imageurls.get(keys[counter]).equals(""))
                btnImage.setEnabled(true);
            else btnImage.setEnabled(false);

        }
    }
    public void fvBack_click(View vw)
    {
        Button num_results = findViewById(R.id.fv_resultc);
        if(qaentries.isEmpty()) return;
        TextView resultVw = findViewById(R.id.fv_qbody);
        Button btnTab = findViewById(R.id.fv_table);
        if(counter==0) counter=keys.length;
        counter--;
        //num_results.setText(rcount+"("+String.valueOf(counter)+")");
        num_results.setText(String.format("%s(%d)",rcount,counter+1));
        String first = qaentries.get(keys[counter]);
        resultVw.setText(keys[counter].toString());
        if(first.split("#").length>1)
            btnTab.setEnabled(true);
        else btnTab.setEnabled(false);
        if(!imageurls.get(keys[counter]).equals(""))
            btnImage.setEnabled(true);
        else btnImage.setEnabled(false);



    }
    protected void startProgress(String msg)
    {
        pDialog = new ProgressDialog(FullView.this);
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

    public void fvgetTables(View view)
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

    public void fvimgvwrClicked(View view)
    {
        Intent intent = new Intent("ikenna.mobi.mitpqsolutions.ImageViewer");
        if(imageurls.isEmpty()) return;
        intent.putExtra("imageurl",imageurls.get(keys[counter]));
        startActivity(intent);
    }

    protected String getLocalPath(String url)
    {
        if(url.equals("")) return "";
        String name = url.substring( url.lastIndexOf('/')+1, url.length() );
        //String appFolder = "PQDoctor";
        // ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File path = new File(Environment.getExternalStorageDirectory() + "/Download/PQDoctor/");
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
            File path = new File(Environment.getExternalStorageDirectory() + "/Download/PQDoctor/");
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
    public void fvtoggle(View view)
    {
        TextView textView = findViewById(R.id.fv_qbody);
        if(textView.getText().equals(keys[counter].toString()))
            textView.setText(qaentries.get(keys[counter]).split("#")[0]);
        else textView.setText(keys[counter].toString());

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
            case R.id.mnu_signout: Intent intent = new Intent(FullView.this,Login.class);
                startActivity(intent);
                finish();
                return true;
        }
        return true;
    }
}
