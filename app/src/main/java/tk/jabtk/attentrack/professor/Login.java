package tk.jabtk.attentrack.professor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import tk.jabtk.attentrack.student.MainActivity;
import tk.jabtk.attentrack.R;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private TextView registerTxt, forgotPasswordTxt;
    private Button logInBtn;
    private EditText uEmail, uPassword;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_login);

        registerTxt = findViewById(R.id.registerTxt);
        registerTxt.setOnClickListener(this);
        logInBtn = findViewById(R.id.loginBtn);
        logInBtn.setOnClickListener(this);

        uEmail = findViewById(R.id.username);
        uPassword = findViewById(R.id.pass);

        progressBar = findViewById(R.id.loading);


        mAuth = FirebaseAuth.getInstance();
        forgotPasswordTxt = findViewById(R.id.forgot);
        forgotPasswordTxt.setOnClickListener(this);
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerTxt:
                startActivity(new Intent(Login.this, Register.class));
                break;
            case R.id.loginBtn:
                userLogin();

                break;
            case R.id.forgot:
                startActivity(new Intent(Login.this, ForgotPassword.class));
                break;
        }
    }

    protected void userLogin() {
        String email = uEmail.getText().toString().trim();
        String password = uPassword.getText().toString().trim();
        if (email.isEmpty()) {
            uEmail.setError("Email is required!");
            uEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uEmail.setError("Enter valid Email!");
            uEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            uPassword.setError("Password is required!");
            uPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            uPassword.setError("Min Password length should be 6 characters!");
            uPassword.requestFocus();
            return;

        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()) {
                        Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(Login.this, "Check your Email to verify your account! ", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Failed to login! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}