package tk.jabtk.attentrack.professor;

import android.app.AlertDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tk.jabtk.attentrack.R;

import static android.content.ContentValues.TAG;

public class StudentInfo extends Fragment {
    private String Subject, MonthYear, ClassCode, StudentID, TotalAbsent, TotalPresent, TotalLectures, StudentNameBundle;
    private LinearLayout HeaderLayout, InfoLayout, PieLayout, LectureList;
    private ImageView studentProfileImage;
    private TextView StudentName, StudentRollNumber, StudentEmail, AcceptedOn, ClassNameText, ClassCodeText, CollegeIDText;
    private ProgressBar progressBar;
    private String isAdmin;
    private PieChart pieChart;
    boolean isPieChanged = false;
    private DatabaseReference dateRef, LectTimeRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_student_info, container, false);
        HeaderLayout = view.findViewById(R.id.headerLayout);
        InfoLayout = view.findViewById(R.id.InfoLayout);
        progressBar = view.findViewById(R.id.progress_bar);
        pieChart = view.findViewById(R.id.pieChart);
        progressBar.setVisibility(View.VISIBLE);

        studentProfileImage = view.findViewById(R.id.studentProfileImage);
        StudentName = view.findViewById(R.id.StudentName);
        StudentRollNumber = view.findViewById(R.id.StudentRollNo);
        StudentEmail = view.findViewById(R.id.StudentEmail);
        ClassCodeText = view.findViewById(R.id.ClassCode);
        CollegeIDText = view.findViewById(R.id.CollegeID);
        ClassNameText = view.findViewById(R.id.studentClass);
        AcceptedOn = view.findViewById(R.id.AcceptedOn);

        LectureList = (LinearLayout) view.findViewById(R.id.lectureList);
        PieLayout = (LinearLayout) view.findViewById(R.id.pieLayout);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ClassCode = bundle.getString("ClassCode");
            Subject = bundle.getString("Subject");
            MonthYear = bundle.getString("MonthYear");
            StudentID = bundle.getString("StudentID");
            TotalAbsent = bundle.getString("TotalAbsent");
            TotalPresent = bundle.getString("TotalPresent");
            TotalLectures = bundle.getString("TotalLectures");
            StudentNameBundle = bundle.getString("StudentName");
            isAdmin = bundle.getString("isAdmin");

            dateRef = FirebaseDatabase.getInstance().getReference("LectureCount").child("DayWise").child(MonthYear);
            LectTimeRef = FirebaseDatabase.getInstance().getReference().child("LectureCount").child("LectTime");
            Log.d("bundle", bundle.toString());

            getActivity().setTitle(StudentNameBundle + " " + Subject + " (" + MonthYear + ")");
            /////end of pie chart section
            getDate();
            /// End of date ref
        }


        return view;
    }


    public void getDate() {
        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot LectDate : snapshot.getChildren()) {
                    //Toast.makeText(getContext(), LectDate.getValue().toString(), Toast.LENGTH_SHORT).show();
                    Log.d("DateTimeLog", "got Date" + LectDate.getValue() + " " + index);
                    getTime(String.valueOf(LectDate.getValue()));
                    index++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "DA404" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getTime(String Date) {
        LectTimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot LectTime : snapshot.getChildren()) {
                    //Toast.makeText(getContext(), LectTime.getValue().toString(), Toast.LENGTH_SHORT).show();
                    Log.d("DateTimeLog", "got time" + LectTime.getValue() + " " + index);
                    addViewQuery(Date, String.valueOf(LectTime.getValue()));
                    index++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "TI404" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addViewQuery(String LectDate, String LectTime) {
        Log.d(TAG, "addViewQuery: inside");
        Query queryOld = FirebaseFirestore.getInstance().collection("LectureCount")
                .document("DayWise")
                .collection(MonthYear)
                .document(String.valueOf(LectDate))
                .collection(ClassCode)
                .document(String.valueOf(LectTime))
                .collection(Subject);

        queryOld.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(" StudentInfo ", "" + task.getResult().isEmpty());
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        String docId = document.getId();
                        if (StudentID.equals(docId)) {
                            Log.d(" StudentInfo \n", document.getId() + " => " + document.getData());
                            Query studentInformation = FirebaseFirestore.getInstance()
                                    .collection("ClassList")
                                    .document(ClassCode)
                                    .collection("Students");
                            studentInformation.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for (QueryDocumentSnapshot studentInfo : Objects.requireNonNull(task.getResult())) {
                                        Map<String, Object> Student = studentInfo.getData();

                                        //Log.d("QUERY : \n", document.getId() + " => \n" + model.getStudentId());

                                        String documentId = studentInfo.getId();
                                        if (StudentID.equals(documentId)) {
                                            Log.d("QUERY : ID Matched ", "\n" + documentId + "\n" + StudentID);
                                            Uri uri = Uri.parse(String.valueOf(Student.get("ProfileUrl")));

                                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) Student.get("AcceptedOn");
                                            java.util.Date time = timestamp.toDate();
                                            SimpleDateFormat pre = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
                                            //Hear Define your returning date format

                                            StudentName.setText(Student.get("StudentName").toString());
                                            StudentRollNumber.setText(Student.get("StudentRollNo").toString());
                                            StudentEmail.setText(Student.get("StudentEmail").toString());
                                            ClassCodeText.setText(Student.get("ClassCode").toString());
                                            CollegeIDText.setText(Student.get("CollegeID").toString());
                                            ClassNameText.setText(Student.get("ClassName").toString());
                                            AcceptedOn.setText(pre.format(time));
                                            if (uri != null) {
                                                //Uri uri = Uri.parse(model.getProfileUrl());
                                                Picasso.get().load(uri).placeholder(R.drawable.ic_student).resize(200, 200).centerCrop().into(studentProfileImage);
                                                Log.i("QUERY : ProfileUri", "Found");
                                            }
                                            ////visual changes show UI
                                            HeaderLayout.setVisibility(View.VISIBLE);
                                            InfoLayout.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);

                                            if (!isPieChanged) {
                                                int absent = Integer.parseInt(TotalAbsent), present = Integer.parseInt(TotalPresent);
                                                showPie(absent, present);
                                            }
                                            Map<String, Object> StudentAtten = document.getData();
                                            //Log.d("isPresentLog",  Student.get("StudentName")+ " isPresent:" + );

                                            boolean isPresent = (boolean) StudentAtten.get("isPresent");

                                            addView(LectDate, LectTime, isPresent);

                                        } else {
                                            //Log.d("QUERY : ID not Matched ", document.getId() + " => " + Student);
                                            Log.d("QUERY : ID not Matched ", "\n" + documentId + "\n" + StudentID);
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "onComplete: Locha");
                        }
                    }
                } else {
                    Log.d(TAG, "onComplete: " + task.getException());
                }
            }
        });
    }

    public void changePie(int absent, int present) {
        showPie(absent, present);
    }

    ////method for pie chart
    public void showPie(int absent, int present) {


        Log.d("isPresentLog inside pie", TotalAbsent + "+" + TotalPresent + "=" + TotalLectures);

        pieChart.setCenterText("Total Lectures\n" + TotalLectures);
        pieChart.setCenterTextColor(Color.parseColor("#1aa6b7"));
        pieChart.setCenterTextSize(16f);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "Lectures";

        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("Absent", absent);
        dataMap.put("Present", present);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#8FBB68"));
        colors.add(Color.parseColor("#f64461"));

        for (String data : dataMap.keySet()) {
            pieEntries.add(new PieEntry(dataMap.get(data), data));
            Log.d("isPresentLog", "" + dataMap.get(data));
        }
        PieLayout.setVisibility(View.VISIBLE);
        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries, label);
        //setting text size of the value

        pieDataSet.setValueTextSize(18);
        pieDataSet.setValueTextColor(Color.parseColor("#FFFFFF"));
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //using percentage as values instead of amount
        pieChart.setUsePercentValues(true);

        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);

        //showing the value of the entries, default true if not set
        pieData.setDrawValues(true);

        //remove the description label on the lower left corner, default true if not set
        pieChart.getDescription().setEnabled(false);

        //adding friction when rotating the pie chart
        pieChart.setDragDecelerationFrictionCoef(0.9f);

        //setting the first entry start from right hand side, default starting from top
        pieChart.setRotationAngle(0);

        ///setting data for pie chart
        pieChart.setData(pieData);
        pieChart.invalidate();
        pieChart.animateXY(500, 500);
    }


    ////method for adding views to
    public void addView(String lectDate, String lectTime, boolean isPresent) {
        View lineView = getLayoutInflater().inflate(R.layout.lect_info_student, null, false);

        TextView LectDate, LectTime, isPresentText;
        LectDate = lineView.findViewById(R.id.LectDate);
        LectTime = lineView.findViewById(R.id.LectTime);
        isPresentText = lineView.findViewById(R.id.isPresent);
        ImageButton imageButton = lineView.findViewById(R.id.isPresentEdit);

        boolean checkAdmin = Boolean.valueOf(isAdmin);
        if (checkAdmin) {
            imageButton.setVisibility(View.VISIBLE);
        }

        LectDate.setText(lectDate);
        LectTime.setText(lectTime);
        if (isPresent) {
            isPresentText.setText("Present");
            isPresentText.setTextColor(Color.parseColor("#8FBB68"));

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editAtten(lectDate, lectTime, "Mark as Absent", true);
                }
            });
        } else {
            isPresentText.setText("Absent");
            isPresentText.setTextColor(Color.parseColor("#f64461"));

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editAtten(lectDate, lectTime, "Mark as Present", false);
                }
            });
        }

        LectureList.addView(lineView);
    }

    public void editAtten(String dateString, String timeString, String isPresentText, boolean isPresent) {
        View alertView = getLayoutInflater().inflate(R.layout.dropdown_item, null);
        AlertDialog dialog;
        TextView textView = alertView.findViewById(R.id.textView);
        textView.setText(isPresentText);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(alertView);
        dialog = builder.create();
        //dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference MonthDB = FirebaseFirestore.getInstance()
                        .collection("LectureCount")
                        .document("Months")
                        .collection(MonthYear)
                        .document(ClassCode)
                        .collection(Subject)
                        .document(StudentID);

                DocumentReference DayDB1 = FirebaseFirestore.getInstance()
                        .collection("LectureCount")
                        .document("DayWise")
                        .collection(MonthYear)
                        .document(dateString)
                        .collection(ClassCode)
                        .document(timeString)
                        .collection(Subject)
                        .document(StudentID);
                int TotalLect = Integer.parseInt(TotalLectures);


                MonthDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Map<String, Object> attenData = new HashMap<>();
                        attenData = snapshot.getData();
                        assert attenData != null;

                        String absentString = String.valueOf(attenData.get("AbsentLectCount"));
                        String presentString = String.valueOf(attenData.get("PresentLectCount"));

                        int absentLect = Integer.parseInt(absentString);
                        int presentLect = Integer.parseInt(presentString);

                        if (isPresent) {
                            ///MonthWise Data
                           /* absentLect = Integer.parseInt(TotalAbsent) + 1;
                            presentLect = Integer.parseInt(TotalPresent) - 1;*/

                            Map<String, Object> presentData = new HashMap<>();

                            presentData.put("AbsentLectCount", absentLect + 1);
                            presentData.put("PresentLectCount", presentLect - 1);
                            MonthDB.update(presentData);

                            ////DayWise
                            Map<String, Object> attend = new HashMap<>();
                            attend.put("isPresent", false);
                            DayDB1.update(attend);

                            Toast.makeText(getContext(), "Absent Marked!", Toast.LENGTH_SHORT).show();
                            showPie(absentLect + 1, presentLect - 1);
                            isPieChanged = true;
                        } else {
                            ///MonthWise Data
                           /* absentLect = Integer.parseInt(TotalAbsent) - 1;
                            presentLect = Integer.parseInt(TotalPresent) + 1;*/

                            Map<String, Object> presentData = new HashMap<>();
                            presentData.put("AbsentLectCount", absentLect - 1);
                            presentData.put("PresentLectCount", presentLect + 1);

                            MonthDB.update(presentData);

                            ////DayWise
                            Map<String, Object> attend = new HashMap<>();
                            attend.put("isPresent", true);
                            DayDB1.update(attend);

                            Toast.makeText(getContext(), "Present Marked!", Toast.LENGTH_SHORT).show();
                            showPie(absentLect - 1, presentLect + 1);
                            isPieChanged = true;
                        }
                    }
                });
                dialog.dismiss();
                getFragmentManager().beginTransaction().detach(StudentInfo.this).attach(StudentInfo.this).commit();

            }
        });
    }
}