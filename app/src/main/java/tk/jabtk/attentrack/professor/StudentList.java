package tk.jabtk.attentrack.professor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.professor.adapters.LectureAdapter;
import tk.jabtk.attentrack.professor.model.LectureModel;


public class StudentList extends Fragment {
    private String ClassName, ClassCode, MonthYear, Subject;
    private LectureAdapter lectureAdapter;
    private RecyclerView recyclerView;
    boolean isAdmin = false;
    private LinearLayout linearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_list, container, false);
        recyclerView = view.findViewById(R.id.studentList);
        linearLayout = view.findViewById(R.id.noStudent);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ClassName = bundle.getString("ClassName");
            ClassCode = bundle.getString("ClassCode");
            MonthYear = bundle.getString("MonthYear");
            Subject = bundle.getString("Subject");
            isAdmin = Boolean.parseBoolean(bundle.getString("isAdmin"));
            getActivity().setTitle(ClassName + " " + Subject + " (" + MonthYear + ")");
            //Toast.makeText(getContext(), ClassName + "\n" + ClassCode, Toast.LENGTH_SHORT).show();
        }



        return view;
    }

    public void setUpStudentList() {

        Query queryOld = FirebaseFirestore.getInstance()
                .collection("ClassList")
                .document(ClassCode)
                .collection("Students");

        Query query = FirebaseFirestore.getInstance()
                .collection("LectureCount")
                .document("Months")
                .collection(MonthYear)
                .document(ClassCode)
                .collection(Subject).orderBy("PresentLectCount", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<LectureModel> lectureModelOptions = new FirestoreRecyclerOptions.Builder<LectureModel>()
                .setQuery(query, LectureModel.class)
                .build();

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    linearLayout.setVisibility(View.GONE);
                    lectureAdapter = new LectureAdapter(lectureModelOptions, queryOld);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(lectureAdapter);
                    DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                    recyclerView.addItemDecoration(decoration);
                    lectureAdapter.startListening();

                    lectureAdapter.setItemClickListener(new LectureAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DocumentSnapshot Snapshot, int position, String name) {
                            Map<String, Object> StudentAtten = Snapshot.getData();
                            Bundle bundle = new Bundle();
                            bundle.putString("Subject", Subject);
                            bundle.putString("ClassCode", ClassCode);
                            bundle.putString("ClassName", ClassName);
                            bundle.putString("MonthYear", MonthYear);
                            bundle.putString("StudentID", Snapshot.getId());
                            bundle.putString("StudentName", name);
                            bundle.putString("TotalAbsent", StudentAtten.get("AbsentLectCount").toString());
                            bundle.putString("TotalPresent", StudentAtten.get("PresentLectCount").toString());
                            bundle.putString("TotalLectures", StudentAtten.get("TotalLectCount").toString());
                            Log.d("StudentList", name);
                            if (isAdmin) {
                                bundle.putString("isAdmin", "true");
                                StudentInfo studentInfo = new StudentInfo();
                                studentInfo.setArguments(bundle);
                                getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.adminMainFrameLayout, studentInfo).commit();
                            } else {
                                bundle.putString("isAdmin", "false");
                                StudentInfo studentInfo = new StudentInfo();
                                studentInfo.setArguments(bundle);
                                getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.professorMainFrameLayout, studentInfo).commit();
                            }
                        }

                        @Override
                        public void onLongItemClick(DocumentSnapshot Snapshot, int position) {

                        }
                    });
                }

            }
        });


    }
    ////////////////////////////////////////////////////
    @Override
    public void onStart() {
        super.onStart();
        setUpStudentList();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (lectureAdapter != null)
            lectureAdapter.stopListening();
    }
}