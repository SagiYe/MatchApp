package sagiyehezkel.matchapp.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MatchAppProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MatchAppDbHelper mOpenHelper;

    static final int PLAYERS = 100;
    static final int GAMES = 200;
    static final int SPECIFIC_GAME = 201;
    static final int GAMES_WITH_GAME_TYPE = 202;

    private static final SQLiteQueryBuilder sGamesWithPlayerDetailsQueryBuilder;

    static{
        sGamesWithPlayerDetailsQueryBuilder = new SQLiteQueryBuilder();

        sGamesWithPlayerDetailsQueryBuilder.setTables(
                MatchAppContract.GamesEntry.TABLE_NAME + " INNER JOIN " +
                        MatchAppContract.PlayersEntry.TABLE_NAME +
                        " ON " + MatchAppContract.GamesEntry.TABLE_NAME +
                        "." + MatchAppContract.GamesEntry.COLUMN_PLAYERS +
                        " = " + MatchAppContract.PlayersEntry.TABLE_NAME +
                        "." + MatchAppContract.PlayersEntry.COLUMN_PHONE_NUM);
    }

    private static final String sGamesPlayerSelection =
            MatchAppContract.GamesEntry.TABLE_NAME+
                    "." + MatchAppContract.GamesEntry.COLUMN_PLAYERS + " = ? ";

    private static final String sGameByGameId =
            MatchAppContract.GamesEntry.TABLE_NAME+
                    "." + MatchAppContract.GamesEntry._ID + " = ? ";

    private Cursor getGameByGameId(
            Uri uri, String[] projection, String sortOrder) {
        int gameId = MatchAppContract.GamesEntry.getGameIdFromUri(uri);

        return sGamesWithPlayerDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sGameByGameId,
                new String[]{Integer.toString(gameId)},
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MatchAppContract.CONTENT_AUTHORITY, MatchAppContract.PATH_PLAYERS, PLAYERS);
        uriMatcher.addURI(MatchAppContract.CONTENT_AUTHORITY, MatchAppContract.PATH_GAMES, GAMES);

        uriMatcher.addURI(MatchAppContract.CONTENT_AUTHORITY, MatchAppContract.PATH_GAMES + "/*", SPECIFIC_GAME);
//        uriMatcher.addURI(MatchAppContract.CONTENT_AUTHORITY, MatchAppContract.PATH_GAMES + "/*/#", GAMES_WITH_GAME_TYPE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MatchAppDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case GAMES:
                return MatchAppContract.GamesEntry.CONTENT_TYPE;
            case PLAYERS:
                return MatchAppContract.PlayersEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case SPECIFIC_GAME: {
                retCursor = getGameByGameId(uri, projection, sortOrder);
                break;
            }
            // "games"
            case GAMES: {
                retCursor = sGamesWithPlayerDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "players"
            case PLAYERS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MatchAppContract.PlayersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case GAMES: {
                long _id = db.insert(MatchAppContract.GamesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MatchAppContract.GamesEntry.buildGameUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLAYERS: {
                long _id = db.insert(MatchAppContract.PlayersEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MatchAppContract.PlayersEntry.buildPlayerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        switch (match) {
            case GAMES: {
                rowsDeleted = db.delete(MatchAppContract.GamesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PLAYERS: {
                rowsDeleted = db.delete(MatchAppContract.PlayersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case GAMES: {
                rowsUpdated = db.update(MatchAppContract.GamesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PLAYERS: {
                rowsUpdated = db.update(MatchAppContract.PlayersEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MatchAppContract.PlayersEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                mOpenHelper.exportDatabaseToSDCard();
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}