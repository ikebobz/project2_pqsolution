package ikenna.mobi.mitpqsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class QAShow extends AppCompatActivity
{


   protected String question,answer;
   @Override public void onCreate(Bundle saved)
{
   super.onCreate(saved);
   setContentView(R.layout.qashow);
   Intent received = getIntent();
   TextView textView = findViewById(R.id.qbody);
   question = received.getStringExtra("question");
   answer = received.getStringExtra("answer");
   textView.setText(received.getStringExtra("question"));
}
  public void showAnswer(View view)
  {
     TextView textView = findViewById(R.id.qbody);
     if(textView.getText().equals(question))
     textView.setText(answer);
     else textView.setText(question);

  }

}
