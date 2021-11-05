package tk.jabtk.attentrack.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import tk.jabtk.attentrack.ForgotPassword;
import tk.jabtk.attentrack.R;

public class AdminLogin extends AppCompatActivity {

    private TextInputLayout adminLoginEmail, adminLoginPass;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login_activity);
        mAuth = FirebaseAuth.getInstance();
        statusBarColor();
        TextView addAdmin = findViewById(R.id.registerTxt);
        addAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Contact Developer of application!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), RegisterAdmin.class));
                finish();
            }
        });

        TextView forgotPassword = findViewById(R.id.forgot);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
            }
        });

        adminLoginEmail = findViewById(R.id.adminLoginEmail);
        adminLoginPass = findViewById(R.id.adminLoginPass);
        progressBar = findViewById(R.id.progressbar);

        Button adminLoginBtn = findViewById(R.id.loginBtn);
        adminLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminLogin();
            }
        });


    }

    public void adminLogin() {
        String adminEmail = adminLoginEmail.getEditText().getText().toString().trim();
        String adminPass = adminLoginPass.getEditText().getText().toString().trim();

        if (adminEmail.isEmpty()) {
            adminLoginEmail.setError("Enter Email Address!");
            adminLoginEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(adminEmail).matches()) {
            adminLoginEmail.setError("Please Enter Valid Email!");
            adminLoginEmail.requestFocus();
            return;
        }
        if (adminPass.isEmpty()) {
            adminLoginPass.setError("Enter Password!");
            adminLoginPass.requestFocus();
            return;
        }
        if (adminPass.length() < 6) {
            adminLoginPass.setError("Min password length should be 6 characters!");
            adminLoginPass.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(adminEmail, adminPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser Admin = FirebaseAuth.getInstance().getCurrentUser();
                    if (Admin.isEmailVerified()) {
                        checkUserAccessLevel(mAuth.getCurrentUser().getUid());
                    } else {
                        Admin.sendEmailVerification();
                        Toast.makeText(getApplicationContext(), "Check your Email to verify your account! ", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(AdminLogin.this, "Failed to Login!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    protected void checkUserAccessLevel(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Administrators").document(uid);
        ///Extraction of Data or Flag isAdmin

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.getBoolean("isAdmin") != null && documentSnapshot.getBoolean("isAdmin")) {
                    progressBar.setVisibility(View.GONE);
                    //user is Admin
                    Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(), AdminMainActivity.class);
                    startActivity(intent);
                    finishAffinity();   ////clearing all previous activity

                } else {
                    FirebaseAuth.getInstance().signOut();//Logout
                    Toast.makeText(getApplicationContext(), "You are not Authorised", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdminLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.darkRed));
    }
}