package tk.jabtk.attentrack.professor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassAdapter;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassModel;


public class Classrooms extends Fragment {
    private FirebaseUser professor;
    private ProgressBar progressBar;
    private View alertView, view;
    private AlertDialog dialog;
    private String userID;
    private ClassAdapter classAdapter;
    private TextInputLayout classJoinCode;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_classrooms, container, false);

        getActivity().setTitle("Classrooms");

        //get Current user
        professor = FirebaseAuth.getInstance().getCurrentUser();
        assert professor != null;
        userID = professor.getUid();

        progressBar = view.findViewById(R.id.Progressbar);

        FloatingActionButton proJoinClass = view.findViewById(R.id.ProJoinClass);
        proJoinClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertView = getLayoutInflater().inflate(R.layout.join_class_dialog, null);
                classJoinCode = alertView.findViewById(R.id.classCode);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(alertView);

                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();


                MaterialButton cancelBtn = alertView.findViewById(R.id.cancelBtn);
                MaterialButton joinClass = alertView.findViewById(R.id.joinClassBtn);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                joinClass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JoinClass();
                    }
                });

            }
        });


        setUpRecyclerView();
        return view;
    }


    public void setUpRecyclerView() {

       /* CollectionReference db = FirebaseFirestore.getInstance().collection("ClassList");
        DocumentReference query = db.document().collection("Professors").document(userID);
*/
        Query ProDB = FirebaseDatabase.getInstance().getReference("ProfessorClassrooms").child(userID).orderByChild("ClassName");

        FirebaseRecyclerOptions<ClassModel> options = new FirebaseRecyclerOptions.Builder<ClassModel>()
                .setQuery(ProDB, ClassModel.class)
                .build();

        ProDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView = view.findViewById(R.id.ProClass);
                    recyclerView.setVisibility(View.VISIBLE);

                    classAdapter = new ClassAdapter(options);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(classAdapter);
                    classAdapter.startListening();

                    classAdapter.setItemClickListener(new ClassAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            ClassModel classModel = Snapshot.getValue(ClassModel.class);
                            Bundle bundle = new Bundle();
                            if (classModel.getClassCode() != null) {
                                //Toast.makeText(getContext(), classModel.getClassName(), Toast.LENGTH_SHORT).show();
                                bundle.putString("ClassName", classModel.getClassName());
                                bundle.putString("ClassCode", classModel.getClassCode());
                            }

                            ClassInfo classInfo = new ClassInfo();
                            classInfo.setArguments(bundle);
                            getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.professorMainFrameLayout, classInfo).commit();
                        }

                        @Override
                        public void onLongItemClick(DataSnapshot Snapshot, int position) {

                        }
                    });

                } else {
                    progressBar.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.not_found_text);
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void JoinClass() {

        boolean isError = false;
        String ClassCode = classJoinCode.getEditText().getText().toString().trim();

        if (ClassCode.isEmpty()) {
            isError = true;
            classJoinCode.setError("Class Code cannot be empty!");
            classJoinCode.requestFocus();
        }
        if (ClassCode.length() != 8) {
            isError = true;
            classJoinCode.setError("Enter 8 characters code!");
            classJoinCode.requestFocus();
        }

        if (!isError) {
            Query query = FirebaseDatabase.getInstance().getReference().child("Class").orderByChild("ClassCode");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String ClassName = null;
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        ClassModel classModel = postSnapshot.getValue(ClassModel.class);
                        if (classModel.getClassCode().equals(ClassCode)) {
                            ClassName = classModel.getClassName();
                            count++;
                            //Toast.makeText(getContext(), "Matching code", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(getContext(), "Not Matching code ", Toast.LENGTH_SHORT).show();

                        }
                    }

                    if (count == 1) {

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String requestTime = simpleDateFormat.format(new Date());

                        progressBar.setVisibility(View.VISIBLE);

                        Map<String, Object> ClassInfo = new HashMap<>();
                        ClassInfo.put("ClassName", ClassName);
                        ClassInfo.put("ClassCode", ClassCode);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProfessorClassrooms");
                        databaseReference.child(userID).child(ClassCode).setValue(ClassInfo);


                        Map<String, Object> JoinRequest = new HashMap<>();
                        JoinRequest.put("ProfessorID", userID);
                        JoinRequest.put("ProfessorName", professor.getDisplayName());
                        JoinRequest.put("ProfessorEmail", professor.getEmail());
                        JoinRequest.put("RequestTimestamp", requestTime);
                        if (professor.getPhotoUrl() != null) {
                            JoinRequest.put("ProfileUrl", professor.getPhotoUrl());
                        }

                        DocumentReference classList = FirebaseFirestore.getInstance().collection("ClassList").
                                document(ClassCode);
                        classList.set(ClassInfo);
                        classList.collection("Professors").document(userID).set(JoinRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Class Joined Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        dialog.dismiss();

                        //Toast.makeText(getContext(), "Parent Match Found"+newProfessorModel.getRegCode(), Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        classJoinCode.setError("Invalid Code!");
                        classJoinCode.requestFocus();
                        Toast.makeText(getContext(), "Invalid Code!", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to request\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        // Otherwise the dialog will stay open.

    }

    ///// data listening for auto refresh
    @Override
    public void onStart() {
        super.onStart();
        setUpRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (classAdapter != null)
            classAdapter.stopListening();
    }

}