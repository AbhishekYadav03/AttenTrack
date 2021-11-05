package tk.jabtk.attentrack.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.professor.ProfessorLogin;
import tk.jabtk.attentrack.student.StudentLogin;


public class ForgotPassword extends Fragment {
    private TextInputLayout emailInputLayout;
    private ProgressBar progressBar;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private boolean isProfessor = false;
    private boolean isStudent = false;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        emailInputLayout = view.findViewById(R.id.emailAddress);
        emailInputLayout.getEditText().setText(mAuth.getCurrentUser().getEmail());

        MaterialButton materialButton = view.findViewById(R.id.resetPassBtn);
        ImageView imageView = view.findViewById(R.id.imageView);
        Bundle bundle = this.getArguments();


        if (bundle != null) {
            if (bundle.getBoolean("isProfessor")) {
                isProfessor = bundle.getBoolean("isProfessor");
            }
            if (bundle.getBoolean("isStudent")) {
                isStudent = bundle.getBoolean("isBoolean");
            }
        }
        if (isProfessor) {
            progressBar.setIndeterminateDrawable(this.getActivity().getResources().getDrawable(R.drawable.progress_bar));
            emailInputLayout.setHintTextColor(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green)));
            emailInputLayout.setBoxStrokeColor(this.getResources().getColor(R.color.green));
            imageView.setImageTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green)));
            materialButton.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green)));
        }
        if (isStudent) {
            progressBar.setIndeterminateDrawable(this.getActivity().getResources().getDrawable(R.drawable.student_progress_bar));
            emailInputLayout.setHintTextColor(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.skyBlue)));
            emailInputLayout.setBoxStrokeColor(this.getResources().getColor(R.color.skyBlue));
            imageView.setImageTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.skyBlue)));
            materialButton.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.skyBlue)));
        }
        materialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        return view;
    }

    private void resetPassword() {
        String email = emailInputLayout.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required!");
            emailInputLayout.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Enter valid Email!");
            emailInputLayout.requestFocus();
            return;
        }
        if (!(email.equals(mAuth.getCurrentUser().getEmail()))) {
            emailInputLayout.setError("Email is different!");
            emailInputLayout.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mAuth.signOut();
                    Toast.makeText(getContext(), "Check Your Email to Reset Your Password! ", Toast.LENGTH_SHORT).show();

                    if (isProfessor) {
                        startActivity(new Intent(getContext(), ProfessorLogin.class));
                    } else if (isStudent) {
                        startActivity(new Intent(getContext(), StudentLogin.class));
                    }else {
                        startActivity(new Intent(getContext(), AdminLogin.class));
                    }

                    getActivity().finish();
                } else {
                    emailInputLayout.getEditText().setText("");
                    Toast.makeText(getContext(), "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}