package sagiyehezkel.matchapp.gcm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import sagiyehezkel.matchapp.R;
import sagiyehezkel.matchapp.Utility;
import sagiyehezkel.matchapp.security.AdvancedEncryptionStandard;

/**
 * Created by Sagi on 08/09/2015.
 */
public class GcmRegistrationManager {
    static final String TAG = GcmRegistrationManager.class.getSimpleName();
    static final String GCM_PROJECT_NUMBER = "1026385324579";

    private Context mContext;
    private SharedPreferences mPrefs;
    private GoogleCloudMessaging mGcm;
    private String mRegid;

    public GcmRegistrationManager(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean verifyClientRegistration() {
        mRegid = getRegistrationIdFromSharedPreferences();

        // This if is like this in order to support testing of registering new users
        if (mRegid.isEmpty() || true) {
            registerInBackground();
        }

        return true;
    }

    /**
     * Gets the current registration token for application on GCM service.
     */
    private String getRegistrationIdFromSharedPreferences() {
        String registrationId = mPrefs.getString(
                mContext.getString(R.string.pref_registration_id), "");

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = mPrefs.getInt(
                mContext.getString(R.string.pref_app_version), Integer.MIN_VALUE);

        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // shouldn't happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM connection servers asynchronously.
     */
    private void registerInBackground() {
        RegistrationAsyncTask registrationAsyncTask = new RegistrationAsyncTask();
        registrationAsyncTask.execute(null, null, null);
    }

    /**
     * Sends the registration ID to the server over HTTP, so it can use GCM to send messages to the app.
     */
    private void sendRegistrationIdToServer() {
        URL url = null;
        try {
            String phone = Utility.getPreferredPhone(mContext);
            if (phone == null)
                return;

            url = new URL(Utility.SERVER_ADDRESS + "/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("PHONE", phone);
            jsonParam.put("REGID", mRegid);

            String str = jsonParam.toString();

            if (Utility.withEncryption()) {
                AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard();
                str = aes.encrypt(str);
            }

            writer.write(str);

            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            conn.getResponseCode();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores the registration ID and app versionCode in the SharedPreferences.
     */
    private void storeRegistrationIdToSharedPreferences() {
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(mContext.getString(R.string.pref_registration_id), mRegid);
        editor.putInt(mContext.getString(R.string.pref_app_version), appVersion);
        editor.commit();
    }

    private class RegistrationAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public RegistrationAsyncTask() {
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Progress start");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (mGcm == null) {
                    mGcm = GoogleCloudMessaging.getInstance(mContext);
                }
                mRegid = mGcm.register(GCM_PROJECT_NUMBER);
                Log.i(TAG, "Device registered, registration ID=" + mRegid);

                sendRegistrationIdToServer();
                storeRegistrationIdToSharedPreferences();
            } catch (IOException ex) {
                Log.i(TAG, "Error :" + ex.getMessage());
            }
            return null;
        }
    }
}
