package com.e.nche;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PublicWatch extends AppCompatActivity {

    private EditText et_digits, et_status, et_registrationNumber;
    private TextView tv_status, tv_registrationNumber;
    private Button btn_submit, btn_scan;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<String> list_key = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_watch);

        et_digits = findViewById(R.id.editText_code);
        et_status = findViewById(R.id.editText_status);
        et_registrationNumber = findViewById(R.id.editText_registration_number);

        tv_status = findViewById(R.id.textView_status);
        tv_registrationNumber = findViewById(R.id.textView_registration_number);

        btn_submit = findViewById(R.id.button_submit);
        btn_scan = findViewById(R.id.button_qrScan);

        loginUser();

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_digits.getText().toString())) {
                    et_digits.setError("Required ...");
                } else {
                    final ProgressDialog mProgressDialog = new ProgressDialog(PublicWatch.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                    mProgressDialog.setTitle("Checking in database");
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.setCancelable(false);
                    String uniqueCode = et_digits.getText().toString();

                    if (list_key.contains(uniqueCode)){
                        readUsers(uniqueCode);
                    }else {
                        tv_status.setVisibility(View.VISIBLE);
                        et_status.setVisibility(View.VISIBLE);
                        tv_registrationNumber.setVisibility(View.GONE);
                        et_registrationNumber.setVisibility(View.GONE);

                        //background and text color
                        et_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
                        et_status.setTextColor(getResources().getColor(R.color.vTextColor));

                        et_status.setText("Not registered");
                    }
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PublicWatch.this, QrScan.class);
                intent.putExtra("ShowRegistrationStatus", "PublicWatch");
                startActivity(intent);
            }
        });

    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(PublicWatch.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.println(Log.ASSERT, "PublicWatch", "Log in");

                    uploadCollection();

                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void uploadCollection() {
        db.collection("Registered Vehicles").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                list_key.add(document.getId());
                            }
                        }else {
                            Log.println(Log.ASSERT, "Error login :", task.getException().toString());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void readUsers(String key) {
        db.collection("Registered Vehicles").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String registration_number = documentSnapshot.get("Vehicle_Registration_Number").toString();

                if (!registration_number.equals(null)){

                    tv_status.setVisibility(View.VISIBLE);
                    et_status.setVisibility(View.VISIBLE);
                    tv_registrationNumber.setVisibility(View.VISIBLE);
                    et_registrationNumber.setVisibility(View.VISIBLE);

                    //background and text color
                    et_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_verified));
                    et_status.setTextColor(getResources().getColor(R.color.vTextColor));

                    et_status.setText("Registered");
                    et_registrationNumber.setText(registration_number);

                    registration_number = null;
                }
            }
        });
    }
}
