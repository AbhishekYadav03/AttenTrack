package tk.jabtk.attentrack;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class ConnectionDetector {

    //Checking Internet Connection
    public static String isConnected(Context context) {
        String status = null;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

/*
        //for Checking Network Type
        NetworkInfo WiFiConnection = connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo MobileConnection = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);
*/

        if (networkInfo == null) {
            status ="Disconnected";

        } else {
            status ="Connected";
        }
        return status;
    }


}
