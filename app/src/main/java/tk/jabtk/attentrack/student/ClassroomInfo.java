package tk.jabtk.attentrack.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageStudents.StudentModel;
import tk.jabtk.attentrack.professor.adapters.StudentAdapterClassrooms;


public class ClassroomInfo extends Fragment {
    private View view;
    private ConstraintLayout ClassStudentLayout;
    private ProgressBar progressBar;
    StudentAdapterClassrooms studentAdapter;
    private String StudentId;
    private TextView textView;
    private final String KEY_CLASSCODE = "ClassCode";
    private final String CLASS_NAME = "ClassName";
    Map<String, Object> StudentData = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_classroom_info, container, false);


        ClassStudentLayout = view.findViewById(R.id.classStudents);
        progressBar = view.findViewById(R.id.Progressbar);
        textView = view.findViewById(R.id.textStudents);

        FirebaseUser student = FirebaseAuth.getInstance().getCurrentUser();
        StudentId = student.getUid();

        return view;
    }

    public void setupStudents() {


        DocumentReference StudentInfoRef = FirebaseFirestore.getInstance().collection("Students").document(StudentId);
        StudentInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                StudentData = documentSnapshot.getData();
                if (documentSnapshot.exists()) {
                    String ClassName = StudentData.get(CLASS_NAME).toString();
                    textView.setText(ClassName);
                    //Toast.makeText(getContext(), "ClassCode:" + StudentData.get(KEY_CLASSCODE), Toast.LENGTH_SHORT).show();
                    CollectionReference db = FirebaseFirestore.getInstance().collection("ClassList")
                            .document(StudentData.get(KEY_CLASSCODE).toString())
                            .collection("Students");

                    FirestoreRecyclerOptions<StudentModel> options = new FirestoreRecyclerOptions.Builder<StudentModel>()
                            .setQuery(db, StudentModel.class)
                            .build();

                    db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Student not Found!", Toast.LENGTH_SHORT).show();
                                } else {

                                    studentAdapter = new StudentAdapterClassrooms(options);
                                    RecyclerView recyclerView = view.findViewById(R.id.recyclerStudents);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(studentAdapter);
                                    DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                                    recyclerView.addItemDecoration(decoration);
                                    studentAdapter.startListening();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(getContext(), " :" + StudentData.get(KEY_CLASSCODE), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    ///// data listening for auto refresh
    @Override
    public void onStart() {
        super.onStart();
        setupStudents();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (studentAdapter != null) {
            studentAdapter.stopListening();
        }
    }
}