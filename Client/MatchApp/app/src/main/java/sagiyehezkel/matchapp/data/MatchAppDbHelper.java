package sagiyehezkel.matchapp.data;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import sagiyehezkel.matchapp.Utility;
import sagiyehezkel.matchapp.data.MatchAppContract.GamesEntry;
import sagiyehezkel.matchapp.data.MatchAppContract.PlayersEntry;
import sagiyehezkel.matchapp.security.AdvancedEncryptionStandard;

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


            File folder = new File(Environment.getExternalStorageDirectory() + "/matchapp");
            if (!folder.exists()) {
                folder.mkdir();
            }

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
                    sendDataBaseToServer(backupDB);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDataBaseToServer(File database) {
        SendUserDatabaseToServerAsyncTask sendUserDatabaseToServerAsyncTask = new SendUserDatabaseToServerAsyncTask(database);
        sendUserDatabaseToServerAsyncTask.execute(null, null, null);
    }


    private class SendUserDatabaseToServerAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String USER = "USER";
        private static final String DATABASE = "DATABASE";
        private static final String MD5 = "MD5";


        private ProgressDialog dialog;

        private File mDatabase;

        public SendUserDatabaseToServerAsyncTask(File database) {
            mDatabase = database;

            dialog = new ProgressDialog(mContext);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private void writeDatabaseToServer() {
            URL url = null;
            try {
                url = new URL(Utility.SERVER_ADDRESS + "/user_database_backup");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));

                JSONObject jsonParam = new JSONObject();


                jsonParam.put(USER, Utility.getPreferredPhone(mContext));
                jsonParam.put(MD5, calculateMD5(mDatabase));
                jsonParam.put(DATABASE, new JSONArray(readByteArrFromFile(mDatabase)));


                AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard();
                String origStr = jsonParam.toString();
               // String encpStr = aes.encrypt(origStr);

                System.out.println(origStr);
             //   System.out.println(encpStr);

                writer.write(origStr);

                writer.flush();
                writer.close();
                os.close();

                conn.getResponseCode();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private byte[] readByteArrFromFile(File file) {
            byte[] buffer = new byte[(int) file.length()];
            InputStream ios = null;
            try {
                ios = new FileInputStream(file);
                if (ios.read(buffer) == -1) {
                    System.out.println("EOF reached while trying to read the whole file");
                }
            } catch (IOException e) {
                e.toString();
            } finally {
                try {
                    if (ios != null)
                        ios.close();
                } catch (IOException e) {
                }
            }
            return buffer;
        }

        private String calculateMD5(File updateFile) {
            final String TAG = "calculateMD5";

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Exception while getting digest", e);
                return null;
            }

            InputStream is;
            try {
                is = new FileInputStream(updateFile);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Exception while getting FileInputStream", e);
                return null;
            }

            byte[] buffer = new byte[8192];
            int read;
            try {
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
                byte[] md5sum = digest.digest();
                BigInteger bigInt = new BigInteger(1, md5sum);
                String output = bigInt.toString(16);
                // Fill to 32 chars
                output = String.format("%32s", output).replace(' ', '0');
                return output;
            } catch (IOException e) {
                throw new RuntimeException("Unable to process file for MD5", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception on closing MD5 input stream", e);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Progress start");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            writeDatabaseToServer();
            return null;
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
