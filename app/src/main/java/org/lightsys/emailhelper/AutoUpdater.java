package org.lightsys.emailhelper;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.net.URL;
import java.util.List;
import xdroid.toaster.Toaster;
import static java.lang.Math.pow;
/**
 * @author Judah Sistrunk
 * created on 5/25/2016.
 * copied from missionary app
 *
 * service class that automatically updates local database with server database
 * for the eventApp
 *
 * Pulled into and modified for emailHelper for notifications - SHADE
 */
public class AutoUpdater extends Service {

    //time constants in milliseconds
    private static final int ONE_SECOND     = 1000;
    private emailNotification gotMail;

    private SharedPreferences sp;

    //TODO remove testing variable
    private boolean testing = false;

    //custom timer that ticks every minute
    //used to constantly check to see if it's time to check for updates
    private final Handler timerHandler  = new Handler();

    private PowerManager powerManager = null;

    public AutoUpdater() {
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    checkForUpdates();
                } catch (Exception e) {
                    Log.d("AutoUpdater", "update check failed: " + e.getMessage());
                }
                //resets timer continuously
                int updateFrequency = Integer.valueOf(sp.getString(getResources().getString(R.string.key_update_frequency),getResources().getString(R.string.default_update_frequency)));
                int updateTimePeriod = Integer.valueOf(sp.getString(getResources().getString(R.string.key_update_time_period),getResources().getString(R.string.value_time_period_minutes)));
                long updateTime = (long) (updateFrequency * pow(60,updateTimePeriod) * ONE_SECOND);
                if(testing){
                    updateTime = 20 * ONE_SECOND;
                }

                timerHandler.postDelayed(this, updateTime);//continuously calls for updates
            }
        };
        timerHandler.postDelayed(timerRunnable, ONE_SECOND);//updates at start up
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sp = getSharedPreferences(getString(R.string.preferences), 0);
        checkForUpdates();
        //keeps service running after app is shut down
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void checkForUpdates(){
        SendNotifications notifier = new SendNotifications();
        notifier.execute();
    }
    private class SendNotifications extends AsyncTask<URL, Integer, Long>{
        @Override
        protected Long doInBackground(URL... urls) {
            GetMail mailer = new GetMail(getApplicationContext());
            gotMail = mailer.getMail();
            if(gotMail.getInvalid_Credentials()){
                sendNotification(getString(R.string.invalid_credentials_notification_title),getString(R.string.invalid_credentials_notification_subject));
            }else{
                while (gotMail.status()){
                    NotificationBase temp = gotMail.pop();
                    sendNotification(temp.getTitle(),temp.getSubject());
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Long l){
        }
    }
    /**********************************************************************************************
     *                                  Notification Builder                                      *
     **********************************************************************************************/
    private void sendNotification(final String title, final String subject){
        SharedPreferences sp = getSharedPreferences(getResources().getString(R.string.preferences),0);
        if(!sp.getBoolean(getResources().getString(R.string.key_update_show_notifications),getResources().getBoolean(R.bool.default_update_show_notifications))){
            return;
        }
        if(appIsRunning()){
            Toaster.toastLong(title+"\n"+subject);
            return;
        }

        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuild;
        PowerManager.WakeLock screenWakeLock = null;
        Notification n;

        // Build the notification to be sent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//this uses the new oreo style if necessary
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel("3142",getString(R.string.app_name),importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setLegacyStreamType(1)
                    .build();
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
            notificationManager.createNotificationChannel(notificationChannel);
            nBuild = new NotificationCompat.Builder(context,"3142");

        }
        else{
            nBuild = new NotificationCompat.Builder(context);

        }
        nBuild.setContentTitle(title)
                .setContentText(subject)

                .setSmallIcon(R.drawable.ic_bell)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notifications_black_24dp))
                .setContentIntent(intent)
                .setPriority(1)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI,1)
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
            //Random inc = new Random();
            notificationManager.notify((int)(System.currentTimeMillis()%10000), n);// + inc.nextInt(100)
        } catch (Exception e) {
            // ignore
        }
        if (screenWakeLock != null && screenWakeLock.isHeld()) {
            screenWakeLock.release();
        }
    }
    public boolean appIsRunning(){
        final ActivityManager manager = (ActivityManager) getBaseContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        if(infos != null){
            for(int i = 0; i < infos.size();i++){
                if(infos.get(i).processName.equals(getString(R.string.package_name))){
                    if(infos.get(i).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
