package tk.jabtk.attentrack.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import tk.jabtk.attentrack.R;

public class StudentProfessors extends Fragment {
    ProfessorAdapter professorAdapter;
    View view;
    RecyclerView recyclerView;
    String ClassCode;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_student_professors, container, false);
        recyclerView = view.findViewById(R.id.professorList);
        progressBar = view.findViewById(R.id.progress_bar);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ClassCode = bundle.getString("ClassCode");
        }

        return view;
    }

    public void setUpRecyclerView() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ///Query query = FirebaseDatabase.getInstance().getReference("Professors").orderByChild("ProfessorName");
        com.google.firebase.firestore.Query query = db.collection("ClassList").document(ClassCode).collection("Professors").orderBy("ProfessorName");

        FirestoreRecyclerOptions<ProfessorModel> recyclerOptions = new FirestoreRecyclerOptions.Builder<ProfessorModel>()
                .setQuery(query, ProfessorModel.class)
                .build();


        DatabaseReference subjectRef = FirebaseDatabase.getInstance().getReference("Subjects").child(ClassCode);
        professorAdapter = new ProfessorAdapter(recyclerOptions, subjectRef);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(professorAdapter);
        professorAdapter.startListening();
        progressBar.setVisibility(View.GONE);
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
        professorAdapter.stopListening();

    }


}