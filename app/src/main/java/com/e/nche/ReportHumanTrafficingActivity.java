package com.e.nche;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e.nche.MyUserPanel.MyComplainAdapter;
import com.e.nche.MyUserPanel.MyComplainModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportHumanTrafficingActivity extends AppCompatActivity {

    private List<MyComplainModel> complainList = new ArrayList<>();
    private MyComplainModel model;
    private MyComplainAdapter adapter;

    private RecyclerView recyclerView;

    public static List<String> complainIdList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText et_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complain);

        recyclerView = findViewById(R.id.recyclerView_my_complains);
        et_search = findViewById(R.id.editText_search_view);

        adapter = new MyComplainAdapter(complainList, "");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

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

        prepareComplainsData("Loading Complains", "Please wait...");

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                prepareComplainsData();
            }
        }, 2000);*/
    }

    private void filter(String text) {
        ArrayList<MyComplainModel> filteredList = new ArrayList<>();

        for (MyComplainModel item : complainList) {
            if (item.getCode().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            } else if (item.getUnique().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        adapter.filterList(filteredList);
    }

    private void prepareComplainsData(String title, String msg) {
        complainIdList.clear();
        complainList.clear();

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setTitle(title);
        mDialog.setMessage(msg);
        mDialog.setCancelable(false);
        mDialog.show();

        Query query = db.collection("General Complaintss").orderBy("TimeStamp", Query.Direction.DESCENDING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String key = document.getId();
                                if (!key.isEmpty()) {
                                    prepareComplainsDataEntries(key);
                                }
                            }
                            mDialog.dismiss();
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

    private void prepareComplainsDataEntries(String key) {
        complainIdList.add(key);
        db.collection("General Complaintss").document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {

                            String Key = documentSnapshot.getId();
                            String Code = documentSnapshot.get("Code").toString();
                            String Unique = documentSnapshot.get("UniqueCode").toString();
                            String Model = documentSnapshot.get("Model").toString();
                            String Name = documentSnapshot.get("Name").toString();
                            String Email = documentSnapshot.get("Email").toString();
                            String Phone = documentSnapshot.get("Phone").toString();
                            String Remarks = documentSnapshot.get("Remarks").toString();
                            String Attachment = documentSnapshot.get("Attachment").toString();
                            String TimeStamp = documentSnapshot.get("TimeStamp").toString();

                            if (Code != null && Model != null && Name != null && Email != null && Phone != null
                                    && Remarks != null && Attachment != null && TimeStamp != null) {

                                if (!Attachment.equals("Not Attached")) {
                                    Attachment = "Attached";
                                }

                                model = new MyComplainModel(Key, Code, Unique, Model, Name, Email, Phone, Remarks, Attachment, TimeStamp);
                                complainList.add(model);

                                Key = null;
                                Code = null;
                                Model = null;
                                Name = null;
                                Email = null;
                                Phone = null;
                                Remarks = null;
                                Attachment = null;
                                TimeStamp = null;
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.println(Log.ASSERT, "MyComplain", "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        prepareComplainsData("Checking if any update", "Please wait...");
        Log.println(Log.ASSERT, "MyComplain", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.println(Log.ASSERT, "MyComplain", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.println(Log.ASSERT, "MyComplain", "onPause");
    }
}
