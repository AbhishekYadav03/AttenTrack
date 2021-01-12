package tk.jabtk.attentrack;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tk.jabtk.attentrack.professor.Login;

public class SplashScreen extends AppCompatActivity {
    private BroadcastReceiver ConnectionReceiver;
    private static int splashTimer = 1000;
    private static int appCloseTimer=3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ConnectionReceiver = new ConnectionReceiver();
        ///
        broadCastRegister();

        String status= ConnectionDetector.isConnected(this);

       if(status.length()==9) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, Login.class));
                    finish();
                }
            }, splashTimer);
        }else {

           Toast.makeText(this, "Connection not available. Kindly check your connectivity", Toast.LENGTH_LONG).show();
           new Handler().postDelayed(new Runnable() {
               @Override
               public void run() {
                   onBackPressed();
               }
           }, appCloseTimer);
       }


    }

    /// Registering BroadCast
    private void broadCastRegister() {
        registerReceiver(ConnectionReceiver,new  IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(ConnectionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadCastRegister();
    }
}