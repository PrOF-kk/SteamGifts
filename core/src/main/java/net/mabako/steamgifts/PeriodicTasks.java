package net.mabako.steamgifts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.mabako.steamgifts.receivers.CheckForNewMessages;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tasks to schedule for execution every now and then.
 */
public class PeriodicTasks {
    private static final Set<Task> scheduledTasks = EnumSet.noneOf(Task.class);

    private static void scheduleTask(Task task, Context context) {
        if (!scheduledTasks.contains(task)) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, task.taskClass), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), task.interval, pendingIntent);

            scheduledTasks.add(task);
        }
    }

    /**
     * Initialize all regularly scheduled tasks to be started.
     */
    public static void scheduleAllTasks(Context context) {
        for (Task task : Task.values())
            scheduleTask(task, context);
    }

    /**
     * Task definitions for regularly executed tasks.
     */
    public enum Task {
        CHECK_FOR_MESSAGES(AlarmManager.INTERVAL_HALF_HOUR, CheckForNewMessages.class);

        private final long interval;
        private final Class<? extends BroadcastReceiver> taskClass;

        Task(long interval, Class<? extends BroadcastReceiver> taskClass) {
            this.interval = interval;
            this.taskClass = taskClass;
        }
    }

}
