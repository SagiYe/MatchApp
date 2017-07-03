package sagiyehezkel.matchapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import sagiyehezkel.matchapp.data.ContactsManager;
import sagiyehezkel.matchapp.gcm.GcmRegistrationManager;

public class RegistrationActivity extends Activity {
    private EditText etCountryCode;
    private EditText etPhoneNumber;
    private String mPhoneNumber;
    private String mCountryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etCountryCode = (EditText) findViewById(R.id.registation_country_code_edittxt);
        etPhoneNumber = (EditText) findViewById(R.id.registation_phone_number_edittxt);

        Button btnSignUp = (Button) findViewById(R.id.registation_signup_button);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signUp() {
        mPhoneNumber = etPhoneNumber.getText().toString();
        mCountryCode = etCountryCode.getText().toString();

        // Make sure country code start with "+"
        if (!mCountryCode.startsWith("+"))
            mCountryCode = "+" + mCountryCode;

        mPhoneNumber = Utility.getPhoneInternationalNumber(mPhoneNumber, mCountryCode);

        Utility.setPreferredPhoneAndCountryCode(this, mPhoneNumber, mCountryCode);

        GcmRegistrationManager rm = new GcmRegistrationManager(this);
        rm.verifyClientRegistration();

        ContactsManager cm = new ContactsManager(this);
        cm.syncContactsWithServer();

        // Go back to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
