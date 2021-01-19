package tk.jabtk.attentrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import tk.jabtk.attentrack.professor.MainActivity;

public class StartActivity extends AppCompatActivity {
    private Button adminLogin,professorLogin,studentLogin;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
/// Auto login process if user already created or logged in
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

///admin login Activity
        adminLogin=findViewById(R.id.AdminLogin);
        adminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(tk.jabtk.attentrack.StartActivity.this, tk.jabtk.attentrack.admin.adminLogin.class));
            }
        });

/// Professor Login Activity
        professorLogin=findViewById(R.id.ProfessorLogin);
        professorLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(tk.jabtk.attentrack.StartActivity.this, tk.jabtk.attentrack.professor.professorLogin.class));
            }
        });
/// Student Login Activity
        studentLogin=findViewById(R.id.StudentLogin);
        studentLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(tk.jabtk.attentrack.StartActivity.this, tk.jabtk.attentrack.student.studentLogin.class));
            }
        });
    }

}