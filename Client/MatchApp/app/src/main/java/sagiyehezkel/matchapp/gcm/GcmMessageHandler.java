package sagiyehezkel.matchapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import sagiyehezkel.matchapp.data.GamesManager;

public class GcmMessageHandler extends IntentService {

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GamesManager gamesManager = new GamesManager(this);

        String title = extras.getString("title");
        String body = extras.getString("message");

        if (title.equals("NEW_GAME")) {
            gamesManager.createNewGame(body);
        } else if (title.equals("UPDATE_GAME")) {
            gamesManager.updateGame(body);
        } else if (title.equals("GAME_COMPLETION")) {
            gamesManager.completeGame(body);
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
