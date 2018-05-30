package org.lightsys.emailhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import java.net.URL;
import java.util.Calendar;
import java.util.Random;

import static java.lang.Math.pow;


/**
 * @author Judah Sistrunk
 * created on 5/25/2016.
 * copied from missionary app
 *
 * service class that automatically updates local database with server database
 * for the eventApp
 *
 * Pulled in to and modified for emailHelper for notifications - SHADE
 */
public class AutoUpdater extends Service {

    //time constants in milliseconds
    private static final int ONE_SECOND     = 1000;
    private static final int ONE_MINUTE     = ONE_SECOND * 60;
    private static final int NEVER          = -1;
    private emailNotification gotMail;

    private DatabaseHelper db; //local database
    private SharedPreferences sp;
    private Resources r;

    private int      updateMillis = NEVER; //number of milliseconds between updates
    private Calendar prevDate     = Calendar.getInstance();

    //custom timer that ticks every minute
    //used to constantly check to see if it's time to check for updates
    private final Handler timerHandler  = new Handler();

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private WifiManager wifiManager = null;
    private WifiManager.WifiLock wifiLock = null;

    public AutoUpdater() {
        gotMail = new emailNotification();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Make sure the alarm is running
                try {
                    checkAlarm();
                } catch (Exception e) {
                    Log.d("AutoUpdater", "checkAlarm exception: " + e.getMessage());
                }

                // check for updates.  wrap in try/catch in the event something
                // goes wrong, so we can keep the service from crashing entirely.
                try {
                    checkForUpdates();
                } catch (Exception e) {
                    Log.d("AutoUpdater", "update check failed: " + e.getMessage());
                }
                //resets timer continuously
                timerHandler.postDelayed(this, ONE_MINUTE);
            }
        };
        timerHandler.postDelayed(timerRunnable, ONE_SECOND);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new DatabaseHelper(getApplicationContext());
        sp = getSharedPreferences("myPreferences", 0);
        r = getResources();
        checkForUpdates();

        //keeps service running after app is shut down
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    // This gets called when the AsyncTask DataConnection completes.
    public void onCompletion() {
        // Release any wake lock now that we're done.
        pmCleanup();
    }


    private void pmCleanup() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
        wifiLock = null;
        wifiManager = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;
        powerManager = null;
    }

    private void checkAlarm() {
        // Our alarm handler reference
        Intent wakeAlarmHandler = new Intent(getApplicationContext(), WakeupAlarmReceiver.class);

        // Already exists? (don't reset it if so)
        PendingIntent isAlarm = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                wakeAlarmHandler,
                PendingIntent.FLAG_NO_CREATE
        );

        if (isAlarm == null) {
        SharedPreferences sp = getSharedPreferences("myPreferences",0);
            // Does not exist -- create a new one.
            PendingIntent wakeAlarm = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    wakeAlarmHandler,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );

            // Set a 15 minute interval for the alarm.
            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            int updateFrequency = Integer.valueOf(sp.getString(getResources().getString(R.string.key_update_frequency),getResources().getString(R.string.default_update_frequency)));
            int updateTimePeriod = Integer.valueOf(sp.getString(getResources().getString(R.string.key_update_time_period),getResources().getString(R.string.value_time_period_minutes)));
            long updateTime = (long) (updateFrequency * pow(60,updateTimePeriod) * 1000);
            alarms.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    SystemClock.elapsedRealtime() + (updateTime),
                    updateTime,
                    wakeAlarm
            );
        }
    }


    public void checkForUpdatesPM() {
        // Acquire the power manager wake lock.
        if (wakeLock == null || !wakeLock.isHeld()) {
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            try {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "org.lightsys.eventApp.tools.AutoUpdater");
            } catch (Exception e) {
                pmCleanup();
                return;
            }
            wakeLock.acquire(5000 /* milliseconds */);
        }

        // Acquire the wifi manager lock.
        if (wifiLock == null || !wifiLock.isHeld()) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            try {
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "org.lightsys.eventApp.tools.AutoUpdater");
            } catch (Exception e) {
                pmCleanup();
                return;
            }
        }

        // Go check for updates
        try {
            checkForUpdates();
        } catch (Exception e) {
            pmCleanup();
            Log.d("checkForUpdatesPM", "update check failed: " + e.getMessage());
        }
    }


    private void checkForUpdates(){
        GetMail mailer = new GetMail(db,sp,r);
        mailer.execute();
    }

    /**********************************************************************************************
     *                                  Notification Builder                                      *
     **********************************************************************************************/
    private void sendNotification(String title, String subject){
        SharedPreferences sp = getSharedPreferences("myPreferences",0);
        if(!sp.getBoolean(getResources().getString(R.string.key_update_show_notifications),getResources().getBoolean(R.bool.default_update_show_notifications))){
            return;
        }

        Context context = this;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuild;
        PowerManager.WakeLock screenWakeLock = null;
        Notification n;


        // Build the notification to be sent

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//this uses the new oreo style if necessary
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel("3141","EMAIL_HELPER",importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
            nBuild = new NotificationCompat.Builder(context,"3141");
        }
        else{
            nBuild = new NotificationCompat.Builder(context);
        }
        nBuild.setContentTitle(title)
                .setContentText(subject)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setSmallIcon(R.drawable.ic_refresh)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notifications_black_24dp))
                .setContentIntent(intent)
                .setPriority(1)
                .setAutoCancel(true)
                // BigTextStyle allows notification to be expanded if text is more than one line
                .setStyle(new NotificationCompat.BigTextStyle().bigText(subject));
        n = nBuild.build();

        // Turn on the device and send the notification.
        if (powerManager != null) {
            screenWakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                    "org.lightsys.eventApp.tools.AutoUpdater"
            );
            screenWakeLock.acquire(500);
        }

        try {
            Random inc = new Random();
            notificationManager.notify((int)(System.currentTimeMillis()/1000 + inc.nextInt(100)), n);
        } catch (Exception e) {
            // ignore
        }
        if (screenWakeLock != null && screenWakeLock.isHeld()) {
            screenWakeLock.release();
        }
    }

}
