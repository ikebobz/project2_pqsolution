package ikenna.mobi.mitpqsolutions;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SolRequest extends AppCompatActivity {
    HashMap<String, String> parameters;
    ProgressDialog pDialog;
    JSONObject jsonobj;
    private static final String BASE_URL = "http://nanawebapps.com/nanawebapps.com/";


    @Override public void onCreate(Bundle saved)
    {
     super.onCreate(saved);
     setContentView(R.layout.solrequest);
    }
    public void uploadQ(View view)
    {
        EditText et1 = findViewById(R.id.sr_uname);
        EditText et2 = findViewById(R.id.sr_q);
        String dop = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String uName =et1.getText().toString();
        String que = et2.getText().toString();

        try {


            if (new PingNetworkStatus().checkConnectionState(getApplicationContext())) {
                parameters = new HashMap<>();
                parameters.put("uname", uName);
                parameters.put("question", que);
                parameters.put("dop", dop);

                // initiateProgress();
                new UploadTask().execute();


            } else
                Toast.makeText(this, "unable to establish remote connection", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {

        }


    }
    protected void initiateProgress()
    {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Submitting question.Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }
    class UploadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpRequestInitiator hri = new HttpRequestInitiator();
            jsonobj = hri.startRequest(BASE_URL + "addEntry.php", "POST", parameters);

            return null;
        }

        @Override
        protected void onPreExecute() {
            initiateProgress();
        }

        @Override
        protected void onPostExecute(String code) {
            try
            {

                Toast.makeText(getApplicationContext(), jsonobj.getString("message"), Toast.LENGTH_LONG).show();

            } catch (Exception ex) {

            }
            pDialog.dismiss();
        }
    }

}
