package net.mabako.steamgifts.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class CheckForPointsFull extends AbstractNotificationCheckReceiver {
    private static final NotificationId NOTIFICATION_ID = NotificationId.POINTS_FULL;

    private static final String PREF_KEY_LAST_POINTS_AMOUNT = "last-points-amount";

    private static final String TAG = CheckForPointsFull.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NOTIFICATIONS_SERVICE, Context.MODE_PRIVATE);

        String action = intent.getAction();
        int lastPointsAmount = sharedPreferences.getInt(PREF_KEY_LAST_POINTS_AMOUNT, 400);
        int currentPointsAmount = SteamGiftsUserData.getCurrent(context).getPoints();

        if (TextUtils.isEmpty(action) && lastPointsAmount <= 400 && currentPointsAmount >= 400) {
            // Points are full, and we haven't shown this notification yet.
            sharedPreferences.edit().putInt(PREF_KEY_LAST_POINTS_AMOUNT, currentPointsAmount).apply();
            showNotification(context, NOTIFICATION_ID, R.drawable.sgwhite, "Your points are full", "You have " + currentPointsAmount + " points.", null, null);
        }
    }
}
