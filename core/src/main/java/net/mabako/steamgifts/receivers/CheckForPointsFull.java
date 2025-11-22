package net.mabako.steamgifts.receivers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class CheckForPointsFull extends AbstractNotificationCheckReceiver {
    private static final NotificationId NOTIFICATION_ID = NotificationId.POINTS_FULL;

    private static final String PREF_KEY_LAST_POINTS_AMOUNT = "last-points-amount";

    private static final String TAG = CheckForPointsFull.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Checking for points full...");
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NOTIFICATIONS_SERVICE, Context.MODE_PRIVATE);

        String action = intent.getAction();
        int lastPointsAmount = sharedPreferences.getInt(PREF_KEY_LAST_POINTS_AMOUNT, 400);
        int currentPointsAmount = SteamGiftsUserData.getCurrent(context).getPoints();

        if (!TextUtils.isEmpty(action)) {
            Log.w(TAG, "Trying to execute action " + action);
            return;
        }

        if (currentPointsAmount >= 400) {
            if (lastPointsAmount < 400) {
                // Points are full, and we haven't shown this notification yet.
                Log.v(TAG, "Points are newly full (" + lastPointsAmount + " -> " + currentPointsAmount + "), showing notification...");

                Intent launchApp = new Intent(context, MainActivity.class);
                PendingIntent launchAppIntent = PendingIntent.getActivity(context, 0, launchApp, PendingIntent.FLAG_IMMUTABLE);
                showNotification(context, NOTIFICATION_ID, R.drawable.sgwhite, "Your points are full", "You have " + currentPointsAmount + " points.", launchAppIntent, null);
            } else {
                Log.v(TAG, "Points are full, but we've already shown this notification.");
            }
        } else {
            Log.v(TAG, "Points are not full.");
        }

        sharedPreferences.edit().putInt(PREF_KEY_LAST_POINTS_AMOUNT, currentPointsAmount).apply();
    }
}
