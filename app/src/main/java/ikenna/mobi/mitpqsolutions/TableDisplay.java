package ikenna.mobi.mitpqsolutions;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TableDisplay extends AppCompatActivity
{
 @Override public void onCreate(Bundle saved) {
     try {
         super.onCreate(saved);
         setContentView(R.layout.tablelay);
         Intent intent = getIntent();
         String colsize = intent.getStringExtra("colsize");
         String[] tabNames = colsize.split(",");
         int rowsize = intent.getIntExtra("rowsize", 0);
         String content = intent.getStringExtra("content");
         LinearLayout container = findViewById(R.id.container);
         String[] tables = content.split("&");
         LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         params.setMargins(0, 20, 0, 0);
         LinearLayout.LayoutParams tbparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         tbparams.setMargins(10, 5, 10, 0);
         int count = 1;
         int tabcount = 0;
         for (String table : tables) {
             TextView txtview = new TextView(this);
             txtview.setGravity(Gravity.CENTER);
             txtview.setLayoutParams(params);
             txtview.setText(tabNames[tabcount]);
             txtview.setTypeface(null, Typeface.BOLD);
             container.addView(txtview);
             String[] rows = table.split(";");
             TableLayout dataTable = new TableLayout(this);
             dataTable.setLayoutParams(tbparams);
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
                     tview.setPadding(7, 0, 0, 0);
                     if (k == 0) tview.setTypeface(null, Typeface.BOLD);
                     tview.setText(colValues[i]);
                     row.addView(tview);
                 }
                 dataTable.addView(row);
             }
             container.addView(dataTable);
             count++;
             tabcount++;
         }

     }
     catch(Exception ex)
     {
         Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
     }

 }



}
