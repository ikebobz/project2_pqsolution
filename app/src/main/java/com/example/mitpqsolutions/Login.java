package com.example.mitpqsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity
{
 @Override public void onCreate(Bundle saved)
 {
  super.onCreate(saved);
  setContentView(R.layout.activity_login);
 }
 JSONObject jsonobj;
 HashMap<String,String> credentials;
 private static final String BASE_URL = "http://10.0.2.2:8082/pqdoctor/";

 public void register_click(View view)
 {
  startActivity(new Intent("com.example.mitpqsolutions.Registration"));
 }
 public void login_click(View view)
 {
  EditText email = findViewById(R.id.txtEmail);
  EditText password = findViewById(R.id.txtPwd);
  String str_email = email.getText().toString();
  String str_pass = password.getText().toString();
  try
  {
   String passcrypt = AESCrypt.Encrypt(str_pass);
   if (new PingNetworkStatus().checkConnectionState(getApplicationContext())) {
    //initiateProgress();
    credentials = new HashMap<>();
    credentials.put("email",str_email);
    credentials.put("pass",passcrypt);
    Thread secondary = new Thread(new Runnable() {
     @Override
     public void run() {
      HttpRequestInitiator hri = new HttpRequestInitiator();
      jsonobj = hri.startRequest(BASE_URL + "authorize.php", "GET", credentials);

     }
    });
    secondary.start();
    secondary.join();
    if (jsonobj.getInt("success") == 1)
    {
     JSONArray array = jsonobj.getJSONArray("data");
     if (array.length()==0)
     {
      Toast.makeText(this,"User does not exist",Toast.LENGTH_LONG).show();
      return;
     }
     JSONObject jobj = array.getJSONObject(0);
     if(jobj.getString("email").equals(str_email)&& jobj.getString("pass").equals(passcrypt))
      startActivity(new Intent("com.example.mitpqsolutions.Active"));
     else Toast.makeText(this,"Wrong Username/Password",Toast.LENGTH_LONG).show();

    }
   } else
    Toast.makeText(this,jsonobj.getString("message"), Toast.LENGTH_LONG).show();

  }
  catch(Exception ex)
  {
    ex.printStackTrace();
  }

 }
}
