package com.example.mitpqsolutions;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class PingNetworkStatus
{
  public boolean checkConnectionState(Context context)
  {
      ConnectivityManager manager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
      NetworkInfo information = manager.getActiveNetworkInfo();
      return information!=null && information.isConnected();
  }

}
