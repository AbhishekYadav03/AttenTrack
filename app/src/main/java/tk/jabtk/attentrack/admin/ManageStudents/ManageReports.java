package tk.jabtk.attentrack.admin.ManageStudents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassAdapter;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassModel;
import tk.jabtk.attentrack.professor.StudentList;

public class ManageReports extends Fragment {
    private View view;
    private Map<String, Object> AdminInfo;
    private ClassAdapter classAdapter;
    private ProgressBar progressBar;
    private View alertViewReport;
    private AlertDialog dialog;
    private String ClassCode, ClassName, MonthYear, Subject;
    private AutoCompleteTextView subject_menu, month_menu;
    private ArrayAdapter<String> subjectAdapter, monthAdapter;
    private ArrayList<String> subjectsList, monthList;
    DatabaseReference subjectRef;
    DatabaseReference monthRef = FirebaseDatabase.getInstance().getReference("LectureCount").child("Months");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reports_admin, container, false);
        progressBar = view.findViewById(R.id.Progressbar);
        getActivity().setTitle("Manage Reports");
        ///Admin Info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userID = user.getUid();
        DocumentReference AdminInfoRef = FirebaseFirestore.getInstance().collection("Administrators").document(userID);
        AdminInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    AdminInfo = documentSnapshot.getData();
                   /* String UserName="UserName";
                    Toast.makeText(getContext(), "AdminInfo:"+AdminInfo.get(UserName), Toast.LENGTH_SHORT).show();*/
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    public void setUpRecyclerView() {
        Query query = FirebaseDatabase.getInstance().getReference("Class").orderByChild("ClassName");

        FirebaseRecyclerOptions<ClassModel> options = new FirebaseRecyclerOptions.Builder<ClassModel>()
                .setQuery(query, ClassModel.class)
                .build();


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RecyclerView recyclerView = view.findViewById(R.id.recycler);
                    progressBar.setVisibility(View.GONE);

                    classAdapter = new ClassAdapter(options);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(classAdapter);
                    classAdapter.startListening();
                    /////            interface calling from adapter class              ///
                    classAdapter.setItemClickListener(new ClassAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            ClassModel classModel = Snapshot.getValue(ClassModel.class);
                            if (classModel != null) {
                                ClassName = classModel.getClassName();
                                ClassCode = classModel.getClassCode();
                                alertViewReport = getLayoutInflater().inflate(R.layout.select_subject_month_year_dialog, null);
                                TextInputLayout subjectName = alertViewReport.findViewById(R.id.subjectName);
                                TextInputLayout monthName = alertViewReport.findViewById(R.id.months);
                                subjectRef = FirebaseDatabase.getInstance().getReference("Subjects").child(classModel.getClassCode());
                                subjectsList = new ArrayList<>();
                                subjectAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, subjectsList);
                                subject_menu = alertViewReport.findViewById(R.id.dropdown_menu_subject);
                                setSubjectsList();
                                subject_menu.setAdapter(subjectAdapter);
                                monthList = new ArrayList<>();
                                monthAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, monthList);
                                month_menu = alertViewReport.findViewById(R.id.dropdown_menu_month_year);
                                setMonthsList();
                                month_menu.setAdapter(monthAdapter);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setView(alertViewReport);
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
                    LinearLayout notFoundLayout = (LinearLayout) view.findViewById(R.id.not_found);
                    notFoundLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            studentList.putString("isAdmin", "true");
        }
        StudentList studentListObj = new StudentList();
        studentListObj.setArguments(studentList);
        dialog.dismiss();
        getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.adminMainFrameLayout, studentListObj).commit();

    }


    public void setSubjectsList() {
        subjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot items : snapshot.getChildren()) {
                    //subjectsList.add(items.getValue().toString());
                    String[] splitString = items.getValue().toString().split("=");
                    String splitString2 = splitString[0];
                    int length = splitString2.length();
                    String subject = splitString2.substring(1, length).trim();
                    subjectsList.add(subject);
                }
                subjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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
        setUpRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (classAdapter != null)
            classAdapter.stopListening();
    }
}