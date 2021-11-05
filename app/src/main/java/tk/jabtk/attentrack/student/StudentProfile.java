package tk.jabtk.attentrack.student;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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

public class StudentProfile extends Fragment {
    public Uri STUDENT_PROFILE;
    private FirebaseUser Student;
    private String userID;
    private Map<String, Object> StudentInfo;
    private ProgressBar progressBar;
    private TextInputLayout editTextName, editTextEmail, editTextRollNo, editTextCollegeId, editTextClassName;
    private ImageView profileStudent;
    private DocumentReference documentReference;
    private StorageReference storageReference;
    private ConstraintLayout ProfileUi;

    private final String
            KEY_USERNAME = "StudentName",
            KEY_EMAIL = "StudentEmail",
            KEY_COLLEGEID = "CollegeID",
            KEY_ROLLNO = "StudentRollNo",
            KEY_CLASSCODE = "ClassCode",
            KEY_CLASSNAME = "ClassName";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Profile");
        View view = inflater.inflate(R.layout.fragment_student_profile, container, false);
        editTextName = view.findViewById(R.id.UpdateName);
        editTextEmail = view.findViewById(R.id.UpdateEmail);
        editTextCollegeId = view.findViewById(R.id.UpdateCollegeId);
        editTextRollNo = view.findViewById(R.id.UpdateRollNo);
        editTextClassName = view.findViewById(R.id.UpdateCLassName);
        profileStudent = view.findViewById(R.id.profileStudent);
        progressBar = view.findViewById(R.id.loading);
        ProfileUi = view.findViewById(R.id.ProfileUi);

        ImageButton selectPic = view.findViewById(R.id.selectPic);
        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("Tag", "Permission is granted");
                    getPictureIntent();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }
            }
        });

        Student = FirebaseAuth.getInstance().getCurrentUser();
        assert Student != null;
        userID = Student.getUid();

        documentReference = FirebaseFirestore.getInstance().collection("Students").document(userID);

        storageReference = FirebaseStorage.getInstance().getReference("StudentsProfileImages/" + userID + "/");

        Student = FirebaseAuth.getInstance().getCurrentUser();
        assert Student != null;
        userID = Student.getUid();

        if (Student.getPhotoUrl() != null) {
            Picasso.get().load(Student.getPhotoUrl())
                    .placeholder(R.drawable.ic_student)
                    .resize(200, 200)
                    .centerCrop()
                    .into(profileStudent);
        }
        DocumentReference ProfessorInfoRef = FirebaseFirestore.getInstance().collection("Students").document(userID);
        ProfessorInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    ProfileUi.setVisibility(View.VISIBLE);
                    StudentInfo = documentSnapshot.getData();
                    editTextName.getEditText().setText(StudentInfo.get(KEY_USERNAME).toString());
                    editTextEmail.getEditText().setText(StudentInfo.get(KEY_EMAIL).toString());
                    editTextCollegeId.getEditText().setText(StudentInfo.get(KEY_COLLEGEID).toString());
                    editTextRollNo.getEditText().setText(StudentInfo.get(KEY_ROLLNO).toString());

                    if (StudentInfo.get(KEY_CLASSNAME) != null) {
                        String isEmpty = "";
                        if (StudentInfo.get(KEY_CLASSNAME).equals(isEmpty)) {
                            editTextClassName.setVisibility(View.GONE);
                        }
                        editTextClassName.getEditText().setText(String.valueOf(StudentInfo.get(KEY_CLASSNAME)));
                    } else {

                        editTextClassName.setVisibility(View.GONE);
                    }
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
        StorageReference fileRef = storageReference.child(StudentInfo.get(KEY_USERNAME) + "-" + userID + ".jpg");
        fileRef.putFile(croppedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).resize(200, 200).centerCrop().into(profileStudent);

                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        Student.updateProfile(userProfileChangeRequest);


                        Map<String, Object> profileUri = new HashMap<>();
                        profileUri.put("ProfileUrl", uri.toString());
                        STUDENT_PROFILE = uri;
                        documentReference.update(profileUri);
                        if (StudentInfo.get(KEY_CLASSCODE) != null) {
                            DocumentReference classListRef = FirebaseFirestore.getInstance().collection("ClassList").document(StudentInfo.get(KEY_CLASSCODE).toString()).collection("Students").document(userID);
                            classListRef.update(profileUri);

                        }
                       /* DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("StudentClassJoinRequest").c.child(userID);
                        if()*/
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