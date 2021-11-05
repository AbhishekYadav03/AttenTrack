package tk.jabtk.attentrack.admin;

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

import java.util.Calendar;
import java.util.Map;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageAdmins.ManageAdmins;
import tk.jabtk.attentrack.admin.ManageClassrooms.ManageClass;
import tk.jabtk.attentrack.admin.ManageProfessors.ManageProfessors;
import tk.jabtk.attentrack.admin.ManageStudents.ManageReports;
import tk.jabtk.attentrack.admin.ManageStudents.ManageStudents;

public class AdminDashboard extends Fragment {
    private View view;
    private TextView greetings;
    private Map<String, Object> AdminInfo;
    public String AdminID, KEY_USERNAME = "AdminName", KEY_REGCODE = "RegCode";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.admin_dashboard, container, false);
        getActivity().setTitle("Dashboard");

        MaterialCardView professorCard = view.findViewById(R.id.manage_professors);
        MaterialCardView studentsCard = view.findViewById(R.id.manage_students);
        MaterialCardView classesCard = view.findViewById(R.id.manage_classes);
        MaterialCardView reportsCard = view.findViewById(R.id.manage_reports);
        MaterialCardView adminsCard = view.findViewById(R.id.manage_admins);

        ///Admin Info
        FirebaseUser Admin = FirebaseAuth.getInstance().getCurrentUser();
        assert Admin != null;
        AdminID = Admin.getUid();

        DocumentReference AdminInfoRef = FirebaseFirestore.getInstance().collection("Administrators").document(AdminID);
        AdminInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    AdminInfo = documentSnapshot.getData();
                    greetings = view.findViewById(R.id.greeting);
                    greetings.setText(getGreetings(AdminInfo.get(KEY_USERNAME).toString()));
                    if (AdminInfo.get(KEY_REGCODE).equals("Administrator")) {
                        adminsCard.setVisibility(View.VISIBLE);
                    }
                    //Toast.makeText(getContext(), "AdminInfo:" + AdminInfo.get(KEY_USERNAME), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        ImageView AdminProfile = view.findViewById(R.id.circleImageView);
        if (Admin.getPhotoUrl() != null) {
            Picasso.get().load(Admin.getPhotoUrl()).placeholder(R.drawable.professor_ic_avatar).into(AdminProfile);
        } else {
            AdminProfile.setBackgroundResource(R.drawable.professor_ic_avatar);
        }
        AdminProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.adminMainFrameLayout, new AdminProfile()).addToBackStack(null).commit();
            }
        });


        professorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setFragment(new ManageProfessors());
                getParentFragmentManager().beginTransaction().replace(R.id.adminMainFrameLayout, new ManageProfessors()).addToBackStack(null).commit();
            }
        });
        studentsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setFragment(new ManageStudents());
                getParentFragmentManager().beginTransaction().replace(R.id.adminMainFrameLayout, new ManageStudents()).addToBackStack(null).commit();
            }
        });
        classesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setFragment(new ManageClass());
                getParentFragmentManager().beginTransaction().replace(R.id.adminMainFrameLayout, new ManageClass()).addToBackStack(null).commit();
            }
        });
        reportsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///fragment(new reportCard)
                getParentFragmentManager().beginTransaction().replace(R.id.adminMainFrameLayout, new ManageReports()).addToBackStack(null).commit();
            }
        });
        adminsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.adminMainFrameLayout, new ManageAdmins()).addToBackStack(null).commit();
            }
        });

        return view;
    }

    public static String getGreetings(String Name) {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay < 12) {
            return "Good Morning\n" + Name;
        } else if (timeOfDay < 16) {
            return "Good Afternoon\n" + Name;
        } else if (timeOfDay < 21) {
            return "Good Evening\n" + Name;
        } else {
            return "Good Night\n" + Name;
        }
    }
}