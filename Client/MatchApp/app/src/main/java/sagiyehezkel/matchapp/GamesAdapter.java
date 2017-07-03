package sagiyehezkel.matchapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class GamesAdapter extends CursorAdapter {

    /**
     * Cache of the children views
     */
    public static class ViewHolder {
        public final ImageView playerImageView;
        public final TextView playerNameTextView;
        public final TextView gameTypeTextView;
        public final TextView updateTimeTextView;
        public final ImageView myTurnImageView;

        public ViewHolder(View view) {
            playerImageView = (ImageView) view.findViewById(R.id.games_list_item_player_photo_imageview);
            playerNameTextView = (TextView) view.findViewById(R.id.games_list_item_player_name_textview);
            gameTypeTextView = (TextView) view.findViewById(R.id.games_list_item_game_type_textview);
            updateTimeTextView = (TextView) view.findViewById(R.id.games_list_item_update_time_textview);
            myTurnImageView = (ImageView) view.findViewById(R.id.games_list_item_my_turn_imageview);
        }
    }

    public GamesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.games_list_item, parent, false);

        // Assigning a ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        String imageSrc = cursor.getString(GamesListFragment.COL_PLAYER_PHOTO_URI);
        if (imageSrc != null)
            viewHolder.playerImageView.setImageURI(Uri.parse(imageSrc));

        String playerName = cursor.getString(GamesListFragment.COL_PLAYER_DISPLAY_NAME);
        viewHolder.playerNameTextView.setText(playerName);

        String gameType = cursor.getString(GamesListFragment.COL_GAME_TYPE);

        long timeInMillis = cursor.getLong(GamesListFragment.COL_UPDATE_TIME);
        String updateTime = Utility.getFriendlyUpdateTimeOrDayString(context, timeInMillis);
        viewHolder.updateTimeTextView.setText(updateTime);

        boolean isMyTurn = cursor.getInt(GamesListFragment.COL_IS_MY_TURN) != 0;
        if (isMyTurn)
            viewHolder.myTurnImageView.setImageResource(R.drawable.my_turn_icon);
        else
            viewHolder.myTurnImageView.setImageDrawable(null);

        boolean seenLastUpdate = cursor.getInt(GamesListFragment.COL_SEEN_LAST_UPDATE) != 0;
        if (!seenLastUpdate) {
            viewHolder.updateTimeTextView.setTypeface(null, Typeface.BOLD);
            viewHolder.playerNameTextView.setTypeface(null, Typeface.BOLD);
        } else {
            viewHolder.updateTimeTextView.setTypeface(null, Typeface.NORMAL);
            viewHolder.playerNameTextView.setTypeface(null, Typeface.NORMAL);
        }
    }
}