package tk.jabtk.attentrack.professor;

import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageStudents.StudentModel;
import tk.jabtk.attentrack.professor.adapters.StudentAdapterAtten;
import tk.jabtk.attentrack.professor.model.LectureModel;


public class StartAttendance extends Fragment implements StudentAdapterAtten.StudentListener {
    private StudentAdapterAtten studentAdapter;
    private String ClassCode, Subject, LectTime1, LectTime2;
    private int LectureCount;
    private String StringDate;
    private View view;
    private DocumentReference DayDB1, DayDB2, DayAtten1, DayAtten2, DayDate1, DayDate2;
    private RecyclerView recyclerView;
    private String Month, Year;
    private CollectionReference temp;
    private TextView stNotFound, swipeText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_start_attendance, container, false);
        recyclerView = view.findViewById(R.id.studentIdCard);
        stNotFound = view.findViewById(R.id.stnot);
        swipeText = view.findViewById(R.id.swipeText);

        DatabaseReference monthRef = FirebaseDatabase.getInstance().getReference("LectureCount").child("Months");
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ClassCode = bundle.getString("ClassCode");
            Subject = bundle.getString("Subject");
            LectureCount = Integer.parseInt(bundle.getString("LectureCount"));
            StringDate = bundle.getString("Date");
            LectTime1 = bundle.getString("LectTime1");
            if (bundle.getString("LectTime2") != null) {
                LectTime2 = bundle.getString("LectTime2");
            }

            Log.i("Bundle", ClassCode + " " + Subject + " " + LectureCount + " " + LectTime1 + " " + LectTime2 + " " + StringDate);

            Calendar c = Calendar.getInstance();
            Month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            Year = new SimpleDateFormat("yyyy").format(c.getTime());

            monthRef.child(Month + Year).setValue(Month + Year);
            DatabaseReference dateRef = FirebaseDatabase.getInstance().getReference("LectureCount").child("DayWise").child(Month + Year);
            dateRef.child(StringDate).setValue(StringDate);

            DatabaseReference LectTimeRef = FirebaseDatabase.getInstance().getReference().child("LectureCount").child("LectTime");
            LectTimeRef.child(LectTime1).setValue(LectTime1);
            if (LectTime2 != null) {
                LectTimeRef.child(LectTime2).setValue(LectTime2);
            }
            Log.i(" TAG Month Year", Month + Year);
        }

        temp = FirebaseFirestore.getInstance()
                .collection("TempAttenData")
                .document(ClassCode)
                .collection(StringDate)
                .document(Subject)
                .collection("Students");
        Query db = FirebaseFirestore.getInstance()
                .collection("ClassList")
                .document(ClassCode)
                .collection("Students")
                .orderBy("StudentRollNo");

        db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        if (bundle.getString("startPrevious") != "true") {
                            temp.document(document.getId()).set(document.getData());
                            Log.d(" TAG DATA FOUND: ", document.getId() + " => " + document.getData());
                        }
                    }
                    // Log.d(" TAG DATA FOUND: ", ""+task.getResult().getDocuments().toString());
                } else {
                    //  stNotFound.setVisibility(View.VISIBLE);
                    Log.d("Not Found", "Error getting documents: ", task.getException());
                }

            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("tag", "keyCode: " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (studentAdapter.getItemCount() == 0) {
                        return false;
                    }
                    Toast.makeText(getContext(), "Please finish attendance!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        return view;
    }


    public void setupStudents() {
        Query tempQuery = FirebaseFirestore.getInstance()
                .collection("TempAttenData")
                .document(ClassCode)
                .collection(StringDate)
                .document(Subject)
                .collection("Students")
                .orderBy("StudentRollNo");

        FirestoreRecyclerOptions<StudentModel> options = new FirestoreRecyclerOptions.Builder<StudentModel>()
                .setQuery(tempQuery, StudentModel.class)
                .build();

        studentAdapter = new StudentAdapterAtten(options, this);
        recyclerView.setAdapter(studentAdapter);
        studentAdapter.startListening();
        studentAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                checkAtten();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkAtten();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                //checkEmpty();
                checkAtten();
            }

            void checkAtten() {
                if (studentAdapter.getItemCount() == 0) {
                    TextView attenDone = view.findViewById(R.id.attenDone);
                    ImageView imageView = view.findViewById(R.id.attenDoneImg);
                    attenDone.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);

                    swipeText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);

                } else {
                    TextView attenDone = view.findViewById(R.id.attenDone);
                    ImageView imageView = view.findViewById(R.id.attenDoneImg);
                    attenDone.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    swipeText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            StudentAdapterAtten.StudentHolder studentHolder;
            if (direction == ItemTouchHelper.LEFT) {
                studentHolder = (StudentAdapterAtten.StudentHolder) viewHolder;
                studentHolder.isAbsent();

            } else {
                studentHolder = (StudentAdapterAtten.StudentHolder) viewHolder;
                studentHolder.isPresent();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightActionIcon(R.drawable.ic_check_circle_24)
                    .addSwipeLeftLabel("Absent")
                    .addSwipeLeftActionIcon(R.drawable.ic_cancel_24)
                    .addSwipeRightLabel("Present")
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    @Override
    public void handleAbsent(DocumentSnapshot snapshot) {
        StudentModel studentModel = snapshot.toObject(StudentModel.class);
        assert studentModel != null;

        Map<String, Object> absentData = new HashMap<>();
        absentData.put("TotalLectCount", LectureCount);
        absentData.put("AbsentLectCount", LectureCount);
        absentData.put("PresentLectCount", 0);
        absentData.put("StudentID", studentModel.getStudentID());
        //absentData.put("PresentLectCount", LectureCount);
        Log.i("TAG", absentData.toString());

        ///DayWise Data
        if (LectTime1 != null) {
            CollectionReference db = FirebaseFirestore.getInstance()
                    .collection("LectureCount")
                    .document("DayWise")
                    .collection(Month + Year);

            DayDate1 = db.document(StringDate);
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("Date", StringDate);
            DayDate1.set(dateMap);


            DayAtten1 = db.document(StringDate).collection(ClassCode).document(LectTime1);


            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("Attendance", "doneAbsent");
            dataMap.put("Time", LectTime1);
            DayAtten1.set(dataMap);


            Map<String, Object> attend = new HashMap<>();
            attend.put("StudentName", studentModel.getStudentName());
            attend.put("StudentID", studentModel.getStudentID());
            attend.put("StudentRollNo", studentModel.getStudentRollNo());
            attend.put("StudentEmail", studentModel.getStudentEmail());
            attend.put("isPresent", false);

            DayDB1 = FirebaseFirestore.getInstance()
                    .collection("LectureCount")
                    .document("DayWise")
                    .collection(Month + Year)
                    .document(StringDate)
                    .collection(ClassCode)
                    .document(LectTime1)
                    .collection(Subject)
                    .document(studentModel.getStudentID());
            DayDB1.set(attend);

            if (LectTime2 != null) {
                DayDB2 = FirebaseFirestore.getInstance()
                        .collection("LectureCount")
                        .document("DayWise")
                        .collection(Month + Year)
                        .document(StringDate)
                        .collection(ClassCode)
                        .document(LectTime2)
                        .collection(Subject)
                        .document(studentModel.getStudentID());
                DayDB2.set(attend);

                DayAtten2 = FirebaseFirestore.getInstance()
                        .collection("LectureCount")
                        .document("DayWise")
                        .collection(Month + Year)
                        .document(StringDate)
                        .collection(ClassCode)
                        .document(LectTime2);


                Map<String, Object> dataMap2 = new HashMap<>();
                dataMap2.put("Attendance", "doneAbsent");
                dataMap2.put("Time", LectTime2);
                DayAtten2.set(dataMap2);


            }
        }


        ///MonthWise Data
        DocumentReference MonthDB = FirebaseFirestore.getInstance()
                .collection("LectureCount")
                .document("Months")
                .collection(Month + Year)
                .document(ClassCode)
                .collection(Subject)
                .document(studentModel.getStudentID());

        MonthDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    int TotalLectCount, AbsentLectCount, PresentLectCount;
                    LectureModel Counts = snapshot.toObject(LectureModel.class);
                    TotalLectCount = Counts.getTotalLectCount();
                    AbsentLectCount = Counts.getAbsentLectCount();
                    PresentLectCount = Counts.getPresentLectCount();

                    absentData.put("TotalLectCount", TotalLectCount + LectureCount);
                    absentData.put("AbsentLectCount", AbsentLectCount + LectureCount);
                    absentData.put("PresentLectCount", PresentLectCount);
                    absentData.put("StudentID", studentModel.getStudentID());
                    Log.i("TAG Absent", absentData.toString());
                }
                MonthDB.set(absentData);
            }
        });

        final DocumentReference tempRef = snapshot.getReference();
        tempRef.delete();
        Log.i("TagDeleted ", "Deleted");

        Snackbar.make(recyclerView, studentModel.getStudentRollNo() + " " + studentModel.getStudentName() + " is Absent", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        temp.document(snapshot.getId()).set(snapshot.getData());
                        MonthDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {

                                int TotalLectCount, AbsentLectCount, PresentLectCount;
                                LectureModel Counts = snapshot.toObject(LectureModel.class);
                                TotalLectCount = Counts.getTotalLectCount();
                                AbsentLectCount = Counts.getAbsentLectCount();
                                PresentLectCount = Counts.getPresentLectCount();

                                absentData.put("TotalLectCount", TotalLectCount - LectureCount);
                                absentData.put("AbsentLectCount", AbsentLectCount - LectureCount);
                                absentData.put("PresentLectCount", PresentLectCount);
                                MonthDB.set(absentData);

                                ///dayWise atten
                                DayDB1.delete(); ///attendance for DayWise
                                DayAtten1.delete();///attendance for lecture 1 deleted
                                DayDate1.delete(); ////date delete
                                if (LectTime2 != null) {
                                    DayDB2.delete();
                                    DayAtten2.delete();
                                }
                                Log.i("TAG Undo Absent", absentData.toString());
                            }
                        });
                    }
                })
                .show();

    }

    @Override
    public void handlePresent(DocumentSnapshot snapshot) {

        StudentModel studentModel = snapshot.toObject(StudentModel.class);
        assert studentModel != null;

        Map<String, Object> presentData = new HashMap<>();
        presentData.put("TotalLectCount", LectureCount);
        presentData.put("AbsentLectCount", 0);
        presentData.put("PresentLectCount", LectureCount);
        presentData.put("StudentID", studentModel.getStudentID());
        Log.i("TAG FirstCount", presentData.toString());

        ///DayWise Data
        if (LectTime1 != null) {
            CollectionReference db = FirebaseFirestore.getInstance()
                    .collection("LectureCount")
                    .document("DayWise")
                    .collection(Month + Year);

            DayDate2 = db.document(StringDate);
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("Date", StringDate);
            DayDate2.set(dateMap);

            DayAtten1 = FirebaseFirestore.getInstance()
                    .collection("LectureCount")
                    .document("DayWise")
                    .collection(Month + Year)
                    .document(StringDate)
                    .collection(ClassCode)
                    .document(LectTime1);


            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("Attendance", "donePresent");
            dataMap.put("Time", LectTime1);
            DayAtten1.set(dataMap);


            Map<String, Object> attend = new HashMap<>();
            attend.put("StudentName", studentModel.getStudentName());
            attend.put("StudentID", studentModel.getStudentID());
            attend.put("StudentRollNo", studentModel.getStudentRollNo());
            attend.put("StudentEmail", studentModel.getStudentEmail());
            attend.put("isPresent", true);

            DayDB1 = FirebaseFirestore.getInstance()
                    .collection("LectureCount")
                    .document("DayWise")
                    .collection(Month + Year)
                    .document(StringDate)
                    .collection(ClassCode)
                    .document(LectTime1)
                    .collection(Subject)
                    .document(studentModel.getStudentID());
            DayDB1.set(attend);

            if (LectTime2 != null) {
                DayDB2 = FirebaseFirestore.getInstance()
                        .collection("LectureCount")
                        .document("DayWise")
                        .collection(Month + Year)
                        .document(StringDate)
                        .collection(ClassCode)
                        .document(LectTime2)
                        .collection(Subject)
                        .document(studentModel.getStudentID());
                DayDB2.set(attend);


                DayAtten2 = FirebaseFirestore.getInstance()
                        .collection("LectureCount")
                        .document("DayWise")
                        .collection(Month + Year)
                        .document(StringDate)
                        .collection(ClassCode)
                        .document(LectTime2);


                Map<String, Object> dataMap2 = new HashMap<>();
                dataMap2.put("Attendance", "donePresent");
                dataMap2.put("Time", LectTime2);
                DayAtten2.set(dataMap2);

            }
        }


        ///MonthWise Data
        DocumentReference MonthDB = FirebaseFirestore.getInstance()
                .collection("LectureCount")
                .document("Months")
                .collection(Month + Year)
                .document(ClassCode)
                .collection(Subject)
                .document(studentModel.getStudentID());

        MonthDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    int TotalLectCount, AbsentLectCount, PresentLectCount;
                    LectureModel Counts = snapshot.toObject(LectureModel.class);
                    assert Counts != null;
                    TotalLectCount = Counts.getTotalLectCount();
                    AbsentLectCount = Counts.getAbsentLectCount();
                    PresentLectCount = Counts.getPresentLectCount();

                    presentData.put("TotalLectCount", TotalLectCount + LectureCount);
                    presentData.put("AbsentLectCount", AbsentLectCount);
                    presentData.put("PresentLectCount", PresentLectCount + LectureCount);
                    presentData.put("StudentID", studentModel.getStudentID());
                    Log.i("TAG Present", presentData.toString());
                }
                MonthDB.set(presentData);
            }
        });


        final DocumentReference tempRef = snapshot.getReference();
        tempRef.delete();
        Log.i("TagDeleted ", "Deleted");

        Snackbar.make(recyclerView, studentModel.getStudentRollNo() + " " + studentModel.getStudentName() + " is Present", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        temp.document(snapshot.getId()).set(snapshot.getData());
                        MonthDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {

                                int TotalLectCount, AbsentLectCount, PresentLectCount;
                                LectureModel Counts = snapshot.toObject(LectureModel.class);
                                assert Counts != null;
                                TotalLectCount = Counts.getTotalLectCount();
                                AbsentLectCount = Counts.getAbsentLectCount();
                                PresentLectCount = Counts.getPresentLectCount();

                                presentData.put("TotalLectCount", TotalLectCount - LectureCount);
                                presentData.put("AbsentLectCount", AbsentLectCount);
                                presentData.put("PresentLectCount", PresentLectCount - LectureCount);
                                MonthDB.set(presentData);

                                ///dayWise atten
                                DayDB1.delete();  ///attendance for DayWise
                                DayAtten1.delete(); ///attendance for lecture 1 deleted
                                DayDate2.delete();  ////date delete
                                if (LectTime2 != null) {
                                    DayDB2.delete();
                                    DayAtten2.delete();
                                }
                                Log.i("TAG Undo Present", presentData.toString());
                            }
                        });
                    }
                })
                .show();
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
        if (studentAdapter != null)
            studentAdapter.stopListening();
    }
}