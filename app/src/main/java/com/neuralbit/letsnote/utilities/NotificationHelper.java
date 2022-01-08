package com.neuralbit.letsnote.utilities;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.neuralbit.letsnote.R;


public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";
    private String noteTitle;
    private String noteDesc;
    private NotificationManager mManager;

    public NotificationHelper(Context base,String noteTitle, String noteDesc) {
        super(base);
        this.noteTitle = noteTitle;
        this.noteDesc = noteDesc;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        if (noteTitle!=null ){
            if (!noteTitle.isEmpty()){
                return new NotificationCompat.Builder(getApplicationContext(), channelID)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(noteTitle)
                        .setSmallIcon(R.mipmap.ic_launcher_round);
            }else{
                return new NotificationCompat.Builder(getApplicationContext(), channelID)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(noteDesc)
                        .setSmallIcon(R.mipmap.ic_launcher_round);
            }
        }else{
            return new NotificationCompat.Builder(getApplicationContext(), channelID)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(noteDesc)
                    .setSmallIcon(R.mipmap.ic_launcher_round);
        }

    }
}