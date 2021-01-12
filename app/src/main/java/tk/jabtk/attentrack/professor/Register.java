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
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.student.MainActivity;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;

    private TextView login;
    private EditText editTextName, editTextEmail, editTextPassword;
    private ProgressBar progressBar;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_register);
        mAuth = FirebaseAuth.getInstance();

        login = findViewById(R.id.loginTxt);
        login.setOnClickListener(this);

        register = findViewById(R.id.registerBtn);
        register.setOnClickListener(this);

        editTextName = findViewById(R.id.name);
        editTextName.setOnClickListener(this);

        editTextEmail = findViewById(R.id.email);
        editTextEmail.setOnClickListener(this);

        editTextPassword = findViewById(R.id.password);
        editTextPassword.setOnClickListener(this);

        progressBar = findViewById(R.id.loading);
        progressBar.setOnClickListener(this);

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            Toast.makeText(Register.this, "Login Successfully", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginTxt:
                startActivity(new Intent(this, Login.class));
                break;
            case R.id.registerBtn:
                registerUser();
                break;
        }
    }


    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String userType="Professor";

        if (name.isEmpty()) {
            editTextName.setError("Name is required!");
            editTextName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter valid Email!");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Min Password length should be 6 characters!");
            editTextPassword.requestFocus();
            return;

        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Teacher teacher = new Teacher(name, email,userType);
                            FirebaseDatabase.getInstance().getReference("Teachers")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                    .setValue(teacher).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, " User has been registered successfully! Check Your Email to verify your account! ", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                        //redirect to login activity
                                        startActivity(new Intent(Register.this, Login.class));
                                        mAuth.getCurrentUser().sendEmailVerification();
                                    } else {
                                        Toast.makeText(Register.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(Register.this, "Failed to register!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}