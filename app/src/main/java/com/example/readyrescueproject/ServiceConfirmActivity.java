package com.example.readyrescueproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readyrescueproject.utils.CalculatePrices;
import com.example.readyrescueproject.utils.CalculateTime;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServiceConfirmActivity extends AppCompatActivity {

    Dialog jobDialog, additionalServices;

    Button mCompleted, mCancel, mJobDone, mAdd, mAdditionalServiceBtn;
    private TextView mCustomerName, mServices, mTime, mPrice, mSeverity, mNotes, mPoints;
    private EditText mAdditionalService, mAdditionalCost;
    private String userID = "";
    public static String price = "20";
    public static String points = "0";
    public static String basePrice = "20";
    public static String priceFinal = "";
    private int i = 0;
    public static int[] time;
    private boolean jobCompleted = false;
    public static String services, service, addService;
    public static String addServiceCost = "0";

    public static List<String> servicesRequested =  new ArrayList<>();
    private DataSnapshot dbData;

    CalculateTime s = new CalculateTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_confirm);

//        get db details

        getDBPrices();

        //        Initialize variables
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

//        Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

//        Item Selected Listener

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        return true;
                    case R.id.navigation_favourite:
                        startActivity(new Intent(getApplicationContext(), MechanicFavoritesActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(), MechanicDashboardActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_profile:
                        startActivity(new Intent(getApplicationContext(), MechanicProfileActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
//        Initialize dialog box

        jobDialog = new Dialog(this);
        additionalServices = new Dialog(this);

//        get additional services
        additionalServices();

//        Method for job completion
        jobCompleted();

//        Method to cancel job
        cancelJob();

//        get job details

//        start timer
//        startTimer();
    }

    private void getDBPrices() {
        mServices = findViewById(R.id.service);
        mSeverity = findViewById(R.id.severity);
        mNotes = findViewById(R.id.additional_notes);
        services = MechanicMapActivity.servicesRequested.toString();
        mServices.setText(services);
        mSeverity.setText(MechanicMapActivity.severity);
        mNotes.setText(MechanicMapActivity.additionalNotes);

        if (services.contains("Tire Change"))
            service = "TireChange";
        if (services.contains("Battery Jump Start"))
            service = "BatteryJumpStart";
        if (services.contains("Lock Smith Services"))
            service = "LockSmithService";
        if (services.contains("Fuel, Oil, Coolant Delivery"))
            service = "FuelDelivery";
        if (services.contains("Engine Related Issues"))
            service = "EngineIssues";
        if (services.contains("Towing Services"))
            service = "TowingServices";

        final DatabaseReference serviceConfirmRef = FirebaseDatabase.getInstance().getReference();

        serviceConfirmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    dbData = dataSnapshot;
                    Map<String, Object> map = (Map <String, Object>) dataSnapshot.child("ProjectData").child("ServicePrices").child(service).getValue();
//                    Getting Mechanic working state and authorization
                    price = map.get("price").toString();
                    basePrice = map.get("BasePrice").toString();
                    try {
                        points = Objects.requireNonNull(dbData.child("Users").child("Mechanics").child(userID).child("JobDetails").child("jobPoints").getValue()).toString();
                    }
                    catch (Exception e){}
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void additionalServices() {
        mAdditionalServiceBtn = findViewById(R.id.addDetails);
        mAdditionalServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                additionalServices.setContentView(R.layout.activity_additional_details);
                mAdd = additionalServices.findViewById(R.id.add_button);
                mAdditionalService = additionalServices.findViewById(R.id.service_text);
                mAdditionalCost = additionalServices.findViewById(R.id.cost_text);
                mAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!(mAdditionalService.getText().toString().isEmpty())&&!(mAdditionalCost.getText().toString().isEmpty())){
                            addService = mAdditionalService.getText().toString();
                            addServiceCost = mAdditionalCost.getText().toString();

                            DatabaseReference serviceConfirmRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                            serviceConfirmRef.child("JobDetails").child("AdditionalServices").setValue(addService);
                            serviceConfirmRef.child("JobDetails").child("AdditionalServiceCost").setValue(addServiceCost);
                            additionalServices.dismiss();
                            Toast.makeText(ServiceConfirmActivity.this,"Additional Services have been added", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(ServiceConfirmActivity.this,"Enter both fields to proceed", Toast.LENGTH_LONG).show();
                    }
                });
                additionalServices.show();
            }
        });
    }

    private void jobCompleted() {
        mTime = findViewById(R.id.time);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        s = new CalculateTime();
        s.startThread();

        final DatabaseReference serviceConfirmRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);

        mCompleted = findViewById(R.id.complete_final);
        mCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConfirmRef.child("JobDetails").child("JobCompleted").setValue(true);
                time = s.getTime();
                s.stopThread();

                jobCompletedPopUp(serviceConfirmRef);
            }
        });
    }

    private void jobCompletedPopUp(DatabaseReference jobData) {
        jobDialog.setContentView(R.layout.activity_job_completed);
        mJobDone = jobDialog.findViewById(R.id.confirm_request);
        mCustomerName = jobDialog.findViewById(R.id.name_mechanic);
        mTime = jobDialog.findViewById(R.id.time);
        mPrice = jobDialog.findViewById(R.id.services);
        mPoints = jobDialog.findViewById(R.id.points);
        mCustomerName.setText(MechanicMapActivity.customersName);
        mTime.setText(time[0] + " : " + time[1] + " : " + time[2]);
        mPrice.setText("Rs. " + CalculatePrices.calculatePrice());
        mPoints.setText(points);
        jobData.child("JobDetails").child("Price").setValue(priceFinal);
        jobData.child("JobDetails").child("Time").setValue(time[0] + " : " + time[1] + " : " + time[2]);
        mJobDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobDialog.dismiss();
                Intent intent = new Intent(ServiceConfirmActivity.this, MechanicMapActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        jobDialog.show();
    }

    private void cancelJob() {

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference serviceConfirmRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);

        mCancel = findViewById(R.id.cancel_final);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConfirmRef.child("CustomerID").removeValue();
                serviceConfirmRef.child("CustomerName").removeValue();
                serviceConfirmRef.child("CustomerPhone").removeValue();
                serviceConfirmRef.child("JobDetails").removeValue();
//                customerId = "";
                serviceConfirmRef.child("CustomerAccepted").setValue(false);
//                customerRequestAccepted = false;

                Intent intent = new Intent(ServiceConfirmActivity.this, MechanicMapActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }


//    private String calculatePrice() {
//
//        int jTime = time[1];
//
//        if (jTime==0){
//            jTime = 1;
//        }
//
//        int finalPrice = Integer.parseInt(price)*jTime;
//
//        priceFinal = String.valueOf(finalPrice);
//
//        return priceFinal;
//    }
}
