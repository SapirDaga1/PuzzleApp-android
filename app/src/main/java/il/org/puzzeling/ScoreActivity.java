package il.org.puzzeling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.ArrayList;
import java.util.Collections;
import static il.org.puzzeling.FirstScreenActivity.isMuted;

public class ScoreActivity extends AppCompatActivity {
    SharedPreferences mUserInfo;
    ListViewAdapter mAdapter;
    ArrayList<UserInfo> mUsers = new ArrayList<>();
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        sp = getSharedPreferences("music",MODE_PRIVATE);
        manageMusic(false);

        ListView listView = findViewById(R.id.listView);

        mUserInfo = getSharedPreferences("users", MODE_PRIVATE);

        int size = mUserInfo.getInt("size", 0);

        /*<-------Getting all the user's info from the device and put it in an array------->*/
        for (int i = 1; i <= size; i++) {
            mUsers.add(new UserInfo(mUserInfo.getString("userName_" + i, "Unknown"),
                    mUserInfo.getInt("userScore_" + i, 0),
                    mUserInfo.getString("userTime_" + i,"00:00"),
                    mUserInfo.getString("userPuzzleImg_" + i, "null"),
                    mUserInfo.getBoolean("userIsAsset_" + i, false)));
        }

        Collections.sort(mUsers);

        mAdapter = new ListViewAdapter(this, R.layout.user_score_layout, mUsers);
        listView.setAdapter(mAdapter);
    }

    public void manageMusic(boolean forceShutdown) {
        if ( isMuted || forceShutdown)
            MusicPlayer.pause();
        else
            MusicPlayer.start(this, MusicPlayer.MUSIC_MENU);
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
                    manageMusic(true);
                    item.setIcon(R.drawable.ic_no_music_btn);
                }
                else
                {
                    manageMusic(false);
                    item.setIcon(R.drawable.ic_music_btn);
                }
                break;
            case R.id.home_icon:
                Intent intent = new Intent(ScoreActivity.this, FirstScreenActivity.class);
                finishAffinity();
                startActivity(intent);
                Animatoo.animateSlideRight(ScoreActivity.this);
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
        Animatoo.animateZoom(ScoreActivity.this);
    }
}