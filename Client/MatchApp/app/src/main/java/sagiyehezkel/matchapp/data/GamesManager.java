package sagiyehezkel.matchapp.data;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import sagiyehezkel.matchapp.Utility;
import sagiyehezkel.matchapp.security.AdvancedEncryptionStandard;

/**
 * Created by Sagi on 31/10/2015.
 */
public class GamesManager {
    final String sGameByServerIdSelection =
            MatchAppContract.GamesEntry.TABLE_NAME+
                    "." + MatchAppContract.GamesEntry.COLUMN_SERVER_GAME_ID + " = ? ";

    final String sPlayerNameByPlayerNumSelection =
            MatchAppContract.PlayersEntry.TABLE_NAME+
                    "." + MatchAppContract.PlayersEntry.COLUMN_PHONE_NUM + " = ? ";

    private Context mContext;

    public GamesManager(Context context) {
        mContext = context;
    }

    public void sendNewGameToServer(String gameType, ArrayList<String> players) {
        SendNewGameToServerAsyncTask sendNewGameToServerAsyncTask = new SendNewGameToServerAsyncTask(gameType, players);
        sendNewGameToServerAsyncTask.execute(null, null, null);
    }

    public void sendGameUpdateToServer(int serverGameId, String newGameStatus) {
        SendGameUpdateToServerAsyncTask sendGameUpdateToServerAsyncTask = new SendGameUpdateToServerAsyncTask(serverGameId, newGameStatus);
        sendGameUpdateToServerAsyncTask.execute(null, null, null);
    }

    public void createNewGame(String jsonStr) {
        ContentValues gameValues = new ContentValues();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
            ArrayList<String> playersList = Utility.fromJsonStrToArrayList(jsonStr, "PLAYERS");
            playersList.remove(playersList.indexOf(Utility.getPreferredPhone(mContext)));

            Date date = new Date();

            String gameType = jsonObject.getString("TYPE");
            String serverGameId = jsonObject.getString("ID");
            String firstPlayer = jsonObject.getString("FIRST_PLAYER");
            String opponentNumber = playersList.get(0);
            int isMyTurn = firstPlayer.equals(Utility.getPreferredPhone(mContext)) ? 1 : 0;

            gameValues.put(MatchAppContract.GamesEntry.COLUMN_GAME_TYPE, gameType);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_SERVER_GAME_ID, serverGameId);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_FIRST_PLAYER, firstPlayer);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_PLAYERS, opponentNumber);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_UPDATE_TIME, date.getTime());
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_IS_MY_TURN, isMyTurn);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE, 0);

            mContext.getContentResolver().insert(MatchAppContract.GamesEntry.CONTENT_URI, gameValues);

            showNotification("New Game", serverGameId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateGame(String jsonStr) {

        ContentValues gameValues = new ContentValues();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
            String serverGameId = jsonObject.getString("ID");
            String newGameStatus = jsonObject.getString("STATUS");
            String nextPlayer = jsonObject.getString("NEXT_PLAYER");
            Date date = new Date();

            String[] arg = {serverGameId};

            int isMyTurn = nextPlayer.equals(Utility.getPreferredPhone(mContext)) ? 1 : 0;

            gameValues.put(MatchAppContract.GamesEntry.COLUMN_UPDATE_TIME, date.getTime());
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_GAME_STATUS, newGameStatus);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE, 0);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_IS_MY_TURN, isMyTurn);

            mContext.getContentResolver().update(
                    MatchAppContract.GamesEntry.CONTENT_URI, gameValues,
                    sGameByServerIdSelection, arg);

            showNotification("Game Update", serverGameId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void completeGame(String jsonStr) {
        ContentValues gameValues = new ContentValues();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
            String serverGameId = jsonObject.getString("ID");
            String newGameStatus = jsonObject.getString("STATUS");
            String winner = jsonObject.getString("WINNER");
            Date date = new Date();

            String[] arg = {serverGameId};

            gameValues.put(MatchAppContract.GamesEntry.COLUMN_UPDATE_TIME, date.getTime());
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_GAME_STATUS, newGameStatus);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE, 0);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_IS_MY_TURN, 0);
            gameValues.put(MatchAppContract.GamesEntry.COLUMN_WINNER_PLAYER, winner);

            mContext.getContentResolver().update(
                    MatchAppContract.GamesEntry.CONTENT_URI, gameValues,
                    sGameByServerIdSelection, arg);

            showNotification("Game Update", serverGameId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showNotification(String header, String gameServerId) {
        if (!Utility.isAppInBackground(mContext))
            return;

        String gameType = null;
        String playerNumber = null;
        String playerDisplayName = null;
        String playerPhotoURI = null;


        // Query for game type and players
        ContentResolver cr =  mContext.getContentResolver();
        Cursor cursor = cr.query(MatchAppContract.GamesEntry.CONTENT_URI,
                new String[]{MatchAppContract.GamesEntry.COLUMN_GAME_TYPE,
                        MatchAppContract.GamesEntry.COLUMN_PLAYERS},
                sGameByServerIdSelection,
                new String[]{gameServerId},
                null);

        while (cursor.moveToNext()) {
            gameType = cursor.getString(
                    cursor.getColumnIndex(MatchAppContract.GamesEntry.COLUMN_GAME_TYPE));
            playerNumber = cursor.getString(
                    cursor.getColumnIndex(MatchAppContract.GamesEntry.COLUMN_PLAYERS));
        }
        cursor.close();

        // Query for player name & photo
        cursor = cr.query(MatchAppContract.PlayersEntry.CONTENT_URI,
                new String[]{MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME,
                        MatchAppContract.PlayersEntry.COLUMN_PHOTO_URI},
                sPlayerNameByPlayerNumSelection,
                new String[]{playerNumber},
                null);


        while (cursor.moveToNext()) {
            playerDisplayName = cursor.getString(
                    cursor.getColumnIndex(MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME));
            playerPhotoURI = cursor.getString(
                    cursor.getColumnIndex(MatchAppContract.PlayersEntry.COLUMN_PHOTO_URI));
        }
        cursor.close();

        String text = gameType + " with " + playerDisplayName;

        // Raise notification if app is in background
        Utility.showNotificationIfAppInBackground(mContext, header, text, playerPhotoURI);
    }

    public void updateSeenStatus(int gameId){
        final String sGameByIdSelection =
                MatchAppContract.GamesEntry.TABLE_NAME+
                        "." + MatchAppContract.GamesEntry._ID + " = ? ";

        ContentValues gameValues = new ContentValues();

        String[] arg = {Integer.toString(gameId)};

        gameValues.put(MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE, 1);

        mContext.getContentResolver().update(
                MatchAppContract.GamesEntry.CONTENT_URI, gameValues,
                sGameByIdSelection, arg);
    }

    private class SendNewGameToServerAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String GAME_TYPE = "GAME_TYPE";
        private static final String PLAYERS = "PLAYERS";

        private ProgressDialog dialog;

        private String mGameType;
        private ArrayList<String> mPlayers;

        public SendNewGameToServerAsyncTask(String gameType, ArrayList<String> players) {
            mGameType = gameType;
            mPlayers = players;

            dialog = new ProgressDialog(mContext);
        }

        private void writeNewGameToServer() {
            URL url = null;
            try {
                url = new URL(Utility.SERVER_ADDRESS + "/new_game");
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

                jsonParam.put(GAME_TYPE, mGameType);
                jsonParam.put(PLAYERS, new JSONArray(mPlayers));

                AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard();
                String origStr = jsonParam.toString();
                String encpStr = aes.encrypt(origStr);
                String decrStr = aes.decrypt(encpStr);

                System.out.println(origStr);
                System.out.println(encpStr);
                System.out.println(decrStr);

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
            writeNewGameToServer();
            return null;
        }
    }

    private class SendGameUpdateToServerAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String GAME_ID = "GAME_ID";
        private static final String STATUS = "STATUS";
        private static final String PLAYER = "PLAYER";

        private ProgressDialog dialog;

        private int mGameId;
        private String mNewStatus;

        public SendGameUpdateToServerAsyncTask(int gameId, String newGameStatus) {
            mGameId = gameId;
            mNewStatus = newGameStatus;

            dialog = new ProgressDialog(mContext);
        }

        private void writeGameUpdateToServer() {
            URL url = null;
            try {
                url = new URL(Utility.SERVER_ADDRESS + "/update_game");
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

                jsonParam.put(GAME_ID, mGameId);
                jsonParam.put(STATUS, mNewStatus);
                jsonParam.put(PLAYER, Utility.getPreferredPhone(mContext));

                writer.write(jsonParam.toString());

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
            writeGameUpdateToServer();
            return null;
        }
    }
}
