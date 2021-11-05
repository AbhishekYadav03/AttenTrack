package tk.jabtk.attentrack.admin.ManageStudents;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import tk.jabtk.attentrack.R;

public class ManageStudents extends Fragment {
    private StudentAdapter studentAdapter;
    private RecyclerView recyclerView;
    private LinearLayout linearLayout;
    private TextInputLayout searchLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Manage Students");
        View view = inflater.inflate(R.layout.fragment_manage_students, container, false);
        recyclerView = view.findViewById(R.id.recyclerStudents);
        linearLayout = view.findViewById(R.id.searchNotFound);
        searchLayout = view.findViewById(R.id.search_box);
        TextInputEditText searchLayoutEditText = (TextInputEditText) searchLayout.getEditText();
        assert searchLayoutEditText != null;


        setupStudents("");

        searchLayoutEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                setupStudents(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }


    public void setupStudents(String Search) {

        Query db = FirebaseFirestore.getInstance().collection("Students")
                .orderBy("StudentName")
                .startAt(Search).endAt(Search + "\uf8ff");
        FirestoreRecyclerOptions<StudentModel> options = new FirestoreRecyclerOptions.Builder<StudentModel>()
                .setQuery(db, StudentModel.class)
                .build();

        db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        Toast.makeText(getContext(), "Searching...", Toast.LENGTH_SHORT).show();
                        linearLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        studentAdapter = new StudentAdapter(options);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(studentAdapter);
                        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                        recyclerView.addItemDecoration(decoration);
                        studentAdapter.startListening();
                    } else {
                        Toast.makeText(getContext(), "Not Found!", Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


    }

    ///// data listening for auto refresh
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (studentAdapter != null)
            studentAdapter.stopListening();
    }


}