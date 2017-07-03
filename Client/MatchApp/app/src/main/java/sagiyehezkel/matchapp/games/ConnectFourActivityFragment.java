package sagiyehezkel.matchapp.games;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sagiyehezkel.matchapp.R;
import sagiyehezkel.matchapp.Utility;
import sagiyehezkel.matchapp.data.GamesManager;
import sagiyehezkel.matchapp.data.MatchAppContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConnectFourActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int GAME_LOADER = 1;
    private static final String[] GAME_COLUMNS = {
            MatchAppContract.GamesEntry.TABLE_NAME + "." + MatchAppContract.GamesEntry._ID,
            MatchAppContract.GamesEntry.COLUMN_GAME_TYPE,
            MatchAppContract.GamesEntry.COLUMN_PLAYERS,
            MatchAppContract.GamesEntry.COLUMN_UPDATE_TIME,
            MatchAppContract.GamesEntry.COLUMN_GAME_STATUS,
            MatchAppContract.GamesEntry.COLUMN_IS_MY_TURN,
            MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME,
            MatchAppContract.PlayersEntry.COLUMN_PHOTO_URI,
            MatchAppContract.GamesEntry.COLUMN_SERVER_GAME_ID,
            MatchAppContract.GamesEntry.COLUMN_FIRST_PLAYER,
            MatchAppContract.GamesEntry.COLUMN_SEEN_LAST_UPDATE,
            MatchAppContract.GamesEntry.COLUMN_WINNER_PLAYER
};

    // These indices are tied to GAMES_COLUMNS.
    // If GAMES_COLUMNS changes, these must change.
    static final int COL_GAME_ID                = 0;
    static final int COL_GAME_TYPE              = 1;
    static final int COL_PLAYER                 = 2;
    static final int COL_UPDATE_TIME            = 3;
    static final int COL_GAME_STATUS            = 4;
    static final int COL_IS_MY_TURN             = 5;
    static final int COL_PLAYER_DISPLAY_NAME    = 6;
    static final int COL_PLAYER_PHOTO_URI       = 7;
    static final int COL_GAME_SERVER_ID         = 8;
    static final int COL_FIRST_PLAYER           = 9;
    static final int COL_SEEN_LAST_UPDATE       = 10;
    static final int COL_WINNER_PLAYER          = 11;

    private Uri         mGameUri = null;
    private View        mRootView;
    private GridView    mGameBoardGridView;
    private FloatingActionButton mSendButton;
    private int mGameServerId;
    private boolean     mImFirstPlayer;
    private boolean     mIsMyTurn;
    private int         mLastChosenCell = -1;


    private ArrayList<Integer>   mBoardState;
    private ConnectFourImageAdapter mConnectFourImageAdapter;


    public ConnectFourActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_connect_four, container, false);
        final Context c = getActivity();

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        mGameUri = intent.getData();

        mGameBoardGridView = (GridView) mRootView.findViewById(R.id.game_connect_four_board);
        mConnectFourImageAdapter = new ConnectFourImageAdapter(c);
        mGameBoardGridView.setAdapter(mConnectFourImageAdapter);

        makeBoardClickableIfItsMyTurn();

        mSendButton = (FloatingActionButton ) mRootView.findViewById(R.id.game_connect_four_send_update_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMyTurn) {
                    Toast.makeText(c, "It's not your turn!", Toast.LENGTH_SHORT).show();
                } else if (mLastChosenCell == -1) {
                    Toast.makeText(c, "You need to make a move first...", Toast.LENGTH_SHORT).show();
                } else {
                    // Send update to server
                    sendGameUpdateToServer();
                }
            }
        });
        return mRootView;
    }

    private void sendGameUpdateToServer() {
        ArrayList<Integer> newBoardState = new ArrayList<>(mBoardState);
        newBoardState.add(mLastChosenCell);

        String newStatus = Utility.fromIntegerListToString(newBoardState);

        GamesManager gm = new GamesManager(getActivity());
        gm.sendGameUpdateToServer(mGameServerId, newStatus);
    }

    private void makeBoardClickableIfItsMyTurn() {
        mGameBoardGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (mIsMyTurn) {
                    mLastChosenCell = mConnectFourImageAdapter.makeMove(position);
                    mSendButton.setBackgroundTintList(ColorStateList.valueOf(
                            getResources().getColor(R.color.colorPrimary)));
                } else {
                    Toast.makeText(getActivity(), "It's not your turn!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GAME_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mGameUri == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                mGameUri,
                GAME_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) { return; }

        // Init board view in a case of data update
        {
            mSendButton.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.colorAccent)));

            mConnectFourImageAdapter.clearLastEditedCell();
        }

        // Set toolbar data
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.game_toolbar);

        String playerName = data.getString(COL_PLAYER_DISPLAY_NAME);
        TextView playerDisplayNameTextView = (TextView) toolbar.findViewById(R.id.game_actionbar_player_name_textview);
        playerDisplayNameTextView.setText(playerName);

        String playerPhotoUri = data.getString(COL_PLAYER_PHOTO_URI);
        ImageView playerPhotoImageView = (ImageView) toolbar.findViewById(R.id.game_actionbar_player_photo_imageview);
        if (playerPhotoUri != null)
            playerPhotoImageView.setImageURI(Uri.parse(playerPhotoUri));

        mGameServerId = data.getInt(COL_GAME_SERVER_ID);

        String gameStatus = data.getString(COL_GAME_STATUS);
        mBoardState = Utility.fromStringToIntegerList(gameStatus);

        String firstPlayer = data.getString(COL_FIRST_PLAYER);
        String myNumber = Utility.getPreferredPhone(getActivity());
        mImFirstPlayer = firstPlayer.equals(myNumber);

        mConnectFourImageAdapter.initBoard(mBoardState, mImFirstPlayer);

        String winnerPlayer = data.getString(COL_WINNER_PLAYER);
        if (winnerPlayer != null) {
            // Announce the winner
            TextView winnerTextView = (TextView)mRootView.findViewById(R.id.game_connect_four_game_winner_textview);
            winnerTextView.setText(winnerPlayer.equals(myNumber) ?
                    "Congratulations! You won!" :
                    "You lost...");
        } else {
            mIsMyTurn = data.getInt(COL_IS_MY_TURN) != 0;
            makeBoardClickableIfItsMyTurn();

            ImageView myTurnImageView = (ImageView)toolbar.findViewById(R.id.game_actionbar_my_turn_imageview);
            if (mIsMyTurn)
                myTurnImageView.setImageResource(R.drawable.my_turn_icon);
            else
                myTurnImageView.setImageDrawable(null);
        }

        // Update seen status at the db
        boolean seenLastUpdate = data.getInt(COL_SEEN_LAST_UPDATE) != 0;
        if (!seenLastUpdate) {
            int gameId = data.getInt(COL_GAME_ID);
            GamesManager gm = new GamesManager(getActivity());
            gm.updateSeenStatus(gameId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
