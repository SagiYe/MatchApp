package sagiyehezkel.matchapp;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import sagiyehezkel.matchapp.data.MatchAppDbHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class DebugActivityFragment extends Fragment {

    public DebugActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_debug, container, false);

        Button btnSignUp = (Button) rootView.findViewById(R.id.mainButton);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                startActivity(intent);
            }
        });

        Button btnPublish = (Button) rootView.findViewById(R.id.publishDB);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatchAppDbHelper matchAppDbHelper = new MatchAppDbHelper(getContext());
                matchAppDbHelper.exportDatabaseToSDCard();
            }
        });

        Button btnClearDB = (Button) rootView.findViewById(R.id.clearDbButton);
        btnClearDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatchAppDbHelper matchAppDbHelper = new MatchAppDbHelper(getContext());
                matchAppDbHelper.clearDB();
            }
        });

        return rootView;
    }
}
