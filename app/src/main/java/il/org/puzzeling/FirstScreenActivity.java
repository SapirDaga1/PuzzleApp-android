package il.org.puzzeling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FirstScreenActivity extends AppCompatActivity {
    Button playBtn;
    Button aboutBtn;
    Button recordsBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firstscreen);

        playBtn=findViewById(R.id.play_btn);
        aboutBtn=findViewById(R.id.about_btn);
        recordsBtn=findViewById(R.id.recordsBtn);

        //moving to puzzleActivity
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Intent intent= new Intent(FirstScreenActivity.this,MainActivity.class);
                startActivity(intent);

            }
        });

        //open table of records
        recordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //open information about the game
        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
