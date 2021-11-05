package tk.jabtk.attentrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import tk.jabtk.attentrack.admin.AdminLogin;
import tk.jabtk.attentrack.admin.AdminMainActivity;
import tk.jabtk.attentrack.professor.ProfessorLogin;
import tk.jabtk.attentrack.professor.ProfessorMainActivity;
import tk.jabtk.attentrack.student.StudentLogin;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        statusBarColor();
        /// Auto login process if user already created or logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            ///checking userType
            checkUserIsAdmin(mAuth.getCurrentUser().getUid());
            checkUserIsProfessor(mAuth.getCurrentUser().getUid());
            checkUserIsStudent(mAuth.getCurrentUser().getUid());
        }

        ///admin login Activity
        Button adminLogin = findViewById(R.id.AdminLogin);
        adminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminLogin.class));
            }
        });

        /// Professor Login Activity
        Button professorLogin = findViewById(R.id.ProfessorLogin);
        professorLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfessorLogin.class));
            }
        });

        /// Student Login Activity
        Button studentLogin = findViewById(R.id.StudentLogin);
        studentLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StudentLogin.class));
            }
        });
    }

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
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getBoolean("isProfessor") != null && documentSnapshot.getBoolean("isProfessor")) {
                    //user is Admin
                    startActivity(new Intent(getApplicationContext(), ProfessorMainActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void checkUserIsStudent(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Students").document(uid);
        ///Extraction of Data or Flag isAdmin
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("isStudent") != null) {
                    //user is Student


                    // startActivity(new Intent(getApplicationContext(), .class));
                }
            }
        });
    }


    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.rgb(67, 183, 225));
    }
}