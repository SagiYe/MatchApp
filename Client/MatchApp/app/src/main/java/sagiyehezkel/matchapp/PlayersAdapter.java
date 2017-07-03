package sagiyehezkel.matchapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayersAdapter extends CursorAdapter {

    Drawable defaultPlayerImage;

    /**
     * Cache of the children views
     */
    public static class ViewHolder {
        public final ImageView photoView;
        public final TextView displayNameView;
        public final TextView phoneNumView;

        public ViewHolder(View view) {
            photoView = (ImageView) view.findViewById(R.id.players_list_item_player_photo_imageview);
            displayNameView = (TextView) view.findViewById(R.id.players_list_item_player_name_textview);
            phoneNumView = (TextView) view.findViewById(R.id.players_list_item_phone_number_textview);
        }
    }

    public PlayersAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        defaultPlayerImage = context.getDrawable(R.drawable.person_icon);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.players_list_item, parent, false);

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

        // Read displayName from cursor
        String displayName = cursor.getString(NewGameFragment.COL_DISPLAY_NAME);
        viewHolder.displayNameView.setText(displayName);

        // Read phoneNum from cursor
        String phone = cursor.getString(NewGameFragment.COL_PHONE_NUM);
        viewHolder.phoneNumView.setText(phone);

        // Set image if available
        viewHolder.photoView.setImageDrawable(defaultPlayerImage);
        String imageSrc = cursor.getString(NewGameFragment.COL_PHOTO_URI);
        if (imageSrc != null)
            viewHolder.photoView.setImageURI(Uri.parse(imageSrc));

    }
}