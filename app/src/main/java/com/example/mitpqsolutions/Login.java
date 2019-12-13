package com.example.mitpqsolutions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity
{
 ProgressDialog pdialog;
 String str_email,str_pass,passcrypt;
 @Override public void onCreate(Bundle saved)
 {
  super.onCreate(saved);
  setContentView(R.layout.activity_login);
 }
 JSONObject jsonobj;
 HashMap<String,String> credentials;
 private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";

 public void register_click(View view)
 {
  startActivity(new Intent("com.example.mitpqsolutions.Registration"));
 }
 public void login_click(View view)
 {
  EditText email = findViewById(R.id.txtEmail);
  EditText password = findViewById(R.id.txtPwd);
   str_email = email.getText().toString();
   str_pass = password.getText().toString();
  try
  {
    passcrypt = AESCrypt.Encrypt(str_pass);
   if (new PingNetworkStatus().checkConnectionState(getApplicationContext())) {
    //initiateProgress();
    credentials = new HashMap<>();
    credentials.put("email", str_email);
    credentials.put("pass", passcrypt);
    //credentials.put("key",getKey());
    new LoginTask().execute();
   } else Toast.makeText(this,"Could not establish remote connection",Toast.LENGTH_SHORT).show();
  }
  catch(Exception ex)
  {
    ex.printStackTrace();
  }

 }
 protected void initProgress()
 {
  pdialog = new ProgressDialog(this);
  pdialog.setMessage("Logging in............");
  pdialog.setIndeterminate(false);
  pdialog.setCancelable(true);
  pdialog.show();
 }
 class LoginTask extends AsyncTask<String,String,String>
 {
  @Override
  protected void onPreExecute()
  {
   initProgress();
  }
  @Override
  protected String doInBackground(String... params)
  {
   HttpRequestInitiator hri = new HttpRequestInitiator();
   jsonobj = hri.startRequest(BASE_URL + "authorize.php", "GET", credentials);
   return "";
  }
  @Override
  protected void onPostExecute(String result)
  {
   try
   {
    if (jsonobj.getInt("success") == 1)
    {
     JSONArray array = jsonobj.getJSONArray("data");
     if (array.length()==0)
     {
      Toast.makeText(getApplicationContext(),"User does not exist",Toast.LENGTH_SHORT).show();
      pdialog.dismiss();
      return;
     }
     JSONObject jobj = array.getJSONObject(0);
     if(jobj.getString("email").equals(str_email)&& jobj.getString("pass").equals(passcrypt))
     {
      if(jobj.getString("key").equals(getKey()))
      startActivity(new Intent("com.example.mitpqsolutions.Active"));
      else Toast.makeText(getApplicationContext(),"Credentials does not match install ID",Toast.LENGTH_SHORT);
     }
     else Toast.makeText(getApplicationContext(),"Wrong Username/Password",Toast.LENGTH_SHORT).show();

    } else Toast.makeText(getApplicationContext(),jsonobj.getString("message"), Toast.LENGTH_SHORT).show();
    pdialog.dismiss();
   }
   catch(JSONException ex)
   {
    ex.printStackTrace();
   }
  }
 }
 protected String getKey()
 {
  String key = "";
  try {
   Uri content = Uri.parse(CachedContent.installInfoURL);
   String[] projection = {CachedContent.INSTALLID};
   String selClause = "";
   String[] args = null;
   Cursor c = getApplicationContext().getContentResolver().query(content, projection, selClause, args, CachedContent.INSTALLID);
   int index = c.getColumnIndex(CachedContent.INSTALLID);
   if (c.moveToNext())
    key = c.getString(index);

   //Toast.makeText(getApplicationContext(), key, Toast.LENGTH_SHORT);

  }
  catch(Exception ex)
  {
   ex.printStackTrace();
  }
  return key;

 }

}
