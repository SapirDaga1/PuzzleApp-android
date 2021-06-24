package il.org.puzzeling;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.GridView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;

import static il.org.puzzeling.FirstScreenActivity.isMuted;


public class MainActivity extends AppCompatActivity {
    String mCurrentPhotoPath;
    String mCurrentPhoto;
    SharedPreferences sp;
    GridView grid;
    ImageView imageView;
    boolean musicClicked;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 3;
    static final int REQUEST_IMAGE_GALLERY = 4;
    String [] items;
    int choice = 4; //default choice is easy level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("music",MODE_PRIVATE);
        manageMusic(false);
        FloatingActionButton musicBtn = findViewById(R.id.musicButton);
        if (isMuted)
            musicBtn.setImageResource(R.drawable.music_off);

        musicBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                isMuted =!isMuted;
                if (isMuted)
                {
                    //Pause/Stop music
                    manageMusic(true);
                    musicBtn.setImageResource(R.drawable.music_off);

                }
                else
                {
                    //Recover music
                    manageMusic(false);
                    musicBtn.setImageResource(R.drawable.music_on);
                }
            }
        });
        AssetManager am = getAssets();
        try {
            final String[] files = am.list("img");

            grid = findViewById(R.id.grid);
            grid.setAdapter(new ImageAdapter(this));
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), PuzzleActivity.class);
                    mCurrentPhoto = files[i % files.length];
                    intent.putExtra("assetName", mCurrentPhoto);
                    showAlertDialog();

                }
            });
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        }



    }
    //choosing level
    public void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(R.string.level_hint);
        Resources res =getResources();
       items= res.getStringArray(R.array.levels);
        int checkedItem = 0;// first item in items array
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        choice = 4;
                        Toast.makeText(MainActivity.this, R.string.easy_selected, Toast.LENGTH_SHORT).show();

                        break;
                    case 1:
                        choice = 5;
                        Toast.makeText(MainActivity.this, R.string.medium_selected, Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        choice = 6;
                        Toast.makeText(MainActivity.this, R.string.hard_selected, Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        choice = 7;
                        Toast.makeText(MainActivity.this, R.string.super_hard_selected, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        alertDialog.setPositiveButton(R.string.select_btn, new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(MainActivity.this, PuzzleActivity.class);


                if(REQUEST_IMAGE_GALLERY==4) {
                    intent.putExtra("level", choice);
                    intent.putExtra("assetName", mCurrentPhoto);
                }

                if(REQUEST_IMAGE_CAPTURE==1) {
                    intent.putExtra("level", choice);
                    intent.putExtra("assetName", mCurrentPhoto);
                }

                startActivity(intent);


            }
        });
        alertDialog.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //back to same activity
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    //choosing picture from camera
    public void onImageFromCameraClick(View view) {
        showAlertDialog();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
            }

            if (photoFile != null) {

                Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    private File createImageFile() throws IOException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted, initiate request
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            mCurrentPhotoPath = image.getAbsolutePath(); // save this to use in the intent
            return image;
        }

        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    onImageFromCameraClick(new View(this));

                }

                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent= new Intent(this,PuzzleActivity.class);
            intent.putExtra("mCurrentPhotoPath", mCurrentPhotoPath);
            intent.putExtra("level",choice);
            startActivity(intent);

        }

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            Intent intent= new Intent(this,PuzzleActivity.class);
            intent.putExtra("mCurrentPhotoUri", uri.toString());
            intent.putExtra("level",choice);
            startActivity(intent);

                }
            }

    public void onImageFromGalleryClick(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        }
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

