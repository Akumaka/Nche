package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.e.nche.SecurityAgentsModel.ComplainModel;
import com.e.nche.SecurityAgentsModel.ComplainModelAdapter;
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

public class AgentComplain extends AppCompatActivity {

    private List<ComplainModel> complainList = new ArrayList<>();
    private ComplainModel model;
    private ComplainModelAdapter adapter;

    private RecyclerView recyclerView;

    private ProgressDialog mDialog;

    public static List<String> complainIdList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText et_search;
    String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_complain);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading, Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();

        type = getIntent().getStringExtra("type");

        recyclerView = findViewById(R.id.recyclerView_security_agents_complains);
        et_search = findViewById(R.id.editText_agent_search_view);

        adapter = new ComplainModelAdapter(complainList, type);
        if (type != null && !type.equals("General Complains")) {
            findViewById(R.id.a_c_code).setVisibility(View.GONE);
            findViewById(R.id.a_c_model).setVisibility(View.GONE);
        }

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

        prepareComplainsData();
    }

    private void filter(String text) {
        ArrayList<ComplainModel> filteredList = new ArrayList<>();

        for (ComplainModel item : complainList) {
            if (item.getCode().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            } else if (item.getUnique().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        adapter.filterList(filteredList);
    }

    private void prepareComplainsData() {
        complainIdList.clear();

        if (type == null || type.equals(""))
            type = "General Complains";
        Query query = db.collection(type).orderBy("TimeStamp", Query.Direction.DESCENDING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String key = document.getId();
                                prepareComplainsDataEntries(key);
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

    private void prepareComplainsDataEntries(String key) {
        complainIdList.add(key);
        db.collection(type).document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {

                            String Code = documentSnapshot.get("Code").toString();
                            String Unique = documentSnapshot.get("UniqueCode").toString();
                            String Model = documentSnapshot.get("Model").toString();
                            String Name = documentSnapshot.get("Name").toString();
                            String Email = "";
                            if (documentSnapshot.get("Email").toString() != null)
                                Email = documentSnapshot.get("Email").toString();
                            String Phone = documentSnapshot.get("Phone").toString();
                            String Remarks = documentSnapshot.get("Remarks").toString();
                            String Attachment = documentSnapshot.get("Attachment").toString();

                            if (Code != null && Model != null && Name != null && Email != null && Phone != null
                                    && Remarks != null && Attachment != null) {

                                if (!Attachment.equals("Not Attached")) {
                                    Attachment = "Attached";
                                }

                                model = new ComplainModel(Code, Unique, Model, Name, Email, Phone, Remarks, Attachment);
                                complainList.add(model);

                                Code = null;
                                Model = null;
                                Name = null;
                                Email = null;
                                Phone = null;
                                Remarks = null;
                                Attachment = null;

                                mDialog.dismiss();
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
