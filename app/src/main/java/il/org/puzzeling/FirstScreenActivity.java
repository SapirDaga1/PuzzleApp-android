package il.org.puzzeling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;
import java.util.Set;

public class
        FirstScreenActivity extends AppCompatActivity {
    Button playBtn;
    Button aboutBtn;
    Button recordsBtn;
    static boolean isMuted =false;
    SharedPreferences sp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstscreen);

        playBtn=findViewById(R.id.play_btn);
        aboutBtn=findViewById(R.id.about_btn);
        recordsBtn=findViewById(R.id.recordsBtn);

        sp = getSharedPreferences("music",MODE_PRIVATE);
        manageMusic(false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.sound_icon);
        if (isMuted)
            item.setIcon(R.drawable.ic_no_music_btn);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sound_icon:
                isMuted = !isMuted;

                if (isMuted)
                {
                    //Pause/Stop music
                    manageMusic(true);
                    item.setIcon(R.drawable.ic_no_music_btn);
                }
                else
                {
                    //Recover music
                    manageMusic(false);
                    item.setIcon(R.drawable.ic_music_btn);
                }
                break;
            case R.id.home_icon:
                Toast.makeText(FirstScreenActivity.this, R.string.homepage, Toast.LENGTH_SHORT).show();


        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("music", isMuted).commit();
        manageMusic(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manageMusic(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        manageMusic(false);
    }

    public void manageMusic(boolean forceShutdown) {
        if ( isMuted || forceShutdown)
            MusicPlayer.pause();
        else
            MusicPlayer.start(this, MusicPlayer.MUSIC_MENU);
    }
}
