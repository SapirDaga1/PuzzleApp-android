package il.org.puzzeling;

import android.content.Context;

import androidx.appcompat.widget.AppCompatImageView;

public class PuzzlePieces extends AppCompatImageView {
    public int xCoord;
    public int yCoord;
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public PuzzlePieces(Context context) {
        super(context);
    }
}
