package tk.jabtk.attentrack.professor;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassAdapter;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassModel;


public class Reports extends Fragment {

    private String ProfessorID, ClassName, ClassCode, Subject, MonthYear;
    private ClassAdapter classAdapter;
    private View view, alertView;
    private AlertDialog dialog;

    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;

    private AutoCompleteTextView subject_menu, month_menu;
    private ArrayAdapter<String> subjectAdapter, monthAdapter;
    private ArrayList<String> subjectsList, monthList;
    DatabaseReference subjectRef;
    DatabaseReference monthRef = FirebaseDatabase.getInstance().getReference("LectureCount").child("Months");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        getActivity().setTitle("Reports");
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reports, container, false);

        progressBar = view.findViewById(R.id.Progressbar);
        FirebaseUser professor = FirebaseAuth.getInstance().getCurrentUser();
        assert professor != null;
        ProfessorID = professor.getUid();


        setRecyclerView();
        return view;
    }


    public void setRecyclerView() {

        // Query query = FirebaseFirestore.getInstance().collection("LectureCount").document("Months").collection(month.getEditText().getText().toString());

        com.google.firebase.database.Query ProDB = FirebaseDatabase.getInstance().getReference("ProfessorClassrooms")
                .child(ProfessorID)
                .orderByChild("ClassName");

        FirebaseRecyclerOptions<ClassModel> options = new FirebaseRecyclerOptions.Builder<ClassModel>()
                .setQuery(ProDB, ClassModel.class)
                .build();

        ProDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);

                    RecyclerView recyclerView = view.findViewById(R.id.classList);
                    constraintLayout = view.findViewById(R.id.constraintLayout);
                    constraintLayout.setVisibility(View.VISIBLE);

                    classAdapter = new ClassAdapter(options);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(classAdapter);
                    classAdapter.startListening();
                    classAdapter.setItemClickListener(new ClassAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            ClassModel classModel = Snapshot.getValue(ClassModel.class);
                            if (classModel != null) {

                                ClassName = classModel.getClassName();
                                ClassCode = classModel.getClassCode();
                                subjectRef = FirebaseDatabase.getInstance().getReference("Subjects").child(classModel.getClassCode()).child(ProfessorID);


                                alertView = getLayoutInflater().inflate(R.layout.select_subject_month_year_dialog, null);
                                TextInputLayout subjectName = alertView.findViewById(R.id.subjectName);
                                TextInputLayout monthName = alertView.findViewById(R.id.months);

                                TextView textView = alertView.findViewById(R.id.textViewClass);

                                textView.setTextColor(getActivity().getResources().getColor(R.color.green));
                                monthName.setHintTextColor(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green)));
                                monthName.setBoxStrokeColor(getActivity().getResources().getColor(R.color.green));

                                subjectName.setBoxStrokeColor(getActivity().getResources().getColor(R.color.green));
                                subjectName.setHintTextColor(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.green)));


                                // month.getEditText().setText("April2021");

                                monthList = new ArrayList<>();
                                monthAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, monthList);
                                month_menu = alertView.findViewById(R.id.dropdown_menu_month_year);
                                setMonthsList();
                                month_menu.setAdapter(monthAdapter);


                                subjectsList = new ArrayList<>();
                                subjectAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, subjectsList);
                                subject_menu = alertView.findViewById(R.id.dropdown_menu_subject);
                                setSubjectsList();
                                subject_menu.setAdapter(subjectAdapter);

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setView(alertView);

                                dialog = builder.create();
                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                dialog.show();

                                month_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Editable selectedSubject = ((AutoCompleteTextView) subjectName.getEditText()).getText();
                                        Editable selectedMonth = ((AutoCompleteTextView) monthName.getEditText()).getText();

                                        if (selectedSubject.length() == 0) {

                                            subject_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    Editable selectedSubject = ((AutoCompleteTextView) subjectName.getEditText()).getText();
                                                    //Editable selectedMonth = ((AutoCompleteTextView) monthName.getEditText()).getText();
                                                    if (selectedMonth.length() == 0) {
                                                        monthName.setError("Select Month!");
                                                    } else {
                                                        monthName.setError(null);
                                                        Subject = String.valueOf(selectedSubject);
                                                        MonthYear = String.valueOf(selectedMonth);
                                                        showStudentList();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });


                                subject_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Editable selectedSubject = ((AutoCompleteTextView) subjectName.getEditText()).getText();
                                        Editable selectedMonth = ((AutoCompleteTextView) monthName.getEditText()).getText();

                                        if (selectedMonth.length() == 0) {

                                            month_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    //Editable selectedSubject = ((AutoCompleteTextView) subjectName.getEditText()).getText();
                                                    Editable selectedMonth = ((AutoCompleteTextView) monthName.getEditText()).getText();
                                                    if (selectedMonth.length() == 0) {
                                                        subjectName.setError("Select Month!");
                                                    } else {
                                                        subjectName.setError(null);
                                                        Subject = String.valueOf(selectedSubject);
                                                        MonthYear = String.valueOf(selectedMonth);
                                                        showStudentList();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });


                            }

                        }

                        @Override
                        public void onLongItemClick(DataSnapshot Snapshot, int position) {

                        }
                    });

                } else {
                    progressBar.setVisibility(View.GONE);
                    TextView notFoundLayout = view.findViewById(R.id.not_found_text);
                    notFoundLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void showStudentList() {

        Bundle studentList = new Bundle();
        if (ClassCode != null) {
            //Toast.makeText(getContext(), classModel.getClassName(), Toast.LENGTH_SHORT).show();
            studentList.putString("ClassName", ClassName);
            studentList.putString("ClassCode", ClassCode);
            studentList.putString("MonthYear", MonthYear);
            studentList.putString("Subject", Subject);
            studentList.putString("isAdmin", "false");
        }

        StudentList studentListObj = new StudentList();
        studentListObj.setArguments(studentList);
        dialog.dismiss();

        getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.professorMainFrameLayout, studentListObj).commit();

    }


    public void setSubjectsList() {

        subjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot items : snapshot.getChildren()) {
                    subjectsList.add(items.getValue().toString());
                    //Toast.makeText(getContext(), items.getValue().toString(), Toast.LENGTH_SHORT).show();
                }
                subjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void setMonthsList() {

        monthRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot items : snapshot.getChildren()) {
                    monthList.add(items.getValue().toString());
                    //Toast.makeText(getContext(), items.getValue().toString(), Toast.LENGTH_SHORT).show();
                }
                monthAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    ///// data listening for auto refresh
    @Override
    public void onStart() {
        super.onStart();
        setRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (classAdapter != null)
            classAdapter.stopListening();
    }


}