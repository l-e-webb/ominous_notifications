package com.tangledwebgames.ominousnotifications;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    static final String CHANNEL_KEY = "channel";
    static final String FREQUENCY_KEY = "frequency";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String channelName = data.getString(CHANNEL_KEY);
        int frequency = data.getInt(FREQUENCY_KEY, Channel.Frequency.DEFAULT);
        Channel channel = Channel.getChannel(channelName);
        Context context = getApplicationContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (notificationManager.areNotificationsEnabled()) {
            Notification notification = channel.buildNotification(context);
            notificationManager.cancel(channel.notificationId);
            notificationManager.notify(channel.notificationId, notification);
        }
        WorkManager workManager = WorkManager.getInstance(context);
        Channel.Delay delay = new Channel.Delay(frequency);
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(NotificationWorker.class)
                .setInputData(new Data(data))
                .setInitialDelay(delay.interval, delay.unit)
                .build();
        workManager.beginUniqueWork(
                channel.id.toString(),
                ExistingWorkPolicy.APPEND,
                request
        ).enqueue();
        return Result.success();
    }
}
