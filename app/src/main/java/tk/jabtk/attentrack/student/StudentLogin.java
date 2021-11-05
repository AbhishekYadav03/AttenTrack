package tk.jabtk.attentrack.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import tk.jabtk.attentrack.ForgotPassword;
import tk.jabtk.attentrack.R;

public class StudentLogin extends AppCompatActivity {
    private TextInputLayout StudentLoginEmail, StudentLoginPass;
    private ProgressBar progressBar;

    private FirebaseAuth sAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_login_activity);
        statusBarColor();
        sAuth = FirebaseAuth.getInstance();

        TextView registerTxt = findViewById(R.id.registerTxt);
        registerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterStudent.class));
                finish();
            }
        });

        MaterialButton logInBtn = findViewById(R.id.loginBtn);
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentLogin();

            }
        });
        TextView forgotPasswordTxt = findViewById(R.id.forgot);
        forgotPasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
            }
        });


        StudentLoginEmail = findViewById(R.id.studentLoginEmail);
        StudentLoginPass = findViewById(R.id.studentLoginPass);

        progressBar = findViewById(R.id.loading);


    }


    protected void studentLogin() {
        if (!(!validateEmail() | !validatePassword())) {
            String email = StudentLoginEmail.getEditText().getText().toString().trim();
            String password = StudentLoginPass.getEditText().getText().toString().trim();
            progressBar.setVisibility(View.VISIBLE);
            sAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user.isEmailVerified()) {
                            checkUserAccessLevel(sAuth.getCurrentUser().getUid());
                        } else {
                            user.sendEmailVerification();
                            Toast.makeText(getApplicationContext(), "Check your Email to verify your account! ", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Failed to login! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }


    protected void checkUserAccessLevel(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Students").document(uid);
        ///Extraction of Data or Flag isAdmin

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.getBoolean("isStudent") != null && documentSnapshot.getBoolean("isStudent")) {
                    progressBar.setVisibility(View.INVISIBLE);
                    //user is Admin
                    Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(),  StudentMainActivity.class);
                    startActivity(intent);
                    finishAffinity();    ////clearing all previous activity

                } else {
                    FirebaseAuth.getInstance().signOut();//Logout
                    Toast.makeText(getApplicationContext(), "You are not Authorised!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    public boolean validateEmail() {
        String email = Objects.requireNonNull(StudentLoginEmail.getEditText()).getText().toString().trim();
        if (email.isEmpty()) {
            StudentLoginEmail.setError("Email is required!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            StudentLoginEmail.setError("Enter valid Email!");
            return false;
        } else {
            StudentLoginEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = Objects.requireNonNull(StudentLoginPass.getEditText().getText()).toString().trim();
        if (password.isEmpty()) {
            StudentLoginPass.setError("Password is required!");
            return false;
        } else if (password.length() < 6) {
            StudentLoginPass.setError("Min Password length should be 6 characters!");
            return false;

        } else {
            StudentLoginPass.setError(null);
            return true;
        }
    }

    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.rgb(8, 115, 158));
    }

}