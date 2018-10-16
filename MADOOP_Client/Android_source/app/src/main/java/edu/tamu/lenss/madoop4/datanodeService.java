package edu.tamu.lenss.madoop4;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

//import org.apache.hadoop.mapred.JobConf;
//import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.StringUtils;

import edu.tamu.lenss.util.LogFactory;
import edu.tamu.lenss.util.ILogger;

import edu.tamu.lenss.R;

public class datanodeService extends Service {
    private static ILogger LOG;// = LogFactory.getLog(MainActivity.class);
    private static DataNode dataNode;


    public void notification() {
//        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.xxx);
//        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
//        mBuilder.setLargeIcon(bitmap);
        mBuilder.setContentTitle("Madoop DataNode");
        mBuilder.setContentText("DataNode service is running");
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
        mNotificationManager.notify(1220, mBuilder.build());
    }


    public void cleanNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        mNotificationManager.cancel(1220);
    }



    @Override
    public void onCreate() {
        LogFactory.AndroidLogcat = false;
        LogFactory.LogFile = "datanode.log";
        LOG = LogFactory.getLog(Main2Activity.class);
        LOG.info("Starting DataNode service!");
        Toast.makeText(this, "Starting DataNode service", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {

            @Override
            public void run() {

                String args [] = {};
                try {
                    StringUtils.startupShutdownMessage(DataNode.class, args, LOG);
                    dataNode = DataNode.createDataNode(args, null);
                    if (dataNode != null)
                        dataNode.join();
                } catch (Throwable e) {
                    LOG.error(StringUtils.stringifyException(e));
//                    System.exit(-1);
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
        Toast.makeText(this, "Shutting down DataNode service", Toast.LENGTH_SHORT).show();
        LOG.info("Stopping DataNode service!");

        try {
            dataNode.shutdown();
        } catch (Exception e) {
            LOG.error(e);
        }
        cleanNotification();


    }
}
