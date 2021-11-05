package tk.jabtk.attentrack.professor;

import android.content.Intent;
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

public class ProfessorLogin extends AppCompatActivity implements View.OnClickListener {
    private TextView registerTxt, forgotPasswordTxt;
    private MaterialButton logInBtn;
    private TextInputLayout ProfessorLoginEmail, ProfessorLoginPass;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_login);

        statusBarColor();
        mAuth = FirebaseAuth.getInstance();

        registerTxt = findViewById(R.id.registerTxt);
        registerTxt.setOnClickListener(this);
        logInBtn = findViewById(R.id.loginBtn);
        logInBtn.setOnClickListener(this);

        ProfessorLoginEmail = findViewById(R.id.professorLoginEmail);
        ProfessorLoginPass = findViewById(R.id.professorLoginPass);

        progressBar = findViewById(R.id.loading);

        forgotPasswordTxt = findViewById(R.id.forgot);
        forgotPasswordTxt.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerTxt:
                startActivity(new Intent(ProfessorLogin.this, RegisterProfessor.class));
                finish();
                break;
            case R.id.loginBtn:
                ProfessorLogin();

                break;
            case R.id.forgot:
                startActivity(new Intent(ProfessorLogin.this, ForgotPassword.class));
                break;
        }
    }


    public boolean validateEmail() {
        String email = Objects.requireNonNull(ProfessorLoginEmail.getEditText()).getText().toString().trim();
        if (email.isEmpty()) {
            ProfessorLoginEmail.setError("Email is required!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ProfessorLoginEmail.setError("Enter valid Email!");
            return false;
        } else {
            ProfessorLoginEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = Objects.requireNonNull(ProfessorLoginPass.getEditText().getText()).toString().trim();
        if (password.isEmpty()) {
            ProfessorLoginPass.setError("Password is required!");
            return false;
        } else if (password.length() < 6) {
            ProfessorLoginPass.setError("Min Password length should be 6 characters!");
            return false;

        } else {
            ProfessorLoginPass.setError(null);
            return true;
        }
    }


    protected void ProfessorLogin() {
        if (!(!validateEmail() | !validatePassword())) {
            String email = ProfessorLoginEmail.getEditText().getText().toString().trim();
            String password = ProfessorLoginPass.getEditText().getText().toString().trim();
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user.isEmailVerified()) {
                            checkUserAccessLevel(mAuth.getCurrentUser().getUid());
                        } else {
                            user.sendEmailVerification();
                            Toast.makeText(ProfessorLogin.this, "Check your Email to verify your account! ", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(ProfessorLogin.this, "Failed to login! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    protected void checkUserAccessLevel(String uid) {
        DocumentReference df = FirebaseFirestore.getInstance().collection("Professors").document(uid);
        ///Extraction of Data or Flag isProfessor

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.getBoolean("isProfessor") != null && documentSnapshot.getBoolean("isProfessor")) {
                    progressBar.setVisibility(View.INVISIBLE);
                    //user is Admin
                    Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(), ProfessorMainActivity.class);
                    startActivity(intent);
                    finishAffinity();   ////clearing all previous activity
                } else {
                    FirebaseAuth.getInstance().signOut();//Logout
                    Toast.makeText(getApplicationContext(), "You are not Authorised", Toast.LENGTH_SHORT).show();
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

    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));
    }
}