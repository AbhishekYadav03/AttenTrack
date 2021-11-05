package tk.jabtk.attentrack.professor;

import android.annotation.SuppressLint;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageProfessors.NewProfessorModel;

import static tk.jabtk.attentrack.R.id.registerBtn;

public class RegisterProfessor extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;

    private TextInputLayout editTextName, editTextEmail, editTextPassword, editTextJoiningCode;
    private ProgressBar progressBar;
    private String AddedBy, AddedOn, ModifiedBy, ModifiedOn, ProfessorName, ProfessorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_register_activity);
        mAuth = FirebaseAuth.getInstance();

        statusBarColor();
        TextView login = findViewById(R.id.loginTxt);
        login.setOnClickListener(this);

        MaterialButton register = findViewById(registerBtn);
        register.setOnClickListener(this);

        editTextName = findViewById(R.id.ProfessorName);
        editTextName.setOnClickListener(this);

        editTextEmail = findViewById(R.id.ProfessorEmail);
        editTextEmail.setOnClickListener(this);

        editTextPassword = findViewById(R.id.ProfessorPass);
        editTextPassword.setOnClickListener(this);

        editTextJoiningCode = findViewById(R.id.JoiningCode);
        editTextJoiningCode.setOnClickListener(this);

        progressBar = findViewById(R.id.loading);
        progressBar.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginTxt:
                startActivity(new Intent(this, ProfessorLogin.class));
                finish();
                break;
            case R.id.registerBtn:
                registerUser();
                break;
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


    private boolean validateJoiningCode() {
        String joiningCode = Objects.requireNonNull(editTextJoiningCode.getEditText().getText()).toString().trim();
        if (joiningCode.isEmpty()) {
            editTextJoiningCode.setError("Joining Code is required!");
            return false;
        } else if (joiningCode.length() > 8) {
            editTextJoiningCode.setError("Enter Valid Joining Code!");
            return false;
        } else if (joiningCode.length() < 8) {
            editTextJoiningCode.setError("Enter Valid Joining Code!");
            return false;
        } else {
            editTextJoiningCode.setError(null);
            return true;
        }
    }

    private void registerUser() {
        if (!(!validateName() | !validateEmail() | !validatePassword() | !validateJoiningCode())) {
            String name = Objects.requireNonNull(editTextName.getEditText()).getText().toString().trim();
            String email = Objects.requireNonNull(editTextEmail.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(editTextPassword.getEditText().getText()).toString().trim();
            String joiningCode = Objects.requireNonNull(editTextJoiningCode.getEditText().getText()).toString().trim();
            progressBar.setVisibility(View.VISIBLE);

            Query query = FirebaseDatabase.getInstance().getReference().child("NewProfessors").orderByChild("ProfessorEmail").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        NewProfessorModel newProfessorModel = postSnapshot.getValue(NewProfessorModel.class);
                        if (newProfessorModel.getProfessorEmail().equals(email)) {
                            Toast.makeText(getApplicationContext(), "Matching Email", Toast.LENGTH_SHORT).show();
                            if (newProfessorModel.getRegCode().equals(joiningCode)) {
                                Toast.makeText(getApplicationContext(), "Matching Joining Code", Toast.LENGTH_SHORT).show();
                                if (newProfessorModel.getProfessorName().equals(name)) {
                                    Toast.makeText(getApplicationContext(), "Matching Name", Toast.LENGTH_SHORT).show();
                                    count++;
                                    AddedBy = newProfessorModel.getAddedBy();
                                    AddedOn = newProfessorModel.getAddedOn();
                                    ProfessorName = newProfessorModel.getProfessorName();
                                    ProfessorEmail = newProfessorModel.getProfessorEmail();
                                    if (ModifiedBy != null) {
                                        ModifiedBy = newProfessorModel.getModifiedBy();
                                        ModifiedOn = newProfessorModel.getModifiedOn();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Name not matching", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    return;
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Joining Code Error!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                return;
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Email Not Found", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            return;
                        }

                    }
                    if (count == 1) {

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                                String RegistrationDate = simpleDateFormat.format(new Date());
                                if (task.isSuccessful()) {
                                    FirebaseUser professor = mAuth.getCurrentUser();
                                    assert professor != null;
                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                                    professor.updateProfile(userProfileChangeRequest);


                                    Map<String, Object> ProfessorInfo = new HashMap<>();

                                    ProfessorInfo.put("RegisteredOn", RegistrationDate);
                                    ProfessorInfo.put("AddedBy", AddedBy);
                                    ProfessorInfo.put("AddedOn", AddedOn);
                                    ProfessorInfo.put("ModifiedBy", ModifiedBy);
                                    ProfessorInfo.put("ModifiedOn", ModifiedOn);
                                    ProfessorInfo.put("ProfessorName", ProfessorName);
                                    ProfessorInfo.put("ProfessorEmail", ProfessorEmail);
                                    ProfessorInfo.put("ProID", professor.getUid());
                                    ///access Level or Role
                                    ProfessorInfo.put("isProfessor", true);

                                    DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Professors").
                                            document(professor.getUid());
                                    documentReference.set(ProfessorInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            FirebaseDatabase.getInstance().getReference("AllProfessors")
                                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                    .setValue(ProfessorInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        professor.sendEmailVerification();

                                                        ////deleted data from new Professor
                                                        deleteFromNewProfessor(joiningCode);

                                                        Toast.makeText(RegisterProfessor.this, " User has been registered successfully! Check Your Email to verify your account! ", Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(View.GONE);
                                                        //redirect to login activity
                                                        startActivity(new Intent(RegisterProfessor.this, ProfessorLogin.class));
                                                        finish();

                                                    } else {

                                                        Toast.makeText(RegisterProfessor.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getApplicationContext(), "Contact Administrator for registration!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Email Not valid!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    ////deleting data from New Professor
    public void deleteFromNewProfessor(String joiningCode) {
        DatabaseReference drNewProfessor = FirebaseDatabase.getInstance().getReference("NewProfessors").child(joiningCode);
        drNewProfessor.removeValue();
        Toast.makeText(this, "Deleted From NewProfessors", Toast.LENGTH_SHORT).show();
    }


    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));
    }
}