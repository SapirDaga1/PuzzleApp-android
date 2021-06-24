package il.org.puzzeling;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Records.db";
    public static final String TABLE_NAME = "records_table";
    public static final String COL_1 = "Rank";
    public static final String COL_2 = "Name";
    public static final String COL_3 = "Points";
    public static final String COL_4 = "Level";
    public static final String COL_5 = "Time";
    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" create table " + TABLE_NAME + "  (RANK INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, POINTS INTEGER, LEVEL INTEGER, TIME TEXT) " );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
    public boolean insertData(String name,int points, int level, String time)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues(); // Create new row
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,points);
        contentValues.put(COL_4,level);
        contentValues.put(COL_5,time);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }
}
