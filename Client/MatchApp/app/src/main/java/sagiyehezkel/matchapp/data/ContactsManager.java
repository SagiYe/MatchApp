package sagiyehezkel.matchapp.data;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import sagiyehezkel.matchapp.Utility;
import sagiyehezkel.matchapp.security.AdvancedEncryptionStandard;

/**
 * Created by Sagi on 10/09/2015.
 */
public class ContactsManager {

    private Context mContext;

    public ContactsManager(Context context) {
        mContext = context;
    }

    public void syncContactsWithServer() {
        SyncContactsAsyncTask syncContactsAsyncTask = new SyncContactsAsyncTask();
        syncContactsAsyncTask.execute(null, null, null);
    }

    private String getContactEmail(ContentResolver cr, String contactId) {
        String email = null;

        Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
        while (emails.moveToNext()) {
            email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            break;
        }
        emails.close();
        return email;
    }

    private class SyncContactsAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String CONTACTS = "CONTACTS";
        private static final String USERS = "USERS";

        private ProgressDialog dialog;

        private class ContactsCollection {
            public class ContactData {
                private String mPhoneNumber;
                private String mDisplayName;
                private String mPhotoUri;

                public String getPhoneNumber() {
                    return mPhoneNumber;
                }

                public String getDisplayName() {
                    return mDisplayName;
                }

                public String getPhotoUri() {
                    return mPhotoUri;
                }

                public ContactData(String phoneNumber, String displayName, String photoUri) {
                    mPhoneNumber = phoneNumber;
                    mDisplayName = displayName;
                    mPhotoUri = photoUri;
                }
            }

            private HashMap<String, ContactData> mContacts;
            String mCountryCode;

            public ContactsCollection() {
                mContacts = new HashMap<String, ContactData>();
                mCountryCode = Utility.getPreferredCountryCode(mContext);
            }
            
            public void add(String phoneNumber, String displayName, String photoUri) {
                String contactKey = Utility.getPhoneInternationalNumber(phoneNumber, mCountryCode);
                ContactData contactData = new ContactData(phoneNumber, displayName, photoUri);

                mContacts.put(contactKey, contactData);
            }

            public ContactData get(String internationalPhone) {
                if (mContacts.containsKey(internationalPhone))
                    return mContacts.get(internationalPhone);

                return null;
            }
            
            public ArrayList<String> getInternationalPhonesAsArrayList() {
                ArrayList<String> arrayList = new ArrayList<String>();

                for (Map.Entry<String, ContactData> entry: mContacts.entrySet()) {
                    arrayList.add(entry.getKey());
                }

                return arrayList;
            }
        }

        public SyncContactsAsyncTask() {
            dialog = new ProgressDialog(mContext);
        }

        private List<String> getContactPhoneList(ContentResolver cr, String contactId) {
            if (contactId == null)
                return null;

            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null);

            List<String> phoneList = new ArrayList<String>();
            String phone;
            int numberColIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                phone = cursor.getString(numberColIndex);

                if (phone != null)
                    phoneList.add(phone);
            }
            cursor.close();

            return phoneList;
        }
        private ContactsCollection getContactsCollection() {
            ContactsCollection contacts = new ContactsCollection();

            ContentResolver cr =  mContext.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_URI},
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ?",
                    new String[]{"1"},
                    null);

            String id;
            String displayName;
            String photoUri;
            int idColIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int displayNameColIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int photoUriColIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);

            while (cursor.moveToNext()) {
                id = cursor.getString(idColIndex);
                displayName = cursor.getString(displayNameColIndex);
                photoUri = cursor.getString(photoUriColIndex);
                String myPhoneNumber = Utility.getPreferredPhone(mContext);
                String countryCode = Utility.getPreferredCountryCode(mContext);

                for (String phone : getContactPhoneList(cr, id)) {
                    if (!myPhoneNumber.equals(Utility.getPhoneInternationalNumber(phone, countryCode)))
                    contacts.add(phone, displayName, photoUri);
                }
            }
            cursor.close();

            return contacts;
        }

        private ArrayList<String> getUsersPhonesFromServer(ArrayList<String> contactsPhones) {
            URL url = null;
            try {
                url = new URL(Utility.SERVER_ADDRESS + "/sync_contacts");
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
                JSONArray ja = new JSONArray(contactsPhones);

                jsonParam.put(CONTACTS, ja);

                String str = jsonParam.toString();

                if (Utility.withEncryption()) {
                    AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard();
                    str = aes.encrypt(str);
                }

                writer.write(str);

                writer.flush();
                writer.close();
                os.close();

                InputStream inputStream = conn.getInputStream();

                String response = null;

                // Read the input stream into a String
                BufferedReader reader = null;
                StringBuffer buffer = new StringBuffer();
                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    response = buffer.toString();
                }

                if (Utility.withEncryption()) {
                    AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard();
                    response = aes.decrypt(response);
                }

                return Utility.fromJsonStrToArrayList(response, USERS);

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

            return null;
        }

        private void saveUsersToLocalDB(ArrayList<String> registeredUsers, ContactsCollection contactsCollection) {
            Vector<ContentValues> cVVector = new Vector<ContentValues>(registeredUsers.size());

            for (String phone : registeredUsers) {
                ContactsCollection.ContactData contactData = contactsCollection.get(phone);
                Log.v("\t", phone +
                        "\t" + contactData.getPhoneNumber() +
                        "\t" + contactData.getDisplayName() +
                        "\t" + contactData.getPhotoUri());

                ContentValues playerValues = new ContentValues();

                playerValues.put(MatchAppContract.PlayersEntry.COLUMN_PHONE_NUM, phone);
                playerValues.put(MatchAppContract.PlayersEntry.COLUMN_DISPLAY_NAME, contactData.getDisplayName());
                playerValues.put(MatchAppContract.PlayersEntry.COLUMN_PHOTO_URI, contactData.getPhotoUri());

                cVVector.add(playerValues);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MatchAppContract.PlayersEntry.CONTENT_URI, cvArray);
            }

            Log.v("","SyncContacts Complete" + inserted + " players Inserted");
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
            Toast.makeText(mContext, "Contacts list updated...", Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContactsCollection contactsCollection = getContactsCollection();
            ArrayList<String> usersPhones =
                    getUsersPhonesFromServer(contactsCollection.getInternationalPhonesAsArrayList());

            saveUsersToLocalDB(usersPhones, contactsCollection);
            return null;
        }
    }
}
