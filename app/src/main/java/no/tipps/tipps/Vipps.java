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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;
import im.delight.android.ddp.db.Document;
import im.delight.android.ddp.db.Query;
import im.delight.android.ddp.db.memory.InMemoryDatabase;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Vipps extends AppCompatActivity implements MeteorCallback {

    private static final int ENDPOINT_PORT = 3000;
    private static final String ENDPOINT_IP = "129.241.221.171";
    private final String TAG = Vipps.class.getSimpleName();

    private BeaconManager beaconManager;
    private TextView nameView;
    private int notificationID = 1;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private Meteor mMeteor;
    private User currentUser;
    private ResultListener resultListener;
    private Region region;

    private Beacon[] beacons;

    private Database database;

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vipps);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.background);

        showTipps();
        setUpMeteor();
        setupBeaconRanger();
//        setupBeaconScanner();

        currentUser = new User(getIntent().getStringExtra("name"), getIntent().getIntExtra("profile", User.USER_PROFILE_NEUTRAL));
        nameView = (TextView)findViewById(R.id.name);
        nameView.setText(currentUser.getName());
    }

    private void setUpMeteor(){
        mMeteor = new Meteor(this, getEndpointURL(ENDPOINT_IP, ENDPOINT_PORT), new InMemoryDatabase());

        database = mMeteor.getDatabase();

        // register the callback that will handle events and receive messages
        mMeteor.addCallback(this);

        Log.d("METEOR", "INITIATING Meteor connection");
        // establish the connection
        mMeteor.connect();

        resultListener = new ResultListener() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "DDP: msg received, res: " + s);

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Tipps.no says: " + s, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }

            @Override
            public void onError(String s, String s1, String s2) {
                Log.d(TAG, "DDP error " + s);
//                mMeteor.reconnect();
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Meteor error..", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        };

    }

    private void setupBeaconRanger(){
        beaconManager = new BeaconManager(this);
        beaconManager.setBackgroundScanPeriod(1550, 2550);

        region = new Region("elevator",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57fe6d"), null, null); // major: 58865 minor: 59405

        final Collection[] beacons = {database.getCollection("Beacon")};

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
//                Log.d("BEACON", "BEACONS Discovered! #" + list.size());
//                Log.d("BEACON", list.toString());

//                beacons[0] = database.getCollection("Beacon");
//                String[] ids = beacons[0].getDocumentIds();
//                Document doc = beacons[0].getDocument(ids[0]);

//                Log.d("GUNNAR", doc.getField("macAddress").toString());
//                doc.
//                Document doc = database.getCollection("Beacon").
                Document doc =  mMeteor.getDatabase().getCollection("Beacons").findOne();
//                query.toString();



                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    if(!doc.equals(null)) {
                        showNotification(doc.getField("title").toString(), doc.getField("message").toString(), Integer.parseInt(doc.getField("price").toString()));
                    }

//                    database.getCollection();
//                    List<String> places = placesNearBeacon(nearestBeacon);

//                        say(list.get(0).getMacAddress() + " near!", Snackbar.LENGTH_SHORT);

                    // TODO: update the UI here
//                    Log.d("Airport", "Nearest places: " + places);
                }
                else{
                    notificationManager.cancel(notificationID);
                }
            }
        });
    }

    private Beacon getBeaconByMAC(String macAddress){



        return null;
    }

    @Deprecated
    private void setupBeaconScanner(){
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(1550, 2550);
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.d(TAG, "ENTERING REGION");
                Log.d("BEACON", region.toString());
                showNotification("Beacon nearby!", "Vil du kjøpe kaffe og boller?", 1);
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "EXITING REGION");
                Log.d("BEACON", region.toString());
                showNotification("Tilbudet er ferdig!", "Nothing in range....", 0);
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override

            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "backOffice",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        53168,  // Major
                        9502    // Minor
                ));
            }
        });
    }

    private String getEndpointURL(String ip, int port){
        return "ws://" + ip + ":" + port + "/websocket";
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

    @Override
    protected void onStop(){
        super.onStop();
//        mMeteor.disconnect();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
//        mMeteor.connect();
    }

    @Override
    protected void onResume(){
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    public void stopNotification(){
        mBuilder.setOngoing(false);

        notificationManager.cancel(notificationID);
    }

    private void say(String s, int length){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, s, length);
        snackbar.show();
    }

    public void showNotification(String title, String message, int price) {
        Intent notifyIntent = new Intent(this, LoginActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mBuilder = null;
        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this);

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent vipps = PaymentHelper.initiatePayment(this, price);
        PendingIntent pendingVipps = PendingIntent.getActivities(this, 0, new Intent[]{vipps}, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        if(price > 0){
            mBuilder.addAction(android.R.drawable.stat_notify_sync, "Kjøp dette Tippset", pendingVipps);
        }

        mBuilder.setContentIntent(pendingIntent);

//        mBuilder.defaults |= Notification.PRIORITY_HIGH;

        notificationManager.notify(notificationID, mBuilder.build());

    }

    private boolean showTipps = false;

    private void showTipps(){

//        notificationManager.notify(notificationID, new );
//        notificationManager.getActiveNotifications()


        final CoordinatorLayout background = (CoordinatorLayout) findViewById(R.id.background);
        ImageButton showTippsButton= (ImageButton) findViewById(R.id.tippsButton);

        showTippsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNotification();
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

    @Override
    public void onConnect(boolean signedInAutomatically) {

        Log.d(TAG, "Connected to tipps.no");

        String subscriptionId = mMeteor.subscribe("beacons");

        Snackbar
                .make(coordinatorLayout, "Connected to Tipps.no", Snackbar.LENGTH_SHORT)
                .setAction("Coolio", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        helloMeteor();
                    }
                })
                .show();

//        mMeteor.call("addData", new Object[]{stringExtra, date.getTime()}, resultListener);
        // Toast.makeText(getBaseContext(), "Sent: " + stringExtra, Toast.LENGTH_SHORT).show();

    }

    public void helloMeteor(){
        mMeteor.call("helloMeteor", new Object[]{"Hei Meteor!", new Date().getTime()}, resultListener);
        Log.d("METEOR", mMeteor.getDatabase().getCollectionNames().toString());
    }

    @Override
    public void onDisconnect() {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Meteor disconnected", Snackbar.LENGTH_LONG)
                .setAction("Reconnect", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMeteor.reconnect();
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Will try to reconnect!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

        snackbar.show();

    }

    @Override
    public void onException(Exception e) {

    }

    /**
     * Callback that is executed whenever a new document is added to a collection
     *
     * @param collectionName the name of the collection that the document is added to
     * @param documentID     the ID of the document that is being added
     * @param newValuesJson  the new fields of the document as a JSON string
     */
    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {
        say("Data data added", Snackbar.LENGTH_SHORT);
//        Log.d("METEOR", Arrays.toString(mMeteor.getDatabase().getCollectionNames()));
        Log.d("METEOR", String.valueOf(mMeteor.getDatabase().getCollection("Beacons")));
    }

    /**
     * Callback that is executed whenever an existing document is changed in a collection
     *
     * @param collectionName    the name of the collection that the document is changed in
     * @param documentID        the ID of the document that is being changed
     * @param updatedValuesJson the modified fields of the document as a JSON string
     * @param removedValuesJson the deleted fields of the document as a JSON string
     */
    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        say("Data changed", Snackbar.LENGTH_SHORT);

    }

    /**
     * Callback that is executed whenever an existing document is removed from a collection
     *
     * @param collectionName the name of the collection that the document is removed from
     * @param documentID     the ID of the document that is being removed
     */
    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        say("Data removed", Snackbar.LENGTH_SHORT);
    }
}
