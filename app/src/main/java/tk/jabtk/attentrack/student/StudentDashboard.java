package tk.jabtk.attentrack.student;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.StartActivity;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassModel;

import static android.content.ContentValues.TAG;
import static tk.jabtk.attentrack.admin.AdminDashboard.getGreetings;

public class StudentDashboard extends Fragment {
    private View view, alertView, alertViewReport;
    private AlertDialog dialog;
    private FirebaseUser Student;
    private String StudentId;
    private final String KEY_USERNAME = "StudentName";
    private final String KEY_CLASSCODE = "ClassCode";
    private MaterialButton cancelBtn, joinClass;
    private TextView greetings;
    private FloatingActionButton floatingActionButton;
    private TextInputLayout classJoinCode;
    private ProgressBar progressBar;

    private String ClassCode, MonthYear, Subject;
    private GridLayout gridLayout;
    private MaterialCardView classroom, professors, reports;
    private Map<String, Object> StudentInfo = new HashMap<>();

    private AutoCompleteTextView subject_menu, month_menu;
    private ArrayAdapter<String> adapter, monthAdapter;
    private ArrayList<String> subjectsList, monthList;
    DatabaseReference subjectRef;
    DatabaseReference monthRef = FirebaseDatabase.getInstance().getReference("LectureCount").child("Months");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_student_dashboard, container, false);


        progressBar = view.findViewById(R.id.Progressbar);
        floatingActionButton = view.findViewById(R.id.joinClass);

        gridLayout = view.findViewById(R.id.gridLayout);
        classroom = view.findViewById(R.id.classroom);
        professors = view.findViewById(R.id.professors);
        reports = view.findViewById(R.id.reports);

        gridLayout.removeView(classroom);
        gridLayout.removeView(professors);
        gridLayout.removeView(reports);

        ///Student Info
        Student = FirebaseAuth.getInstance().getCurrentUser();
        assert Student != null;
        StudentId = Student.getUid();
        DocumentReference StudentInfoRef = FirebaseFirestore.getInstance().collection("Students").document(StudentId);
        StudentInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    ImageView userProfile = view.findViewById(R.id.studentImg);
                    StudentInfo = documentSnapshot.getData();
                    greetings = view.findViewById(R.id.greeting);
                    if (Student.getPhotoUrl() != null) {
                        Picasso.get().load(Student.getPhotoUrl()).placeholder(R.drawable.ic_student).into(userProfile);
                    } else {
                        //Toast.makeText(getContext(), "Null", Toast.LENGTH_SHORT).show();
                        userProfile.setBackgroundResource(R.drawable.ic_student);
                    }

                    greetings.setText(getGreetings(StudentInfo.get(KEY_USERNAME).toString()));

                    if (StudentInfo.get(KEY_CLASSCODE) != null) {
                        showUI();
                    } else {
                        floatingActionButton.setVisibility(View.VISIBLE);
                    }

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton student_logout = view.findViewById(R.id.student_logout);

        student_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you really want to log out?");
                builder.setTitle("Logout");
                builder.setCancelable(false);
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(getContext(), "Logout Successfully!",
                                Toast.LENGTH_LONG).show();

                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getContext(), StartActivity.class));
                        getActivity().finishAffinity();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getActivity().getResources().getColor(R.color.skyBlue));



            }
        });

        ImageButton selectPic = view.findViewById(R.id.selectPic);
        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.StudentMainFrameLayout, new StudentProfile()).addToBackStack(null).commit();
            }
        });

        if (gridLayout != null) {

            classroom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //setFragment(new ClassroomInfo());
                    getParentFragmentManager().beginTransaction().replace(R.id.StudentMainFrameLayout, new ClassroomInfo()).addToBackStack(null).commit();
                }
            });

            professors.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //setFragment(new ClassroomInfo());
                    Bundle bundle = new Bundle();
                    bundle.putString("ClassCode", ClassCode);
                    StudentProfessors studentProfessors = new StudentProfessors();
                    studentProfessors.setArguments(bundle);

                    getParentFragmentManager().beginTransaction().replace(R.id.StudentMainFrameLayout, studentProfessors).addToBackStack(null).commit();
                }
            });

            reports.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //setFragment(new report())());

                    alertViewReport = getLayoutInflater().inflate(R.layout.select_subject_month_year_dialog, null);

                    TextInputLayout subjectName = alertViewReport.findViewById(R.id.subjectName);
                    TextInputLayout monthName = alertViewReport.findViewById(R.id.months);

                    subjectRef = FirebaseDatabase.getInstance().getReference("Subjects").child(ClassCode);
                    subjectsList = new ArrayList<>();
                    adapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, subjectsList);
                    subject_menu = alertViewReport.findViewById(R.id.dropdown_menu_subject);
                    setSubjectsList();
                    subject_menu.setAdapter(adapter);

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
                                            showReport();
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
                                            showReport();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            });

        }


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertView = getLayoutInflater().inflate(R.layout.join_class_dialog, null);
                classJoinCode = alertView.findViewById(R.id.classCode);

                cancelBtn = alertView.findViewById(R.id.cancelBtn);
                joinClass = alertView.findViewById(R.id.joinClassBtn);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(alertView);
                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                joinClass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JoinClassRequest();
                    }
                });
            }
        });

        return view;
    }

    private void showUI() {
        gridLayout.setVisibility(View.VISIBLE);
        gridLayout.addView(classroom);
        gridLayout.addView(professors);
        gridLayout.addView(reports);
        floatingActionButton.setVisibility(View.GONE);
        ClassCode = StudentInfo.get(KEY_CLASSCODE).toString();
        //Toast.makeText(getContext(), ClassCode, Toast.LENGTH_SHORT).show();
    }

    public void JoinClassRequest() {

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
            com.google.firebase.database.Query query = FirebaseDatabase.getInstance().getReference().child("Class").orderByChild("ClassCode");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String ClassName = null;
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        ClassModel classModel = postSnapshot.getValue(ClassModel.class);
                        if (classModel.getClassCode().equals(ClassCode)) {
                            count++;
                            ClassName = classModel.getClassName();
                            //Toast.makeText(getContext(), "Matching code", Toast.LENGTH_SHORT).show();
                        } else {

                            //Toast.makeText(getContext(), "Not Matching code ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (count == 1) {

                        progressBar.setVisibility(View.VISIBLE);


                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String requestTime = simpleDateFormat.format(new Date());

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("StudentClassJoinRequest");
                        Map<String, Object> JoinRequest = new HashMap<>();
                        JoinRequest.put("ClassCode", ClassCode);
                        JoinRequest.put("ClassName", ClassName);
                        JoinRequest.put("StudentID", StudentId);
                        if (Student.getPhotoUrl() != null) {
                            JoinRequest.put("ProfileUrl", Student.getPhotoUrl().toString());
                        }
                        JoinRequest.put("StudentName", Student.getDisplayName());
                        JoinRequest.put("StudentEmail", Student.getEmail());
                        JoinRequest.put("CollegeID", StudentInfo.get("CollegeID"));
                        JoinRequest.put("StudentRollNo", StudentInfo.get("StudentRollNo"));
                        JoinRequest.put("RequestTimestamp", requestTime);

                        databaseReference.child(ClassCode).child(StudentId).setValue(JoinRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Log.i("JoinRequest", JoinRequest.toString());
                                    Toast.makeText(getContext(), "Request Send Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to request\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();

                        //Toast.makeText(getContext(), "Parent Match Found"+newProfessorModel.getRegCode(), Toast.LENGTH_SHORT).show();
                    } else {
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
                    //Toast.makeText(getContext(), String.valueOf(subject.length()), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), items.getValue().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    public void showReport() {
        Log.d(TAG, "showReport: start ");
        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("LectureCount")
                .document("Months")
                .collection(MonthYear)
                .document(ClassCode)
                .collection(Subject);


        Query query = collectionReference.
                orderBy("PresentLectCount", com.google.firebase.firestore.Query.Direction.DESCENDING);


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: task successful" + task.getResult());
                    if (task.getResult().size() > 0) {

                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Log.d(" TAG DATA FOUND: ", document.getId() + " => " + document.getData());
                            if (document.getData() != null) {
                                Log.d(TAG, "onComplete: document is not null");
                                Map<String, Object> StudentAtten = document.getData();
                                String documentId = document.getId();
                                StudentAtten.get("StudentID");
                                if (StudentId.equals(documentId)) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("Subject", Subject);
                                    bundle.putString("ClassCode", ClassCode);
                                    bundle.putString("MonthYear", MonthYear);
                                    bundle.putString("StudentID", StudentId);
                                    bundle.putString("ClassName", StudentInfo.get("ClassName").toString());
                                    bundle.putString("StudentName", StudentInfo.get(KEY_USERNAME).toString());
                                    bundle.putString("TotalAbsent", StudentAtten.get("AbsentLectCount").toString());
                                    bundle.putString("TotalPresent", StudentAtten.get("PresentLectCount").toString());
                                    bundle.putString("TotalLectures", StudentAtten.get("TotalLectCount").toString());

                                    StudentReports studentReports = new StudentReports();
                                    studentReports.setArguments(bundle);

                                    Log.d("attenat", bundle.toString());

                                    getParentFragmentManager().beginTransaction().replace(R.id.StudentMainFrameLayout, studentReports).addToBackStack(null).commit();
                                } else {
                                    Log.d(TAG, "ID Not Matched \n" + StudentId + "\n" + StudentAtten.get("StudentId"));
                                }
                            } else {
                                Log.d(TAG, "onComplete: Document is null");
                            }

                        }
                    } else {
                        Toast.makeText(getContext(), "Data not found!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: Task result is null");
                    }

                } else {
                    Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}