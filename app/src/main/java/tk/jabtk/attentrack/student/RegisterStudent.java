package tk.jabtk.attentrack.student;

import android.annotation.SuppressLint;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tk.jabtk.attentrack.R;

import static tk.jabtk.attentrack.R.id.registerBtn;

public class RegisterStudent extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;

    private TextInputLayout editTextName, editTextEmail, editTextPassword, editTextCollegeID, editTextRollNo;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_register_activity);

        statusBarColor();
        mAuth = FirebaseAuth.getInstance();

        TextView login = findViewById(R.id.loginTxt);
        login.setOnClickListener(this);

        MaterialButton register = findViewById(registerBtn);
        register.setOnClickListener(this);

        editTextName = findViewById(R.id.StudentName);
        editTextName.setOnClickListener(this);

        editTextEmail = findViewById(R.id.StudentEmail);
        editTextEmail.setOnClickListener(this);

        editTextCollegeID = findViewById(R.id.CollegeID);
        editTextCollegeID.setOnClickListener(this);

        editTextRollNo = findViewById(R.id.RollNo);
        editTextRollNo.setOnClickListener(this);

        editTextPassword = findViewById(R.id.StudentPass);
        editTextPassword.setOnClickListener(this);

        progressBar = findViewById(R.id.loading);
        progressBar.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginTxt:
                startActivity(new Intent(getApplicationContext(), StudentLogin.class));
                finish();
                break;
            case R.id.registerBtn:
                registerUser();
                break;
        }
    }


    private void registerUser() {
        if (!(!validateName() | !validateEmail() | !validatePassword() | !validateRollNo() | !validateCollegeId())) {
            String name = Objects.requireNonNull(editTextName.getEditText()).getText().toString().trim();
            String email = Objects.requireNonNull(editTextEmail.getEditText()).getText().toString().trim();
            String roll = Objects.requireNonNull(editTextRollNo.getEditText()).getText().toString().trim();
            String id = Objects.requireNonNull(editTextCollegeID.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(editTextPassword.getEditText().getText()).toString().trim();

            progressBar.setVisibility(View.VISIBLE);


            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                    String RegistrationDate = simpleDateFormat.format(new Date());

                    if (task.isSuccessful()) {
                        FirebaseUser student = mAuth.getCurrentUser();
                        assert student != null;
                        Map<String, Object> StudentInfo = new HashMap<>();

                        StudentInfo.put("RegisteredOn", RegistrationDate);
                        StudentInfo.put("StudentName", name);
                        StudentInfo.put("StudentEmail", email);
                        StudentInfo.put("CollegeID", Integer.parseInt(id));
                        StudentInfo.put("StudentRollNo", Integer.parseInt(roll));
                        StudentInfo.put("StudentID", student.getUid());
                        ///access Level or Role
                        StudentInfo.put("isStudent", true);

                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Students").
                                document(student.getUid());
                        documentReference.set(StudentInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                student.updateProfile(userProfileChangeRequest);

                                FirebaseDatabase.getInstance().getReference("AllStudents")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(StudentInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            student.sendEmailVerification();

                                            Toast.makeText(getApplicationContext(), " Registered successfully! Check Your Email to verify your account! ", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                            //redirect to login activity

                                            startActivity(new Intent(getApplicationContext(), StudentLogin.class));
                                            finish();

                                        } else {
                                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });


                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            editTextEmail.setError("Email Address Already Registered!");
                            editTextEmail.requestFocus();
                            Toast.makeText(getApplicationContext(), "Email Address Already Registered!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Contact Administrator for registration!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }

    private boolean validateName() {
        String name = Objects.requireNonNull(editTextName.getEditText()).getText().toString().trim();
        if (name.isEmpty()) {
            editTextName.setError("Name is required!");
            return false;
        } else {
            editTextName.setError(null);
            return true;
        }
    }

    private boolean validateRollNo() {
        String rollNO = Objects.requireNonNull(editTextRollNo.getEditText()).getText().toString().trim();
        if (rollNO.isEmpty()) {
            editTextRollNo.setError("Roll Number is required!");
            return false;
        } else if (rollNO.length() > 2) {
            editTextRollNo.setError("Invalid Roll Number!");
            return false;
        } else {
            editTextRollNo.setError(null);
            return true;
        }
    }

    private boolean validateCollegeId() {
        String ID = Objects.requireNonNull(editTextCollegeID.getEditText()).getText().toString().trim();
        if (ID.isEmpty()) {
            editTextCollegeID.setError("CollegeId Number is required!");
            return false;
        } else if (ID.length() > 6) {
            editTextCollegeID.setError("Invalid CollegeId Number!");
            return false;
        } else if (ID.length() < 6) {
            editTextCollegeID.setError("Invalid CollegeId Number!");
            return false;
        } else {
            editTextCollegeID.setError(null);
            return true;
        }
    }

    public boolean validateEmail() {
        String email = Objects.requireNonNull(editTextEmail.getEditText()).getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter valid Email!");
            return false;
        } else {
            editTextEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = Objects.requireNonNull(editTextPassword.getEditText().getText()).toString().trim();
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            return false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Min Password length should be 6 characters!");
            return false;
        } else {
            editTextPassword.setError(null);
            return true;
        }
    }

    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.rgb(8, 115, 158));
    }
}