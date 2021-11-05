package tk.jabtk.attentrack.admin.ManageClassrooms;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.jabtk.attentrack.R;

public class ManageClass extends Fragment {
    private View alertView, view;
    private Button cancelBtn, createBtn, updateBtn, deleteBtn;
    private TextInputLayout className, updateName;
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private final String KEY_USERNAME = "AdminName";
    private ClassAdapter classAdapter;
    private Map<String, Object> AdminInfo;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Manage Class");


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

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_manage_class, container, false);
        progressBar = view.findViewById(R.id.Progressbar);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertView = getLayoutInflater().inflate(R.layout.add_class_dialog, null);
                className = alertView.findViewById(R.id.className);

                cancelBtn = alertView.findViewById(R.id.cancelBtn);
                createBtn = alertView.findViewById(R.id.createClassBtn);

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

                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createClass();
                    }
                });
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
                    recyclerView = view.findViewById(R.id.recycler);
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    classAdapter = new ClassAdapter(options);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(classAdapter);
                    classAdapter.startListening();
                    classAdapter.setItemClickListener(new ClassAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            ClassModel classModel = Snapshot.getValue(ClassModel.class);
                            if (classModel != null) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Class Information", "You can join class using this information.\nClassName: " + classModel.getClassName() + "\nClassCode: " + classModel.getClassCode());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLongItemClick(DataSnapshot Snapshot, int position) {

                            ClassModel classModel = Snapshot.getValue(ClassModel.class);
                            String parentName = classModel.getClassCode();
                            String className = classModel.getClassName();
                            alertView = getLayoutInflater().inflate(R.layout.update_class_dialog, null);

                            updateName = alertView.findViewById(R.id.className);
                            updateName.getEditText().setText(classModel.getClassName());

                            updateBtn = alertView.findViewById(R.id.updateClassBtn);
                            updateBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateClassName(className, parentName);
                                }
                            });

                            deleteBtn = alertView.findViewById(R.id.deleteBtn);
                            deleteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    classAdapter.deleteItem(position);
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setView(alertView);
                            dialog = builder.create();
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.show();

                        }
                    });

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Create Class!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateClassName(String oldName, String parentName) {
        boolean isError = false;
        String ClassName = updateName.getEditText().getText().toString().trim();
        if (ClassName.isEmpty()) {
            isError = true;
            updateName.setError("Class Name cannot be empty!");
            updateName.requestFocus();
        }
        if (ClassName.length() < 5) {
            isError = true;
            updateName.setError("Name should > 5 characters!!");
            updateName.requestFocus();
        }
        if (ClassName.length() > 10) {
            isError = true;
            updateName.setError("Enter Valid Class Name!");
            updateName.requestFocus();
        }
        if (ClassName.equals(oldName)) {
            isError = true;
            updateName.setError("Class Name is same!");
            updateName.requestFocus();
        }
        if (!(validClass(ClassName))) {
            isError = true;
            className.setError("Pattern Error!");
            className.requestFocus();
        }

        if (!isError) {
            Query query = FirebaseDatabase.getInstance().getReference().child("Class").orderByChild("ClassName").equalTo(ClassName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        count++;
                    }
                    if (count > 0) {
                        Toast.makeText(getContext(), "Already Exist", Toast.LENGTH_SHORT).show();

                    } else {
                        progressBar.setVisibility(View.VISIBLE);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String modifiedOn = simpleDateFormat.format(new Date());

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Class");
                        Map<String, Object> classInfo = new HashMap<>();

                        classInfo.put("ClassName", ClassName);
                        classInfo.put("ModifiedBy", AdminInfo.get(KEY_USERNAME));
                        classInfo.put("ModifiedOn", modifiedOn);

                        databaseReference.child(parentName).updateChildren(classInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Class updated Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to update class\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to update class\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        // Otherwise the dialog will stay open.

    }

    ////method for creating class
    public void createClass() {

        boolean isError = false;
        String ClassName = className.getEditText().getText().toString().trim();
        if (ClassName.isEmpty()) {
            isError = true;
            className.setError("Class Name cannot be empty!");
            className.requestFocus();
        }
        if (ClassName.length() < 5) {
            isError = true;
            className.setError("Name should > 5 characters!");
            className.requestFocus();
        }
        if (ClassName.length() > 10) {
            isError = true;
            className.setError("Enter Valid Class Name!");
            className.requestFocus();
        }
        if (!(validClass(ClassName))) {
            isError = true;
            className.setError("Pattern Error!");
            className.requestFocus();
        }

        if (!isError) {

            Query query = FirebaseDatabase.getInstance().getReference().child("Class").orderByChild("ClassName").equalTo(ClassName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        count++;
                    }
                    if (count > 0) {
                        Toast.makeText(getContext(), "Already Exist", Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        String ClassCode = randomString(8);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String createdOn = simpleDateFormat.format(new Date());
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Class");
                        Map<String, Object> classInfo = new HashMap<>();
                        classInfo.put("ClassName", ClassName);
                        classInfo.put("ClassCode", ClassCode);
                        classInfo.put("CreatedBy", AdminInfo.get(KEY_USERNAME));
                        classInfo.put("CreatedOn", createdOn);
                        databaseReference.child(ClassCode).setValue(classInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext(), "Class Created Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext(), "Failed to Create class\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to Create class\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Otherwise the dialog will stay open.
    }

    public static final Pattern VALID_CLASS_NAME_REGEX = Pattern.compile("^[A-Z{4,8}]+-[A-Z{1}]$", 0);

    public static boolean validClass(String className) {
        Matcher matcher = VALID_CLASS_NAME_REGEX.matcher(className);
        return matcher.find();
    }

    ///class code generation
    static final String text = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(text.charAt(rnd.nextInt(text.length())));
        return sb.toString();
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