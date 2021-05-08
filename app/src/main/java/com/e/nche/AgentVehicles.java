package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.e.nche.SecurityAgentsModel.Model;
import com.e.nche.SecurityAgentsModel.SecurityAgentsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AgentVehicles extends AppCompatActivity {

    private List<Model> vehicleList = new ArrayList<>();
    private Model model;
    private SecurityAgentsAdapter adapter;

    private RecyclerView recyclerView;

    private ProgressDialog mDialog;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_vehicles);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading, Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();

        recyclerView = findViewById(R.id.recyclerView_security_agents_vehicles);

        adapter = new SecurityAgentsAdapter(vehicleList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


        prepareVehiclesData();
    }

    private void prepareVehiclesData() {
        db.collection("Registered Vehicles").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                String key = document.getId();
                                prepareVehiclesDataEntries(key);
                            }
                        }else {
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
        db.collection("Registered Vehicles").document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {

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
                                    && Driver_Image != null && Driver_Finger_Print != null && Driver_Phone_Number != null && Driver_Email != null){

                                model = new Model(User, Id, Vehicle_Type, Vehicle_Model, Vehicle_Route, Vehicle_Operation_Unit, Vehicle_Registration_Number,
                                        Vehicle_Engine_Number, Vehicle_Tracking_Number, Owner_Name, Owner_Current_Address, Owner_Nationality,
                                        Owner_Origin, Owner_Government_Area, Owner_Home_Town, Owner_Phone_Number, Owner_Email, Driver_Name, Driver_Residence_Address,
                                        Driver_Nationality, Driver_Origin, Driver_Government_Area, Driver_Home_Town, Driver_Image, Driver_Finger_Print, Driver_Phone_Number, Driver_Email );


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
}
