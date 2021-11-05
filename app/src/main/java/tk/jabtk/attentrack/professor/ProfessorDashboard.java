package tk.jabtk.attentrack.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageProfessors.OldProfessorModel;

import static tk.jabtk.attentrack.admin.AdminDashboard.getGreetings;

public class ProfessorDashboard extends Fragment {
    private String ProfessorName, ProfessorEmail, userType;
    private MaterialCardView classrooms, attendance, reports;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_professor_dashboard, container, false);
        getActivity().setTitle("Dashboard");


        final TextView greetingTxt = view.findViewById(R.id.greeting);

        classrooms = view.findViewById(R.id.classrooms);
        classrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.professorMainFrameLayout, new Classrooms()).addToBackStack(null).commit();
            }
        });

        attendance = view.findViewById(R.id.attendance);
        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.professorMainFrameLayout, new SelectAttendance()).addToBackStack(null).commit();
            }
        });
        reports = view.findViewById(R.id.reports);
        reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.professorMainFrameLayout, new Reports()).addToBackStack(null).commit();
            }
        });


        //get Current user
        FirebaseUser professor = FirebaseAuth.getInstance().getCurrentUser();
        assert professor != null;
        String userID = professor.getUid();

        ImageView userProfile = view.findViewById(R.id.userProfileImg);
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Professors").document(userID);
        if (professor.getPhotoUrl() != null) {
            Picasso.get().load(professor.getPhotoUrl()).placeholder(R.drawable.professor_ic_avatar).into(userProfile);
        } else {
            userProfile.setBackgroundResource(R.drawable.professor_ic_avatar);
        }
        //retrieving data from firebase
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getBoolean("isProfessor")) {
                    userType = "Professor";
                    //Toast.makeText(ProfessorMainActivity.this, userType, Toast.LENGTH_SHORT).show();
                }

                final OldProfessorModel oldProfessorModel = documentSnapshot.toObject(OldProfessorModel.class);
                if (oldProfessorModel != null && userType != null) {
                    ProfessorName = oldProfessorModel.getProfessorName();
                    ProfessorEmail = oldProfessorModel.getProfessorEmail();
                    greetingTxt.setText(getGreetings(ProfessorName));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.professorMainFrameLayout, new ProfessorProfile()).addToBackStack(null).commit();
            }
        });


        return view;
    }




}