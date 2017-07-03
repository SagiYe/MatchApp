package sagiyehezkel.matchapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the MatchApp database.
 */
public class MatchAppContract {
    public static final String CONTENT_AUTHORITY = "sagiyehezkel.matchapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PLAYERS = "players";
    public static final String PATH_GAMES = "games";

    /* Inner class that defines the table contents of the players table */
    public static final class PlayersEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";

        public static final String COLUMN_PHONE_NUM = "phone_num";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_PHOTO_URI = "photo_uri";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAYERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAYERS;

        public static Uri buildPlayerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the games table */
    public static final class GamesEntry implements BaseColumns {

        public static final String TABLE_NAME = "games";

        // Column with the foreign key into the players table.
        public static final String COLUMN_PLAYERS = "player_id";
        public static final String COLUMN_FIRST_PLAYER = "first_player_id";
        public static final String COLUMN_SERVER_GAME_ID = "server_game_id";
        public static final String COLUMN_UPDATE_TIME = "update_time";
        public static final String COLUMN_GAME_TYPE = "game_type";
        public static final String COLUMN_GAME_STATUS = "game_status";
        public static final String COLUMN_IS_MY_TURN = "my_turn";
        public static final String COLUMN_SEEN_LAST_UPDATE = "seen";
        public static final String COLUMN_WINNER_PLAYER = "winner_player_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GAMES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;


        public static Uri buildGameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getGameIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
}
