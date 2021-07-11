package il.org.puzzeling;


import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;

import static il.org.puzzeling.MainActivity.FLAG_LEVEL;
import static il.org.puzzeling.MainActivity.points;
import static il.org.puzzeling.MainActivity.score;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.abs;

public class TouchListener implements View.OnTouchListener {
    private float deltaX;
    private  float deltaY;
    private PuzzleActivity activity;


    public TouchListener(PuzzleActivity activity) {
        this.activity = activity;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();

        DisplayMetrics displayMetrics= activity.getResources().getDisplayMetrics();
        int screenHeight =displayMetrics.heightPixels;
        int screenWidth= displayMetrics.widthPixels;

        final double tolerance = sqrt(pow(v.getWidth(), 2) + pow(v.getHeight(), 2)) / 10;

        PuzzlePieces piece = (PuzzlePieces) v;
        if (!piece.canMove) {
            return true;
        }

        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                deltaX = x - lParams.leftMargin;
                deltaY = y - lParams.topMargin;
                piece.bringToFront();
                break;

            case MotionEvent.ACTION_MOVE:
                lParams.leftMargin =  Math.min(Math.max(0, (int)(x - deltaX)), screenWidth - v.getWidth());
                lParams.topMargin = Math.min(Math.max(0, (int)(y - deltaY)), screenHeight - v.getHeight() - 210);
                lParams.bottomMargin = screenHeight;
                v.setLayoutParams(lParams);
                break;

            case MotionEvent.ACTION_UP:
                int xDiff = abs(piece.xCoord - lParams.leftMargin);
                int yDiff = abs(piece.yCoord - lParams.topMargin);
                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = piece.xCoord;
                    lParams.topMargin = piece.yCoord;
                    piece.setLayoutParams(lParams);
                    piece.canMove = false;
                    piece.startAnimation(AnimationUtils.loadAnimation(activity.getApplicationContext(),R.anim.pulse));
                    score = score+ points*FLAG_LEVEL;
                    PuzzleActivity.syncScore();
                    sendViewToBack(piece);
                    activity.checkGameOver();
                }
                break;
        }


        return true;
    }

    public void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup)child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }
}