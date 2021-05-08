package com.e.nche;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import asia.kanopi.fingerscan.Status;

public class UpdateVehicleInformation extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int SCAN_FINGER = 0;
    byte[] img;
    Bitmap bm;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    private Spinner et_vehicleType;
    private EditText et_vehicleModel, et_vehicleRoute, et_vehicleOperationUnit, et_vehicleRegistrationNumber, et_vehicleEngineNumber, et_vehicleTrackingNumber,
            et_ownerName, et_ownerCurrentAddress, et_ownerNationality, et_ownerOrigin, et_ownerGovernmentArea, et_ownerHomeTown, et_ownerPhoneNumber, et_ownerEmail,
            et_driverName, et_driverResidenceAddress, et_driverNationality, et_driverOrigin, et_driverGovernmentArea, et_driverHomeTown, et_driverPhoneNumber, et_driverEmail;
    private ImageView iv_capture, iv_fingerPrint;
    private Button btn_capture, btn_scan, btn_update, btn_delete, btn_verifyOwnerEmail, btn_verifyDriverEmail;
    private int width, height;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> uniqueList = new ArrayList<>();
    private Uri resultUri;
    private boolean scanCount = false;
    private String infoKey, status;
    private Uri imageDownloadUrl;
    //    private Uri fingerDownloadUrl;
    private Uri imageUri, fingerUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_vehicle_information);

        Intent intent = getIntent();
        infoKey = intent.getStringExtra("InfoKey");
        status = intent.getStringExtra("status");

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
        btn_update = findViewById(R.id.button_update);
        btn_delete = findViewById(R.id.button_delete);
        btn_verifyOwnerEmail = findViewById(R.id.button_verify_owner_email);
        btn_verifyDriverEmail = findViewById(R.id.button_verify_driver_email);

        if (status.equals("SuperAdmin"))
            btn_delete.setVisibility(View.VISIBLE);

        downloadInfo();

        et_ownerEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    textWatcher(et_ownerEmail, btn_verifyOwnerEmail);
                else {
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

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCount = true;

                bm = BitmapFactory.decodeResource(UpdateVehicleInformation.this.getResources(), R.drawable.capture_finger);

                Intent intent = new Intent(UpdateVehicleInformation.this, ScanActivity.class);
                startActivityForResult(intent, SCAN_FINGER);
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (notValidateInfo()) {
                    Toast.makeText(UpdateVehicleInformation.this, "Please complete all fields first to update", Toast.LENGTH_SHORT).show();
                } else {
                    updateImage();
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(UpdateVehicleInformation.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(imageUri));
//                                final StorageReference fingerRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(fingerUri));

                                imageRef.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.println(Log.ASSERT, "Storage", "delete");

//                                                fingerRef.delete();

                                                Log.println(Log.ASSERT, "Infokey", "" + infoKey);
                                                db.collection("Registered Vehicles").document(infoKey).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.e("Deleted", "Successful");
                                                                Toast.makeText(UpdateVehicleInformation.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
//                                                                finish();
//                                                                startActivity(new Intent(UpdateVehicleInformation.this, AdminVehicles.class));
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.println(Log.ASSERT, "Deleted", "Error " + e.toString());
                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.println(Log.ASSERT, "Storage", "Error " + e.toString());
                                            }
                                        });
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void updateImage() {
        final ProgressDialog mProgressDialog = new ProgressDialog(UpdateVehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Uploading Driver Image");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("driver images/" + infoKey + "/" + infoKey);

        Uri file;
        if (resultUri != null) {
            file = resultUri;
        } else {
            //Drawable drawable = iv_capture.getDrawable();
            Bitmap bm = ((BitmapDrawable) iv_capture.getDrawable()).getBitmap();
            //Bitmap bitmap = BitmapFactory.decodeResource(UpdateVehicleInformation.this.getResources(), R.drawable.capture_finger);
            file = getImageUri(UpdateVehicleInformation.this, bm);
        }

        sRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageDownloadUrl = taskSnapshot.getUploadSessionUri();

                        if (imageDownloadUrl != null) {
                            String url = imageDownloadUrl.toString();
                            mProgressDialog.dismiss();
//                            uploadFingerPrint(infoKey, url);
                            updateDatabase(infoKey, url, "");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.ASSERT, "Storage", "UpdateError");
                    }
                });
    }

//    private void uploadFingerPrint(final String uniqueId, final String url) {
//        //Upload image to storage
//        final ProgressDialog mProgressDialog = new ProgressDialog(UpdateVehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
//        mProgressDialog.setTitle("Uploading Finger Print");
//        mProgressDialog.setMessage("Please wait ...");
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.show();
//        final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("driver images/" + uniqueId + "/" + "finger" + uniqueId);
//
//        Uri file;
//
//        if (bm != null) {
//            file = getImageUri(UpdateVehicleInformation.this, bm);
//        } else {
//            Bitmap bitmap = BitmapFactory.decodeResource(UpdateVehicleInformation.this.getResources(), R.drawable.capture_finger);
//            file = getImageUri(UpdateVehicleInformation.this, bitmap);
//        }
//
//        sRef.putFile(file)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        fingerDownloadUrl = taskSnapshot.getUploadSessionUri();
//
//                        if (fingerDownloadUrl != null) {
//                            String fingerUrl = fingerDownloadUrl.toString();
//                            mProgressDialog.dismiss();
//                            updateDatabase(uniqueId, url, fingerUrl);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.println(Log.ASSERT, "Storage", "UpdateError");
//                    }
//                });
//
//    }

    private void updateDatabase(String uniqueId, String imageUrl, String fingerUrl) {
        final ProgressDialog mProgressDialog = new ProgressDialog(UpdateVehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Finalizing");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        db.collection("randoms").document(uniqueId).set(new HashMap<String, Object>(), SetOptions.merge());

        //String imageId = db.collection("randoms").document().getId();
        Map<String, Object> note = new HashMap<>();

       /* User
        note.put("User", getIntent().getStringExtra("UserName"));
        note.put("Id", uniqueId);*/

        //Vehicle
        note.put("Vehicle_Type", et_vehicleType.getSelectedItem().toString());
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

        db.collection("Registered Vehicles").document(uniqueId).update(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        new AlertDialog.Builder(UpdateVehicleInformation.this)
                                .setTitle("Update")
                                .setMessage("Updated successfully.")
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                        mProgressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(UpdateVehicleInformation.this, "Not Update" + e.toString(), Toast.LENGTH_LONG).show();
                        Log.println(Log.ASSERT, "Error in DB", e.toString());
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
                    // Toast.makeText(getApplicationContext(),"valid email address",Toast.LENGTH_SHORT).show();
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
            //btn_scan.setError("Scan not confirmed");
            //iv_fingerPrint.requestFocus();
            result = true;
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
            //et_vehicleType.requestFocus();
            findViewById(R.id.textView_vehicle_type).requestFocus();
            result = false;
        }

        return !result;
    }

    private void downloadInfo() {
        final ProgressDialog mProgressDialog = new ProgressDialog(UpdateVehicleInformation.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Loading Information");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        db.collection("Registered Vehicles").document(infoKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                //Vehicle
                String Vehicle_Type = documentSnapshot.get("Vehicle_Type").toString();
                String Vehicle_Model = documentSnapshot.get("Vehicle_Model").toString();
                String Vehicle_Route = documentSnapshot.get("Vehicle_Route").toString();
                String Vehicle_Operation_Unit = documentSnapshot.get("Vehicle_Operation_Unit").toString();
                String Vehicle_Registration_Number = documentSnapshot.get("Vehicle_Registration_Number").toString();
                String Vehicle_Engine_Number = documentSnapshot.get("Vehicle_Engine_Number").toString();
                String Vehicle_Tracking_Number = documentSnapshot.get("Vehicle_Tracking_Number").toString();

                //Owner
                String Owner_Name = documentSnapshot.get("Owner_Name").toString();
                String Owner_Current_Address = documentSnapshot.get("Owner_Current_Address").toString();
                String Owner_Nationality = documentSnapshot.get("Owner_Nationality").toString();
                String Owner_Origin = documentSnapshot.get("Owner_Origin").toString();
                String Owner_Government_Area = documentSnapshot.get("Owner_Government_Area").toString();
                String Owner_Home_Town = documentSnapshot.get("Owner_Home_Town").toString();
                String Owner_Phone_Number = documentSnapshot.get("Owner_Phone_Number").toString();
                String Owner_Email = documentSnapshot.get("Owner_Email").toString();

                //Driver
                String Driver_Name = documentSnapshot.get("Driver_Name").toString();
                String Driver_Residence_Address = documentSnapshot.get("Driver_Residence_Address").toString();
                String Driver_Nationality = documentSnapshot.get("Driver_Nationality").toString();
                String Driver_Origin = documentSnapshot.get("Driver_Origin").toString();
                String Driver_Government_Area = documentSnapshot.get("Driver_Government_Area").toString();
                String Driver_Home_Town = documentSnapshot.get("Driver_Home_Town").toString();
                String Driver_Image = documentSnapshot.get("Driver_Image").toString();
                String Driver_Finger_Print = documentSnapshot.get("Driver_Finger_Print").toString();
                String Driver_Phone_Number = documentSnapshot.get("Driver_Phone_Number").toString();
                String Driver_Email = documentSnapshot.get("Driver_Email").toString();

                if (Vehicle_Type != null && Vehicle_Model != null && Vehicle_Route != null && Vehicle_Operation_Unit != null
                        && Vehicle_Registration_Number != null && Vehicle_Engine_Number != null && Vehicle_Tracking_Number != null
                        && Owner_Name != null && Owner_Current_Address != null && Owner_Nationality != null
                        && Owner_Origin != null && Owner_Government_Area != null && Owner_Home_Town != null && Owner_Phone_Number != null
                        && Owner_Email != null && Driver_Name != null && Driver_Residence_Address != null
                        && Driver_Nationality != null && Driver_Origin != null && Driver_Government_Area != null && Driver_Home_Town != null
                        && Driver_Image != null && Driver_Finger_Print != null && Driver_Phone_Number != null && Driver_Email != null) {

                    //EditText Vehicle
                    et_vehicleType.setSelection(setSelection(Vehicle_Type));
                    et_vehicleModel.setText(Vehicle_Model);
                    et_vehicleRoute.setText(Vehicle_Route);
                    et_vehicleOperationUnit.setText(Vehicle_Operation_Unit);
                    et_vehicleRegistrationNumber.setText(Vehicle_Registration_Number);
                    et_vehicleEngineNumber.setText(Vehicle_Engine_Number);
                    et_vehicleTrackingNumber.setText(Vehicle_Tracking_Number);

                    //EditText Owner
                    et_ownerName.setText(Owner_Name);
                    et_ownerCurrentAddress.setText(Owner_Current_Address);
                    et_ownerNationality.setText(Owner_Nationality);
                    et_ownerOrigin.setText(Owner_Origin);
                    et_ownerGovernmentArea.setText(Owner_Government_Area);
                    et_ownerHomeTown.setText(Owner_Home_Town);
                    et_ownerPhoneNumber.setText(Owner_Phone_Number);
                    et_ownerEmail.setText(Owner_Email);

                    //EditText Driver
                    et_driverName.setText(Driver_Name);
                    et_driverResidenceAddress.setText(Driver_Residence_Address);
                    et_driverNationality.setText(Driver_Nationality);
                    et_driverOrigin.setText(Driver_Origin);
                    et_driverGovernmentArea.setText(Driver_Government_Area);
                    et_driverHomeTown.setText(Driver_Home_Town);
                    et_driverPhoneNumber.setText(Driver_Phone_Number);
                    et_driverEmail.setText(Driver_Email);

                    StorageReference refImage = FirebaseStorage.getInstance().getReference().child("driver images/" + infoKey + "/" + infoKey);
                    refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUri = uri;
                            Glide.with(UpdateVehicleInformation.this).load(uri).into(iv_capture);
                            mProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.println(Log.ASSERT, "Image", "Error");
                        }
                    });

                    StorageReference refFinger = FirebaseStorage.getInstance().getReference().child("driver images/" + infoKey + "/" + "finger" + infoKey);
                    refFinger.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            fingerUri = uri;
                            Glide.with(UpdateVehicleInformation.this).load(uri).into(iv_fingerPrint);
                            mProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.println(Log.ASSERT, "Image", "Error");
                        }
                    });
                }
            }
        });
    }

    private int setSelection(String vehicle_type) {
        int index = 0;
        for (int i = 0; i < et_vehicleType.getCount(); i++) {
            if (et_vehicleType.getItemAtPosition(i).equals(vehicle_type)) {
                index = i;
            }
        }
        return index;
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                width = iv_capture.getWidth();
                height = iv_capture.getWidth();
                iv_capture.setImageBitmap(null);

                photo = Bitmap.createScaledBitmap(photo, width, height, false);

                resultUri = getImageUri(UpdateVehicleInformation.this, photo);

                iv_capture.setImageBitmap(photo);
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
                    bm = BitmapFactory.decodeResource(UpdateVehicleInformation.this.getResources(), R.drawable.capture_finger);
                    errorMesssage = data.getStringExtra("errorMessage");
                    Toast.makeText(UpdateVehicleInformation.this, errorMesssage, Toast.LENGTH_LONG).show();
                    Log.println(Log.ASSERT, "Fingerprint", "-- Error: " + errorMesssage + " --");
                }
            }
        }
    }
}
