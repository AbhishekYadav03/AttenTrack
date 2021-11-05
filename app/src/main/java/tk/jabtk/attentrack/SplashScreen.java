package tk.jabtk.attentrack;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import tk.jabtk.attentrack.admin.AdminMainActivity;
import tk.jabtk.attentrack.professor.ProfessorMainActivity;
import tk.jabtk.attentrack.student.StudentMainActivity;

public class SplashScreen extends AppCompatActivity {
    private BroadcastReceiver ConnectionReceiver;
    private static final int splashTimer = 1000;
    private static final int appCloseTimer = 3000;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        setContentView(R.layout.activity_splash_screen);
        ConnectionReceiver = new ConnectionReceiver();
        broadCastRegister();
        String status = ConnectionDetector.isConnected(this);
        mAuth = FirebaseAuth.getInstance();
        if (status.length() == 9) {
            //checking already Login or reg;
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                checkUserIsAdmin(mAuth.getCurrentUser().getUid());
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreen.this, StartActivity.class));
                        finish();
                    }
                }, splashTimer);
            }
        } else {
            Toast.makeText(this, "Connection not available. Kindly check your connectivity", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, appCloseTimer);
        }
    }


    /*checking user auto login level */
    protected void checkUserIsAdmin(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Administrators").document(uid);
        ///Extraction of Data or Flag isAdmin
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getBoolean("isAdmin") != null && documentSnapshot.getBoolean("isAdmin")) {
                    //user is Admin
                    startActivity(new Intent(getApplicationContext(), AdminMainActivity.class));
                    finish();
                } else {
                    checkUserIsProfessor(mAuth.getCurrentUser().getUid());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void checkUserIsProfessor(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Professors").document(uid);
        ///Extraction of Data or Flag isAdmin
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getBoolean("isProfessor") != null && documentSnapshot.getBoolean("isProfessor")) {
                    //user is Professor
                    startActivity(new Intent(getApplicationContext(), ProfessorMainActivity.class));
                    finish();
                } else {
                    checkUserIsStudent(mAuth.getCurrentUser().getUid());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error while auto Login! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void checkUserIsStudent(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Students").document(uid);
        ///Extraction of Data or Flag isAdmin
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getBoolean("isStudent") != null && documentSnapshot.getBoolean("isStudent")) {
                    //user is Student
                    startActivity(new Intent(getApplicationContext(), StudentMainActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                }
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /// Registering BroadCast
    private void broadCastRegister() {
        registerReceiver(ConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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