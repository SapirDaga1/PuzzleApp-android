package il.org.puzzeling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class
FirstScreenActivity extends AppCompatActivity {
    Button playBtn;
    Button recordsBtn;

    static boolean isMuted =false;
    SharedPreferences sp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstscreen);

        playBtn=findViewById(R.id.play_btn);
        recordsBtn=findViewById(R.id.recordsBtn);

        sp = getSharedPreferences("music",MODE_PRIVATE);
        manageMusic(false);

        //moving to puzzleActivity
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(100);
                scaleAnimation.setRepeatMode(Animation.REVERSE);
                scaleAnimation.setRepeatCount(1);
                playBtn.startAnimation(scaleAnimation);
                final long sleep = 300L;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent= new Intent(FirstScreenActivity.this,MainActivity.class);
                        startActivity(intent);
                        Animatoo.animateZoom(FirstScreenActivity.this);
                    }
                }, sleep);


            }
        });
        recordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(100);
                scaleAnimation.setRepeatMode(Animation.REVERSE);
                scaleAnimation.setRepeatCount(1);
                recordsBtn.startAnimation(scaleAnimation);
                final long sleep = 300L;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent= new Intent(FirstScreenActivity.this,ScoreActivity.class);
                        startActivity(intent);
                        Animatoo.animateZoom(FirstScreenActivity.this);
                    }
                }, sleep);

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
                showToast(R.string.home_page, Gravity.BOTTOM,0,30);
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

    // Showing designed Toast
    public void showToast(int resId,int gravity, int xOffset, int yOffset){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,findViewById(R.id.toast_layout));
        TextView toast_text = layout.findViewById(R.id.toast_tv);
        toast_text.setText(resId);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(gravity,xOffset,yOffset);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }


}
