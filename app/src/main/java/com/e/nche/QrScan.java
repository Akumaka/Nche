package com.e.nche;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
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
import com.google.zxing.Result;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

public class QrScan extends AppCompatActivity {

    private CodeScannerView scannerView;
    private CodeScanner codeScanner;

    private TextView tv_qrResult, tv_qrRegistration;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<String> list_key = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        scannerView = findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(this, scannerView);

        tv_qrResult = findViewById(R.id.textView_qr_result);
        tv_qrRegistration = findViewById(R.id.textView_qr_registration);

        requestForCameraPermission();

        loginUser();

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_qrResult.setVisibility(View.VISIBLE);

                        if (getIntent().getStringExtra("ShowRegistrationStatus").equals("PublicWatch")){
                            showStatus(result.getText());
                        }else {
                            showAgentStatus(result.getText());
                        }
                    }
                });
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_qrResult.setVisibility(View.GONE);
                tv_qrRegistration.setVisibility(View.GONE);
                codeScanner.startPreview();
            }
        });

    }

    private void showAgentStatus(String uniqueCode) {
        if (list_key.contains(uniqueCode)){
            Intent intent = new Intent(QrScan.this, ShowComplain.class);
            intent.putExtra("ShowComplainId", uniqueCode);
            startActivity(intent);
        }else {
            tv_qrResult.setVisibility(View.VISIBLE);
            tv_qrRegistration.setVisibility(View.GONE);

            //background and text color
            tv_qrResult.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
            tv_qrResult.setTextColor(getResources().getColor(R.color.vTextColor));

            tv_qrResult.setText("Not registered");
        }
    }

    private void showStatus(String uniqueCode) {
        if (list_key.contains(uniqueCode)){
            readUsers(uniqueCode);
        }else {
            tv_qrResult.setVisibility(View.VISIBLE);
            tv_qrRegistration.setVisibility(View.GONE);

            //background and text color
            tv_qrResult.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
            tv_qrResult.setTextColor(getResources().getColor(R.color.vTextColor));

            tv_qrResult.setText("Not registered");
        }
    }

    private void requestForCameraPermission() {
        Permissions.check(QrScan.this, Manifest.permission.CAMERA, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                codeScanner.startPreview();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(QrScan.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                Toast.makeText(QrScan.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {
                Toast.makeText(QrScan.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });
    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(QrScan.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
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

                    tv_qrResult.setVisibility(View.VISIBLE);
                    tv_qrRegistration.setVisibility(View.VISIBLE);

                    //background and text color
                    tv_qrResult.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_verified));
                    tv_qrResult.setTextColor(getResources().getColor(R.color.vTextColor));

                    tv_qrResult.setText("Registered");
                    tv_qrRegistration.setText(registration_number);

                    registration_number = null;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestForCameraPermission();
    }
}
