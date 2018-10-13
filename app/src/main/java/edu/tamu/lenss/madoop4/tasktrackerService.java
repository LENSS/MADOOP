package edu.tamu.lenss.madoop4;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.StringUtils;
import org.mortbay.jetty.Main;

import edu.tamu.lenss.util.LogFactory;
import edu.tamu.lenss.util.ILogger;
import edu.tamu.lenss.R;

public class tasktrackerService extends Service {
    private static ILogger LOG;// = LogFactory.getLog(MainActivity.class);
    private static TaskTracker taskTracker;


    public void notification() {
//        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.xxx);
//        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
//        mBuilder.setLargeIcon(bitmap);
        mBuilder.setContentTitle("Madoop TaskTracker");
        mBuilder.setContentText("TaskTracker service is running");
//    mBuilder.setSubText("subtext");
        mBuilder.setAutoCancel(false);
//        mBuilder.setContentInfo("Info");
//        mBuilder.setNumber(2);
//        mBuilder.setTicker("ticker");
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
//        mBuilder.setWhen(System.currentTimeMillis() - 3600000);
        mBuilder.setOngoing(true);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
//        mBuilder.setVibrate(new long[]{0, 1000, 1000, 1000});

//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final Intent notificationIntent = new Intent(this, Main2Activity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1219, mBuilder.build());
    }


    public void cleanNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        mNotificationManager.cancel(1219);
    }


    @Override
    public void onCreate() {

        LogFactory.AndroidLogcat = false;
        LogFactory.LogFile = "tasktracker.log";
        LOG = LogFactory.getLog(Main2Activity.class);
        LOG.info("Starting TaskTracker service!");
        Toast.makeText(this, "Starting TaskTracker service", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {

            @Override
            public void run() {

                String argv [] = {};
                StringUtils.startupShutdownMessage(TaskTracker.class, argv, LOG);

                try {
                    JobConf conf=new JobConf();
                    // enable the server to track time spent waiting on locks
                    ReflectionUtils.setContentionTracing
                            (conf.getBoolean("tasktracker.contention.tracking", false));
                    taskTracker = new TaskTracker(tasktrackerService.this, conf);
                    taskTracker.run();
                } catch (Throwable e) {
                    LOG.error("Can not start task tracker because "+
                            StringUtils.stringifyException(e));
                }

//                try {
//                    String argv [] = {};
//                    if(TaskTracker.main(argv)) {
////                        append(tv, "  ***TaskTracker is Running...!!!***");
////                        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
//                        LOG.info("  ***TaskTracker is Running...!!!***");
//                    } else {
////                        append(tv, "  ***TaskTracker failed!!!***");
////                        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
//                        LOG.info("  ***TaskTracker has failed...!!!***");
//                    }
////                    cleanup();
//
//                } catch (Exception e) {
////                    cleanup();
//                    LOG.error(e);
//                }

            }
        }).start();
        notification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "TaskTracker service is running", Toast.LENGTH_SHORT).show();

        // If we get killed, after returning from here, restart
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Shutting down TaskTracker service", Toast.LENGTH_SHORT).show();
        LOG.info("Stopping TaskTracker service!");

        try {
            taskTracker.shutdown();
        } catch (Exception e) {
            LOG.error(e);
        }
        cleanNotification();


    }
}
