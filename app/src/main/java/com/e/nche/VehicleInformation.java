package com.e.nche;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.e.nche.Email.Connection;
import com.e.nche.Email.GMailSender;
import com.e.nche.Email.GMailVerify;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;
import asia.kanopi.fingerscan.Status;

public class VehicleInformation extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int SCAN_FINGER = 0;
    byte[] img;
    Bitmap bm;
    boolean cameraPermission = false;
    boolean readStoragePermission = false;
    boolean writeStoragePermission = false;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    private Spinner et_vehicleType;
    private EditText et_vehicleModel, et_vehicleRoute, et_vehicleOperationUnit, et_vehicleRegistrationNumber, et_vehicleEngineNumber, et_vehicleTrackingNumber,
            et_ownerName, et_ownerCurrentAddress, et_ownerNationality, et_ownerOrigin, et_ownerGovernmentArea, et_ownerHomeTown, et_ownerPhoneNumber, et_ownerEmail,
            et_driverName, et_driverResidenceAddress, et_driverNationality, et_driverOrigin, et_driverGovernmentArea, et_driverHomeTown, et_driverPhoneNumber, et_driverEmail;
    private ImageView iv_capture, iv_fingerPrint;
    private Button btn_capture, btn_scan, btn_submit, btn_verifyOwnerEmail, btn_verifyDriverEmail;
    private int width, height;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> uniqueList = new ArrayList<>();

    private ImageButton ib_menu;

    //private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private Uri resultUri;
    private boolean scanCount = true;
    private Uri imageDownloadUrl;
    private Uri fingerDownloadUrl;
    private boolean connected = false;

    private List<String> list_key = new ArrayList<>();
    private List<String> list_name = new ArrayList<>();
    private List<String> list_pwd = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_information);

        requestWriteStoragePermission();
        requestReadStoragePermission();
        requestCameraPermission();

        if (checkInternet()) {
            connected = true;
            loginUser();
            myUniqueList();
        }

        //EditText Vehicle
        et_vehicleType = findViewById(R.id.editText_vehicle_type);
        et_vehicleModel = findViewById(R.id.editText_model);
        et_vehicleRoute = findViewById(R.id.editText_vehicle_route);
        et_vehicleOperationUnit = findViewById(R.id.editText_operation_unit);
        et_vehicleRegistrationNumber = findViewById(R.id.editText_registration_number);
        et_vehicleEngineNumber = findViewById(R.id.editText_engine_number);
        et_vehicleTrackingNumber = findViewById(R.id.editText_tracking_number);

        //EditText Owner
        et_ownerName = findViewById(R.id.editText_owner_name);
        et_ownerCurrentAddress = findViewById(R.id.editText_owner_current_address);
        et_ownerNationality = findViewById(R.id.editText_owner_nationality);
        et_ownerOrigin = findViewById(R.id.editText_owner_origin);
        et_ownerGovernmentArea = findViewById(R.id.editText_owner_govt_area);
        et_ownerHomeTown = findViewById(R.id.editText_owner_homeTown);
        et_ownerPhoneNumber = findViewById(R.id.editText_owner_phoneNumber);
        et_ownerEmail = findViewById(R.id.editText_owner_email);

        //EditText Driver
        et_driverName = findViewById(R.id.editText_driver_name);
        et_driverResidenceAddress = findViewById(R.id.editText_driver_ResidenceAddress);
        et_driverNationality = findViewById(R.id.editText_driver_Nationality);
        et_driverOrigin = findViewById(R.id.editText_driver_Origin);
        et_driverGovernmentArea = findViewById(R.id.editText_driver_Government_area);
        et_driverHomeTown = findViewById(R.id.editText_driver_Home_town);
        et_driverPhoneNumber = findViewById(R.id.editText_driver_phonenumber);
        et_driverEmail = findViewById(R.id.editText_driver_email);

        //ImageView
        iv_capture = findViewById(R.id.imageView_capture);
        iv_fingerPrint = findViewById(R.id.imageView_fingurePrint);

        //Button
        btn_capture = findViewById(R.id.button_capture);
        btn_scan = findViewById(R.id.button_scan);
        btn_submit = findViewById(R.id.button_submit);
        btn_verifyOwnerEmail = findViewById(R.id.button_verify_owner_email);
        btn_verifyDriverEmail = findViewById(R.id.button_verify_driver_email);

        ib_menu = findViewById(R.id.imageButton_menu);

        //et_vehicleType.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.verified, 0);

        ib_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(VehicleInformation.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_logout, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.logout) {
                            SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            myEdit.remove("CodeCoy_Vicab_Remember_User_Login");
                            myEdit.apply();

                            Intent logout = new Intent(VehicleInformation.this, UserLogin.class);
                            startActivity(logout);
                            finish();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        et_ownerEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textWatcher(et_ownerEmail, btn_verifyOwnerEmail);
                } else {
                    if (et_ownerEmail.getText().toString().length() == 0) {
                        et_ownerEmail.setError("");
                        btn_verifyOwnerEmail.setVisibility(View.GONE);
                    }
                }
            }
        });

        et_driverEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textWatcher(et_driverEmail, btn_verifyDriverEmail);
                } else {
                    if (et_driverEmail.getText().toString().length() == 0) {
                        et_driverEmail.setError("");
                        btn_verifyDriverEmail.setVisibility(View.GONE);
                    }
                }
            }
        });

        btn_verifyOwnerEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    verifyEmail(et_ownerEmail, btn_verifyOwnerEmail);
                } else {
                    if (checkInternet()) {
                        connected = true;
                        loginUser();
                        myUniqueList();
                    }
                }
            }
        });

        btn_verifyDriverEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    verifyEmail(et_driverEmail, btn_verifyDriverEmail);
                } else {
                    if (checkInternet()) {
                        connected = true;
                        loginUser();
                        myUniqueList();
                    }
                }
            }
        });

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //requestStoragePermission();
                btn_capture.setError(null);
                if (cameraPermission) {
                    if (readStoragePermission && writeStoragePermission) {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    } else {
                        requestWriteStoragePermission();
                        requestReadStoragePermission();
                    }

                } else {
                    requestCameraPermission();
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCount = true;

                bm = BitmapFactory.decodeResource(VehicleInformation.this.getResources(), R.drawable.capture_finger);

                Intent intent = new Intent(VehicleInformation.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_FINGER);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (connected) {
                    if (readStoragePermission && writeStoragePermission) {
                        if (notValidateInfo()) {
                            Toast.makeText(VehicleInformation.this, "Please complete all fields first to submit", Toast.LENGTH_SHORT).show();
                        } else {
                            saveUserInfo();
                        }
                    } else {
                        requestReadStoragePermission();
                        requestWriteStoragePermission();
                    }
                } else {
                    if (checkInternet()) {
                        connected = true;
                        loginUser();
                        myUniqueList();
                    }
                }

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_WRITE_PERMISSION_CODE);
                    }
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_READ_PERMISSION_CODE);
                    } else {
                        if (notValidateInfo()){
                            Toast.makeText(VehicleInformation.this,"Please complete all fields first to submit", Toast.LENGTH_SHORT).show();
                        }else {
                            saveUserInfo();
                        }
                    }
                } else {
                    if (notValidateInfo()){
                        Toast.makeText(VehicleInformation.this,"Please complete all fields first to submit", Toast.LENGTH_SHORT).show();
                    }else {
                        saveUserInfo();
                    }
                }*/
            }
        });
    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(VehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.println(Log.ASSERT, "UserLogin", "Log in");

                            databaseCollection();

                            mProgressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.ASSERT, "UserLogin", e.toString());
                    }
                });
    }

    private void databaseCollection() {
        list_key.clear();
        list_name.clear();
        list_pwd.clear();
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsers(key);
                    }
                } else {
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
        list_key.add(key);
        db.collection("Users").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("Name").toString();
                String pwd = documentSnapshot.get("Password").toString();

                if (!name.equals(null) && !pwd.equals(null)) {
                    list_name.add(name);
                    list_pwd.add(pwd);

                    name = null;
                    pwd = null;
                }
            }
        });
    }

    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(VehicleInformation.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(VehicleInformation.this);
        }
        return result;
    }

    private void requestCameraPermission() {
        Permissions.check(VehicleInformation.this, Manifest.permission.CAMERA, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                cameraPermission = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(VehicleInformation.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                Toast.makeText(VehicleInformation.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {

                new AlertDialog.Builder(VehicleInformation.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });


    }

    private void requestReadStoragePermission() {
        Permissions.check(VehicleInformation.this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                readStoragePermission = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(VehicleInformation.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                Toast.makeText(VehicleInformation.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {

                new AlertDialog.Builder(VehicleInformation.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });
    }

    private void requestWriteStoragePermission() {

        Permissions.check(VehicleInformation.this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                writeStoragePermission = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(VehicleInformation.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                Toast.makeText(VehicleInformation.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {

                new AlertDialog.Builder(VehicleInformation.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });
    }

    private void verifyEmail(final EditText et, final Button btn) {
        final ProgressDialog mProgressDialog = new ProgressDialog(VehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Verifying Email");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailVerify gMailVerify = new GMailVerify("bunubunudata@gmail.com", "Admin123@@");

                    gMailVerify.allMessagesCount();
                    gMailVerify.sendMail(getResources().getString(R.string.app_name),
                            "This email sent by Nche system to ensures that this email is valid or not.",
                            "bunubunudata@gmail.com",
                            et.getText().toString());

                    if (gMailVerify.sent) {
                        Log.println(Log.ASSERT, "verifiedLog", "Successful");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI(et, btn, true);
                            }
                        });
                        mProgressDialog.dismiss();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI(et, btn, false);
                            }
                        });
                        Log.println(Log.ASSERT, "verifiedLog", "Error");
                        mProgressDialog.dismiss();
                    }

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(et, btn, false);
                        }
                    });
                    mProgressDialog.dismiss();
                    Log.println(Log.ASSERT, "verifiedLogFinal", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        sender.start();
    }

    private void updateUI(EditText et, Button btn, boolean verified) {
        if (verified) {
            btn.setVisibility(View.GONE);
            //et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.verified, 0);
            Drawable myIcon = getResources().getDrawable(R.drawable.verified);
            myIcon.setBounds(0, 0, myIcon.getIntrinsicWidth(), myIcon.getIntrinsicHeight());
            et.setError("Valid", myIcon);
        } else {
            et.setError("Email not verified");
        }
    }

    private boolean notValidateInfo() {
        boolean result = true;

        if (et_driverEmail.getText().toString().equals("")) {
            et_driverEmail.setError("");
            et_driverEmail.requestFocus();
            result = false;
        }
        if (et_driverPhoneNumber.getText().toString().equals("")) {
            et_driverPhoneNumber.setError("");
            et_driverPhoneNumber.requestFocus();
            result = false;
        }
        if (!scanCount) {
            btn_scan.setError("Scan not confirmed");
            iv_fingerPrint.requestFocus();
            result = true;
        }
        if (iv_capture.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.capture_image).getConstantState()) {
            btn_capture.setError("Image not attached");
            iv_capture.requestFocus();
            result = false;
        }
        if (et_driverHomeTown.getText().toString().equals("")) {
            et_driverHomeTown.setError("");
            et_driverHomeTown.requestFocus();
            result = false;
        }
        if (et_driverGovernmentArea.getText().toString().equals("")) {
            et_driverGovernmentArea.setError("");
            et_driverGovernmentArea.requestFocus();
            result = false;
        }
        if (et_driverOrigin.getText().toString().equals("")) {
            et_driverOrigin.setError("");
            et_driverOrigin.requestFocus();
            result = false;
        }
        if (et_driverNationality.getText().toString().equals("")) {
            et_driverNationality.setError("");
            et_driverNationality.requestFocus();
            result = false;
        }
        if (et_driverResidenceAddress.getText().toString().equals("")) {
            et_driverResidenceAddress.setError("");
            et_driverResidenceAddress.requestFocus();
            result = false;
        }
        if (et_driverName.getText().toString().equals("")) {
            et_driverName.setError("");
            et_driverName.requestFocus();
            result = false;
        }
        if (et_ownerEmail.getText().toString().equals("")) {
            et_ownerEmail.setError("");
            et_ownerEmail.requestFocus();
            result = false;
        }
        if (et_ownerPhoneNumber.getText().toString().equals("")) {
            et_ownerPhoneNumber.setError("");
            et_ownerPhoneNumber.requestFocus();
            result = false;
        }
        if (et_ownerHomeTown.getText().toString().equals("")) {
            et_ownerHomeTown.setError("");
            et_ownerHomeTown.requestFocus();
            result = false;
        }
        if (et_ownerGovernmentArea.getText().toString().equals("")) {
            et_ownerGovernmentArea.setError("");
            et_ownerGovernmentArea.requestFocus();
            result = false;
        }
        if (et_ownerOrigin.getText().toString().equals("")) {
            et_ownerOrigin.setError("");
            et_ownerOrigin.requestFocus();
            result = false;
        }
        if (et_ownerNationality.getText().toString().equals("")) {
            et_ownerNationality.setError("");
            et_ownerNationality.requestFocus();
            result = false;
        }
        if (et_ownerCurrentAddress.getText().toString().equals("")) {
            et_ownerCurrentAddress.setError("");
            et_ownerCurrentAddress.requestFocus();
            result = false;
        }
        if (et_ownerName.getText().toString().equals("")) {
            et_ownerName.setError("");
            et_ownerName.requestFocus();
            result = false;
        }
        if (et_vehicleTrackingNumber.getText().toString().equals("")) {
            et_vehicleTrackingNumber.setError("");
            et_vehicleTrackingNumber.requestFocus();
            result = false;
        }
        if (et_vehicleEngineNumber.getText().toString().equals("")) {
            et_vehicleEngineNumber.setError("");
            et_vehicleEngineNumber.requestFocus();
            result = false;
        }
        if (et_vehicleRegistrationNumber.getText().toString().equals("")) {
            et_vehicleRegistrationNumber.setError("");
            et_driverHomeTown.requestFocus();
            result = false;
        }
        if (et_vehicleOperationUnit.getText().toString().equals("")) {
            et_vehicleOperationUnit.setError("");
            et_vehicleOperationUnit.requestFocus();
            result = false;
        }
        if (et_vehicleRoute.getText().toString().equals("")) {
            et_vehicleRoute.setError("");
            et_vehicleRoute.requestFocus();
            result = false;
        }
        if (et_vehicleModel.getText().toString().equals("")) {
            et_vehicleModel.setError("");
            et_vehicleModel.requestFocus();
            result = false;
        }
        if (et_vehicleType.getSelectedItem().toString().equals("Select One")) {
            ((TextView) et_vehicleType.getSelectedView()).setError("");
            et_vehicleType.requestFocus();
            findViewById(R.id.textView_vehicle_type).requestFocus();
            result = false;
        }

        return !result;
    }

    private void saveUserInfo() {
        String uniqueId = getRandomUnique();

//        uniqueId = et_vehicleRegistrationNumber.getText().toString();
        if (uniqueList.contains(uniqueId)) {
            saveUserInfo();
//            uniqueId = getRandomUnique();
//            Toast.makeText(this, "An error has occurred, Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!uniqueList.contains(uniqueId)) {
            QRGEncoder qrgEncoder = new QRGEncoder(uniqueId, null, QRGContents.Type.TEXT, 500);
            try {
                Bitmap qrBits = qrgEncoder.getBitmap();
                //iv_fingerPrint.setImageBitmap(qrBits);

                try {
                    boolean save = new QRGSaver().save(savePath, uniqueId, qrBits, QRGContents.ImageType.IMAGE_JPEG);
                    String result = save ? "QR attached in email" : "QR not attached in email";
//                    Toast.makeText(VehicleInformation.this, result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.println(Log.ASSERT, "QR Error", e.getMessage());
            }

            Log.println(Log.ASSERT, "Unique ", uniqueId + "");
            String filePath = savePath + uniqueId + ".jpg";

            sendEmail(uniqueId, filePath);
        }
    }

    private void sendEmail(final String uniqueId, final String filePath) {
        final ProgressDialog mProgressDialog = new ProgressDialog(VehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Sending Email");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender gMailSender = new GMailSender("bunubunudata@gmail.com", "Admin123@@");

                    gMailSender.allMessagesCount();
//                    gMailSender.addAttachment(filePath);
                    gMailSender.sendMail(getResources().getString(R.string.app_name),
                            "Thanks for your registration. We shall contact you shortly. Your Unique Code is: " + uniqueId,
                            "bunubunudata@gmail.com",
                            et_ownerEmail.getText().toString() + "," + et_driverEmail.getText().toString());

                    if (gMailSender.sent) {
                        Log.println(Log.ASSERT, "sendingLog", "Successful");
                        mProgressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uploadImage(uniqueId);
                            }
                        });
                    } else {
                        mProgressDialog.dismiss();
//                        Toast.makeText(VehicleInformation.this, "Error sending Email", Toast.LENGTH_SHORT).show();
                        Log.println(Log.ASSERT, "sendingLog", "Error");
                    }
                } catch (Exception e) {
                    mProgressDialog.dismiss();
//                    Toast.makeText(VehicleInformation.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.println(Log.ASSERT, "sendingLog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();

    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(VehicleInformation.this)
                .setTitle("Alert")
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
//                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void uploadImage(final String uniqueId) {
        //Upload image to storage
        final ProgressDialog mProgressDialog = new ProgressDialog(VehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Uploading Driver Image");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("driver images/" + uniqueId + "/" + uniqueId);
        Uri file = resultUri;

        sRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageDownloadUrl = taskSnapshot.getUploadSessionUri();

                        if (imageDownloadUrl != null) {
                            String url = imageDownloadUrl.toString();
                            mProgressDialog.dismiss();
//                            uploadFingerPrint(uniqueId, url);
                            updateDatabase(uniqueId, url, "");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.ASSERT, "Storage", "Error");
                    }
                });
    }

    private void uploadFingerPrint(final String uniqueId, final String url) {
        //Upload image to storage
        final ProgressDialog mProgressDialog = new ProgressDialog(VehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Uploading Finger Print");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("driver images/" + uniqueId + "/" + "finger" + uniqueId);

        Uri file;
        if (bm != null) {
            file = getImageUri(VehicleInformation.this, bm);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(VehicleInformation.this.getResources(), R.drawable.capture_finger);
            file = getImageUri(VehicleInformation.this, bitmap);
        }

        sRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fingerDownloadUrl = taskSnapshot.getUploadSessionUri();

                        if (fingerDownloadUrl != null) {
                            String fingerUrl = fingerDownloadUrl.toString();
                            mProgressDialog.dismiss();
                            updateDatabase(uniqueId, url, fingerUrl);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.ASSERT, "Storage", "Error");
                    }
                });

    }

    private void updateDatabase(String uniqueId, String imageUrl, String fingerUrl) {
        final ProgressDialog mProgressDialog = new ProgressDialog(VehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Finalizing");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        db.collection("randoms").document(uniqueId).set(new HashMap<String, Object>(), SetOptions.merge());

        //String imageId = db.collection("randoms").document().getId();
        Map<String, Object> note = new HashMap<>();

        //User
        note.put("User", getIntent().getStringExtra("UserName"));
        note.put("Id", uniqueId);

        //Vehicle
        note.put("Vehicle_Type", et_vehicleType.getSelectedItem().toString().trim());
        note.put("Vehicle_Model", et_vehicleModel.getText().toString().trim());
        note.put("Vehicle_Route", et_vehicleRoute.getText().toString().trim());
        note.put("Vehicle_Operation_Unit", et_vehicleOperationUnit.getText().toString().trim());
        note.put("Vehicle_Registration_Number", et_vehicleRegistrationNumber.getText().toString().trim());
        note.put("Vehicle_Engine_Number", et_vehicleEngineNumber.getText().toString().trim());
        note.put("Vehicle_Tracking_Number", et_vehicleTrackingNumber.getText().toString().trim());

        //Owner
        note.put("Owner_Name", et_ownerName.getText().toString().trim());
        note.put("Owner_Current_Address", et_ownerCurrentAddress.getText().toString().trim());
        note.put("Owner_Nationality", et_ownerNationality.getText().toString().trim());
        note.put("Owner_Origin", et_ownerOrigin.getText().toString().trim());
        note.put("Owner_Government_Area", et_ownerGovernmentArea.getText().toString().trim());
        note.put("Owner_Home_Town", et_ownerHomeTown.getText().toString().trim());
        note.put("Owner_Phone_Number", et_ownerPhoneNumber.getText().toString().trim());
        note.put("Owner_Email", et_ownerEmail.getText().toString().trim());

        //Driver
        note.put("Driver_Name", et_driverName.getText().toString().trim());
        note.put("Driver_Residence_Address", et_driverResidenceAddress.getText().toString().trim());
        note.put("Driver_Nationality", et_driverNationality.getText().toString().trim());
        note.put("Driver_Origin", et_driverOrigin.getText().toString().trim());
        note.put("Driver_Government_Area", et_driverGovernmentArea.getText().toString().trim());
        note.put("Driver_Home_Town", et_driverHomeTown.getText().toString().trim());
        note.put("Driver_Image", imageUrl);
        note.put("Driver_Finger_Print", fingerUrl);
        note.put("Driver_Phone_Number", et_driverPhoneNumber.getText().toString().trim());
        note.put("Driver_Email", et_driverEmail.getText().toString().trim());

        Date currentTime = Calendar.getInstance().getTime();
        Log.e("DateTime", "" + currentTime);
        note.put("Date_Time", currentTime.toString());

        db.collection("Registered Vehicles").document(uniqueId).set(note)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            new AlertDialog.Builder(VehicleInformation.this)
                                    .setTitle("Vehicle Registered")
                                    .setMessage("You have successfully registered a vehicle.")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .show();
                            emptyFields();
                            mProgressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(VehicleInformation.this, "Not Registered", Toast.LENGTH_LONG).show();
                        Log.println(Log.ASSERT, "Error in DB", e.toString());
                    }
                });
    }

    private void emptyFields() {
        et_vehicleType.setSelection(0);
        et_vehicleModel.setText("");
        et_vehicleRoute.setText("");
        et_vehicleOperationUnit.setText("");
        et_vehicleRegistrationNumber.setText("");
        et_vehicleEngineNumber.setText("");
        et_vehicleTrackingNumber.setText("");

        et_ownerName.setText("");
        et_ownerCurrentAddress.setText("");
        et_ownerNationality.setText("");
        et_ownerOrigin.setText("");
        et_ownerHomeTown.setText("");
        et_ownerPhoneNumber.setText("");
        et_ownerEmail.setText("");

        et_driverName.setText("");
        et_driverResidenceAddress.setText("");
        et_driverNationality.setText("");
        et_driverOrigin.setText("");
        et_driverGovernmentArea.setText("");
        et_driverHomeTown.setText("");
        et_driverPhoneNumber.setText("");
        et_driverEmail.setText("");
        scanCount = false;

        iv_capture.setImageDrawable(getResources().getDrawable(R.drawable.capture_image));
        iv_fingerPrint.setImageDrawable(getResources().getDrawable(R.drawable.capture_finger));

        et_ownerEmail.setError(null);
        et_driverEmail.setError(null);
        btn_scan.setError(null);
        btn_capture.setError(null);
        btn_verifyDriverEmail.setError(null);
        btn_verifyOwnerEmail.setError(null);
        btn_verifyOwnerEmail.setVisibility(View.GONE);
        btn_verifyDriverEmail.setVisibility(View.GONE);
    }

    private void myUniqueList() {
        uniqueList.clear();
        db.collection("randoms").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.println(Log.ASSERT, "List ", "Loaded Successfully");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        uniqueList.add(document.getId());
                    }
                }
            }
        });
    }

    private void textWatcher(final EditText et, final Button btn) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidEmailId(et.getText().toString().trim()) && s.length() <= 0) {
                    //Toast.makeText(getApplicationContext(),"Invalid email address",Toast.LENGTH_SHORT).show();
                    et.setError("Invalid Email");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isValidEmailId(et.getText().toString().trim()) && s.length() > 0) {
                    Toast.makeText(getApplicationContext(), "valid email address", Toast.LENGTH_SHORT).show();
                    btn.setVisibility(View.VISIBLE);
                } else {
                    //Toast.makeText(getApplicationContext(),"Invalid email address",Toast.LENGTH_SHORT).show();
                    et.setError("Invalid Email");
                }
            }
        });
    }

    private boolean isValidEmailId(String email) {

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    private String getRandomUnique() {

        //Log.println(Log.ASSERT, "Randoms ", id);

        return String.format("%06d", new Random().nextInt(1000000));
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestWriteStoragePermission();
        requestReadStoragePermission();
        requestCameraPermission();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(VehicleInformation.this)
                .setMessage("Please select a option.")
                .setPositiveButton("Main Menu", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(VehicleInformation.this, UserLogin.class);
                        intent.putExtra("BackPress", "OK");
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //super.onBackPressed();
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    width = iv_capture.getWidth();
                    height = iv_capture.getWidth();
                    iv_capture.setImageBitmap(null);

                    photo = Bitmap.createScaledBitmap(photo, width, height, false);

                    resultUri = getImageUri(VehicleInformation.this, photo);

                    iv_capture.setImageBitmap(photo);
                }
            }
        }

        int status;
        String errorMesssage;
        if (requestCode == SCAN_FINGER) {
            if (resultCode == RESULT_OK) {
                status = data.getIntExtra("status", Status.ERROR);
                if (status == Status.SUCCESS) {
                    Log.println(Log.ASSERT, "Fingerprint", "Fingerprint captured");
                    img = data.getByteArrayExtra("img");
                    bm = BitmapFactory.decodeByteArray(img, 0, img.length);
                    iv_fingerPrint.setImageBitmap(bm);
                } else {
                    bm = BitmapFactory.decodeResource(VehicleInformation.this.getResources(), R.drawable.capture_finger);
                    errorMesssage = data.getStringExtra("errorMessage");
                    Toast.makeText(VehicleInformation.this, errorMesssage, Toast.LENGTH_LONG).show();
                    Log.println(Log.ASSERT, "Fingerprint", "-- Error: " + errorMesssage + " --");
                }
            }
        }
    }
}