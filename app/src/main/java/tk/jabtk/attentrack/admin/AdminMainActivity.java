package tk.jabtk.attentrack.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import tk.jabtk.attentrack.admin.ManageProfessors.ManageProfessors;
import tk.jabtk.attentrack.admin.ManageStudents.ManageStudents;

import static tk.jabtk.attentrack.R.id.adminForgotPass;
import static tk.jabtk.attentrack.R.id.admin_nav_view;
import static tk.jabtk.attentrack.R.id.headerUserEmail;
import static tk.jabtk.attentrack.R.id.headerUserName;
import static tk.jabtk.attentrack.R.id.ratings;

public class AdminMainActivity extends AppCompatActivity {
    private static final String TAG ="AdminMainActivity" ;
    private FirebaseUser Admin;
    private DocumentReference AdminInfoRef;
    private String KEY_USERNAME = "AdminName", KEY_USEREMAIL = "AdminEmail";
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    boolean fragmentPopped;
    Map<String, Object> AdminInfo;
    String userType;
    String AdminID;
    int selectedItem=0;
    MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main_activity);

        statusBarColor();

        ///Admin Info
        Admin = FirebaseAuth.getInstance().getCurrentUser();
        assert Admin != null;
        AdminID = Admin.getUid();

        AdminInfoRef = FirebaseFirestore.getInstance().collection("Administrators").document(AdminID);
        AdminInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    AdminInfo = documentSnapshot.getData();
                    //Toast.makeText(getContext(), "AdminInfo:" + AdminInfo.get(KEY_USERNAME), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //for toolbar
        MaterialToolbar toolbar = (MaterialToolbar) findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        //for navigation bar
        NavigationView navigationView = findViewById(R.id.admin_nav_view);
        navigationView.bringToFront();
        //navigationView.setCheckedItem(R.id.admin_nav_view);

        ///checking default layout to changing with fragment
        fragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.adminMainFrameLayout) != null) {
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
            setFragment(new AdminDashboard());
        }


        updateHeaderProfile();


        drawerLayout = findViewById(R.id.adminDrawer);

        final View parentView = navigationView.getHeaderView(0);
        final ImageView navProfilePic = parentView.findViewById(R.id.headerUserProfile);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                }
                selectedItem=item.getItemId();
                switch (item.getItemId()) {
                    case R.id.adminDashboard:
                        setFragment(new AdminDashboard());
                        navigationView.setCheckedItem(R.id.adminDashboard);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.AdminNavProfile:
                        setFragment(new AdminProfile());
                        navigationView.setCheckedItem(R.id.AdminNavProfile);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case adminForgotPass:
                        setFragment(new ForgotPassword());
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.manageProfessors:
                        setFragment(new ManageProfessors());
                        navigationView.setCheckedItem(R.id.manageProfessors);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.manageStudents:
                        setFragment(new ManageStudents());
                        navigationView.setCheckedItem(R.id.manageStudents);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.share:
                        shareAppLink();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.rateUs:
                        Ratings(this);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value : " + item.getItemId());
                }
                return true;
            }
        });


        Window window = this.getWindow();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                window.setStatusBarColor(getApplicationContext().getResources().getColor(R.color.headerRed));
                if (Admin.getPhotoUrl() != null) {
                    Picasso.get().load(Admin.getPhotoUrl()).placeholder(R.drawable.admin_ic_avatar).into(navProfilePic);
                } else {
                    navProfilePic.setImageResource(R.drawable.admin_ic_avatar);
                }

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                window.setStatusBarColor(getApplicationContext().getResources().getColor(R.color.darkRed));
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        toggle.syncState();

    }

    // Nav Header
    public void updateHeaderProfile() {
        NavigationView navigationView = findViewById(admin_nav_view);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView HeaderUserName = headerView.findViewById(headerUserName);
        TextView HeaderUserEmail = headerView.findViewById(headerUserEmail);
        TextView HeaderUserType = headerView.findViewById(R.id.headerUserType);
        ImageView HeaderUserProfile = headerView.findViewById(R.id.headerUserProfile);


        AdminInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    AdminInfo = documentSnapshot.getData();
                    if (documentSnapshot.getBoolean("isAdmin")) {
                        userType = "Administrator";
                    } else {
                        FirebaseAuth.getInstance().signOut();
                    }
                    HeaderUserName.setText(AdminInfo.get(KEY_USERNAME).toString());
                    HeaderUserType.setText(userType);
                    HeaderUserEmail.setText(AdminInfo.get(KEY_USEREMAIL).toString());

                    //for image
                    if (Admin.getPhotoUrl() != null) {
                        Picasso.get().load(Admin.getPhotoUrl()).placeholder(R.drawable.admin_ic_avatar).into(HeaderUserProfile);
                    } else {
                        Picasso.get().load(R.drawable.admin_ic_avatar).into(HeaderUserProfile);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //////bottom of menu
    private void shareAppLink() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AttenTrack");

        String shareMessage = "\n" + "Link of application " + "\n\n";

        shareMessage = shareMessage + "https://tinyurl.com/AttenTrack" + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent,"Share via"));
    }

    private void Ratings(NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener) {
        Ratings(this);
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
                FeedbackMap.put("UserName", AdminInfo.get(KEY_USERNAME));
                FeedbackMap.put("UserEmail", AdminInfo.get(KEY_USEREMAIL));

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

                dbRef.child("Feedbacks").child("Admin").child(mAuth.getCurrentUser().getUid()).setValue(FeedbackMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Thanks for your valuable feedback!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Field to upload feedback!", Toast.LENGTH_SHORT).show();
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


    //setting fragment
    private void setFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped) {
            //fragment not in back stack, create it
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.adminMainFrameLayout, fragment);
            fragmentTransaction.addToBackStack(backStateName);
            fragmentTransaction.commit();
        }
    }

    //on back press nav close
    @Override
    public void onBackPressed() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.adminMainFrameLayout);
        String backStateName = fragment.getClass().getName();
        fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);//closing nav
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    ////status bar color
    public void statusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.darkRed));
    }
}