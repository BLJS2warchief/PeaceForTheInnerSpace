package com.mns2b.space;

import android.app.Activity;
import android.os.Bundle;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.*;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.provider.Settings;
import android.net.Uri;
import android.support.v4.app.*;

import java.io.File;
import java.util.Formatter;
import java.math.*;

public class  SpaceActivity extends Activity 
{
    StatFs stat2;
    long blockSize, availableBlocks;
    double sizeInGB, formatedSize;
    File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent i = getIntent();

        switch(i.getAction()){
            case ("delete"):
                dir = new File(Environment.getExternalStorageDirectory()+"/SHAREit/audios");
                deleteDir(dir);
                break;
            case ("refresh"):
            default:
                findFreeSpace();
                break;
        }
        finish();
    }

/**************************************************************************/
/*  Calculate available space in the system                               */
/**************************************************************************/
    public void findFreeSpace(){
        stat2 = new StatFs(Environment.getDataDirectory().getPath());
        blockSize = stat2.getBlockSize();
        availableBlocks = stat2.getAvailableBlocks();

        sizeInGB = availableBlocks / 1073741824.0 * blockSize;

        formatedSize = BigDecimal.valueOf(sizeInGB)
            .setScale(3, RoundingMode.HALF_UP)
            .doubleValue();

        Toast.makeText(getApplicationContext(), formatedSize + "GB free", Toast.LENGTH_LONG).show();
        displayNotification();
    }

/**************************************************************************/
/*  Delete directory and all it's contents                                */
/**************************************************************************/
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

/**************************************************************************/
/*  Initiate and display notification                                     */
/**************************************************************************/
    public void displayNotification(){
        Notification noti;
        NotificationManager mNotificationManager;
        Intent refreshIntent;
        PendingIntent storageIntent, refreshPendingIntent, deleteDirIntent;
        RemoteViews mRemoteViews;

        refreshPendingIntent = PendingIntent.getActivity(this, 0, getPackageManager().getLaunchIntentForPackage("com.mns2b.space").setAction("refresh"), 0);
        storageIntent = PendingIntent.getActivity(this, 0, new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS), 0);

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        mRemoteViews.setImageViewResource(R.id.notif_icon, R.drawable.appicon);
        mRemoteViews.setTextViewText(R.id.notif_title, "Free space");
        mRemoteViews.setTextViewText(R.id.notif_content, Double.toString(formatedSize) + "GB");
        mRemoteViews.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.btnCache, storageIntent);

        noti = new Notification.Builder(this)
            .setContentTitle("Free Space")
            .setContentText(formatedSize + "GB")
            .setSmallIcon(R.drawable.appicon)
            .setOngoing(true)
            .setContentIntent(refreshPendingIntent)
            .build();
        
        noti.bigContentView = mRemoteViews;

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, noti);
    }
}
