package tk.jabtk.attentrack.professor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageClassrooms.ClassModel;

public class ProfessorProfile extends Fragment {
    private FirebaseUser Professor;
    public Uri PROFESSOR_PROFILE;
    private String userID, KEY_USERNAME = "ProfessorName", KEY_EMAIL = "ProfessorEmail";
    private Map<String, Object> ProfessorInfo;
    private View view;
    private ImageButton selectPic;
    private ProgressBar progressBar;
    private TextInputLayout editTextName, editTextEmail, editTextPass;
    private ImageView profileProfessor;
    private DocumentReference documentReference;
    private DocumentReference documentReference2;
    private StorageReference storageReference;
    private ConstraintLayout constraintLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Profile");
        view = inflater.inflate(R.layout.fragment_professor_profile, container, false);
        editTextName = view.findViewById(R.id.UpdateName);
        editTextEmail = view.findViewById(R.id.UpdateEmail);
        editTextPass = view.findViewById(R.id.currentPass);
        profileProfessor = view.findViewById(R.id.profileProfessor);
        progressBar = view.findViewById(R.id.loading);
        constraintLayout = view.findViewById(R.id.constraintLayout);


        selectPic = view.findViewById(R.id.selectPic);
        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPictureIntent();
            }
        });

        Professor = FirebaseAuth.getInstance().getCurrentUser();
        assert Professor != null;
        userID = Professor.getUid();

        documentReference = FirebaseFirestore.getInstance().collection("Professors").document(userID);


        storageReference = FirebaseStorage.getInstance().getReference("ProfessorsProfileImages/" + userID + "/");

        Professor = FirebaseAuth.getInstance().getCurrentUser();
        assert Professor != null;
        userID = Professor.getUid();

        if (Professor.getPhotoUrl() != null) {
            Picasso.get().load(Professor.getPhotoUrl()).into(profileProfessor);
        }
        DocumentReference ProfessorInfoRef = FirebaseFirestore.getInstance().collection("Professors").document(userID);
        ProfessorInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    constraintLayout.setVisibility(View.VISIBLE);
                    ProfessorInfo = documentSnapshot.getData();
                    editTextName.getEditText().setText(ProfessorInfo.get(KEY_USERNAME).toString());
                    editTextEmail.getEditText().setText(ProfessorInfo.get(KEY_EMAIL).toString());

                    //Toast.makeText(getContext(), "AdminInfo:" + AdminInfo.get(KEY_USERNAME), Toast.LENGTH_SHORT).show();
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

    ////////uploading profile
    private void getPictureIntent() {
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, 1081);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1081 && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();

            //userProfile.setImageURI(imageUri);
            CropImage.activity(imageUri).setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getContext(), this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (result != null) {
                Uri croppedImage = result.getUri();

                uploadImage(croppedImage);
            } else {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 101);
            }
        }
    }

    private void uploadImage(Uri croppedImage) {
        ////progressbar code will be here
        progressBar.setVisibility(View.VISIBLE);
        StorageReference fileRef = storageReference.child(ProfessorInfo.get(KEY_USERNAME) + "-" + userID + ".jpg");
        fileRef.putFile(croppedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).placeholder(R.drawable.professor_ic_avatar).resize(200, 200).centerCrop().into(profileProfessor);

                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        Professor.updateProfile(userProfileChangeRequest);

                        //////////////for classrooms////////
                        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("ProfessorClassrooms");
                        dbReference.child(userID).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot snapshot) {
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    ClassModel classModel = snapshot1.getValue(ClassModel.class);
                                    if (classModel != null) {
                                        String ClassCode = classModel.getClassCode();
                                        documentReference2 = FirebaseFirestore.getInstance()
                                                .collection("ClassList")
                                                .document(ClassCode)
                                                .collection("Professors")
                                                .document(userID);
                                        Map<String, Object> profileUri = new HashMap<>();
                                        profileUri.put("ProfileUrl", uri.toString());
                                        documentReference2.update(profileUri);
                                        //Toast.makeText(getContext(), "" + ClassCode, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                        ////////for main profile update profileImage
                        Map<String, Object> profileUri = new HashMap<>();
                        profileUri.put("ProfileUrl", uri.toString());
                        PROFESSOR_PROFILE = uri;
                        documentReference.update(profileUri);

                       /* ProfessorMainActivity mainActivity = new ProfessorMainActivity();
                        mainActivity.updateHeaderProfile();*/

                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "Profile Uploaded Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to Upload Profile", Toast.LENGTH_SHORT).show();
            }
        });

    }

}