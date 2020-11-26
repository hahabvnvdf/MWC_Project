package com.example.stepmapper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stepmapper.ui.home.HomeFragment;
import com.example.stepmapper.ui.map.MapFragment;
import com.example.stepmapper.ui.report.ReportFragment;
import com.example.stepmapper.ui.scoreboard.ScoreboardFragment;
import com.example.stepmapper.ui.user.LoginFragment;
import com.example.stepmapper.ui.user.SignUpFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity  {


    private DrawerLayout drawer;
    private NavigationView navigationView;

    private AppBarConfiguration mAppBarConfiguration;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 45;
    private boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
    private FirebaseAuth firebaseAuth;
    private String Username = null;

//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
////        updateUI(currentUser);
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Setup drawer view

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_report, R.id.nav_map, R.id.nav_score)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setupDrawerContent(navigationView);
        View headerView = navigationView.getHeaderView(0);
        firebaseAuth = FirebaseAuth.getInstance();
        Button signin = (Button)headerView.findViewById(R.id.loginBut);
        Button logout = (Button)headerView.findViewById(R.id.logoutBut);
        TextView username = (TextView)headerView.findViewById(R.id.username);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginFragment.class);
                startActivity(intent);
                finish();
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                HomeFragment.stepsCompleted = 0;
                firebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginFragment.class);
                startActivity(intent);
                finish();
            }
        });

        // Ask for activity recognition permission
        if (runningQOrLater) {
            getActivity();
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){
            logout.setVisibility(View.VISIBLE);
            signin.setVisibility(View.GONE);
            username.setVisibility(View.VISIBLE);
            username.setText(user.getEmail());
        }else if(firebaseAuth.getCurrentUser() == null){
            logout.setVisibility(View.GONE);
            signin.setVisibility(View.VISIBLE);
            username.setVisibility(View.INVISIBLE);
        }
//        navigationView.getMenu().findItem(R.id.loginName).setVisible(true);



    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragmentClass = HomeFragment.class;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Steps");
                }
                break;
            case R.id.nav_report:
                fragmentClass = ReportFragment.class;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Report");
                }
                break;
            case R.id.nav_map:
                fragmentClass = MapFragment.class;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Map");
                }
                break;
            case R.id.nav_score:
                fragmentClass = ScoreboardFragment.class;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Scoreboard");
                }
                break;
            default:
                fragmentClass = HomeFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        drawer.closeDrawers();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Ask for permission
    private void getActivity() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            return;        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION) {
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getActivity();
            } else {
                Toast.makeText(this,
                        R.string.step_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}