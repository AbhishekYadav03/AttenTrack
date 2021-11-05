package tk.jabtk.attentrack.admin.ManageAdmins;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.button.MaterialButton;
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

import tk.jabtk.attentrack.R;


public class ManageAdmins extends Fragment {
    private View view, alertView;
    private AlertDialog dialog;
    private TextView showNewPro, showOldPro;
    private RecyclerView NewRecyclerView, OldRecyclerView;
    private ProgressBar progressBar;
    private boolean isVisible = true;
    private String AdminID, KEY_USERNAME = "AdminName";
    private Map<String, Object> AdminInfo;
    private TextInputLayout AdminEmailLayout, AdminNameLayout, updateAdminNameTx, updateAdminEmailTx;
    private MaterialButton cancelBtn, addAdminBtn;
    TextView TextViewNew, TextViewOld;
    MaterialButton UpdateAdminBtn, deleteAdminBtn;
    private FirebaseUser Admin;
    private NewAdminAdapter newAdminAdapter;
    private AllAdminAdapter allAdminAdapter;
    String AdminName, AdminEmail;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_manage_admins, container, false);

        getActivity().setTitle("Manage Administrators");

        ///Admin Info
        Admin = FirebaseAuth.getInstance().getCurrentUser();
        assert Admin != null;
        AdminID = Admin.getUid();

        DocumentReference AdminInfoRef = FirebaseFirestore.getInstance().collection("Administrators").document(AdminID);
        AdminInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    AdminInfo = documentSnapshot.getData();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        progressBar = view.findViewById(R.id.Progressbar);
        NewRecyclerView = view.findViewById(R.id.newRecycler);
        OldRecyclerView = view.findViewById(R.id.oldRecycler);
        TextViewNew = view.findViewById(R.id.showNewAdmins);
        TextViewOld = view.findViewById(R.id.showAllAdmins);


        FloatingActionButton floatingActionButton = view.findViewById(R.id.addProfessors);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertView = getLayoutInflater().inflate(R.layout.add_professor_dialog, null);
                TextView dialogTitle = alertView.findViewById(R.id.textViewClass);
                dialogTitle.setText("Add New Admin");


                AdminNameLayout = alertView.findViewById(R.id.textField);
                AdminEmailLayout = alertView.findViewById(R.id.textEmail);
                AdminNameLayout.setHint("Enter Name");
                AdminEmailLayout.setHint("Enter Email");

                cancelBtn = alertView.findViewById(R.id.cancelBtn);
                addAdminBtn = alertView.findViewById(R.id.addProBtn);

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

                addAdminBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addAdmin();
                    }
                });
            }
        });

        showNewPro = view.findViewById(R.id.showNewAdmins);
        showNewPro.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_24, 0);
        showNewPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideNewPro();
            }
        });

        showOldPro = view.findViewById(R.id.showAllAdmins);
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
        Query query = FirebaseDatabase.getInstance().getReference("NewAdmins").orderByChild("AdminName");

        FirebaseRecyclerOptions<AdminModel> recyclerOptions = new FirebaseRecyclerOptions.Builder<AdminModel>()
                .setQuery(query, AdminModel.class)
                .build();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TextViewNew.setVisibility(View.VISIBLE);
                    NewRecyclerView.setVisibility(View.VISIBLE);

                    newAdminAdapter = new NewAdminAdapter(recyclerOptions);
                    NewRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    NewRecyclerView.setAdapter(newAdminAdapter);
                    newAdminAdapter.startListening();

                    /////         use of  interface  from adapter class              ///
                    newAdminAdapter.setItemClickListener(new NewAdminAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DataSnapshot Snapshot, int position) {
                            AdminModel adminModel = Snapshot.getValue(AdminModel.class);
                            if (adminModel != null) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Class Information",
                                        "You can register in app using this information.\nAdmin Name: "
                                                + adminModel.getAdminName() + "\nAdmin Email: " + adminModel.getAdminEmail()
                                                + "\nAdmin JoinCode: " + adminModel.getRegCode());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLongItemClick(DataSnapshot Snapshot, int position) {
                            AdminModel adminModel = Snapshot.getValue(AdminModel.class);
                            assert adminModel != null;
                            String parentName = adminModel.getRegCode();
                            String updateProName = adminModel.getAdminName();
                            String updateProEmail = adminModel.getAdminName();

                            alertView = getLayoutInflater().inflate(R.layout.update_professor_dialog, null);
                            TextView dialogTitle = alertView.findViewById(R.id.textViewClass);
                            dialogTitle.setText("Update Admin Info");

                            updateAdminNameTx = alertView.findViewById(R.id.textField);
                            updateAdminNameTx.getEditText().setText(adminModel.getAdminName());
                            updateAdminEmailTx = alertView.findViewById(R.id.textEmail);
                            updateAdminEmailTx.getEditText().setText(adminModel.getAdminEmail());

                            updateAdminNameTx.setHint("Name");
                            updateAdminEmailTx.setHint("Email");


                            UpdateAdminBtn = alertView.findViewById(R.id.updateProBtn);
                            UpdateAdminBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateAdminInfo(updateProName, updateProEmail, parentName);
                                }
                            });

                            deleteAdminBtn = alertView.findViewById(R.id.deleteProBtn);
                            deleteAdminBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    newAdminAdapter.deleteItem(position);
                                    dialog.dismiss();
                                    getFragmentManager().beginTransaction().detach(ManageAdmins.this).attach(ManageAdmins.this).commit();
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

            }
        });


    }

    public void setUpAllAdmin() {


        com.google.firebase.firestore.Query query1 = db.collection("Administrators").orderBy("AdminName");

        FirestoreRecyclerOptions<AdminModel> recyclerOptions = new FirestoreRecyclerOptions.Builder<AdminModel>()
                .setQuery(query1, AdminModel.class)
                .build();

        allAdminAdapter = new AllAdminAdapter(recyclerOptions);
        OldRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        OldRecyclerView.setAdapter(allAdminAdapter);
        allAdminAdapter.startListening();


        /////         use of  interface  from adapter class              ///
        /*allAdminAdapter.setItemClickListener(new AllAdminAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot Snapshot, int position) {

            }

            @Override
            public void onLongItemClick(@NonNull DocumentSnapshot Snapshot, int position) {
                AdminModel adminModel = Snapshot.toObject(AdminModel.class);
                assert adminModel != null;
                String parentName = adminModel.getRegCode();
                String adminName = adminModel.getAdminName();
                String updateAdminEmail = adminModel.getAdminEmail();

                Toast.makeText(getContext(), "admin model"+  adminModel.getRegCode(), Toast.LENGTH_SHORT).show();
                alertView = getLayoutInflater().inflate(R.layout.update_professor_dialog, null);

                updateAdminNameTx = alertView.findViewById(R.id.textField);
                updateAdminNameTx.getEditText().setText(adminModel.getAdminName());

                updateAdminEmailTx = alertView.findViewById(R.id.textEmail);
                updateAdminEmailTx.getEditText().setText(adminModel.getAdminEmail());

                updateAdminBtn = alertView.findViewById(R.id.updateProBtn);
                updateAdminBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //updateAdminInfo(adminName, updateAdminEmail, parentName);
                    }
                });

                deleteAdminBtn = alertView.findViewById(R.id.deleteProBtn);
                deleteAdminBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allAdminAdapter.deleteItem(position);
                        dialog.dismiss();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(alertView);
                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });*/

    }

    public void addAdmin() {

        String AdminName = AdminNameLayout.getEditText().getText().toString().trim();
        String AdminEmail = AdminEmailLayout.getEditText().getText().toString().trim();

        boolean isError = false;
        if (AdminName.isEmpty()) {
            isError = true;
            AdminNameLayout.setError("Name cannot be empty!");
            AdminNameLayout.requestFocus();
        }
        if (AdminName.length() < 5) {
            isError = true;
            AdminNameLayout.setError("Enter Valid Name!");
            AdminNameLayout.requestFocus();
        }
        if (!isFullName(AdminName)) {
            isError = true;
            AdminNameLayout.setError("Enter Valid Name!");
            AdminNameLayout.requestFocus();

        }
        if (AdminName.length() > 20) {
            isError = true;
            AdminNameLayout.setError("Enter Valid  Name!");
            AdminNameLayout.requestFocus();
        }
        if (AdminEmail.isEmpty()) {
            isError = true;
            AdminEmailLayout.setError("Email can'nt be empty!");
            AdminEmailLayout.requestFocus();
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(AdminEmail).matches()) {
            isError = true;
            AdminEmailLayout.setError("Please Enter Valid Email!");
            AdminEmailLayout.requestFocus();
        }


        if (!isError) {

            Query query = FirebaseDatabase.getInstance().getReference().child("NewAdmins").orderByChild("AdminEmail").equalTo(AdminEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        count++;
                    }
                    if (count > 0) {
                        Toast.makeText(getContext(), "Admin Already Exist", Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String AddedOn = simpleDateFormat.format(new Date());

                        String RegCode = randomString(8);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("NewAdmins");
                        Map<String, Object> adminInfo = new HashMap<>();

                        adminInfo.put("AdminName", capitalizeWord(AdminName));
                        adminInfo.put("RegCode", RegCode);
                        adminInfo.put("AdminEmail", AdminEmail);
                        adminInfo.put("AddedBy", AdminInfo.get(KEY_USERNAME));
                        adminInfo.put("AddedOn", AddedOn);
                        databaseReference.child(RegCode).setValue(adminInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    getFragmentManager().beginTransaction().detach(ManageAdmins.this).attach(ManageAdmins.this).commit();
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
                                            .getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Admin Information", "You can register in application using this information.\nRegistrationEmail: " + AdminEmail + "\nRegistrationCode: " + RegCode);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Admin added Successfully! and Information Copied to Clipboard!", Toast.LENGTH_LONG).show();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext(), "Failed to add Admin" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        // Otherwise the dialog will stay open.
    }

    private void updateAdminInfo(String oldName, String oldEmail, String parentName) {
        boolean isError = false;
        AdminName = updateAdminNameTx.getEditText().getText().toString().toLowerCase();
        AdminEmail = updateAdminEmailTx.getEditText().getText().toString().toLowerCase();

        if (AdminName.equals(oldName.toLowerCase()) && AdminEmail.equals(oldEmail.toLowerCase())) {
            isError = true;
            updateAdminNameTx.setError("Change information to update!");
            updateAdminNameTx.requestFocus();
        }
        if (AdminName.isEmpty()) {
            isError = true;
            updateAdminNameTx.setError("Professor Name cannot be empty!");
            updateAdminNameTx.requestFocus();
        }
        if (AdminName.length() < 5) {
            isError = true;
            updateAdminNameTx.setError("Enter Valid Name!");
            updateAdminNameTx.requestFocus();
        }
        if (!isFullName(AdminName)) {
            isError = true;
            updateAdminNameTx.setError("Enter Valid Professor Name!");
            updateAdminNameTx.requestFocus();
        }
        if (AdminName.length() > 20) {
            isError = true;
            updateAdminNameTx.setError("Enter Valid  Name!");
            updateAdminNameTx.requestFocus();
        }
        if (AdminEmail.isEmpty()) {
            isError = true;
            updateAdminEmailTx.setError("Enter Email Address!");
            updateAdminEmailTx.requestFocus();
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(AdminEmail).matches()) {
            isError = true;
            updateAdminEmailTx.setError("Please Enter Valid Email!");
            updateAdminEmailTx.requestFocus();
        }


        if (!isError) {
            progressBar.setVisibility(View.VISIBLE);
            Query query = FirebaseDatabase.getInstance().getReference().child("NewAdmins").orderByChild("AdminEmail").equalTo(AdminEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                        AdminModel adminModel = postSnapshot.getValue(AdminModel.class);
                        if (adminModel.getAdminEmail().equals(AdminEmail)) {
                            //Toast.makeText(getContext(), "Email Match found"+newProfessorModel.getProfessorEmail(), Toast.LENGTH_SHORT).show();
                            if (!adminModel.getRegCode().equals(parentName)) {
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
                        updateAdminEmailTx.setError("Email is Already in use!");
                        updateAdminEmailTx.requestFocus();
                        Toast.makeText(getContext(), "Admin Already Exist", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                    } else {


                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a  ");
                        String modifiedOn = simpleDateFormat.format(new Date());

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("NewAdmins");
                        Map<String, Object> professorInfo = new HashMap<>();

                        professorInfo.put("AdminName", capitalizeWord(AdminName));
                        professorInfo.put("RegCode", parentName);
                        professorInfo.put("AdminEmail", AdminEmail);
                        professorInfo.put("ModifiedBy", AdminInfo.get(KEY_USERNAME));
                        professorInfo.put("ModifiedOn", modifiedOn);
                        databaseReference.child(parentName).updateChildren(professorInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    dialog.dismiss();
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Admin Information",
                                            "You can register in application using this information.\nRegistrationEmail: " + AdminEmail +
                                                    "\nRegistrationCode: " + parentName);

                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "Admin Info updated Successfully!\nInformation Copied to Clipboard!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                } else {

                                    dialog.dismiss();
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
        setUpAllAdmin();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (newAdminAdapter != null)
            newAdminAdapter.stopListening();
        if (allAdminAdapter != null)
            allAdminAdapter.stopListening();
    }

}