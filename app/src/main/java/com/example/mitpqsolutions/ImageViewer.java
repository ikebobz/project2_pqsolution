package com.example.mitpqsolutions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageViewer extends AppCompatActivity
{
    String url;
    ImageView imgvw;
    Drawable d;
@Override public void onCreate(Bundle saved)
{
  super.onCreate(saved);
  setContentView(R.layout.imageview);
  Intent intent = getIntent();
  String qlabel = intent.getStringExtra("imageurl").split("&")[0];
  url = intent.getStringExtra("imageurl").split("&")[1];
  TextView tview = findViewById(R.id.qlabel);
  tview.setText(qlabel);
  imgvw = findViewById(R.id.imageview1);
  new backgroundTask().execute();
}
class backgroundTask extends AsyncTask<String,String,String>
{
  @Override protected String doInBackground(String... params)
  {
      try
      {
          InputStream is = ((InputStream) new URL(url).getContent());
           d = Drawable.createFromStream(is, "erd.png");
      }
      catch(MalformedURLException ex)
      {
          Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT);
      }
      catch(IOException e)
      {
          Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);

      }

      return null;
  }
  @Override protected void onPostExecute(String result)
  {
      imgvw.setImageDrawable(d);
  }

}
}
