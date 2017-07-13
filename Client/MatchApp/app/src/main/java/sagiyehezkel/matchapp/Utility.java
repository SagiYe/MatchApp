package sagiyehezkel.matchapp;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Sagi on 08/09/2015.
 */
public class Utility {
    public static final boolean TO_ENCRYPT = true;

    public static final String SERVER_ADDRESS = "http://a88a5487.ngrok.io";


    private static int mNotificationId = 1;

    public static String getPreferredPhone(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_user_phone_number), null);
    }

    public static String getPreferredCountryCode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_user_country_code), null);
    }

    public static void setPreferredPhoneAndCountryCode(Context context, String phone, String countryCode) {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putString(context.getString(R.string.pref_user_phone_number), phone);
        editor.putString(context.getString(R.string.pref_user_country_code), countryCode);
        editor.commit();
    }

    public static String getPhoneInternationalNumber(String phone, String countryCode) {
        if (phone.startsWith("0"))
            phone = phone.substring(1);

        if (!phone.startsWith("+"))
            phone = countryCode + phone;

        // Removing "-" and " "
        phone = phone.replaceAll("\\s", "");
        phone = phone.replaceAll("-", "");

        // Removing plus sign (+) from the start
        phone = phone.substring(1);
        return phone;
    }

    public static ArrayList<String> fromJsonStrToArrayList(String jsonStr, String listName) throws JSONException {
        ArrayList<String> arrayList = new ArrayList<>();

        if (jsonStr != null) {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray(listName);

            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.getString(i));
            }
        }
        return arrayList;
    }

    public static String fromArrayListToJsonStr(ArrayList<?> arrayList, String listName) throws JSONException {
        JSONObject jsonOutput = new JSONObject();
        JSONArray jsonArray = new JSONArray(arrayList);
        jsonOutput.put(listName, jsonArray);

        return jsonOutput.toString();
    }

    public static String getFriendlyUpdateTimeOrDayString(Context context, long dateInMillis) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(dateInMillis));

        Calendar calendarNow = new GregorianCalendar();
        calendarNow.setTime(new Date());

        // If its the same day
        if ((calendar.get(Calendar.YEAR) == calendarNow.get(Calendar.YEAR)) &&
            (calendar.get(Calendar.DAY_OF_YEAR) == calendarNow.get(Calendar.DAY_OF_YEAR))) {
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("HH:mm");
            return monthDayFormat.format(dateInMillis);
        } else if ((calendar.get(Calendar.YEAR) == calendarNow.get(Calendar.YEAR)) &&
                (calendar.get(Calendar.DAY_OF_YEAR) + 1 == calendarNow.get(Calendar.DAY_OF_YEAR))) {
            return context.getString(R.string.yesterday);
        } else {
            // Otherwise, use the form "21/03/2015"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    public static String fromIntegerListToString(ArrayList<Integer> list) {
        StringBuilder sb = new StringBuilder();

        for (Integer i : list) {
            sb.append(i);
            sb.append(",");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public static ArrayList<Integer> fromStringToIntegerList(String s) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        if (s != null) {
            String[] sArr = s.split(",");
            for (int i = 0; i < sArr.length; ++i) {
                list.add(Integer.parseInt(sArr[i]));
            }
        }

        return list;
    }

    public static void showNotificationIfAppInBackground(Context context, String title,
                                                         String text, String photoUri) {
        if (!isAppInBackground(context))
            return;

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.matchapp_notificition_icon3)
                        .setContentTitle(title)
                        .setContentText(text);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSound(alarmSound);

        if (photoUri != null) {
            Uri uri = Uri.parse(photoUri);
            Bitmap photo =  null;
            try {
                photo = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            notificationBuilder.setLargeIcon(photo);
        }

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, notificationBuilder.build());

        ++mNotificationId;
    }

    public static boolean isAppInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public static boolean withEncryption() {
        return TO_ENCRYPT;
    }
}
