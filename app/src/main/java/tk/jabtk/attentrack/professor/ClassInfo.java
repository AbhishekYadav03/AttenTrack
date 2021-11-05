package tk.jabtk.attentrack.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageStudents.StudentModel;
import tk.jabtk.attentrack.professor.adapters.StudentAdapterClassrooms;
import tk.jabtk.attentrack.professor.adapters.StudentAdapterRequest;
import tk.jabtk.attentrack.professor.model.StudentRequest;


public class ClassInfo extends Fragment {
    private View view;
    private String ClassName, ClassCode;
    ConstraintLayout JoinReqLayout, ClassStudentLayout;
    private StudentAdapterRequest requestAdapter;
    private StudentAdapterClassrooms studentAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_class_info, container, false);
        JoinReqLayout = view.findViewById(R.id.JoinReqLayout);
        ClassStudentLayout = view.findViewById(R.id.classStudents);


        Bundle bundle = this.getArguments();


        if (bundle != null) {
            ClassName = bundle.getString("ClassName");
            ClassCode = bundle.getString("ClassCode");
            getActivity().setTitle(ClassName);
            //Toast.makeText(getContext(), ClassName + "\n" + ClassCode, Toast.LENGTH_SHORT).show();
        }


        return view;
    }

    public void setupRequest() {
        Query query = FirebaseDatabase.getInstance().getReference("StudentClassJoinRequest").child(ClassCode);
        FirebaseRecyclerOptions<StudentRequest> options = new FirebaseRecyclerOptions.Builder<StudentRequest>()
                .setQuery(query, StudentRequest.class)
                .build();

        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {


                if (task.getResult().exists()) {
                    JoinReqLayout.setVisibility(View.VISIBLE);
                    requestAdapter = new StudentAdapterRequest(options);
                    RecyclerView recyclerView = view.findViewById(R.id.recyclerRequest);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(requestAdapter);
                    DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                    recyclerView.addItemDecoration(decoration);
                    requestAdapter.startListening();
                } else {
                    JoinReqLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setupStudents() {

        CollectionReference db = FirebaseFirestore.getInstance().collection("ClassList").document(ClassCode).collection("Students");
        FirestoreRecyclerOptions<StudentModel> options = new FirestoreRecyclerOptions.Builder<StudentModel>()
                .setQuery(db, StudentModel.class)
                .build();

        db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {

                    } else {
                        studentAdapter = new StudentAdapterClassrooms(options);
                        RecyclerView recyclerView = view.findViewById(R.id.recyclerStudents);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(studentAdapter);
                        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                        recyclerView.addItemDecoration(decoration);
                        studentAdapter.startListening();
                    }
                }
            }
        });


    }

    ///// data listening for auto refresh
    @Override
    public void onStart() {
        super.onStart();
        setupRequest();
        setupStudents();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (requestAdapter != null) {
            requestAdapter.stopListening();
        }
        if (studentAdapter != null) {
            studentAdapter.stopListening();
        }
    }

}