package il.org.puzzeling;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
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
    static int FLAG_LEVEL=1;
    static int score;
    static int points=10;;

    private static final long DEFAULT_DURATION_MILLIS = 2000L;
    private long duration = DEFAULT_DURATION_MILLIS;

    GridView grid;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 3;
    static final int REQUEST_IMAGE_GALLERY = 4;
    String[] items;
    Dialog level_dialog;
    int choice = 4; //default choice is easy level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score=0;

        sp = getSharedPreferences("music", MODE_PRIVATE);
        manageMusic(false);

        //dialog for choosing level
        level_dialog = new Dialog(this);

        AssetManager am = getAssets();
        try {
            final String[] files = am.list("img");

            grid = findViewById(R.id.grid);
            grid.setAdapter(new ImageAdapter(this));
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    Intent intent = new Intent(getApplicationContext(), PuzzleActivity.class);
                    mCurrentPhoto = files[i % files.length];
//                    intent.putExtra("assetName", mCurrentPhoto);
                    showLevelDialog(mCurrentPhoto,"mCurrentPhoto");

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
        Resources res = getResources();
        items = res.getStringArray(R.array.levels);
        int checkedItem = 0;// first item in items array
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        choice = 4;
                        Toast.makeText(MainActivity.this, R.string.easy_selected, Toast.LENGTH_SHORT).show();
                        FLAG_LEVEL=1;
                        break;
                    case 1:
                        choice = 5;
                        Toast.makeText(MainActivity.this, R.string.medium_selected, Toast.LENGTH_SHORT).show();
                        FLAG_LEVEL=2;
                        break;
                    case 2:
                        choice = 6;
                        Toast.makeText(MainActivity.this, R.string.hard_selected, Toast.LENGTH_SHORT).show();
                        FLAG_LEVEL=3;
                        break;
                    case 3:
                        choice = 7;
                        Toast.makeText(MainActivity.this, R.string.super_hard_selected, Toast.LENGTH_SHORT).show();
                        FLAG_LEVEL=4;
                        break;
                }
            }
        });
        alertDialog.setPositiveButton(R.string.select_btn, new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(MainActivity.this, PuzzleActivity.class);


                if (REQUEST_IMAGE_GALLERY == 4) {
                    intent.putExtra("level", choice);
                    intent.putExtra("assetName", mCurrentPhoto);
                }

                if (REQUEST_IMAGE_CAPTURE == 1) {
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
                finishAffinity();
                startActivity(intent);

            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
    //choosing picture from camera
    public void onImageFromCameraClick(View view) {
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

            showLevelDialog(mCurrentPhotoPath,"mCurrentPhotoPath");
//            Intent intent = new Intent(this, PuzzleActivity.class);
//            intent.putExtra("mCurrentPhotoPath", mCurrentPhotoPath);
//            intent.putExtra("level", choice);
//            startActivity(intent);

        }

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            showLevelDialog(uri.toString(),"mCurrentPhotoUri");
//            Intent intent = new Intent(this, PuzzleActivity.class);
//            intent.putExtra("mCurrentPhotoUri", uri.toString());
//            intent.putExtra("level", choice);
//            startActivity(intent);

        }
    }

    public void onImageFromGalleryClick(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        else {

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
        manageMusic(false);
        super.onBackPressed();
    }

    public void manageMusic(boolean forceShutdown) {
        if (isMuted || forceShutdown)
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

                if (isMuted) {
                    manageMusic(true);
                    item.setIcon(R.drawable.ic_no_music_btn);
                } else {
                    manageMusic(false);
                    item.setIcon(R.drawable.ic_music_btn);
                }
                break;
            case R.id.home_icon:
                Intent intent = new Intent(MainActivity.this, FirstScreenActivity.class);
                finishAffinity();
                startActivity(intent);
                break;


        }
        return true;
    }

    public void showLevelDialog(String photoForPuzzle,String kind){
        LayoutInflater factory = LayoutInflater.from(this);
        final View levelDialogView = factory.inflate(R.layout.level_dialog, null);
        final AlertDialog levelDialog = new AlertDialog.Builder(this).create();
        levelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        levelDialog.setView(levelDialogView);
        levelDialogView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelDialog.dismiss();
            }
        });

        RadioGroup levelRg = (RadioGroup) levelDialogView.findViewById(R.id.level_rg);
        levelRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.easy_rb:
                        choice = 4;
                        showToast(R.string.easy_selected, Gravity.BOTTOM,0,30);
                        break;
                    case R.id.medium_rb:
                        choice = 5;
                        showToast(R.string.medium_selected, Gravity.BOTTOM,0,30);
                        break;
                    case R.id.hard_rb:
                        choice = 6;
                        showToast(R.string.hard_selected, Gravity.BOTTOM,0,30);
                        break;
                    case R.id.very_hard_rb:
                        choice = 7;
                        showToast(R.string.super_hard_selected, Gravity.BOTTOM,0,30);
                        break;
                }
            }
        });


        levelDialogView.findViewById(R.id.select_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, PuzzleActivity.class);
                intent.putExtra("level", choice);
                intent.putExtra(kind, photoForPuzzle);
                startActivity(intent);
            }
        });
        levelDialog.show();
    }

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
