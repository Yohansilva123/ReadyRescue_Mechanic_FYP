package com.example.readyrescueproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MechanicRegisterActivity extends AppCompatActivity {
    private EditText mEmail, mPassword, mName, mPhone;
    private Button mLogin, mRegister;
    private CheckBox mTire, mBattery, mLockSmith, mFuel, mEngine, mTowing;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;

    private String user_id;
    private boolean servicesSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_register);
        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    Intent intent = new Intent(MechanicRegisterActivity.this, MechanicMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mName = findViewById(R.id.name);
        mPhone = findViewById(R.id.phone_number);

        mLogin = findViewById(R.id.login);
        mRegister = findViewById(R.id.register);
        mTire = findViewById(R.id.tire_change);
        mBattery = findViewById(R.id.battery_jump_start);
        mLockSmith = findViewById(R.id.lock_smith_services);
        mFuel = findViewById(R.id.fuel_delivery);
        mEngine = findViewById(R.id.engine_issues);
        mTowing = findViewById(R.id.towing_services);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                final String phone = mPhone.getText().toString();

                if (email.isEmpty())
                    Toast.makeText(MechanicRegisterActivity.this,"Email required", Toast.LENGTH_SHORT).show();
                if (password.isEmpty())
                    Toast.makeText(MechanicRegisterActivity.this,"Password required", Toast.LENGTH_SHORT).show();
                if (name.isEmpty())
                    Toast.makeText(MechanicRegisterActivity.this,"Name required", Toast.LENGTH_SHORT).show();
                if (phone.isEmpty())
                    Toast.makeText(MechanicRegisterActivity.this,"Phone Number required", Toast.LENGTH_SHORT).show();

                if (!(mTire.isChecked()||mBattery.isChecked()||mLockSmith.isChecked()||mFuel.isChecked()||mEngine.isChecked()||mTowing.isChecked())){
                    Toast.makeText(MechanicRegisterActivity.this,"At lest on service is required", Toast.LENGTH_SHORT).show();
                }else
                    servicesSelected = true;

                if (!email.isEmpty()&&!password.isEmpty()&&!name.isEmpty()&&!phone.isEmpty()&&servicesSelected){
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MechanicRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MechanicRegisterActivity.this,"sign up error", Toast.LENGTH_LONG).show();
                            }
                            else{
                                user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id);
                                current_user_db.setValue(true);
                                DatabaseReference userName = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Name");
                                userName.setValue(name);
                                DatabaseReference number = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Phone");
                                number.setValue(phone);

                                setMechanicServices();

                                DatabaseReference auth = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Authorization");
                                auth.setValue(false);

                                DatabaseReference working = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("WorkingState");
                                working.setValue(false);

                                DatabaseReference customerAccept = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("CustomerAccepted");
                                customerAccept.setValue(false);
                            }
                        }
                    });
                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MechanicRegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    public void setMechanicServices(){

        DatabaseReference tireChange = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Services").child("TireChange");
        if (mTire.isChecked())
            tireChange.setValue(true);
        else
            tireChange.setValue(false);

        DatabaseReference batteryChange = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Services").child("BatteryJumpStart");
        if (mBattery.isChecked())
            batteryChange.setValue(true);
        else
            batteryChange.setValue(false);

        DatabaseReference lockSmithService = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Services").child("LockSmithService");
        if (mLockSmith.isChecked())
            lockSmithService.setValue(true);
        else
            lockSmithService.setValue(false);

        DatabaseReference FuelDelivery = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Services").child("FuelDelivery");
        if (mFuel.isChecked())
            FuelDelivery.setValue(true);
        else
            FuelDelivery.setValue(false);

        DatabaseReference EngineIssues = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Services").child("EngineIssues");
        if (mEngine.isChecked())
            EngineIssues.setValue(true);
        else
            EngineIssues.setValue(false);

        DatabaseReference towingServices = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id).child("Services").child("TowingServices");
        if (mTowing.isChecked())
            towingServices.setValue(true);
        else
            towingServices.setValue(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListner);
    }
}
