package tk.jabtk.attentrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    private TextInputLayout editTextEmailForgot;

    private Button forgotBtn;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        statusBarColor();
        editTextEmailForgot = findViewById(R.id.emailAddress);
        progressBar = findViewById(R.id.progress_bar);


        mAuth = FirebaseAuth.getInstance();
        forgotBtn = findViewById(R.id.resetPassBtn);

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });


    }

    private void resetPassword() {
        String email = editTextEmailForgot.getEditText().getText().toString().trim();

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

                    Toast.makeText(ForgotPassword.this, "Check Your Email to Reset Your Password! ", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPassword.this, StartActivity.class));
                } else {
                    Toast.makeText(ForgotPassword.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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