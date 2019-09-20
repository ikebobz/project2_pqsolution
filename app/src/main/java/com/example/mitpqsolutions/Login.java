package com.example.mitpqsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Login extends AppCompatActivity
{
 @Override public void onCreate(Bundle saved)
 {
  super.onCreate(saved);
  setContentView(R.layout.activity_login);
 }
 public void register_click(View view)
 {
  startActivity(new Intent("com.example.mitpqsolutions.Registration"));
 }
 public void login_click(View view)
 {
  startActivity(new Intent("com.example.mitpqsolutions.Active"));
 }
}
