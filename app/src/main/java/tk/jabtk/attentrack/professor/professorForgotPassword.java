package tk.jabtk.attentrack.professor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import tk.jabtk.attentrack.R;

public class professorForgotPassword extends AppCompatActivity {
    private EditText editTextEmailForgot;
    private ImageButton backBtn;
    private Button forgotBtn;
    private ProgressBar progressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_forgot_password);
        editTextEmailForgot = findViewById(R.id.emailForgot);
        progressBar = findViewById(R.id.progressBar);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        forgotBtn = findViewById(R.id.resetBtn);

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });


    }

    private void resetPassword() {
        String email = editTextEmailForgot.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmailForgot.setError("Email is required!");
            editTextEmailForgot.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailForgot.setError("Enter valid Email!");
            editTextEmailForgot.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    Toast.makeText(professorForgotPassword.this, "Check Your Email to Reset Your Password! ", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(professorForgotPassword.this, professorLogin.class));
                } else {
                    Toast.makeText(professorForgotPassword.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}