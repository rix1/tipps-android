package no.tipps.tipps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.JsonArray;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.JsonElement;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.JsonObject;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.JsonParser;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.annotations.JsonAdapter;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;
import im.delight.android.ddp.db.Document;
import im.delight.android.ddp.db.Query;
import im.delight.android.ddp.db.memory.InMemoryDatabase;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class Vipps extends AppCompatActivity implements MeteorCallback {

    private static final int ENDPOINT_PORT = 3000;
    private static final String ENDPOINT_IP = "129.241.221.119";
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


    private boolean first;

    private SingleTipps currentTipps;
    private Beacon currentBeacon;


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

        first = true;


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
                Log.d("METEOR", "DDP: msg received, res: " + s);

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Tipps.no says: " + s, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }

            @Override
            public void onError(String s, String s1, String s2) {
                Log.d("METEOR", "DDP error " + s);
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
        currentTipps = new SingleTipps();

        region = new Region("elevator",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57fe6d"), null, null); // major: 58865 minor: 59405

        final Collection[] beacons = {database.getCollection("beacons")};

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {

                Query query;
                Boolean beaconChange = false;


                if(!list.isEmpty()){

                    if (mMeteor.isConnected() && !list.isEmpty()) { // Server is connected and beacons are nearby
                        Beacon newBeacon = list.get(0);

//                        if(first){
//                            currentBeacon = newBeacon;
//                            first = false;
//                        }

                        if (newBeacon.getMacAddress() != currentBeacon.getMacAddress()){
                            beaconChange = true;
                            Log.d("SIRI_UPDATE", "Beacon has been updated");
                        }

                        query = mMeteor.getDatabase().getCollection("beacons").whereEqual("macAddress", currentBeacon.getMacAddress().toStandardString());

                        if(query.toString().length() <= 2){   // There are no beacons matching the ones from the server
                            Log.d("QUERY", "OBJECT IS NULL");
                            notificationManager.cancel(notificationID);
                        }else{                                // Beacons from server are recognized
                            Log.d("SIRI", query.toString());
                            Document doc = query.findOne();

                            // Get the Tipps accosiated with this beacon.
                            if(currentTipps.isSet() && beaconChange){
                                currentTipps = new SingleTipps(doc.getField("title").toString(), doc.getField("message").toString(), doc.getField("price").toString());
                            }else if(!currentTipps.isSet()){
                                currentTipps = new SingleTipps(doc.getField("title").toString(), doc.getField("message").toString(), doc.getField("price").toString());
                            }
//                            showNotification(doc.getField("title").toString(), doc.getField("message").toString(), Integer.parseInt(doc.getField("price").toString()));
                        }
                    }
                    else{
                        Log.d("METEOR", "Meteor conection: "+  mMeteor.isConnected());
                        notificationManager.cancel(notificationID);
                    }
                    if(list.isEmpty()){
                        notificationManager.cancel(notificationID);
                        currentTipps = new SingleTipps("Tomt Tipps", "Her har det skjedd noe feil", "0"); // There are no beacons, hence no tipps.
                    }else{
                        if(currentTipps.isSet()){
                            showNotification(currentTipps.getTitle(), currentTipps.getMessage(), currentTipps.getPrice());
                        }
                    }
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
        String url = "ws://" + ip + ":" + port + "/websocket";
        Log.d("METEOR", "Endpoint URL: "+ url);
        return url;
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
//        beaconManager.stopRanging(region);

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
        Intent notifyIntent = new Intent(this, TippsActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mBuilder = null;
        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this);

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent vipps = PaymentHelper.initiatePayment(this, price);
        PendingIntent pendingVipps = PendingIntent.getActivities(this, 0, new Intent[]{vipps}, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(R.drawable.smallnot)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        if(price > 0){
            mBuilder.addAction(android.R.drawable.stat_notify_sync, "Kjøp dette Tippset", pendingVipps);
        }

        mBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, mBuilder.build());

    }

    private void showTipps(){

//        notificationManager.notify(notificationID, new );
//        notificationManager.getActiveNotifications()


        final CoordinatorLayout background = (CoordinatorLayout) findViewById(R.id.background);
        ImageButton showTippsButton= (ImageButton) findViewById(R.id.tippsButton);

        showTippsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNotification();
                Intent i = new Intent(getApplicationContext(), TippsActivity.class);
                startActivity(i);

                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {

        Log.d("METEOR", "Connected to tipps.no");

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
        Log.d("METEOR", "Meteor disconnected...");
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Meteor disconnected", Snackbar.LENGTH_INDEFINITE)
                .setAction("Reconnect", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMeteor.reconnect();
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Will try to reconnect!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });


        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });

        snackbar.show();

    }

    @Override
    public void onException(Exception e) {

        Log.d("METEOR", "onException: " + e.toString());
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
        Log.d("METEOR", Arrays.toString(mMeteor.getDatabase().getCollectionNames()));
        Log.d("METEOR", String.valueOf(mMeteor.getDatabase().getCollection("beacons")));
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

        Document doc = database.getCollection("beacon").getDocument(documentID);
        currentTipps = new SingleTipps(doc.getField("title").toString(), doc.getField("message").toString(), doc.getField("price").toString());
        Log.d("SIRI", "Data changed!");
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
