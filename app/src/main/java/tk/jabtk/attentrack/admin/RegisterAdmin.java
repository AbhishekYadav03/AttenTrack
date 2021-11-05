package tk.jabtk.attentrack.admin;

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
import tk.jabtk.attentrack.admin.ManageAdmins.AdminModel;

public class RegisterAdmin extends AppCompatActivity {
    private TextInputLayout AdminName, AdminEmail, AdminPass, JoinCode;
    private String AddedOn, AddedBy, ModifiedBy = null,
            ModifiedOn, adminNameStr, adminEmailStr, adminRegCode;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_register_activity);
        statusBarColor();
        mAuth = FirebaseAuth.getInstance();
        //view binding
        TextView loginTextView = findViewById(R.id.loginTxt);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),
                        AdminLogin.class));
                finish();
            }
        });

        //view binding
        AdminName = findViewById(R.id.AdminName);
        AdminEmail = findViewById(R.id.AdminEmail);
        AdminPass = findViewById(R.id.AdminPassword);
        JoinCode = findViewById(R.id.JoiningCode);
        progressBar = findViewById(R.id.progressbar);

        MaterialButton registerAdmin = findViewById(R.id.registerBtn);
        registerAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerAdmin();
            }
        });
    }

    public void registerAdmin() {
        String adminName = AdminName.getEditText().getText().toString().trim();
        String adminEmail = AdminEmail.getEditText().getText().toString().trim();
        String adminPass = AdminPass.getEditText().getText().toString().trim();
        String joinCode = JoinCode.getEditText().getText().toString().trim();
        if (adminName.isEmpty()) {
            AdminName.setError("Enter Name!");
            AdminName.requestFocus();
            return;
        }
        if (!isFullName(adminName)) {
            AdminName.setError("Enter Valid Name!");
            AdminName.requestFocus();
            return;
        }
        if (adminEmail.isEmpty()) {
            AdminEmail.setError("Enter Email Address!");
            AdminEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(adminEmail).matches()) {
            AdminEmail.setError("Please Enter Valid Email!");
            AdminEmail.requestFocus();
            return;
        }
        if (adminPass.isEmpty()) {
            AdminPass.setError("Enter Password!");
            AdminPass.requestFocus();
            return;
        }
        if (adminPass.length() < 6) {
            AdminPass.setError("Min password length should be 6 characters!");
            AdminPass.requestFocus();
            return;
        }
        if (joinCode.isEmpty()) {
            JoinCode.setError("Enter Join Code!");
            return;
        }
        if (joinCode.length() < 8) {
            if (joinCode.length() > 8) {
                JoinCode.setError("Enter Valid Join Code!");
                return;
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        Query query = FirebaseDatabase.getInstance().getReference("NewAdmins")
                .orderByChild("AdminName");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    AdminModel newAdmin = postSnapshot.getValue(AdminModel.class);
                    if (newAdmin.getAdminEmail().equals(adminEmail)) {
                        Toast.makeText(getApplicationContext(), "Matching Email", Toast.LENGTH_SHORT).show();
                        if (newAdmin.getRegCode().equals(joinCode)) {
                            //Toast.makeText(getApplicationContext(), "Matching Joining Code", Toast.LENGTH_SHORT).show();
                            if (newAdmin.getAdminName().equals(adminName)) {
                                //Toast.makeText(getApplicationContext(), "Matching Name", Toast.LENGTH_SHORT).show();
                                count++;
                                AddedOn = newAdmin.getAddedOn();
                                AddedBy = newAdmin.getAddedBy();

                                adminNameStr = newAdmin.getAdminName();
                                adminEmailStr = newAdmin.getAdminEmail();
                                adminRegCode = newAdmin.getRegCode();
                                if (ModifiedBy != null) {
                                    ModifiedBy = newAdmin.getModifiedBy();
                                    ModifiedOn = newAdmin.getModifiedOn();
                                }
                            } else {
                                AdminName.setError("Invalid Name!");
                                AdminName.requestFocus();
                                //Toast.makeText(getApplicationContext(), "Name not matching", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                return;
                            }
                        } else {
                            JoinCode.setError("Invalid Join Code!");
                            JoinCode.requestFocus();
                            //Toast.makeText(getApplicationContext(), "Joining Code Error!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }
                    } else {
                        AdminEmail.setError("Invalid Email Address!");
                        AdminEmail.requestFocus();
                        //Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    if (count == 1) {
                        mAuth.createUserWithEmailAndPassword(adminEmail, adminPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                                String RegistrationDate = simpleDateFormat.format(new Date());
                                if (task.isSuccessful()) {
                                    FirebaseUser Admin = mAuth.getCurrentUser();
                                    assert Admin != null;
                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(adminName)
                                            .build();
                                    Admin.updateProfile(userProfileChangeRequest);
                                    Map<String, Object> AdminInfo = new HashMap<>();
                                    AdminInfo.put("RegisteredOn", RegistrationDate);
                                    AdminInfo.put("AddedBy", AddedBy);
                                    AdminInfo.put("AddedOn", AddedOn);
                                    AdminInfo.put("ModifiedBy", ModifiedBy);
                                    AdminInfo.put("ModifiedOn", ModifiedOn);
                                    AdminInfo.put("RegCode", adminRegCode);
                                    AdminInfo.put("AdminName", capitalizeWord(adminNameStr));
                                    AdminInfo.put("AdminEmail", capitalizeWord(adminEmailStr));
                                    AdminInfo.put("AdminID", Admin.getUid());
                                    ///access Level or Role
                                    AdminInfo.put("isAdmin", true);
                                    DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Administrators").
                                            document(Admin.getUid());
                                    documentReference.set(AdminInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            FirebaseDatabase.getInstance().getReference("AllAdmins")
                                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                    .setValue(AdminInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Admin.sendEmailVerification();
                                                        ////deleted data from new Professor
                                                        deleteFromNewAdmins(joinCode);
                                                        Toast.makeText(getApplicationContext(), " Admin has been registered successfully! Check Your Email to verify your account! ", Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(View.GONE);
                                                        //redirect to login activity
                                                        startActivity(new Intent(getApplicationContext(), AdminLogin.class));
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
                                    Toast.makeText(getApplicationContext(), "Error! " + task.getException(), Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(getApplicationContext(), "Contact Administrator for registration!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Email Not valid!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterAdmin.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ////deleting data from New Professor
    public void deleteFromNewAdmins(String joiningCode) {
        DatabaseReference drNewAdmin = FirebaseDatabase.getInstance().getReference("NewAdmins").child(joiningCode);
        drNewAdmin.removeValue();
        Toast.makeText(this, "Deleted From NewProfessors", Toast.LENGTH_SHORT).show();
    }

    public String capitalizeWord(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterFirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterFirst.toLowerCase() + " ";
        }
        return capitalizeWord.trim();
    }

    public static boolean isFullName(String str) {
        String expression = "^[a-zA-Z\\s]+";
        return str.matches(expression);
    }

    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.darkRed));
    }
}