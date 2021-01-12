package tk.jabtk.attentrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class ConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = ConnectionDetector.isConnected(context);


        if (status.length() == 12) {
            //showDialog(context);
            Toast.makeText(context, "Connection not available. Kindly check your connectivity", Toast.LENGTH_SHORT).show();
        } else if (status.length() == 9) {
            // nothing to display
        }

    }



    //// Method for Alert show Dialog
    public void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Connection not available. Kindly check your connectivity")
                .setCancelable(false)
                .setNegativeButton("Restart App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
