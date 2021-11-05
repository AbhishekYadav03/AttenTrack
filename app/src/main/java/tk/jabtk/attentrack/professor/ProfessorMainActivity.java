package tk.jabtk.attentrack.professor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.StartActivity;
import tk.jabtk.attentrack.admin.ForgotPassword;
import tk.jabtk.attentrack.admin.ManageProfessors.OldProfessorModel;

import static tk.jabtk.attentrack.R.id.headerUserEmail;
import static tk.jabtk.attentrack.R.id.headerUserName;
import static tk.jabtk.attentrack.R.id.nav_view;
import static tk.jabtk.attentrack.R.id.ratings;
import static tk.jabtk.attentrack.R.id.share;


public class ProfessorMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseUser Professor;
    private DocumentReference documentReference;
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout;
    private String KEY_USERNAME = "ProfessorName", KEY_USEREMAIL = "ProfessorEmail";

    boolean fragmentPopped;
    private String userType;
    Window window;
    Map<String, Object> ProfessorInfo;

    int selectedItem = 0;
    MenuItem menuItem;
    private String TAG = "ProfessorMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_main);

        statusBarColor();
        window = this.getWindow();

        Professor = FirebaseAuth.getInstance().getCurrentUser();
        assert Professor != null;
        String userID = Professor.getUid();
        documentReference = FirebaseFirestore.getInstance().collection("Professors").document(userID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    ProfessorInfo = documentSnapshot.getData();
                    //Toast.makeText(getContext(), "AdminInfo:" + AdminInfo.get(KEY_USERNAME), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        //for navigation bar
        NavigationView navigationView = findViewById(nav_view);
        navigationView.bringToFront();

        final View parentView = navigationView.getHeaderView(0);
        final ImageView navProfilePic = parentView.findViewById(R.id.headerUserProfile);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(nav_view);

        ///checking default layout to changing with fragment
        fragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.professorMainFrameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            menuItem = navigationView.getMenu().getItem(0).getSubMenu().getItem(selectedItem);
            if (menuItem.isChecked()) {
                menuItem.setChecked(false);
            } else {
                menuItem.setChecked(true);
            }
            Log.d(TAG, "onCreate: " + menuItem.getTitle());
            setFragment(new ProfessorDashboard());
        }

        updateHeaderProfile();


        //for toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        MaterialToolbar toolbar = (MaterialToolbar) findViewById(R.id.professorToolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);


        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                window.setStatusBarColor(getApplicationContext().getResources().getColor(R.color.darkPurple));
                if (Professor.getPhotoUrl() != null) {
                    Picasso.get().load(Professor.getPhotoUrl()).placeholder(R.drawable.professor_ic_avatar).into(navProfilePic);
                } else {
                    Picasso.get().load(R.drawable.professor_ic_avatar).placeholder(R.drawable.professor_ic_avatar).into(navProfilePic);
                }


            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                window.setStatusBarColor(getApplicationContext().getResources().getColor(R.color.green));

            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        toggle.syncState();
    }


    // for navigation menu items call
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (menuItem.isChecked()) {
            menuItem.setChecked(false);
        }
        switch (item.getItemId()) {

            case R.id.professorDashboard:
                setFragment(new ProfessorDashboard());
                break;

            case R.id.professorProfile:
                setFragment(new ProfessorProfile());
                break;

            case R.id.forgotPass:
                ForgotPassword forgotPassword = new ForgotPassword();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isProfessor", true);
                forgotPassword.setArguments(bundle);
                setFragment(forgotPassword);
                break;

            case share:
                shareAppLink();
                break;

            case R.id.rateUs:
                Ratings(this);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //////bottom of menu
    private void shareAppLink() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AttenTrack");

        String shareMessage = "\n" + "Link of application " + "\n\n";

        shareMessage = shareMessage + "https://tinyurl.com/AttenTrack" + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    public void Ratings(Activity activity) {
        View alertView = getLayoutInflater().inflate(R.layout.rating_bar_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(alertView);
        AlertDialog dialog;

        drawerLayout.closeDrawer(GravityCompat.START);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        RatingBar ratingBar = (RatingBar) alertView.findViewById(ratings);
        TextView ratingText = alertView.findViewById(R.id.ratingText);
        Button submitButton = alertView.findViewById(R.id.submitRating);
        TextInputLayout feedbackText = alertView.findViewById(R.id.feedback);
        feedbackText.setHintTextColor(ColorStateList.valueOf(this.getResources().getColor(R.color.green)));
        feedbackText.setBoxStrokeColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundTintList(ColorStateList.valueOf(this.getResources().getColor(R.color.green)));

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 1.0) {
                    ratingText.setText("Total " + rating + " Stars!");
                } else {
                    ratingText.setText("Total " + rating + " Star!");
                }

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                Map<String, Object> FeedbackMap = new HashMap<>();
                float ratingCount = ratingBar.getRating();
                String feedback;
                if (feedbackText.getEditText().getText() != null) {
                    feedback = feedbackText.getEditText().getText().toString();
                    FeedbackMap.put("Feedback", feedback);
                }
                FeedbackMap.put("RatingCount", ratingCount);
                FeedbackMap.put("UserName", ProfessorInfo.get(KEY_USERNAME));
                FeedbackMap.put("UserEmail", ProfessorInfo.get(KEY_USEREMAIL));

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

                dbRef.child("Feedbacks").child("Professors").child(mAuth.getCurrentUser().getUid()).setValue(FeedbackMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Thanks for your valuable feedback!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Field to upload feedback! " + task.getException(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }


    ////app bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout(this);
                return false;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    ////// Logout method
    public void logout(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Do you really want to log out?");
        builder.setTitle("Logout");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(getApplicationContext(), "Logout Successfully!",
                        Toast.LENGTH_LONG).show();

                FirebaseAuth.getInstance().signOut();
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(getApplicationContext(), StartActivity.class));
                finishAffinity();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    ///set fragments
    private void setFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();
        fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped) {//fragment not in back stack, create it
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.professorMainFrameLayout, fragment);
            fragmentTransaction.addToBackStack(backStateName);
            fragmentTransaction.commit();
        }
    }

    // Nav Header
    public void updateHeaderProfile() {
        NavigationView navigationView = findViewById(nav_view);
        navigationView.bringToFront();
        View headerView = navigationView.getHeaderView(0);
        TextView HeaderUserName = headerView.findViewById(headerUserName);
        TextView HeaderUserEmail = headerView.findViewById(headerUserEmail);
        TextView HeaderUserType = headerView.findViewById(R.id.headerUserType);
        ImageView HeaderUserProfile = headerView.findViewById(R.id.headerUserProfile);


        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                OldProfessorModel oldProfessorModel = documentSnapshot.toObject(OldProfessorModel.class);
                if (documentSnapshot.getBoolean("isProfessor")) {
                    userType = "Professor";
                    //Toast.makeText(ProfessorMainActivity.this, userType, Toast.LENGTH_SHORT).show();
                }

                if (oldProfessorModel != null) {
                    //for user info
                    HeaderUserName.setText(oldProfessorModel.getProfessorName());
                    HeaderUserType.setText(userType);
                    HeaderUserEmail.setText(oldProfessorModel.getProfessorEmail());

                    //for image
                    if (Professor.getPhotoUrl() != null) {
                        Picasso.get().load(Professor.getPhotoUrl()).placeholder(R.drawable.professor_ic_avatar).into(HeaderUserProfile);
                    } else {
                        Picasso.get().load(R.drawable.professor_ic_avatar).into(HeaderUserProfile);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    //on back press nav close
    @Override
    public void onBackPressed() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.professorMainFrameLayout);
        String backStateName = fragment.getClass().getName();

        fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);  //closing nav
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    ////color of status bar
    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.green));
    }
}