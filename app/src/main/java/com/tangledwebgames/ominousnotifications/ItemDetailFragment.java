package com.tangledwebgames.ominousnotifications;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Channel mChannel;
    private Switch onOffSwitch;
    private RadioGroup mFrequencyGroup;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mChannel = Channel.getChannel(getArguments().getString(ARG_ITEM_ID));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mChannel.name);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mChannel != null) {

            String prefsKey = Channel.PREFERENCES_KEY + mChannel.id.toString();
            SharedPreferences prefs = getContext().getSharedPreferences(prefsKey, Context.MODE_PRIVATE);

            ((TextView)rootView.findViewById(R.id.description))
                    .setText(mChannel.description);

            onOffSwitch = rootView.findViewById(R.id.on_off_switch);
            onOffSwitch.setChecked(prefs.getBoolean(Channel.ON_OFF_KEY, false));
            onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    updatePreferences();
                    enqueueNotificationTask();
                    //TODO: if the channel is manually disabled, let the user know.
                }
            });

            mFrequencyGroup = rootView.findViewById(R.id.frequency_group);
            mFrequencyGroup.check(prefs.getInt(Channel.Frequency.FREQUENCY_KEY, Channel.Frequency.DEFAULT));
            mFrequencyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    updatePreferences();
                    enqueueNotificationTask();
                }
            });

            updatePreferences();

            rootView.findViewById(R.id.test_notification_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pushExampleNotification();
                }
            });

            rootView.findViewById(R.id.configure_channel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Util.goToNotificationSettings(mChannel.id.toString(), getContext());
                }
            });
        }
        return rootView;
    }

    void updatePreferences() {
        SharedPreferences prefs = getContext().getSharedPreferences(
                Channel.PREFERENCES_KEY + mChannel.id.toString(), Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Channel.ON_OFF_KEY, onOffSwitch.isChecked());
        editor.putInt(Channel.Frequency.FREQUENCY_KEY, mFrequencyGroup.getCheckedRadioButtonId());
        editor.apply();
    }

    void pushExampleNotification() {
        Context context = getContext();
        if (context == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (!manager.areNotificationsEnabled()) {
                //TODO: warn the user that they have manually disabled notifications for the app.
                return;
            }
            NotificationChannel channel = manager.getNotificationChannel(mChannel.id.toString());
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                //TODO: warn the user that they have manually disabled notifications for this channel.
                return;
            }
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        if (!notificationManager.areNotificationsEnabled()) {
            //TODO: popup that prompts a user to go turn on notifications for the app.
            return;
        }
        Notification notification = mChannel.buildNotification(getContext());
        notificationManager.cancel(mChannel.notificationId);
        notificationManager.notify(mChannel.notificationId, notification);
    }

    void enqueueNotificationTask() {
        Context context = getContext();
        if (context == null) return;
        WorkManager workManager = WorkManager.getInstance(context);
        int frequency = mFrequencyGroup.getCheckedRadioButtonId();
        Data data = new Data.Builder()
                .putString(NotificationWorker.CHANNEL_KEY, mChannel.name)
                .putInt(NotificationWorker.FREQUENCY_KEY, frequency)
                .build();
        Channel.Delay delay = new Channel.Delay(frequency);
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(NotificationWorker.class)
                .setInputData(data)
                .setInitialDelay(delay.interval, delay.unit)
                .build();
        workManager.beginUniqueWork(
                mChannel.id.toString(),
                ExistingWorkPolicy.REPLACE,
                request
        ).enqueue();
    }

}
