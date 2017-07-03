package sagiyehezkel.matchapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import sagiyehezkel.matchapp.data.MatchAppContract.GamesEntry;
import sagiyehezkel.matchapp.data.MatchAppContract.PlayersEntry;

public class MatchAppDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "matchapp.db";

    private Context mContext;

    public MatchAppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public void clearDB() {
        onUpgrade(getWritableDatabase(),0,0);
    }

    public void exportDatabaseToSDCard() {
        final String BACKUP_DATABASE_NAME = "matchapp/db_backup_";

        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String currentDBPath = getReadableDatabase().getPath();
                Calendar gc = GregorianCalendar.getInstance();
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm");
                String timeStr = shortenedDateFormat.format(gc.getTimeInMillis());

                String backupDBPath = BACKUP_DATABASE_NAME + timeStr + ".db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                Log.v("exportDatabaseToSDCard", "Output file location: " + backupDB.getPath());

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    MediaScannerConnection.scanFile(mContext, new String[]{backupDB.getAbsolutePath()}, null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PLAYERS_TABLE = "CREATE TABLE " + PlayersEntry.TABLE_NAME + " (" +
                PlayersEntry._ID + " INTEGER PRIMARY KEY, " +
                PlayersEntry.COLUMN_PHONE_NUM + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE, " +
                PlayersEntry.COLUMN_DISPLAY_NAME + " TEXT, " +
                PlayersEntry.COLUMN_PHOTO_URI + " TEXT " +
                ");";

        final String SQL_CREATE_GAMES_TABLE = "CREATE TABLE " + GamesEntry.TABLE_NAME + " (" +
                GamesEntry._ID + " INTEGER PRIMARY KEY, " +
                GamesEntry.COLUMN_SERVER_GAME_ID + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE, " +
                GamesEntry.COLUMN_PLAYERS + " TEXT NOT NULL, " +
                GamesEntry.COLUMN_FIRST_PLAYER + " TEXT NOT NULL, " +
                GamesEntry.COLUMN_WINNER_PLAYER + " TEXT, " +
                GamesEntry.COLUMN_GAME_TYPE + " TEXT NOT NULL, " +
                GamesEntry.COLUMN_GAME_STATUS + " TEXT, " +
                GamesEntry.COLUMN_UPDATE_TIME + " INTEGER NOT NULL, " +
                GamesEntry.COLUMN_IS_MY_TURN + " INTEGER NOT NULL, " +
                GamesEntry.COLUMN_SEEN_LAST_UPDATE + " INTEGER DEFAULT 0, " +


                // Set up the player_key column as a foreign key to players table.
                " FOREIGN KEY (" + GamesEntry.COLUMN_PLAYERS + ") REFERENCES " +
                PlayersEntry.TABLE_NAME + " (" + PlayersEntry._ID + ") " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_PLAYERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GAMES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GamesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlayersEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
