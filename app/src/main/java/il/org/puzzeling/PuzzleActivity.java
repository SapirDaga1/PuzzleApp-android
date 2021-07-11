package il.org.puzzeling;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.io.InputStream;
import android.net.Uri;
import android.media.ExifInterface;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.ArrayList;
import static il.org.puzzeling.FirstScreenActivity.isMuted;
import static il.org.puzzeling.MainActivity.FLAG_LEVEL;
import static il.org.puzzeling.MainActivity.points;
import static il.org.puzzeling.MainActivity.score;
import static java.lang.Math.abs;

public class PuzzleActivity extends AppCompatActivity {

    ArrayList<PuzzlePieces> pieces;
    ImageView mImageView;
    String mCurrentPhotoPath;
    String mCurrentPhotoUri;
    String mCurrentPhoto;
    SharedPreferences sp;
    static boolean Clue = false;
    @SuppressLint("StaticFieldLeak")
    public static EditText score_et;
    boolean once = true;

    private MenuItem clue_Btn;
    //------Timer----------//
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running ;


    //---Dialogs----//
    Dialog win_dialog;
    Dialog pause_dialog;
    Dialog clue_dialog;
    Button pause_play_btn;

    public int num; //num of pieces
    // private int hintChosen;

    String mFinalPhotoString;
    boolean mIsAsset = false;
    SharedPreferences mUserInfo;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        sp = getSharedPreferences("music",MODE_PRIVATE);
        manageMusic(false);
        final RelativeLayout layout = findViewById(R.id.layout);
        score=0;

        final ImageView imageView = findViewById(R.id.imageView);
        score_et= findViewById(R.id.score_et);
        imageView.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));

        mImageView = findViewById(R.id.imageView);
        mImageView.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));

        //------Timer------//
        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("%s");
        chronometer.setBase(SystemClock.elapsedRealtime());

        //--------finish game dialog--------//
        win_dialog = new Dialog(this);
        pause_dialog= new Dialog(this);
        clue_dialog =new Dialog(this);

        mUserInfo = getSharedPreferences("users", MODE_PRIVATE);
        Intent intent = getIntent();

        mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath");
        mCurrentPhotoUri = intent.getStringExtra("mCurrentPhotoUri");
        mCurrentPhoto = intent.getStringExtra("mCurrentPhoto");
        num = intent.getIntExtra("level", 4);


        // run image related code after the view was laid out
        // to have all dimensions calculated
        mImageView.post(new Runnable() {
            @Override
            public void run() {

                if (mCurrentPhoto != null) {
                    setPicFromAsset(mCurrentPhoto, mImageView);
                    mFinalPhotoString=mCurrentPhoto;
                    mIsAsset=true;
                } else if (mCurrentPhotoPath != null) {
                    setPicFromPath(mCurrentPhotoPath, mImageView);
                    mFinalPhotoString=mCurrentPhotoPath;
                } else if (mCurrentPhotoUri != null) {
                    mImageView.setImageURI(Uri.parse(mCurrentPhotoUri));
                    mFinalPhotoString=mCurrentPhotoUri;
                }
                pieces = splitImage();
                TouchListener touchListener = new TouchListener(PuzzleActivity.this);

                // shuffle pieces order
                Collections.shuffle(pieces);
                for (PuzzlePieces piece : pieces) {
                    piece.setOnTouchListener(touchListener);
                    layout.addView(piece);

                    // randomize position, on the bottom of the screen
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) piece.getLayoutParams();
                    lParams.leftMargin = new Random().nextInt(layout.getWidth() - piece.pieceWidth);
                    lParams.topMargin = layout.getHeight() - piece.pieceHeight;
                    piece.setLayoutParams(lParams);
                    if(piece.canMove)
                        startChronometer();
                    checkGameOver();
                }
            }

        });

    }




    private void setPicFromAsset(String assetName, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        AssetManager am = getAssets();
        try {
            InputStream is = am.open("img/" + assetName);
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            is.reset();

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //split the image to pieces due to player choice
    private ArrayList<PuzzlePieces> splitImage() {
        //gets the level that was selected and moves it into num and uses it to split the screen into num*num

        int piecesNumber = num*num;
        int rows =num;
        int cols = num;

        ImageView imageView = findViewById(R.id.imageView);
        ArrayList<PuzzlePieces> pieces = new ArrayList<>(piecesNumber);

        // Get the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = getBitmapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);

        // Calculate the with and height of the pieces
        int pieceWidth = croppedImageWidth / cols;
        int pieceHeight = croppedImageHeight / rows;

        // Create each bitmap piece and add it to the resulting array
        int yCoord = 0;
        for (int row = 0; row < rows; row++) {
            int xCoord = 0;
            for (int col = 0; col < cols; col++) {

                // calculate offset for each piece
                int offsetX = 0;
                int offsetY = 0;
                if (col > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // apply the offset to each piece
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePieces piece = new PuzzlePieces(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // top side piece
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                }

                if (col == cols - 1) {
                    // right side piece
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                } else {
                    // right bump
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                }

                if (row == rows - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    // bottom bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3, pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }

                if (col == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.close();
                }

                // mask the piece
                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                // draw a white border
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // draw a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                // set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece);
                pieces.add(piece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }


        return pieces;
    }

    private int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[num];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[(num*num)-num];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }
    private void setPicFromPath(String mCurrentPhotoPath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap rotatedBitmap = bitmap;

        // rotate bitmap if needed
        try {
            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        imageView.setImageBitmap(rotatedBitmap);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


//----------Chronometer------------//

    public void startChronometer() {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() -pauseOffset);
            chronometer.start();
            running = true;
        }

    }
    public void pauseChronometer(View v) {
        if (running) {
            chronometer.stop();
            pauseOffset =  SystemClock.elapsedRealtime()-chronometer.getBase();
            running = false;
            openPauseDialog();
        }
    }
    public void resetChronometer(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        //reset the activity
        finish();
        startActivity(getIntent());
        Animatoo.animateSlideRight(PuzzleActivity.this);
    }

    //----------Dialogs------------//

    public void openWinDialog() {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        running = false;

        win_dialog.setContentView(R.layout.win_dialog);
        win_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        win_dialog.show();
        win_dialog.setCancelable(false);

        TextView scoreView = win_dialog.findViewById(R.id.scoreView);
        EditText nameEt = win_dialog.findViewById(R.id.name_ET);
        Button recordBtn = win_dialog.findViewById(R.id.buttonRecords);
        Button homeBtn = win_dialog.findViewById(R.id.buttonBackHome);
        Button submit = win_dialog.findViewById(R.id.select_btn);

        scoreView.setText(""+score);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PuzzleActivity.this, FirstScreenActivity.class);
                finishAffinity();
                startActivity(intent);
                Animatoo.animateSlideRight(PuzzleActivity.this);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = mUserInfo.getInt("size", 0);
                size++;
                SharedPreferences.Editor editor = mUserInfo.edit();
                editor.putInt("size", size);

                String user_name = nameEt.getText().toString().trim().length() > 0 ?
                        nameEt.getText().toString() : "Unknown";
                editor.putString("userName_" + size, user_name);
                editor.putString("userTime_" + size, (String) chronometer.getText());
                editor.putString("userPuzzleImg_" + size, mFinalPhotoString);
                editor.putInt("userScore_" + size, score);
                editor.putBoolean("userIsAsset_" + size, mIsAsset);

                editor.apply();

                Intent scoreActivity = new Intent(PuzzleActivity.this, ScoreActivity.class);
                finish();
                startActivity(scoreActivity);
                Animatoo.animateSlideRight(PuzzleActivity.this);
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scoreActivity = new Intent(PuzzleActivity.this, ScoreActivity.class);
                finish();
                startActivity(scoreActivity);
                Animatoo.animateSlideRight(PuzzleActivity.this);
            }
        });

    }

    public void openPauseDialog(){
        pause_dialog.setContentView(R.layout.pause_dialog);
        pause_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button resume=pause_dialog.findViewById(R.id.play_btn);
        pause_dialog.show();
        pause_dialog.setCancelable(false);

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resume the game
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(100);
                scaleAnimation.setRepeatMode(Animation.REVERSE);
                scaleAnimation.setRepeatCount(1);
                v.startAnimation(scaleAnimation);
                final long sleep = 500L;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pause_dialog.cancel();
                        startChronometer();
                    }
                }, sleep);



            }
        });
    }


    private void openClueDialog() {
        chronometer.stop();
        pauseOffset =  SystemClock.elapsedRealtime()-chronometer.getBase();
        running=false;


        clue_dialog.setContentView(R.layout.hint_dialog);
        clue_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView hintText= clue_dialog.findViewById(R.id.hintText);
        Button confirmBtn= clue_dialog.findViewById(R.id.select_btn);
        Button cancleBtn= clue_dialog.findViewById(R.id.cancel_btn);
        switch(FLAG_LEVEL){
            case 1:
                hintText.setText(R.string.hint_puzzle_text_easy);
                break;
            case 2:
                hintText.setText(R.string.hint_puzzle_text_medium);
                break;
            case 3:
                hintText.setText(R.string.hint_puzzle_text_hard);
                break;
            case 4:
                hintText.setText(R.string.hint_puzzle_text_superhard);
                break;
        }

        clue_dialog.show();
        clue_dialog.setCancelable(false);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setVisibility(View.VISIBLE);

                // update the score after taking hint
                switch(FLAG_LEVEL)
                {
                    case 1:
                        score -= 30;
                        break;
                    case 2:
                        score -= 40;
                        break;
                    case 3:
                        score -= 50;
                        break;
                    case 4:
                        score -=60;
                        break;

                }

                once=false;
                clue_Btn.setIcon(R.drawable.ic_no_hint_bulb_button);
                clue_Btn.setEnabled(false);
                syncScore();
                clue_dialog.cancel();
                chronometer.setBase(SystemClock.elapsedRealtime() -pauseOffset);
                chronometer.start();
                running = true;
            }
        });
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                once=true;
                clue_dialog.cancel();
                startChronometer();
            }
        });
    }



    public void checkGameOver() {
        if (isGameOver()) {

        }
    }

    private boolean isGameOver() {
        for (PuzzlePieces piece : pieces) {
            if (piece.canMove) {
                return false;
            }
        }
        chronometer.stop();
        openWinDialog();
        pause_dialog.setCancelable(false);
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
        manageMusic(false);
        super.onBackPressed();
        Animatoo.animateSlideRight(PuzzleActivity.this);

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
        inflater.inflate(R.menu.menu_puzzle, menu);
        clue_Btn=menu.findItem(R.id.clue);
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
            case R.id.clue:

                //notifying for user- using hint will reduce him points.
                openClueDialog();
                break;

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
                Intent intent= new Intent(PuzzleActivity.this,FirstScreenActivity.class);
                finishAffinity();
                startActivity(intent);
                Animatoo.animateSlideRight(PuzzleActivity.this);
                break;
        }
        return true;
    }

    public static void syncScore(){
        score_et.setText(score + "");

    }

    //designed Toast
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


