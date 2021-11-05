package tk.jabtk.attentrack.admin.ManageProfessors;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tk.jabtk.attentrack.R;

public class ManageProfessors extends Fragment {
    private View alertView;
    private AlertDialog dialog;
    private Button cancelBtn, addProBtn, updateProBtn, deleteProBtn;
    private TextInputLayout professorName, professorEmail, updateProfessorName, updateProfessorEmail;
    private TextView showNewPro, showOldPro;
    private RecyclerView NewRecyclerView, OldRecyclerView;
    private ProgressBar progressBar;
    private boolean isVisible = true;
    private final String KEY_USERNAME = "AdminName";
    private FirebaseFirestore db;
    private NewProfessorAdapter newProfessorAdapter;
    private OldProfessorAdapter oldProfessorAdapter;
    private Map<String, Object> AdminInfo;
    private TextView TextViewNew, TextViewOld;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Manage Professors");

        ///Admin Info
        FirebaseUser admin = FirebaseAuth.getInstance().getCurrentUser();
        assert admin != null;
        String adminID = admin.getUid();
        db = FirebaseFirestore.getInstance();
        DocumentReference AdminInfoRef = FirebaseFirestore.getInstance().collection("Administrators").document(adminID);
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
        View view = inflater.inflate(R.layout.fragment_manage_professors, container, false);
        getActivity().setTitle("Manage Professors");
        progressBar = view.findViewById(R.id.Progressbar);
        NewRecyclerView = view.findViewById(R.id.newRecycler);
        TextViewNew = view.findViewById(R.id.showNewProfessors);

        OldRecyclerView = view.findViewById(R.id.oldRecycler);
        TextViewOld = view.findViewById(R.id.showOldProfessors);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.addProfessors);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertView = getLayoutInflater().inflate(R.layout.add_professor_dialog, null);
                professorName = alertView.findViewById(R.id.textField);
                professorEmail = alertView.findViewById(R.id.textEmail);
                cancelBtn = alertView.findViewById(R.id.cancelBtn);
                addProBtn = alertView.findViewById(R.id.addProBtn);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(alertView);
                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                addProBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addProfessor();
                    }
                });
            }
        });

        showNewPro = view.findViewById(R.id.showNewProfessors);
        showNewPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_24, 0);
        showNewPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideNewPro();
            }
        });

        showOldPro = view.findViewById(R.id.showOldProfessors);
        showOldPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_24, 0);
        showOldPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideOldPro();
            }
        });

        return view;
    }


    public void showHideNewPro() {
        if (isVisible) {
            NewRecyclerView.setVisibility(View.GONE);
            isVisible = false;
            showNewPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more_24, 0);
        } else {
            showNewPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_24, 0);
            NewRecyclerView.setVisibility(View.VISIBLE);
            isVisible = true;
        }
    }

    public void showHideOldPro() {
        if (isVisible) {
            OldRecyclerView.setVisibility(View.GONE);
            isVisible = false;
            showOldPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more_24, 0);
        } else {
            showOldPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_24, 0);
            OldRecyclerView.setVisibility(View.VISIBLE);
            isVisible = true;
        }
    }

    public void setNewUpRecyclerView() {
        Query query = FirebaseDatabase.getInstance().getReference("NewProfessors").orderByChild("ProfessorName");

        FirebaseRecyclerOptions<NewProfessorModel> recyclerOptions = new FirebaseRecyclerOptions.Builder<NewProfessorModel>()
                .setQuery(query, NewProfessorModel.class)
                .build();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TextViewNew.setVisibility(View.VISIBLE);
                    NewRecyclerView.setVisibility(View.VISIBLE);

                    newProfessorAdapter = new NewProfessorAdapter(recyclerOptions);
                    NewRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    NewRecyclerView.setAdapter(newProfessorAdapter);
                    newProfessorAdapter.startListening();

                    /////         use of  interface  from adapter class              ///
                    newProfessorAdapter.setItemClickListener(new NewProfessorAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            NewProfessorModel newProfessorModel = Snapshot.getValue(NewProfessorModel.class);
                            if (newProfessorModel != null) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Class Information",
                                        "You can register in app using this information.\nProfessor Name: " + newProfessorModel.getProfessorName()
                                                + "\nProfessor Email: " + newProfessorModel.getProfessorEmail()
                                                + "\nProfessor Code: " + newProfessorModel.getRegCode());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLongItemClick(@NonNull DataSnapshot Snapshot, int position) {
                            NewProfessorModel newProfessorModel = Snapshot.getValue(NewProfessorModel.class);
                            assert newProfessorModel != null;
                            String parentName = newProfessorModel.getRegCode();
                            String updateProName = newProfessorModel.getProfessorName();
                            String updateProEmail = newProfessorModel.getProfessorEmail();

                            alertView = getLayoutInflater().inflate(R.layout.update_professor_dialog, null);
                            updateProfessorName = alertView.findViewById(R.id.textField);
                            updateProfessorName.getEditText().setText(newProfessorModel.getProfessorName());
                            updateProfessorEmail = alertView.findViewById(R.id.textEmail);
                            updateProfessorEmail.getEditText().setText(newProfessorModel.getProfessorEmail());
                            updateProBtn = alertView.findViewById(R.id.updateProBtn);
                            deleteProBtn = alertView.findViewById(R.id.deleteProBtn);

                            updateProBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateProfessorInfo(updateProName, updateProEmail, parentName);
                                }
                            });
                            deleteProBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    newProfessorAdapter.deleteItem(position);
                                    dialog.dismiss();
                                    getFragmentManager().beginTransaction().detach(ManageProfessors.this).attach(ManageProfessors.this).commit();
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
                    TextViewNew.setVisibility(View.GONE);
                    NewRecyclerView.setVisibility(View.GONE);
                }
            }////on data change

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfessorInfo(String oldName, String oldEmail, String parentName) {
        boolean isError = false;
        String ProfessorName = updateProfessorName.getEditText().getText().toString().trim().toLowerCase();
        String ProfessorEmail = updateProfessorEmail.getEditText().getText().toString().trim().toLowerCase();
        TextViewOld.setVisibility(View.GONE);
        OldRecyclerView.setVisibility(View.GONE);

        if (ProfessorName.equals(oldName.toLowerCase()) && ProfessorEmail.equals(oldEmail.toLowerCase())) {
            isError = true;
            updateProfessorName.setError("Change information to update!");
            updateProfessorName.requestFocus();
            updateProfessorEmail.setError("Change information to update!");
        }
        if (ProfessorName.isEmpty()) {
            isError = true;
            updateProfessorName.setError("Professor Name cannot be empty!");
            updateProfessorName.requestFocus();
        }
        if (ProfessorName.length() < 5) {
            isError = true;
            updateProfessorName.setError("Enter Valid Name!");
            updateProfessorName.requestFocus();
        }
        if (!isFullName(ProfessorName)) {
            isError = true;
            updateProfessorName.setError("Enter Valid Professor Name!");
            updateProfessorName.requestFocus();
        }
        if (ProfessorName.length() > 20) {
            isError = true;
            updateProfessorName.setError("Enter Valid  Name!");
            updateProfessorName.requestFocus();
        }
        if (ProfessorEmail.isEmpty()) {
            isError = true;
            updateProfessorEmail.setError("Enter Email Address!");
            updateProfessorEmail.requestFocus();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(ProfessorEmail).matches()) {
            isError = true;
            updateProfessorEmail.setError("Please Enter Valid Email!");
            updateProfessorEmail.requestFocus();
        }


        if (!isError) {
            progressBar.setVisibility(View.VISIBLE);
            Query query = FirebaseDatabase.getInstance().getReference().child("NewProfessors").orderByChild("ProfessorEmail").equalTo(ProfessorEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        NewProfessorModel newProfessorModel = postSnapshot.getValue(NewProfessorModel.class);
                        if (newProfessorModel.getProfessorEmail().equals(ProfessorEmail)) {
                            //Toast.makeText(getContext(), "Email Match found"+newProfessorModel.getProfessorEmail(), Toast.LENGTH_SHORT).show();
                            if (!newProfessorModel.getRegCode().equals(parentName)) {
                                count++;
                                //Toast.makeText(getContext(), "Not Matching Parent\nR "+newProfessorModel.getRegCode()+"\nP "+parentName, Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(getContext(), "Parent Match Found"+newProfessorModel.getRegCode(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //Toast.makeText(getContext(), "MatchNotFound \nE "+newProfessorModel.getProfessorEmail()+"\nO "+ProfessorEmail, Toast.LENGTH_LONG).show();
                        }

                    }
                    if (count > 0) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Professor Already Exist", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        getFragmentManager().beginTransaction().detach(ManageProfessors.this).attach(ManageProfessors.this).commit();

                    } else {


                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String modifiedOn = simpleDateFormat.format(new Date());

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("NewProfessors");
                        Map<String, Object> professorInfo = new HashMap<>();

                        professorInfo.put("ProfessorName", capitalizeWord(ProfessorName));
                        professorInfo.put("RegCode", parentName);
                        professorInfo.put("ProfessorEmail", ProfessorEmail);
                        professorInfo.put("ModifiedBy", AdminInfo.get(KEY_USERNAME));
                        professorInfo.put("ModifiedOn", modifiedOn);
                        databaseReference.child(parentName).updateChildren(professorInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    dialog.dismiss();
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.
                                            newPlainText("Professor Information", "You can register in application using this information.\nRegistrationEmail: "
                                                    + ProfessorEmail + "\nRegistrationCode: " + parentName);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Professor Info updated Successfully!\nInformation Copied to Clipboard!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    getFragmentManager().beginTransaction().detach(ManageProfessors.this).attach(ManageProfessors.this).commit();
                                } else {

                                    dialog.dismiss();
                                    getFragmentManager().beginTransaction().detach(ManageProfessors.this).attach(ManageProfessors.this).commit();
                                    Toast.makeText(getContext(), "Failed to update information" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Otherwise the dialog will stay open.
    }

    public void setOldUpRecyclerView() {

        ///Query query = FirebaseDatabase.getInstance().getReference("Professors").orderByChild("ProfessorName");
        com.google.firebase.firestore.Query query1 = db.collection("Professors").orderBy("ProfessorName");

        FirestoreRecyclerOptions<OldProfessorModel> recyclerOptions = new FirestoreRecyclerOptions.Builder<OldProfessorModel>()
                .setQuery(query1, OldProfessorModel.class)
                .build();

        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    TextViewOld.setVisibility(View.VISIBLE);
                    OldRecyclerView.setVisibility(View.VISIBLE);

                    oldProfessorAdapter = new OldProfessorAdapter(recyclerOptions);
                    OldRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    OldRecyclerView.setAdapter(oldProfessorAdapter);
                    oldProfessorAdapter.startListening();


                    /////         use of  interface  from adapter class              ///
                    oldProfessorAdapter.setItemClickListener(new OldProfessorAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DocumentSnapshot Snapshot, int position) {
                            OldProfessorModel oldProfessorModel = Snapshot.toObject(OldProfessorModel.class);
                            if (oldProfessorModel != null) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData
                                        .newPlainText("Class Information", "You can register in app using this information.\nProfessor Name: "
                                                + oldProfessorModel.getProfessorName() + "\nProfessor Email: "
                                                + oldProfessorModel.getProfessorEmail() + "\nProfessor Code: "
                                                + oldProfessorModel.getRegCode());
                                clipboard.setPrimaryClip(clip);
                                //Toast.makeText(getContext(), Snapshot.toObject(OldProfessorModel.class).toString(), Toast.LENGTH_SHORT).show();
                                Toast.makeText(getContext(), "Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLongItemClick(@NonNull DocumentSnapshot Snapshot, int position) {
                            /*OldProfessorModel oldProfessorModel = Snapshot.toObject(OldProfessorModel.class);

                            String parentName = oldProfessorModel.getRegCode();
                            String updateProName = oldProfessorModel.getProfessorName();
                            String updateProEmail = oldProfessorModel.getProfessorEmail();

                            alertView = getLayoutInflater().inflate(R.layout.update_professor_dialog, null);

                            updateProfessorName = alertView.findViewById(R.id.updateProfessorName);
                            updateProfessorName.getEditText().setText(oldProfessorModel.getProfessorName());

                            updateProfessorEmail = alertView.findViewById(R.id.updateProfessorEmail);
                            updateProfessorEmail.getEditText().setText(oldProfessorModel.getProfessorEmail());

                            updateProBtn = alertView.findViewById(R.id.updateProBtn);
                            updateProBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateProfessorInfo(updateProName, updateProEmail, parentName);
                                }
                            });

                            deleteProBtn = alertView.findViewById(R.id.deleteProBtn);
                            deleteProBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    oldProfessorAdapter.deleteItem(position);
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setView(alertView);
                            dialog = builder.create();
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.show();*/
                        }
                    });
                } else {
                    TextViewOld.setVisibility(View.GONE);
                    OldRecyclerView.setVisibility(View.GONE);
                }
            }
        });

    }

    private void addProfessor() {
        String ProfessorName = professorName.getEditText().getText().toString().trim();
        String ProfessorEmail = professorEmail.getEditText().getText().toString().trim();

        boolean isError = false;
        if (ProfessorName.isEmpty()) {
            isError = true;
            professorName.setError("Professor Name cannot be empty!");
            professorName.requestFocus();
        }
        if (ProfessorName.length() < 5) {
            isError = true;
            professorName.setError("Enter Valid Name!");
            professorName.requestFocus();
        }
        if (!isFullName(ProfessorName)) {
            isError = true;
            professorName.setError("Enter Valid Professor Name!");
            professorName.requestFocus();

        }
        if (ProfessorName.length() > 20) {
            isError = true;
            professorName.setError("Enter Valid  Name!");
            professorName.requestFocus();
        }
        if (ProfessorEmail.isEmpty()) {
            isError = true;
            professorEmail.setError("Email can'nt be empty!");
            professorEmail.requestFocus();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(ProfessorEmail).matches()) {
            isError = true;
            professorEmail.setError("Please Enter Valid Email!");
            professorEmail.requestFocus();
        }
        if (!isError) {

            Query query = FirebaseDatabase.getInstance().getReference().child("NewProfessors").orderByChild("ProfessorEmail").equalTo(ProfessorEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        count++;
                    }
                    if (count > 0) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Professor Already Exist", Toast.LENGTH_SHORT).show();

                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String AddedOn = simpleDateFormat.format(new Date());

                        String RegCode = randomString(8);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("NewProfessors");
                        Map<String, Object> professorInfo = new HashMap<>();

                        professorInfo.put("ProfessorName", capitalizeWord(ProfessorName));
                        professorInfo.put("RegCode", RegCode);
                        professorInfo.put("ProfessorEmail", ProfessorEmail);
                        professorInfo.put("AddedBy", AdminInfo.get(KEY_USERNAME));
                        professorInfo.put("AddedOn", AddedOn);
                        databaseReference.child(RegCode).setValue(professorInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    getFragmentManager().beginTransaction().detach(ManageProfessors.this).attach(ManageProfessors.this).commit();
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Professor Information",
                                            "You can register in application using this information.\n" +
                                                    "Professor Name: " + professorName + "Professor Email: " + ProfessorEmail + "\nRegistration Code: " + RegCode);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Professor added Successfully! and Information Copied to Clipboard!", Toast.LENGTH_LONG).show();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Failed to add Professor" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Otherwise the dialog will stay open.
    }

    ///registration code generation code generation
    static final String text = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(text.charAt(rnd.nextInt(text.length())));
        return sb.toString();
    }

    ////sentence case NAME
    public String capitalizeWord(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterFirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterFirst.toLowerCase() + " ";
        }
        return capitalizeWord.trim();
    }

    //validating name
    public static boolean isFullName(String str) {
        String expression = "^[a-zA-Z\\s]+";
        return str.matches(expression);
    }

    ///// data listening for auto refresh
    @Override
    public void onStart() {
        super.onStart();
        setNewUpRecyclerView();
        setOldUpRecyclerView();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (newProfessorAdapter != null)
            newProfessorAdapter.stopListening();
        if (oldProfessorAdapter != null)
            oldProfessorAdapter.stopListening();
    }
}