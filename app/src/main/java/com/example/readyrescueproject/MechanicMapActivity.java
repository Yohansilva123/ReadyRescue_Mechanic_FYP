package com.example.readyrescueproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readyrescueproject.utils.DBDataValues;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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


public class MechanicMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mWorking, mArrived, mCancel, mDone;
    private RelativeLayout mLayout;
    private TextView mCustomerName, mCustomerNumber, mServices, mServiceType, mCustomerNamePopUp, mNotes, mSeverity;
    private int i = 0;

    private FusedLocationProviderClient mFusedLocationClient;

    private DBDataValues workingDetails = new DBDataValues();
    DataSnapshot wokring;

    private String customerId = "";
    public static String customersName = "";
    private String customersNumber = "";
    private String favCustomerId = "";
    private String favCustomersName = "";
    private String userID = "";
    private String userName = "";
    private String workingState = "";
    private String authorization = "";

    public static List<String> servicesRequested = new ArrayList<>();
    public static String severity, additionalNotes;
    private Boolean customerRequestAccepted = false;

    DataSnapshot dbDetails = null;

    Dialog jobDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

//        Mechanic starts working
        getWorkingState();
//        Identify the Customer
        getAssignedCustomer();
//            Start working or cancel the request
        proceedRequest();

//        getJobDetails
        jobDialog = new Dialog(this);
        jobDialog.setContentView(R.layout.activity_job_details);
        mDone = jobDialog.findViewById(R.id.done);
        serviceDetailsPopUp();
//        serviceDetailsPopUpClose();
        getJobDetails();

//        Verify Customer favorite request
        verifyCustomerFavRequest();

    }

    private void getWorkingState() {
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference workingRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
        workingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    Getting Mechanic working state and authorization
                    workingState = map.get("WorkingState").toString();
                    authorization = map.get("Authorization").toString();
                    startWorking();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });
        startWorking();
    }

    private void startWorking() {
        mWorking = findViewById(R.id.start_working);
//        According to the authorization the button will be enabled or disabled
        switch (authorization) {
            case "true":
                mWorking.setEnabled(true);
                break;
            case "false":
                Toast.makeText(MechanicMapActivity.this, "You haven't been Authorized yet", Toast.LENGTH_LONG).show();
                mWorking.setEnabled(false);
        }
        if (Boolean.parseBoolean(workingState)) {
            mWorking.setText("Stop Working");
        } else
            mWorking.setText("Start Working");

        mWorking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference startWorkingRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
                switch (workingState) {
//                    set working state in button and DB according to the button click
                    case "true":
                        startWorkingRef.child("WorkingState").setValue("false");
                        startWorkingRef.child("CustomerAccepted").setValue("false");
                        startWorkingRef.child("CustomerID").removeValue();
                        startWorkingRef.child("CustomerName").removeValue();
                        startWorkingRef.child("CustomerPhone").removeValue();
                        startWorkingRef.child("JobDetails").removeValue();
                        workingState = "false";
                        mWorking.setText("Start Working");
                        break;
                    case "false":
                        startWorkingRef.child("WorkingState").setValue("true");
                        workingState = "true";
                        mWorking.setText("Stop Working");
                        break;
                }
            }
        });
    }

    public void getAssignedCustomer() {
        mLayout = findViewById(R.id.customer_details);
        mCustomerName = findViewById(R.id.customer_text);
        mCustomerNumber = findViewById(R.id.customer_Number);
        mServices = findViewById(R.id.expand);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);

        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dbDetails = dataSnapshot;
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if ((map.get("CustomerID") != null) && (map.get("CustomerName") != null)) {
                        if (map.get("CustomerAccepted") != null) {
                            customerRequestAccepted = Boolean.parseBoolean(map.get("CustomerAccepted").toString());
                        }
                        customerId = map.get("CustomerID").toString();
                        customersName = map.get("CustomerName").toString();
                        customersNumber = map.get("CustomerPhone").toString();

                        if (workingState.equalsIgnoreCase("true") && customerRequestAccepted == false && !customerId.equalsIgnoreCase(""))
                            confirmRequest();
                    }
                    if (map.get("CustomerID") == null && !Boolean.parseBoolean(map.get("CustomerAccepted").toString())) {
                        customerRequestAccepted = false;
                        mLayout.setVisibility(View.INVISIBLE);
                        customerId = "";
                        mMap.clear();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void confirmRequest() {
//        mLayout = findViewById(R.id.customer_details);
        try {
            final DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);

            new AlertDialog.Builder(this)
                    .setTitle("Customer Request")
                    .setMessage(customersName + " is requesting for assistance")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            customerRequestRef.child("CustomerAccepted").setValue(true);
                            customerRequestAccepted = true;
                            getCustomerAssistanceLocation();
                            mLayout.setVisibility(View.VISIBLE);
                            mCustomerName.setText(customersName);
                            mCustomerNumber.setText(customersNumber);
                        }
                    })
                    .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            customerRequestRef.child("CustomerID").removeValue();
                            customerRequestRef.child("CustomerName").removeValue();
                            customerRequestRef.child("CustomerPhone").removeValue();
                            customerRequestRef.child("JobDetails").removeValue();
                            customerId = "";
                        }
                    })
                    .create()
                    .show();
        } catch (Exception e) {
        }
    }

    private void serviceDetailsPopUp() {

        mServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                severity = dbDetails.child("JobDetails").child("JobSeverity").getValue().toString();
                additionalNotes = dbDetails.child("JobDetails").child("AdditionalNotes").getValue().toString();
                mServiceType = jobDialog.findViewById(R.id.service_type);
                mCustomerNamePopUp = jobDialog.findViewById(R.id.name_customer);
                mNotes = jobDialog.findViewById(R.id.additional_notes);
                mSeverity = jobDialog.findViewById(R.id.severity);
                mServiceType.setText(servicesRequested.toString());
                mCustomerNamePopUp.setText(customersName);
                mNotes.setText(additionalNotes);
                mSeverity.setText(severity);

                mDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jobDialog.dismiss();
                    }
                });
                jobDialog.show();
            }
        });
    }

    private void getCustomerAssistanceLocation() {
        DatabaseReference customerAssistLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");

        customerAssistLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLog = 0;
//                    mRequest.setText("Looking for a Mechanic");
                    locationLat = Double.parseDouble(map.get(0).toString());
                    locationLog = Double.parseDouble(map.get(1).toString());

                    LatLng mechanicLatLng = new LatLng(locationLat, locationLog);
                    mMap.addMarker(new MarkerOptions().position(mechanicLatLng).title("The Assistance Location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void proceedRequest() {
        mArrived = findViewById(R.id.start_job);
        mCancel = findViewById(R.id.cancel_job);
        final DatabaseReference serviceConfirmRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);

        mArrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConfirmRef.child("JobDetails").child("JobStarted").setValue(true);
                Intent intent = new Intent(MechanicMapActivity.this, ServiceConfirmActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConfirmRef.child("CustomerID").removeValue();
                serviceConfirmRef.child("CustomerName").removeValue();
                serviceConfirmRef.child("CustomerPhone").removeValue();
                serviceConfirmRef.child("JobDetails").removeValue();
                customerId = "";
                serviceConfirmRef.child("CustomerAccepted").setValue(false);
                customerRequestAccepted = false;
                mLayout.setVisibility(View.INVISIBLE);
                mMap.clear();
            }
        });
    }

    private void getJobDetails() {
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference serviceConfirmRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID).child("JobDetails").child("CustomerServices");

        serviceConfirmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    servicesRequested.clear();
                    try {
                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                        for (int i = 0; i < map.size(); i++) {
                            servicesRequested.add(map.get(i).toString());
                        }
                    } catch (Exception e) {
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void verifyCustomerFavRequest() {
        DatabaseReference customerFavRequest = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID).child("CustomerFavoritesRequest");

        customerFavRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if ((map.get("CustomerID") != null) && (map.get("CustomerName") != null)) {
                        favCustomerId = map.get("CustomerID").toString();
                        favCustomersName = map.get("CustomerName").toString();
                        getMechanicName();
                        confirmFavRequest();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getMechanicName() {
        final DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("Users");

        customerRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("Mechanics").child(userID).child("Name").getValue().toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });
    }

    private void confirmFavRequest() {
        try {
            final DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("Users");

            new AlertDialog.Builder(this)
                    .setTitle("Customer's Favorite Request")
                    .setMessage(favCustomersName + " is requesting to add you as a favorite")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            customerRequestRef.child("Mechanics").child(userID).child("CustomerFavorites").child(favCustomerId).setValue(favCustomersName);
                            customerRequestRef.child("Customers").child(favCustomerId).child("MechanicFavorites").child(userID).setValue(userName);
                            customerRequestRef.child("Mechanics").child(userID).child("CustomerFavoritesRequest").removeValue();
                        }
                    })
                    .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            customerRequestRef.child("Mechanics").child(userID).child("CustomerFavoritesRequest").removeValue();
                        }
                    })
                    .create()
                    .show();
        } catch (Exception e) {
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            checkLocationPermission();
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            connectDriver();
            for (Location location : locationResult.getLocations()) {
                mLastLocation = location;

                LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("MechanicAvailable");

                GeoFire geoFireAvailable = new GeoFire(availableRef);
                geoFireAvailable.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) { }
                });

            }
        }
    };

    private void checkLocationPermission() {
        ActivityCompat.requestPermissions(MechanicMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }
                break;
            }
        }
    }

    public void connectDriver() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("MechanicAvailable");
        GeoFire geoFire = new GeoFire(availableRef);
        geoFire.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) { }
        });

    }
}
