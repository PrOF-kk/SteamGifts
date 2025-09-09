package net.mabako.steamgifts.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import net.mabako.common.Compat;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.util.List;
import java.util.Set;

public abstract class AbstractNotificationCheckReceiver extends BroadcastReceiver {
    private static final String DEFAULT_PREF_NOTIFICATIONS_ENABLED = "preference_notifications";

    /**
     * The preference wherein all notification services store their stuff.
     */
    protected static final String PREFS_NOTIFICATIONS_SERVICE = "notification-service";

    /**
     * Number of notifications we display at most.
     */
    protected static final int MAX_DISPLAYED_NOTIFICATIONS = 5;

    public enum NotificationId {
        MESSAGES("Messages"),
        WON("Won Giveaways"),
        POINTS_FULL("Points Full"),
        NO_TYPE("Other");

        private final String channelName;
        NotificationId(String channelName) {
            this.channelName = channelName;
        }
        public String channelId() { return this.name(); }
        public String channelName() { return channelName; }
    }

    public static void initNotificationChannels(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        Set<String> notificationChannelIds = Compat.HashSet.newHashSet(NotificationId.values().length);

        for (NotificationId notificationId : NotificationId.values()) {
            NotificationChannelCompat channel =
                    new NotificationChannelCompat.Builder(notificationId.channelId(), NotificationManagerCompat.IMPORTANCE_LOW)
                            .setName(notificationId.channelName())
                            .build();

            notificationChannelIds.add(notificationId.channelId());
            manager.createNotificationChannel(channel);
        }

        manager.deleteUnlistedNotificationChannels(notificationChannelIds);
    }

    /**
     * Check if we should run the network task.
     *
     * @param tag     tag to be used for logging
     * @param context context of the broadcast receiver's onReceive
     * @return true if we should execute the network task, false otherwise
     */
    protected static boolean shouldRunNetworkTask(final String tag, final Context context) {
        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DEFAULT_PREF_NOTIFICATIONS_ENABLED, true);
        if (!notificationsEnabled) {
            Log.v(tag, "Notifications disabled");
            return false;
        }

        SteamGiftsUserData userData = SteamGiftsUserData.getCurrent(context);
        if (!userData.isLoggedIn()) {
            Log.v(tag, "Not checking for remote data, no session info available");
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities net = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean activeNetworkMetered = cm.isActiveNetworkMetered();
        if (net == null ||!net.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) || !net.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) || activeNetworkMetered) {
            Log.v(tag, "Not checking for messages due to network capabilities: " + net + " metered: " + activeNetworkMetered);
            return false;
        }

        return true;
    }

    /**
     * Display a notification for a single item.
     *
     * @param context        receiver context
     * @param notificationId the notification id to replace (should be unique per class)
     * @param iconResource   icon to display along with the notification
     * @param title          title
     * @param content        text to display in the notification
     * @param viewIntent     what happens when clicking the notification
     * @param deleteIntent   what happens when dismissing the notification
     */
    protected static void showNotification(Context context, NotificationId notificationId, @DrawableRes int iconResource, String title, CharSequence content, PendingIntent viewIntent, PendingIntent deleteIntent) {
        Notification notification = new NotificationCompat.Builder(context, notificationId.channelId())
                .setSmallIcon(iconResource)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)) /* 4.1+ */
                .setContentIntent(viewIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true)
                .build();

        showNotification(context, notificationId, notification);
    }

    /**
     * Display a notification for multiple items.
     *
     * @param context        receiver context
     * @param notificationId the notification id to replace (should be unique per class)
     * @param iconResource   icon to display along with the notification
     * @param title          title
     * @param content        texts to display in the notification
     * @param viewIntent     what happens when clicking the notification
     * @param deleteIntent   what happens when dismissing the notification
     */

    protected static void showNotification(Context context, NotificationId notificationId, @DrawableRes int iconResource, String title, List<CharSequence> content, PendingIntent viewIntent, PendingIntent deleteIntent) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (CharSequence c : content)
            inboxStyle.addLine(c);

        Notification notification = new NotificationCompat.Builder(context, notificationId.channelId())
                .setSmallIcon(iconResource)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(title)
                .setContentText(content.get(0))
                .setStyle(inboxStyle) /* 4.1+ */
                .setNumber(SteamGiftsUserData.getCurrent(context).getMessageNotification())
                .setContentIntent(viewIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true)
                .build();

        showNotification(context, notificationId, notification);
    }

    /**
     * Show the built notification in the systray.
     *
     * @param context        receiver context
     * @param notificationId notification id to use for this notification
     * @param notification   notification to display
     */
    private static void showNotification(Context context, NotificationId notificationId, Notification notification) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId.ordinal(), notification);
    }
}
