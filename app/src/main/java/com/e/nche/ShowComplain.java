package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ShowComplain extends AppCompatActivity {

    private EditText et_registrationNumber, et_trackingNumber, et_vehicleType, et_vehicleRoute, et_phoneNumber, et_address;
    private ImageView iv_operator;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_complain);

        et_registrationNumber = findViewById(R.id.editText_registration_number);
        et_trackingNumber = findViewById(R.id.editText_tracking_number);
        et_vehicleType = findViewById(R.id.editText_type);
        et_vehicleRoute = findViewById(R.id.editText_route);
        et_phoneNumber = findViewById(R.id.editText_phone);
        et_address = findViewById(R.id.editText_address);

        iv_operator = findViewById(R.id.imageView_driverImage);

        readUsers(getIntent().getStringExtra("ShowComplainId"));
    }


    private void readUsers(final String key) {
        final ProgressDialog mProgressDialog = new ProgressDialog(ShowComplain.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Loading Information");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        db.collection("Registered Vehicles").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String Registration_Number = documentSnapshot.get("Vehicle_Registration_Number").toString();
                String Tracking_Number = documentSnapshot.get("Vehicle_Tracking_Number").toString();
                String Vehicle_Type = documentSnapshot.get("Vehicle_Type").toString();
                String Vehicle_Route = documentSnapshot.get("Vehicle_Route").toString();
                String Driver_Phone_Number = documentSnapshot.get("Driver_Phone_Number").toString();
                String Driver_Residence_Address = documentSnapshot.get("Driver_Residence_Address").toString();
                String Driver_Image = documentSnapshot.get("Driver_Image").toString();

                if (!Registration_Number.equals(null) && !Tracking_Number.equals(null) && !Vehicle_Type.equals(null) && !Vehicle_Route.equals(null)
                        && !Driver_Phone_Number.equals(null) && !Driver_Residence_Address.equals(null) && !Driver_Image.equals(null)) {

                    et_registrationNumber.setText(Registration_Number);
                    et_trackingNumber.setText(Tracking_Number);
                    et_vehicleType.setText(Vehicle_Type);
                    et_vehicleRoute.setText(Vehicle_Route);
                    et_phoneNumber.setText(Driver_Phone_Number);
                    et_address.setText(Driver_Residence_Address);

                    try {
                        StorageReference refImage = FirebaseStorage.getInstance().getReference().child("driver images/" + key + "/" + key);
                        refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(ShowComplain.this).load(uri).into(iv_operator);
                                mProgressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.println(Log.ASSERT, "Image", "Error");
                            }
                        });
                    } catch (RuntimeException ex) {
                        ex.getLocalizedMessage();
                    }

                    Registration_Number = null;
                    Tracking_Number = null;
                    Vehicle_Type = null;
                    Vehicle_Route = null;
                    Driver_Phone_Number = null;
                    Driver_Residence_Address = null;
                    Driver_Image = null;
                }
            }
        });
    }
}
