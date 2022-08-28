package com.neuralbit.letsnote.utilities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.neuralbit.letsnote.R;


public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Reminders";
    private final String noteTitle;
    private final String noteDesc;
    private final Boolean noteProtected;
    private NotificationManager mManager;

    public NotificationHelper(Context base,String noteTitle, String noteDesc,Boolean noteProtected) {
        super(base);
        this.noteTitle = noteTitle;
        this.noteDesc = noteDesc;
        this.noteProtected = noteProtected;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        Uri ringingSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (noteTitle!=null ){
            if (!noteTitle.isEmpty()){
                return new NotificationCompat.Builder(getApplicationContext(), channelID)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(noteTitle)
                        .setGroup("REMINDERS")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSound(ringingSound)
                        .setSmallIcon(R.mipmap.ic_launcher1_round)
                        .setAutoCancel(true);
            }else{
                if (noteProtected){
                    return new NotificationCompat.Builder(getApplicationContext(), channelID)
                            .setContentTitle("Reminder")
                            .setContentText("**Protected**")
                            .setGroup("REMINDERS")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setSound(ringingSound)
                            .setSmallIcon(R.mipmap.ic_launcher1_round)
                            .setAutoCancel(true);
                }else{
                    return new NotificationCompat.Builder(getApplicationContext(), channelID)
                            .setContentTitle("Reminder")
                            .setContentText(noteDesc)
                            .setGroup("REMINDERS")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setSound(ringingSound)
                            .setSmallIcon(R.mipmap.ic_launcher1_round)
                            .setAutoCancel(true);
                }
            }
        }else{
            if (noteProtected){
                return new NotificationCompat.Builder(getApplicationContext(), channelID)
                        .setContentTitle("Reminder")
                        .setContentText("**Protected**")
                        .setGroup("REMINDERS")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSound(ringingSound)
                        .setSmallIcon(R.mipmap.ic_launcher1_round)
                        .setAutoCancel(true);
            }else{
                return new NotificationCompat.Builder(getApplicationContext(), channelID)
                        .setContentTitle("Reminder")
                        .setContentText(noteDesc)
                        .setGroup("REMINDERS")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSound(ringingSound)
                        .setSmallIcon(R.mipmap.ic_launcher1_round)
                        .setAutoCancel(true);
            }
        }

    }
}