package com.e.nche;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.e.nche.AdminModel.VehiclesModel;
import com.e.nche.AdminModel.VehiclesModelAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdminVehicles extends AppCompatActivity {

    public static String Status, Name;
    public CheckBox cb_user, cb_registration;
    private List<VehiclesModel> vehicleList = new ArrayList<>();
    private VehiclesModel model;
    private VehiclesModelAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressDialog mDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageButton ib_menu;
    private EditText et_search;

    private LinearLayout viewImage;
    private TextView tv_cancel;
    private ImageView iv_operator;

    private int position;

    private HorizontalScrollView hsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_vehicles);

        et_search = findViewById(R.id.search_view);

        ib_menu = findViewById(R.id.imageButton_menu);

        cb_user = findViewById(R.id.checkbox_user);
        cb_registration = findViewById(R.id.checkbox_registration);

        viewImage = findViewById(R.id.layout_view_image);
        tv_cancel = findViewById(R.id.image_view_cancel);
        iv_operator = findViewById(R.id.image_view_operator_image);

        hsv = findViewById(R.id.scroll);

        SharedPreferences prefs = getSharedPreferences("statusAdmin", MODE_PRIVATE);
        Status = prefs.getString("status", "");//"No name defined" is the default value.

//        Intent intent = getIntent();
//        Status = intent.getStringExtra("status");

        if (Status.equals("Admin")) {
            Name = "Admin";
        } else {
            Name = "SuperAdmin";
        }

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading, Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();

        recyclerView = findViewById(R.id.recyclerView_security_admin_vehicles);

        adapter = new VehiclesModelAdapter(vehicleList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImage.setVisibility(View.GONE);
                hsv.setVisibility(View.VISIBLE);
                iv_operator.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.capture_image));
                recyclerView.scrollToPosition(position);
                hsv.postDelayed(new Runnable() {
                    public void run() {
                        hsv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100L);
            }
        });

        ib_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(AdminVehicles.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_logout, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout:
                                SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.remove("CodeCoy_Vicab_Remember_Admin_Login");
                                myEdit.apply();

                                Intent logout = new Intent(AdminVehicles.this, UserLogin.class);
                                logout.putExtra("status", "Admin");
                                startActivity(logout);
                                finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });

        cb_user.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    filter(et_search.getText().toString());
                    if (!cb_registration.isChecked()) {
                        cb_user.setChecked(true);
                    }
                } else {
                    filter(et_search.getText().toString());
                    if (!cb_registration.isChecked()) {
                        cb_user.setChecked(true);
                    }
                }
            }
        });

        cb_registration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    filter(et_search.getText().toString());
                    if (!cb_user.isChecked()) {
                        cb_registration.setChecked(true);
                    }
                } else {
                    filter(et_search.getText().toString());
                    if (!cb_user.isChecked()) {
                        cb_registration.setChecked(true);
                    }
                }
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        prepareVehiclesData();

        if (getIntent().getStringExtra("key") != null) {
            findViewById(R.id.scroll).setVisibility(View.GONE);
            position = getIntent().getIntExtra("Position", 0);
            onViewImageClick(getIntent().getStringExtra("key"));
        }
    }

    private void filter(String text) {
        ArrayList<VehiclesModel> filteredList = new ArrayList<>();

        for (VehiclesModel item : vehicleList) {
            if (cb_user.isChecked() && cb_registration.isChecked()) {
                if (item.getVehicle_Registration_Number().toLowerCase().contains(text.toLowerCase()) || item.getVehicle_Registration_Number().contains(text) || item.getUser().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            } else {
                if (cb_user.isChecked()) {
                    if (item.getUser().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                if (cb_registration.isChecked()) {
                    if (item.getVehicle_Registration_Number().contains(text) || item.getVehicle_Registration_Number().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            }
        }

        adapter.filterList(filteredList);
    }

    private void prepareVehiclesData() {
        vehicleList.clear();
        db.collection("Registered Vehicles").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String key = document.getId();
                                Log.e("Key 253", "" + key);
                                prepareVehiclesDataEntries(key);
                            }
                        } else {
                            mDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.ASSERT, "Registered Vehicles", e.toString());
                    }
                });
    }

    private void prepareVehiclesDataEntries(String key) {
        Log.println(Log.ASSERT, "prepareVehiclesData", "Key: " + key);
        db.collection("Registered Vehicles").document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {

                            Log.println(Log.ASSERT, "getUserSnap", "" + documentSnapshot);
                            //User
                            String User = documentSnapshot.get("User").toString();
                            String Id = documentSnapshot.get("Id").toString();

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

                            if (User != null && Id != null && Vehicle_Type != null && Vehicle_Model != null && Vehicle_Route != null
                                    && Vehicle_Operation_Unit != null && Vehicle_Registration_Number != null && Vehicle_Engine_Number != null
                                    && Vehicle_Tracking_Number != null && Owner_Name != null && Owner_Current_Address != null
                                    && Owner_Nationality != null && Owner_Origin != null && Owner_Government_Area != null && Owner_Home_Town != null
                                    && Owner_Phone_Number != null && Owner_Email != null && Driver_Name != null && Driver_Residence_Address != null
                                    && Driver_Nationality != null && Driver_Origin != null && Driver_Government_Area != null && Driver_Home_Town != null
                                    && Driver_Image != null && Driver_Finger_Print != null && Driver_Phone_Number != null && Driver_Email != null) {

                                model = new VehiclesModel(User, Id, Vehicle_Type, Vehicle_Model, Vehicle_Route, Vehicle_Operation_Unit, Vehicle_Registration_Number,
                                        Vehicle_Engine_Number, Vehicle_Tracking_Number, Owner_Name, Owner_Current_Address, Owner_Nationality,
                                        Owner_Origin, Owner_Government_Area, Owner_Home_Town, Owner_Phone_Number, Owner_Email, Driver_Name, Driver_Residence_Address,
                                        Driver_Nationality, Driver_Origin, Driver_Government_Area, Driver_Home_Town, Driver_Image, Driver_Finger_Print,
                                        Driver_Phone_Number, Driver_Email);


                                vehicleList.add(model);

                                //User
                                User = null;
                                Id = null;

                                //Vehicle
                                Vehicle_Type = null;
                                Vehicle_Model = null;
                                Vehicle_Route = null;
                                Vehicle_Operation_Unit = null;
                                Vehicle_Registration_Number = null;
                                Vehicle_Engine_Number = null;
                                Vehicle_Tracking_Number = null;

                                //Owner
                                Owner_Name = null;
                                Owner_Current_Address = null;
                                Owner_Nationality = null;
                                Owner_Origin = null;
                                Owner_Government_Area = null;
                                Owner_Home_Town = null;
                                Owner_Phone_Number = null;
                                Owner_Email = null;

                                //Driver
                                Driver_Name = null;
                                Driver_Residence_Address = null;
                                Driver_Nationality = null;
                                Driver_Origin = null;
                                Driver_Government_Area = null;
                                Driver_Home_Town = null;
                                Driver_Image = null;
                                Driver_Finger_Print = null;
                                Driver_Phone_Number = null;
                                Driver_Email = null;

                                mDialog.dismiss();
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void onViewImageClick(String key) {
        viewImage.setVisibility(View.VISIBLE);

        StorageReference refImage = FirebaseStorage.getInstance().getReference().child("driver images/" + key + "/" + key);
        refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(AdminVehicles.this).load(uri).into(iv_operator);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.ASSERT, "Image", "Error");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
