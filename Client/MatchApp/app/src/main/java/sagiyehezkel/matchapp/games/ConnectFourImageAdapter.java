package sagiyehezkel.matchapp.games;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;

import sagiyehezkel.matchapp.R;

/**
 * Created by Sagi on 23/11/2015.
 */
public class ConnectFourImageAdapter extends BaseAdapter {
    final private int NUM_OF_ROWS = 6;
    final private int NUM_OF_COLUMNS = 7;

    private Context mContext;

    // references to our images
    private Integer[] mThumbIds = new Integer[NUM_OF_ROWS * NUM_OF_COLUMNS];

    private int lastCellEdited = -1;

    public ConnectFourImageAdapter(Context c) {
        mContext = c;
        Arrays.fill(mThumbIds, R.drawable.connect_four_gray);
    }

    public void clearLastEditedCell() {
        lastCellEdited = -1;
    }

    public int makeMove(int cell) {
        // First clear last chosen cell
        if (lastCellEdited != -1)
            mThumbIds[lastCellEdited] = R.drawable.connect_four_gray;

        int col = cell % NUM_OF_COLUMNS;

        // Move to the last row
        cell = (NUM_OF_COLUMNS * (NUM_OF_ROWS - 1)) + col;

        // Search up for the first available cell
        while (cell > 0 &&
                mThumbIds[cell] != R.drawable.connect_four_gray)
            cell -= NUM_OF_COLUMNS;

        mThumbIds[cell] = R.drawable.connect_four_green;
        lastCellEdited = cell;

        notifyDataSetChanged();

        return lastCellEdited;
    }

    public void initBoard(ArrayList<Integer> initState, boolean imTheFirstPlayer) {
        int nextColor;

        if (imTheFirstPlayer)
            nextColor = R.drawable.connect_four_green;
        else
            nextColor = R.drawable.connect_four_red;

        for (Integer i : initState) {
            mThumbIds[i] = nextColor;
            nextColor = nextColor(nextColor);
        }

        notifyDataSetChanged();
    }

    private int nextColor(int curColor) {
        int nextColor;

        if (curColor == R.drawable.connect_four_red)
            nextColor = R.drawable.connect_four_green;
        else
            nextColor = R.drawable.connect_four_red;

        return nextColor;
    }


    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }
}
