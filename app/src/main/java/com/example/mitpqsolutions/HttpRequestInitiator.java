package com.example.mitpqsolutions;

import android.net.Uri;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpRequestInitiator //class that would make request for data
{
 HttpRequestInitiator()
 {

 }
 private InputStream istream = null;
 private JSONObject jsonobj = null;
 private String json = "";
 private HttpURLConnection urlcon = null;
 public JSONObject startRequest(String resource,String method, Map<String,String> parameters)
 {
  try
  {
      Uri.Builder builder = new Uri.Builder();
      URL urlObj;
      String encodedParams = "";
      if (parameters != null) {
          for (Map.Entry<String, String> entry : parameters.entrySet()) {
              builder.appendQueryParameter(entry.getKey(), entry.getValue());
          }
      }
      if (builder.build().getEncodedQuery() != null) {
          encodedParams =  builder.build().getEncodedQuery();

      }
      if ("GET".equals(method)) {
          resource = resource + "?" + encodedParams;
          urlObj = new URL(resource);
          urlcon = (HttpURLConnection) urlObj.openConnection();
          urlcon.setRequestMethod(method);


      } else {
          urlObj = new URL(resource);
          urlcon = (HttpURLConnection) urlObj.openConnection();
          urlcon.setRequestMethod(method);
          urlcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
          urlcon.setRequestProperty("Content-Length", String.valueOf(encodedParams.getBytes().length));
          urlcon.getOutputStream().write(encodedParams.getBytes());
      }


      urlcon.connect();
      istream = urlcon.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
          sb.append(line + "\n");
      }
      istream.close();
      json = sb.toString();
      jsonobj = new JSONObject(json);




  }
  catch(Exception ex)
  {
     ex.printStackTrace();
  }
  return jsonobj;
 }
}
