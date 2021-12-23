package com.neuralbit.letsnote.utilities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.neuralbit.letsnote.AddEditNoteActivity;
import com.neuralbit.letsnote.MainActivity;
import com.neuralbit.letsnote.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, String.valueOf(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Let's note alert")
                .setContentText("lets note")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(123,builder.build());

    }
}
