package tk.jabtk.attentrack.professor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassAdapter;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassModel;

public class SelectAttendance extends Fragment {

    private View alertView, view;
    private AlertDialog dialog;
    private String ProfessorID, startPrevious;
    private TextInputLayout selectSubject, addSubject, date;
    private AutoCompleteTextView dropdown_menu;
    private CollectionReference DayDB;
    private MaterialButton startBtn;
    private FloatingActionButton addSubjectBtn;
    private ClassAdapter classAdapter;
    private ImageView cancel_button;
    private DatabaseReference subjectRef;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> subjectsList;
    private int pressCount = 0;
    private RecyclerView recyclerView;
    private ChipGroup chipGroup;
    int chipCount;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_select_attendance, container, false);
        getActivity().setTitle("Attendance");
        relativeLayout = view.findViewById(R.id.classListView);
        progressBar = view.findViewById(R.id.progressbar);

        //get Current user
        FirebaseUser professor = FirebaseAuth.getInstance().getCurrentUser();
        assert professor != null;
        ProfessorID = professor.getUid();

        return view;
    }


    public void setUpRecyclerView() {

        Query ProDB = FirebaseDatabase.getInstance().getReference("ProfessorClassrooms").child(ProfessorID).orderByChild("ClassName");

        FirebaseRecyclerOptions<ClassModel> options = new FirebaseRecyclerOptions.Builder<ClassModel>()
                .setQuery(ProDB, ClassModel.class)
                .build();


        ProDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView = view.findViewById(R.id.classRecycler);
                    relativeLayout.setVisibility(View.VISIBLE);
                    classAdapter = new ClassAdapter(options);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(classAdapter);
                    classAdapter.startListening();
                    classAdapter.setItemClickListener(new ClassAdapter.OnItemClickListener() {
                        @SuppressLint("SimpleDateFormat")
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            Calendar c = Calendar.getInstance();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat;
                            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            ClassModel classModel = Snapshot.getValue(ClassModel.class);

                            assert classModel != null;
                            String classCode = classModel.getClassCode();
                            String Date = simpleDateFormat.format(new Date());
                            String Month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                            String Year = new SimpleDateFormat("yyyy").format(c.getTime());
                            Bundle bundle = new Bundle();

                            if (classModel.getClassCode() != null) {
                                Toast.makeText(getContext(), classModel.getClassName(), Toast.LENGTH_SHORT).show();
                                bundle.putString("ClassName", classModel.getClassName());
                                bundle.putString("ClassCode", classModel.getClassCode());
                            }

                            subjectRef = FirebaseDatabase.getInstance().getReference("Subjects").child(classModel.getClassCode()).child(ProfessorID);
                            alertView = getLayoutInflater().inflate(R.layout.select_time_subject, null);

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setView(alertView);
                            dialog = builder.create();
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.show();

                            date = alertView.findViewById(R.id.date);
                            date.setEnabled(false);
                            Objects.requireNonNull(date.getEditText()).setText(Date);

                            chipGroup = alertView.findViewById(R.id.chip_group);
                            assert classCode != null;
                            DayDB = FirebaseFirestore.getInstance()
                                    .collection("LectureCount")
                                    .document("DayWise")
                                    .collection(Month + Year)
                                    .document(Date)
                                    .collection(classCode);

                            Chip chip0 = (Chip) chipGroup.getChildAt(0);
                            Chip chip1 = (Chip) chipGroup.getChildAt(1);
                            Chip chip2 = (Chip) chipGroup.getChildAt(2);
                            Chip chip3 = (Chip) chipGroup.getChildAt(3);
                            Chip chip4 = (Chip) chipGroup.getChildAt(4);
                            Chip chip5 = (Chip) chipGroup.getChildAt(5);
                            Chip chip6 = (Chip) chipGroup.getChildAt(6);
                            Chip chip7 = (Chip) chipGroup.getChildAt(7);
                            Chip chip8 = (Chip) chipGroup.getChildAt(8);

                            removeChipsFrom(chip0.getText().toString());
                            removeChipsFrom(chip1.getText().toString());
                            removeChipsFrom(chip2.getText().toString());
                            removeChipsFrom(chip3.getText().toString());
                            removeChipsFrom(chip4.getText().toString());
                            removeChipsFrom(chip5.getText().toString());
                            removeChipsFrom(chip6.getText().toString());
                            removeChipsFrom(chip7.getText().toString());
                            removeChipsFrom(chip8.getText().toString());

                            cancel_button = alertView.findViewById(R.id.cancel_button);
                            selectSubject = alertView.findViewById(R.id.selectSubject);
                            subjectsList = new ArrayList<>();
                            adapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, subjectsList);
                            dropdown_menu = alertView.findViewById(R.id.dropdown_menu);
                            setSubjectsList();
                            dropdown_menu.setAdapter(adapter);
                            startBtn = alertView.findViewById(R.id.startAtten);
                            startBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean error = false;
                                    String attenDate;
                                    String sub = "";
                                    chipCount = LectureCount();
                                    Editable selectedValue = ((AutoCompleteTextView) selectSubject.getEditText()).getText();
                                    //Toast.makeText(getContext(), selectedValue, Toast.LENGTH_SHORT).show();
                                    if (selectedValue.length() == 0) {
                                        //Toast.makeText(getContext(), "Not selected", Toast.LENGTH_SHORT).show();
                                        selectSubject.setError("Subject is required!");
                                        error = true;
                                    } else {
                                        sub = selectedValue.toString();
                                    }
                                    if (chipCount == 0) {
                                        Toast.makeText(getContext(), "Min 1 lecture is required!", Toast.LENGTH_SHORT).show();
                                    } else if (chipCount > 2) {
                                        Toast.makeText(getContext(), "Max 2 lecture at a time!", Toast.LENGTH_SHORT).show();
                                    } else if (!error) {
                                        String LectTime1 = null, LectTime2 = null;
                                        if (chipCount == 2) {
                                            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                                                Chip LectChip1 = (Chip) chipGroup.getChildAt(i);
                                                Chip LectChip2 = (Chip) chipGroup.getChildAt(i + 1);
                                                if (LectChip1 != null && LectChip2 != null) {
                                                    if (LectChip1.isChecked() && LectChip2.isChecked()) {
                                                        LectTime1 = LectChip1.getText().toString();
                                                        LectTime2 = LectChip2.getText().toString();

                                                        Log.i("inside if ", i + " chip1 = " + chip1.getText().toString());
                                                        Log.i("inside if ", i + 1 + " chip2 = " + chip2.getText().toString());
                                                    }
                                                }
                                            }
                                            if (LectTime2 == null) {
                                                Log.i("Tag ", "Continuous 2 Lectures are allowed");
                                                Toast.makeText(getContext(), "Continuous 2 Lectures are allowed!", Toast.LENGTH_SHORT).show();
                                            }
                                            //Toast.makeText(getContext(), "LectTime1 " + LectTime1 + " \n" + "LectTime2 " + LectTime2, Toast.LENGTH_SHORT).show();
                                        } else {
                                            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                                                Chip chip1 = (Chip) chipGroup.getChildAt(i);
                                                if (chip1.isChecked()) {
                                                    LectTime1 = chip1.getText().toString();
                                                }
                                            }
                                            Toast.makeText(getContext(), "LectTime " + LectTime1, Toast.LENGTH_SHORT).show();
                                        }

                                        if (LectTime1 != null) {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("ClassCode", classCode);
                                            bundle.putString("Subject", sub);
                                            bundle.putString("LectureCount", String.valueOf(chipCount));
                                            bundle.putString("Date", Date);
                                            bundle.putString("LectTime1", LectTime1);
                                            if (LectTime2 != null) {
                                                bundle.putString("LectTime2", LectTime2);
                                            }
                                            if (startPrevious != null) {
                                                bundle.putString("startPrevious", startPrevious);
                                            }
                                            StartAttendance startAttendance = new StartAttendance();
                                            startAttendance.setArguments(bundle);
                                            getParentFragmentManager().beginTransaction()
                                                    .replace(R.id.professorMainFrameLayout, startAttendance, "StartAttendance")
                                                    .addToBackStack(null)
                                                    .commit();
                                            Log.i("Count", String.valueOf(chipCount));
                                            Log.i("AttenDate", Date);
                                            Log.i("ClassCode", classCode);
                                            Log.i("Subject", sub);
                                            dialog.dismiss();
                                        }
                                    }
                                }
                            });

                            addSubjectBtn = alertView.findViewById(R.id.addSubjectBtn);
                            addSubjectBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                            addSubject = alertView.findViewById(R.id.addSubject);
                            addSubjectBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    addSubjectBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                                    selectSubject.setError(null);
                                    addSubject.setError(null);
                                    selectSubject.setVisibility(View.GONE);
                                    startBtn.setVisibility(View.INVISIBLE);
                                    cancel_button.setVisibility(View.VISIBLE);
                                    addSubject.setVisibility(View.VISIBLE);
                                    addSubject.requestFocus();
                                    String subjectName = addSubject.getEditText().getText().toString();
                                    if (subjectName.isEmpty()) {
                                        pressCount++;
                                        if (pressCount > 1) {
                                            Log.i("PressCount", String.valueOf(pressCount));
                                            addSubject.setError("Subject name is required!");
                                            addSubject.requestFocus();
                                        }
                                    } else {
                                        addSubject.setError(null);

                                        subjectRef.child(subjectName).setValue(subjectName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                subjectsList.clear();
                                                setSubjectsList();
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(getContext(), "Subject Added", Toast.LENGTH_SHORT).show();
                                                addSubject.setVisibility(View.GONE);
                                                addSubject.getEditText().setText("");
                                                dropdown_menu.clearListSelection();
                                                selectSubject.setVisibility(View.VISIBLE);
                                                onCancelPressed();
                                            }
                                        });
                                    }
                                }
                            });

                            cancel_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onCancelPressed();
                                }
                            });
                        }

                        @Override
                        public void onLongItemClick(DataSnapshot Snapshot, int position) {

                        }
                    });
                } else {
                    /////snapshot not found
                    progressBar.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.not_found_text);
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeChipsFrom(String LectTime) {
        DayDB.document(LectTime).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Map<String, Object> map = task.getResult().getData();
                //temp.document(document.getId()).set(document.getData());
                if (map != null) {
                    if (map.get("Time") != null) {
                        String LectTimeData = map.get("Time").toString();
                        if (LectTime.equals(LectTimeData)) {
                            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                                Chip chip = (Chip) chipGroup.getChildAt(i);
                                String chipName = chip.getText().toString();
                                if (chipName.equals(LectTime)) {
                                    chipGroup.removeViewAt(i);
                                    Log.i("Removed at", i + " " + LectTime);
                                }
                            }
                        }
                    } else {
                        startPrevious = "true";
                    }
                } else {
                    Log.i("Removed Map is null at", " " + LectTime);
                }
            }
        });
    }

    public void onCancelPressed() {
        hideKeyboard(alertView);
        addSubjectBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
        pressCount = 0;
        selectSubject.setError(null);
        addSubject.setError(null);
        cancel_button.setVisibility(View.GONE);
        addSubject.setVisibility(View.GONE);
        selectSubject.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
    }

    ///function for hide Keyboard ///
    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    ///retrieve subject list
    public void setSubjectsList() {
        subjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot items : snapshot.getChildren()) {
                    subjectsList.add(items.getValue().toString());
                    //Toast.makeText(getContext(), items.getValue().toString(), Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public int LectureCount() {
        int lectCount = 0;
        Chip lecture1 = alertView.findViewById(R.id.Lecture1);
        Chip lecture2 = alertView.findViewById(R.id.Lecture2);
        Chip lecture3 = alertView.findViewById(R.id.Lecture3);
        Chip lecture4 = alertView.findViewById(R.id.Lecture4);
        Chip lecture5 = alertView.findViewById(R.id.Lecture5);
        Chip lecture6 = alertView.findViewById(R.id.Lecture6);
        Chip lecture7 = alertView.findViewById(R.id.Lecture7);
        Chip lecture8 = alertView.findViewById(R.id.Lecture8);
        Chip lecture9 = alertView.findViewById(R.id.Lecture9);

        if (lecture1 != null) {
            if (lecture1.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture2 != null) {
            if (lecture2.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture3 != null) {
            if (lecture3.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture4 != null) {
            if (lecture4.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture5 != null) {
            if (lecture5.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture6 != null) {
            if (lecture6.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture7 != null) {
            if (lecture7.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture8 != null) {
            if (lecture8.isChecked()) {
                lectCount = lectCount + 1;
            }
        }
        if (lecture9 != null) {
            if (lecture9.isChecked()) {
                lectCount = lectCount + 1;
            }
        }

        return lectCount;
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