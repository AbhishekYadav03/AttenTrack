package tk.jabtk.attentrack.admin;

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
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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


public class AdminProfile extends Fragment {
    ScrollView scrollView;
    View view;
    private FirebaseUser Admin;
    private String AdminID, KEY_USERNAME = "AdminName", KEY_USEREMAIL = "AdminEmail";
    private Map<String, Object> AdminInfo;
    private ImageButton selectPic;
    private ProgressBar progressBar;
    private TextInputLayout editTextName, editTextEmail, editTextPass;
    private ImageView profileAdmin;
    private DocumentReference documentReference;
    private StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
        scrollView = view.findViewById(R.id.mainLayout);
        editTextName = view.findViewById(R.id.UpdateName);
        editTextEmail = view.findViewById(R.id.UpdateEmail);
        editTextPass = view.findViewById(R.id.currentPass);
        profileAdmin = view.findViewById(R.id.profileAdmin);
        progressBar = view.findViewById(R.id.loading);


        selectPic = view.findViewById(R.id.selectPic);
        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getPictureIntent();
            }
        });

        Admin = FirebaseAuth.getInstance().getCurrentUser();
        assert Admin != null;
        AdminID = Admin.getUid();

        documentReference = FirebaseFirestore.getInstance().collection("Administrators").document(AdminID);


        storageReference = FirebaseStorage.getInstance().getReference("AdminProfileImages/" + AdminID + "/");


        if (Admin.getPhotoUrl() != null) {
            Picasso.get().load(Admin.getPhotoUrl()).placeholder(R.drawable.admin_ic_avatar).into(profileAdmin);
        }
        DocumentReference AdminInfoRef = FirebaseFirestore.getInstance().collection("Administrators").document(AdminID);
        AdminInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    AdminInfo = documentSnapshot.getData();
                    editTextName.getEditText().setText(AdminInfo.get(KEY_USERNAME).toString());
                    editTextEmail.getEditText().setText(AdminInfo.get(KEY_USEREMAIL
                    ).toString());
                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
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
        StorageReference fileRef = storageReference.child(AdminInfo.get(KEY_USERNAME) + "-" + AdminID + ".jpg");
        fileRef.putFile(croppedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri)
                                .resize(200, 200)
                                .placeholder(R.drawable.admin_ic_avatar)
                                .centerCrop().into(profileAdmin);

                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        Admin.updateProfile(userProfileChangeRequest);

                        ////////for main profile update profileImage
                        Map<String, Object> profileUri = new HashMap<>();
                        profileUri.put("ProfileUrl", uri.toString());
                        documentReference.update(profileUri);

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