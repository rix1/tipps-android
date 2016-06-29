package no.tipps.tipps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import java.util.Random;

/**
 * Created by rikardeide on 29/06/16.
 *
 */

public class PaymentHelper {

    private static final String APP_ID = "c4c49550-5def-4c5e-972d-e78ecd1fc2c8";
    private static final int MERCHANT_SERIAL_NUMBER = 24815;


    private static final String TAG = PaymentHelper.class.getSimpleName();
    private static final int RESULT_OK = 1;


    public static Intent initiatePayment(Context context, int price) {
//        Context context = this.getApplicationContext();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo("no.dnb.vipps", PackageManager.GET_ACTIVITIES);

            final int REQUEST_CODE = 1; // Used to identify the request.
            Log.d(TAG, info.toString());

            if (versionCompare(info.versionName, "1.4.0") >= 0) {
                Uri uri = getVippsUri("tipps://&message=TestMessage&data=Hei", price);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);

                return intent;

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
            return storeIntent;
        }
        return null;
    }


    /**
     * Generates and returns a Vipps "deep-link" Uri. Converts the price/amount to "øre".
     * @param fallbackurl
     * @param amount
     * @return
     */
    private static Uri getVippsUri(String fallbackurl, int amount){
        amount *= 100; // Convert from NOK to Øre
        int orderID = new Random().nextInt(1000) + 1;
        String base = "vipps://?action=inAppPayment&appID="+ APP_ID + "&orderID=" + orderID + "&amount="+ amount +"&merchantSerialNumber="+MERCHANT_SERIAL_NUMBER+"&fallbackURL="+fallbackurl;
        return Uri.parse(base);
    }

    private static Integer versionCompare(String str1, String str2) {
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
