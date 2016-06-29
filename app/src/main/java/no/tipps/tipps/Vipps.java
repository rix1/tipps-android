package no.tipps.tipps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

public class Vipps extends AppCompatActivity {

    private BeaconManager beaconManager;
    private final String TAG = Vipps.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vipps);
        showTipps();

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.d(TAG, "ENTERING REGION");
                showNotification(
                        "Du er et kontoret",
                        "Kontoret er et lære");
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "EXITING REGION");

            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "office region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        58865,
                        34307
                ));
            }
        });

        // Floating button with email icon
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "RECEIVED SOMETHING FROM TIPPS?");
        String url = null;
        if (intent != null && intent.getData() != null) {
            try{
                url = URLDecoder.decode(intent.getData().toString(),"UTF-8");
                Log.d(TAG, "Data: " + url);
                //TODO Handle result
            }catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }


    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, LoginActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private boolean showTipps = false;

    private void showTipps(){
        final CoordinatorLayout background = (CoordinatorLayout) findViewById(R.id.background);
        ImageButton showTippsButton= (ImageButton) findViewById(R.id.tippsButton);

        showTippsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!showTipps) {
                    background.setBackgroundResource(R.drawable.borsbarentippser1);
                    showTipps = true;
                }else{
                    background.setBackgroundResource(R.drawable.vippsbackground);
                    showTipps = false;
                }
            }
        });
    }
}
