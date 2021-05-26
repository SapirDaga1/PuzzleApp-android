package il.org.puzzeling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;
import java.util.Set;

public class FirstScreenActivity extends AppCompatActivity {
    Button playBtn;
    Button aboutBtn;
    Button recordsBtn;
    static boolean musicClicked=true;
    MediaPlayer mediaPlayer;
    SharedPreferences sp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       setContentView(R.layout.activity_firstscreen);

        playBtn=findViewById(R.id.play_btn);
        aboutBtn=findViewById(R.id.about_btn);
        recordsBtn=findViewById(R.id.recordsBtn);
        //Initiate Music Background
        mediaPlayer = MediaPlayer.create(this,R.raw.feeling_free);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(100,100);
        mediaPlayer.start();
        FloatingActionButton musicBtn = findViewById(R.id.musicButton);
        musicBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (musicClicked)
                {
                    //Pause/Stop music
                    musicClicked=!musicClicked;
                    FloatingActionButton i = new FloatingActionButton(FirstScreenActivity.this);
                    i = findViewById(R.id.musicButton);
                    i.setImageResource(R.drawable.music_off);
                    mediaPlayer.pause();

                }
                else
                {
                    //Recover music
                    musicClicked=!musicClicked;
                    FloatingActionButton i = new FloatingActionButton(FirstScreenActivity.this);
                    i = findViewById(R.id.musicButton);
                    i.setImageResource(R.drawable.music_on);
                    mediaPlayer.start();

                }
            }
        });
        //moving to puzzleActivity
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(FirstScreenActivity.this,MainActivity.class);
                sp = getSharedPreferences("music",MODE_PRIVATE);
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

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("music",musicClicked).commit();
        mediaPlayer.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
