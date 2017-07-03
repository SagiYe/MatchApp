package sagiyehezkel.matchapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import sagiyehezkel.matchapp.data.MatchAppContract;
import sagiyehezkel.matchapp.games.ConnectFourActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class GamesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String GAMES_MODE = "MODE";
    public static final int ACTIVE_GAMES       = 1;
    public static final int COMPLETED_GAMES    = 2;

    private static final int GAMES_LOADER = 0;
    private static final String[] GAMES_COLUMNS = {
            MatchAppContract.GamesEntry.TABLE_NAME + "." + MatchAppContract.GamesEntry._ID,
            MatchAppContract.GamesEntry.COLUMN_GAME_TYPE,
            MatchAppContract.GamesEntry.COLUMN_PLAYERS,
            MatchAppContract.GamesEntry.COLUMN_UPDATE_TIME,
            MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE,
            MatchAppContract.GamesEntry.COLUMN_IS_MY_TURN,
            MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME,
            MatchAppContract.PlayersEntry.COLUMN_PHOTO_URI

    };

    // These indices are tied to GAMES_COLUMNS.
    // If GAMES_COLUMNS changes, these must change.
    static final int COL_GAME_ID                = 0;
    static final int COL_GAME_TYPE              = 1;
    static final int COL_PLAYER                 = 2;
    static final int COL_UPDATE_TIME            = 3;
    static final int COL_SEEN_LAST_UPDATE       = 4;
    static final int COL_IS_MY_TURN             = 5;
    static final int COL_PLAYER_DISPLAY_NAME    = 6;
    static final int COL_PLAYER_PHOTO_URI       = 7;

    private GamesAdapter mGamesAdapter;
    private ListView mListView;
    private int mGamesMode;

    public GamesListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGamesMode = getArguments().getInt(GAMES_MODE, ACTIVE_GAMES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.games_list_listview);
        mGamesAdapter = new GamesAdapter(getActivity(), null, 0);
        mListView.setAdapter(mGamesAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), ConnectFourActivity.class)
                            .setData(MatchAppContract.GamesEntry.buildGameUri(
                            cursor.getInt(COL_GAME_ID)));
                    startActivity(intent);
                    return;
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GAMES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri gamesUri = MatchAppContract.GamesEntry.CONTENT_URI;
        String sortOrder = MatchAppContract.GamesEntry.COLUMN_UPDATE_TIME + " DESC";

        String selection = null;

        switch (mGamesMode) {
            case ACTIVE_GAMES:
                selection = MatchAppContract.GamesEntry.COLUMN_WINNER_PLAYER + " IS NULL OR " +
                        MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE + " = 0";
                break;
            case COMPLETED_GAMES:
                selection = MatchAppContract.GamesEntry.COLUMN_WINNER_PLAYER + " IS NOT NULL AND " +
                        MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE + " != 0";
                break;
        }

        return new CursorLoader(getActivity(), gamesUri,
                GAMES_COLUMNS, selection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGamesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGamesAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
