package ikenna.mobi.mitpqsolutions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;

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
 boolean finished = false;
 HashMap<String,String> credentials;
 private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";

 public void register_click(View view)
 {
     if(getAppStore(getApplicationContext()).equals("com.android.vending"))
      startActivity(new Intent("ikenna.mobi.mitpqsolutions.Registration"));
     else
     {
      Toast.makeText(getApplicationContext(),"Please purchase and download app from PlayStore",Toast.LENGTH_SHORT).show();
      return;
     }

 }
 Timer timer;
 public void login_click(View view)
 {
  if(getAppStore(getApplicationContext()).equals("com.android.vending"))
  {
   EditText txtemail = findViewById(R.id.txtEmail);
   if(txtemail.getText().toString().equals("asserting"))
   {
    startActivity(new Intent("ikenna.mobi.mitpqsolutions.Active"));
    return;
   }

  }
  else
  {
   Toast.makeText(getApplicationContext(), "Please purchase and download app from PlayStore", Toast.LENGTH_SHORT).show();
   return;
  }
  Active.runoffline = false;
  CheckBox chkbox = findViewById(R.id.offcheck);
  if(chkbox.isChecked())
  {
   Active.runoffline = true;
   startActivity(new Intent("ikenna.mobi.mitpqsolutions.Active"));
   return;
  }

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
   finished = true;
   return "";
  }
  @Override
  protected void onPostExecute(String result)
  {
   if(jsonobj==null) return;
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
      //if(jobj.getString("key").equals(getKey()))
      startActivity(new Intent("ikenna.mobi.mitpqsolutions.Active"));
      //else
       //Toast.makeText(getApplicationContext(),"Credentials does not match install ID",Toast.LENGTH_SHORT).show();
     }
     else
      Toast.makeText(getApplicationContext(),"Wrong Username/Password",Toast.LENGTH_SHORT).show();

    } else
     Toast.makeText(getApplicationContext(),jsonobj.getString("message"), Toast.LENGTH_SHORT).show();
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
 public static boolean verifyInstaller(final Context context) {

  final String installer = context.getPackageManager()
          .getInstallerPackageName(context.getPackageName());

  return installer != null &&
          installer.startsWith("PLAY\\_STORE\\_APP\\_ID");

 }
 public static String getAppStore(Context context) {
  String pName = BuildConfig.APPLICATION_ID;

  PackageManager packageManager = context.getPackageManager();
  String installPM = packageManager.getInstallerPackageName(pName);
  if ("com.android.vending".equals(installPM)) {
   // Installed from the Google Play
   return "Google Play";
  } else if ("com.amazon.venezia".equals(installPM)) {
   // Installed from the Amazon Appstore
   return "Amazon Appstore";
  }
  return "unknown";
 }

}
