package com.tangledwebgames.ominousnotifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Channel {

    static final List<Channel> CHANNELS = new ArrayList<>();
    private static final Map<String, Channel> CHANNEL_NAME_MAP = new HashMap<>();
    private static final Map<ChannelId, Channel> CHANNEL_ID_MAP = new HashMap<>();

    final ChannelId id;
    final int nameId;
    final int descriptionId;

    String name;
    String description;

    Channel(ChannelId id, int nameId, int descriptionId) {
        this.id = id;
        this.nameId = nameId;
        this.descriptionId = descriptionId;
    }

    void init(Context context) {
        this.name = context.getString(nameId);
        this.description = context.getString(descriptionId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(id.toString(), name, importance);
            notificationChannel.setDescription(description);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }

    Notification buildNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, id.toString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(name)
                .setContentText(generateNotificationText(context))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        return builder.build();
    }

    String generateNotificationText(Context context) {
        switch (id) {
            case ELDRITCH_HORROR:
                return generateEldritchNotification(context);
            case DEAD_DROP:
                return generateDeadDropNotification(context);
            case CYBERSPACE:
                return generateCyberspaceNotification(context);
            case SPACEWAR:
                return generateSpacewarNotification(context);
        }
        return "";
    }

    static String generateEldritchNotification(Context context) {
        return "The void is watching. It draws ever nearer.";
    }

    static String generateDeadDropNotification(Context context) {
        return "Whiskey on the move.";
    }

    static String generateCyberspaceNotification(Context context) {
        return "Black ice detected. Mainframe lockdown in 5 minutes.";
    }

    static String generateSpacewarNotification(Context context) {
        return "Main fleet entering hyperspace. ETA 0300.14.98 Andromeda time";
    }

    static Channel getChannel(String channelName) {
        return CHANNEL_NAME_MAP.get(channelName);
    }

    static Channel getChannel(ChannelId channelId) {
        return CHANNEL_ID_MAP.get(channelId);
    }

    static void initChannels(Context context) {
        CHANNELS.clear();
        CHANNEL_NAME_MAP.clear();
        CHANNEL_ID_MAP.clear();
        Channel eldritchHorror = new Channel(
                ChannelId.ELDRITCH_HORROR,
                R.string.eldritch_horror,
                R.string.eldritch_horror_description
        );
        Channel cyberspace = new Channel(
                ChannelId.CYBERSPACE,
                R.string.cyperspace,
                R.string.cyberspace_description
        );
        Channel deadDrop = new Channel(
                ChannelId.DEAD_DROP,
                R.string.dead_drop,
                R.string.dead_drop_description
        );
        Channel spacewar = new Channel(
                ChannelId.SPACEWAR,
                R.string.spacewar,
                R.string.spacewar_description
        );
        CHANNELS.add(eldritchHorror);
        CHANNELS.add(cyberspace);
        CHANNELS.add(deadDrop);
        CHANNELS.add(spacewar);
        for (Channel channel : CHANNELS) {
            channel.init(context);
            CHANNEL_NAME_MAP.put(channel.name, channel);
            CHANNEL_ID_MAP.put(channel.id, channel);
        }

    }

    enum ChannelId {
        ELDRITCH_HORROR,
        CYBERSPACE,
        DEAD_DROP,
        SPACEWAR
    }
}
