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
import org.json.JSONObject;
import java.util.HashMap;

public class Registration extends AppCompatActivity {

    ProgressDialog pDialog;
    HashMap<String, String> parameters;
    String encrypted_phrase;
    JSONObject jsonobj;
    private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.registration);
    }

    public void register_click(View view) {
        EditText txtName = findViewById(R.id.txtName);
        EditText txtEmail = findViewById(R.id.txtEmail);
        EditText txtPwd = findViewById(R.id.txtPwd);
        String fullnames = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPwd.getText().toString();
        try {
            encrypted_phrase = AESCrypt.Encrypt(password);

            if (new PingNetworkStatus().checkConnectionState(getApplicationContext())) {
                parameters = new HashMap<>();
                parameters.put("names", fullnames);
                parameters.put("email", email);
                parameters.put("pass", encrypted_phrase);
                parameters.put("key",getKey());
                // initiateProgress();
                new RegisterTask().execute();


            } else
                Toast.makeText(this, "unable to establish remote connection", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {

        }



    }

    protected void initiateProgress() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Registering User. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    class RegisterTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpRequestInitiator hri = new HttpRequestInitiator();
            jsonobj = hri.startRequest(Registration.BASE_URL + "addEntry.php", "POST", parameters);

            return null;
        }

        @Override
        protected void onPreExecute() {
            initiateProgress();
        }

        @Override
        protected void onPostExecute(String code) {
            try {

                // if(jsonobj.getInt("success")==1)
                Toast.makeText(getApplicationContext(), jsonobj.getString("message"), Toast.LENGTH_LONG).show();


                /*if (jsonobj.getInt("success") == 1)
                    Toast.makeText(getApplicationContext(), jsonobj.getString("message"), Toast.LENGTH_SHORT).show();*/



                if (jsonobj.getInt("success") == 1) {
                    Intent intent = new Intent(Registration.this, Login.class);
                    startActivity(intent);
                }
            } catch (Exception ex) {

            }
            pDialog.dismiss();
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
