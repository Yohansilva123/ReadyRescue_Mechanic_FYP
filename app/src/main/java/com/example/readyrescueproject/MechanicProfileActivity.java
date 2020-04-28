package com.example.readyrescueproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MechanicProfileActivity extends AppCompatActivity {

    private Button mLogout;
    private TextView mName, mNumber, mPoints;
    private String userId, userName, userNumber, userPoints;
    public static List<String> servicesRequested =  new ArrayList<>();

    private DataSnapshot mechanicDetails, serviceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //        Initialize variables
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

//        Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

//        Item Selected Listener

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(), MechanicMapActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_favourite:
                        startActivity(new Intent(getApplicationContext(), MechanicFavoritesActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(), MechanicProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_profile:
                        return true;
                }
                return false;
            }
        });
        getUserNameNumber();

        logOut();


    }

    private void getUserNameNumber() {
        mName = findViewById(R.id.mechanic_heading);
        mNumber = findViewById(R.id.number_heading);

        userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //        Setting the values

                if (dataSnapshot.exists()) {
                    mechanicDetails = dataSnapshot;
                    userName = dataSnapshot.child("Users").child("Mechanics").child(userId).child("Name").getValue().toString();
                    userNumber = dataSnapshot.child("Users").child("Mechanics").child(userId).child("Phone").getValue().toString();

                    mName.setText(userName);
                    mNumber.setText(userNumber);
                    selectServices(mechanicDetails);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void logOut() {
        mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MechanicProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    public void selectServices(DataSnapshot mechanicDetails) {
        mPoints = findViewById(R.id.services);
//        if (mechanicDetails != null) {
            servicesRequested.clear();
            serviceDetails = mechanicDetails.child("Users").child("Mechanics").child(userId).child("Services");

            String userId = FirebaseAuth.getInstance().getUid();

            if (Boolean.parseBoolean(serviceDetails.child("TireChange").getValue().toString()))
                servicesRequested.add("Tire Change");

            if (Boolean.parseBoolean(serviceDetails.child("BatteryJumpStart").getValue().toString()))
                servicesRequested.add("Battery Jump Start");

            if (Boolean.parseBoolean(serviceDetails.child("LockSmithService").getValue().toString()))
                servicesRequested.add("Lock Smith Services");

            if (Boolean.parseBoolean(serviceDetails.child("FuelDelivery").getValue().toString()))
                servicesRequested.add("Fuel, Oil, Coolant Delivery");

            if (Boolean.parseBoolean(serviceDetails.child("EngineIssues").getValue().toString()))
                servicesRequested.add("Engine Related Issues");

            if (Boolean.parseBoolean(serviceDetails.child("TowingServices").getValue().toString()))
                servicesRequested.add("Towing Services");

            mPoints.setText(servicesRequested.toString());
//        }
    }
}
