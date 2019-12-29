package com.example.mitpqsolutions;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageViewer extends AppCompatActivity
{
    String url;
    ImageView imgvw;
    Drawable d;
    boolean success = true;
@Override public void onCreate(Bundle saved)
{
  try {
      super.onCreate(saved);
      setContentView(R.layout.imageview);
      Intent intent = getIntent();
      String qlabel = intent.getStringExtra("imageurl").split("&")[0];
      url = intent.getStringExtra("imageurl").split("&")[1];
      TextView tview = findViewById(R.id.qlabel);
      tview.setText(qlabel);
      imgvw = findViewById(R.id.imageview1);
      if(url.contains("http:"))
      new backgroundTask().execute();
      else imgvw.setImageBitmap(BitmapFactory.decodeFile(url));
     // Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
  }
  catch(Exception ex)
  {
   Toast.makeText(getApplicationContext(),"image could not retrieved!",Toast.LENGTH_SHORT);
  }
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
          Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
      }
      catch(IOException e)
      {
          Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

      }
      catch(Exception e)
      {
         success = false;
      }

      return null;
  }
  @Override protected void onPostExecute(String result)
  {
      if(!success)
      {
          Toast.makeText(getApplicationContext(),"remote image unreachable",Toast.LENGTH_SHORT).show();
          return;
      }
      imgvw.setImageDrawable(d);
  }

}

}
