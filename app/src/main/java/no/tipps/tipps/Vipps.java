package no.tipps.tipps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
    private String name;
    private TextView nameView;
    //    private NotificationManager notificationManager;
    private int notificationID = 1;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vipps);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this);


        showTipps();

        beaconManager = new BeaconManager(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        beaconManager.setBackgroundScanPeriod(1250, 250);

        name = getIntent().getStringExtra("name");



        nameView = (TextView)findViewById(R.id.name);
        nameView.setText(name);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.d(TAG, "ENTERING REGION");
                showNotification("Beacon nearby!", "Vil du kjøpe kaffe og boller?");
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "EXITING REGION");
                showNotification("Tilbudet er ferdig!", "Nothing in range....");
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
    }
//        // Floating button with email icon
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
//                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
//                startActivity(intent);
//            }
//        });

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


        int price = 1; // price in NOK
        Intent vipps = PaymentHelper.initiatePayment(this, price);
        PendingIntent pendingVipps = PendingIntent.getActivities(this, 0, new Intent[]{vipps}, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(R.drawable.smallnot)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .addAction(android.R.drawable.stat_notify_sync, "Kjøp dette Tippset", pendingVipps);

        mBuilder.setContentIntent(pendingIntent);

//        mBuilder.defaults |= Notification.PRIORITY_HIGH;

        notificationManager.notify(notificationID, mBuilder.build());

    }

    private void showTipps(){

//        notificationManager.notify(notificationID, new );
//        notificationManager.getActiveNotifications()


        ImageButton showTippsButton= (ImageButton) findViewById(R.id.tippsButton);

        showTippsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), TippsActivity.class);
                startActivity(i);

                overridePendingTransition(R.anim.enter, R.anim.exit);

            }
        });
    }
}
