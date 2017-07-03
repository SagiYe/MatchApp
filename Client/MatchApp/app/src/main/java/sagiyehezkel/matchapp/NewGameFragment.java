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

import java.util.ArrayList;

import sagiyehezkel.matchapp.data.GamesManager;
import sagiyehezkel.matchapp.data.MatchAppContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewGameFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PLAYERS_LOADER = 0;
    private static final String[] PLAYERS_COLUMNS = {
            MatchAppContract.PlayersEntry.TABLE_NAME + "." + MatchAppContract.PlayersEntry._ID,
            MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME,
            MatchAppContract.PlayersEntry.COLUMN_PHONE_NUM,
            MatchAppContract.PlayersEntry.COLUMN_PHOTO_URI
    };

    // These indices are tied to PLAYERS_COLUMNS.
    // If PLAYERS_COLUMNS changes, these must change.
    static final int COL_PLAYER_ID = 0;
    static final int COL_DISPLAY_NAME = 1;
    static final int COL_PHONE_NUM = 2;
    static final int COL_PHOTO_URI = 3;

    private PlayersAdapter mPlayersAdapter;
    private ListView mListView;

    public NewGameFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_game, container, false);

        mListView = (ListView) rootView.findViewById(R.id.new_game_players_list_listview);
        mPlayersAdapter = new PlayersAdapter(getActivity(), null, 0);
        mListView.setAdapter(mPlayersAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                if (cursor != null) {
                    String phone = cursor.getString(NewGameFragment.COL_PHONE_NUM);
                    startNewGame("ConnectFour", phone);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PLAYERS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by display name.
        String sortOrder = MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME + " ASC";
        Uri playersUri = MatchAppContract.PlayersEntry.CONTENT_URI;

        return new CursorLoader(getActivity(), playersUri,
                PLAYERS_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPlayersAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPlayersAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void startNewGame(String gameType, String player) {
        GamesManager gamesManager = new GamesManager(getActivity());
        ArrayList<String> players = new ArrayList<String>();
        players.add(Utility.getPreferredPhone(getActivity()));
        players.add(player);

        gamesManager.sendNewGameToServer(gameType, players);

        // Go back to MainActivity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}
