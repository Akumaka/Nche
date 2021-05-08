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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

public class MyComplain extends AppCompatActivity {

    private List<MyComplainModel> complainList = new ArrayList<>();
    private MyComplainModel model;
    private MyComplainAdapter adapter;

    private RecyclerView recyclerView;

    public static List<String> complainIdList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText et_search;
    TextView a_c_code;
    private Spinner spinner_complain_type;
    String type = "General Complains";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complain);

        recyclerView = findViewById(R.id.recyclerView_my_complains);
        et_search = findViewById(R.id.editText_search_view);
        a_c_code = findViewById(R.id.a_c_code);

        adapter = new MyComplainAdapter(complainList, type);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(adapter);

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

        spinner_complain_type = findViewById(R.id.spinner_complain_type);
        spinner_complain_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.e("onItemSelected", "pos: " + position + " selected: " + spinner_complain_type.getSelectedItem().toString().trim());
                type = spinner_complain_type.getSelectedItem().toString().trim();
                prepareComplainsData("Loading", "Please wait...", spinner_complain_type.getSelectedItem().toString().trim());
                if (spinner_complain_type.getSelectedItem().toString().trim().equals("General Complains"))
                    a_c_code.setVisibility(View.VISIBLE);
                else
                    a_c_code.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


//        prepareComplainsData("Loading Complains", "Please wait...", "General Complains");

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

    private void prepareComplainsData(String title, String msg, final String path) {
        complainIdList.clear();
        complainList.clear();

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setTitle(title);
        mDialog.setMessage(msg);
        mDialog.setCancelable(false);
        mDialog.show();

        Query query = db.collection(path).orderBy("TimeStamp", Query.Direction.DESCENDING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String key = document.getId();
                                if (!key.isEmpty()) {
                                    prepareComplainsDataEntries(key, path);
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

    private void prepareComplainsDataEntries(String key, final String path) {
//        complainIdList.clear();
//        complainList.clear();
        complainIdList.add(key);
        db.collection(path).document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {

                            String Code = documentSnapshot.get("Code").toString();
                            String Unique = documentSnapshot.get("UniqueCode").toString();
//                            String Model = documentSnapshot.get("Model").toString();
                            String Model = path;
                            String Name = documentSnapshot.get("Name").toString();
                            String Email = documentSnapshot.get("Email").toString();
                            String Phone = documentSnapshot.get("Phone").toString();
                            String Remarks = documentSnapshot.get("Remarks").toString();
                            String Attachment = documentSnapshot.get("Attachment").toString();

                            if (Code != null && Model != null && Name != null && Email != null && Phone != null
                                    && Remarks != null && Attachment != null) {

                                if (!Attachment.equals("Not Attached")) {
                                    Attachment = "Attached";
                                }

                                model = new MyComplainModel(Code, Unique, Model, Name, Email, Phone, Remarks, Attachment);
                                complainList.add(model);

                                Code = null;
                                Model = null;
                                Name = null;
                                Email = null;
                                Phone = null;
                                Remarks = null;
                                Attachment = null;
                            }
                            adapter = new MyComplainAdapter(complainList, type);
                            recyclerView.setAdapter(adapter);

//                            adapter.notifyDataSetChanged();
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
//        prepareComplainsData("Checking if any update", "Please wait...", "General Complains");
//        spinner_complain_type.setSelection(0);
        Log.println(Log.ASSERT, "MyComplain", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        spinner_complain_type.setSelection(0);
        Log.println(Log.ASSERT, "MyComplain", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.println(Log.ASSERT, "MyComplain", "onPause");
    }
}
