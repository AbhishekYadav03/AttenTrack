package tk.jabtk.attentrack.professor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import tk.jabtk.attentrack.R;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private StorageReference storageReference, profileRef;

    private String userID;
    private ImageView userProfile;
    private TextView logoutTxtBtn;
    private Uri croppedImage;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private androidx.appcompat.widget.Toolbar toolbar;

    String userName,userEmail,userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_main);

        logoutTxtBtn = findViewById(R.id.logOutBtn);
        logoutTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();//Logout
                startActivity(new Intent(MainActivity.this, professorLogin.class));
                finish();
            }
        });

        userProfile = findViewById(R.id.userProfile);
        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPictureIntent();
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference("Professors");

        //get Current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        userID = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference("ProfessorsProfileImages/" + userID + "/");

        profileRef = FirebaseStorage.getInstance().getReference("ProfessorsProfileImages/" + userID +"/"+"profile_"+userID+".jpg" );

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(userProfile);
            }
        });

        final TextView greetingTxt = findViewById(R.id.greeting);
        final TextView userNameTxt = findViewById(R.id.userName);
        final TextView userEmailTxt = findViewById(R.id.userEmail);
        final TextView userTypeTxt= findViewById(R.id.userType);

        //retrieving data from firebase
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Professor userProfile = dataSnapshot.getValue(Professor.class);
                if (userProfile != null) {
                    userName = userProfile.name;
                    userEmail = userProfile.email;
                    userType =userProfile.userType;

                    greetingTxt.setText("Welcome,  " + userName + "!");
                    userNameTxt.setText(userName);
                    userEmailTxt.setText(userEmail);
                    userTypeTxt.setText(userType);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Something wrong happened with database!", Toast.LENGTH_SHORT).show();
            }
        });


        drawerLayout = findViewById(R.id.drawer_layout);

        //for navigation bar
        navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_view);

        updateHeaderProfile();

        //for toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


    }

    private void getPictureIntent() {
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();
            //userProfile.setImageURI(imageUri);
            CropImage.activity(imageUri).setAspectRatio(1, 1).setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result!=null) {
                croppedImage = result.getUri();
                uploadImage(croppedImage);
            }
            else {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 101);
            }
        }
    }

    private void uploadImage(Uri croppedImage) {
        StorageReference fileRef = storageReference.child("profile_"+userID+".jpg");
        fileRef.putFile(croppedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).resize(200, 200).centerCrop().into(userProfile);
                        updateHeaderProfile();

                    }
                });
                Toast.makeText(MainActivity.this, "Profile Uploaded Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to Upload Profile", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //on back press nav close
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);  //closing nav
        } else {
            super.onBackPressed(); //closing Activity
        }
    }

    // for navigation menu items call
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Nav_Profile:
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();//Logout
                startActivity(new Intent(MainActivity.this, professorLogin.class));
                finish();
                break;
            case R.id.forgotPass:
                startActivity(new Intent(MainActivity.this, professorForgotPassword.class));
                break;


        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    // Nav Header
    public void updateHeaderProfile() {
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView HeaderUserName = headerView.findViewById(R.id.headerUserName);
        TextView HeaderUserEmail = headerView.findViewById(R.id.headerUserEmail);
        TextView HeaderUserType = headerView.findViewById(R.id.headerUserType);
        ImageView HeaderUserProfile = headerView.findViewById(R.id.headerUserProfile);


        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Professor userProfile=snapshot.getValue(Professor.class);

                if (userProfile != null) {
                    //for user info
                    HeaderUserName.setText(userProfile.name);
                    HeaderUserType.setText(userProfile.userType);
                    HeaderUserEmail.setText(userProfile.email);

                    //for image
                    profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(HeaderUserProfile);
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Something wrong happened!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}