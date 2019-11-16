package com.example.mitpqsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TableDisplay extends AppCompatActivity
{
 @Override public void onCreate(Bundle saved)
 {
   super.onCreate(saved);
   setContentView(R.layout.tablelay);
   Intent intent = getIntent();
   int colsize = intent.getIntExtra("colsize",0);
   int rowsize = intent.getIntExtra("rowsize",0);
   String content = intent.getStringExtra("content");
   LinearLayout container = findViewById(R.id.container);
   String[] tables = content.split(" ");
   LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
   params.setMargins(0,20,0,0);
   int count =1;
   for(String table:tables)
   {
       TextView txtview = new TextView(this);
       txtview.setGravity(Gravity.CENTER);
       txtview.setLayoutParams(params);
       txtview.setText("TABLE "+String.valueOf(count));
       container.addView(txtview);
       String[] rows = table.split(";");
       TableLayout dataTable = new TableLayout(this);
       //dataTable.setLayoutParams(params);
       dataTable.setStretchAllColumns(true);

       //populating table
       for (int k = 0; k < rows.length; k++) {
           TableRow row = new TableRow(this);
           //row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));
           row.setGravity(Gravity.CENTER);
           String[] colValues = rows[k].split(",");
           for (int i = 0; i < colValues.length; i++) {
               TextView tview = new TextView(this);
               tview.setGravity(Gravity.LEFT);
               tview.setBackgroundResource(R.drawable.cell_shape);
               //tview.setPadding(3, 3, 3, 3);
               tview.setText(colValues[i]);
               row.addView(tview);
           }
           dataTable.addView(row);
       }
       container.addView(dataTable);
       count++;
   }

 }


}
