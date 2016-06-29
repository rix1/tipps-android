package no.tipps.tipps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class PaymentActivity extends AppCompatActivity {

    private Button payBtn;
    private final String TAG = PaymentActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Log.d(TAG, "FAB btn clicked!!");
                initiatePayment();
            }
        });
    }

    // DEPRECATED
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "RECEIVED SOMETHING FROM TIPPS??? ON NEW INTENT?");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "RECEIVED SOMETHING FROM VIPPS! requestCode: " + requestCode + " resCode: " + resultCode);
        Log.d(TAG, intent.toString());

        if(resultCode == RESULT_OK) {
            String url = null;
            if (intent != null && intent.getData() != null) {
                try{
                    url = URLDecoder.decode(intent.getData().toString(),"UTF-8");
                    Log.d(TAG, "DATA: " + intent.getData().toString());

                    //TODO Handle result
                }catch(UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void initiatePayment() {
        Context context = this.getApplicationContext();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo("no.dnb.vipps", PackageManager.GET_ACTIVITIES);

            final int REQUEST_CODE = 1; // Used to identify the request.
            Log.d(TAG, info.toString());

            if (versionCompare(info.versionName, "1.4.0") >= 0) {
                String uri = "vipps://?action=inAppPayment&appID=c4c49550-5def-4c5e-972d-e78ecd1fc2c8&orderID=14&amount=100&merchantSerialNumber=24815&fallbackURL=tipps://&message=TestMessage";

//                String uri = "vipps://?action=inAppPayment&appID=c4c49550-5def-4c5e-972d-e78ecd1fc2c8&orderID=14&amount=100&merchantSerialNumber=24815&fallbackURL=INTENT";

//                String encodedUrl = URLEncoder.encode(uri, "UTF-8");

                Intent intent = new Intent(Intent.ACTION_VIEW);

                intent.setData(Uri.parse(uri));

//                intent.setPackage("no.dnb.vipps"); // Avoids application picker

                Log.d(TAG, "STARTING VIPPS WITH DATA: " + intent.getData());
                Log.d(TAG, "URI: " + uri);
                Log.d(TAG, "URI PARSED: " + Uri.parse(uri));
//                Log.d(TAG, "URI Encoded: " + encodedUrl);
//                Log.d(TAG, "ENCODED URI PARSED: " + Uri.parse(encodedUrl));
                Log.d(TAG, "Intent: " + intent.toString());

                startActivity(intent); //Approach 1 explain below OR

//                startActivityForResult(launchIntent, REQUEST_CODE);

            } else {
                // Notify user to download the latest version of Vipps application.
                Log.d(TAG, "Old app found - we need to upgrade");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "FANT IKKE VIPPS");
            // No Vipps app! Open play store page.
            String url = "https://play.google.com/store/apps/details?id=no.dnb.vipps";
            Intent storeIntent = new Intent(Intent.ACTION_VIEW);
            storeIntent.setData(Uri.parse(url));
            startActivity(storeIntent);
        }
    }

    private Integer versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0; // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        } // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }
}

