package com.e.nche.AdminModel;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.nche.AdminVehicles;
import com.e.nche.R;
import com.e.nche.UpdateVehicleInformation;

import java.util.ArrayList;
import java.util.List;

public class VehiclesModelAdapter extends RecyclerView.Adapter<VehiclesModelAdapter.MyViewHolder> {

    private List<VehiclesModel> vehiclesList;
    private List<VehiclesModel> vehiclesListFull;

    public VehiclesModelAdapter(List<VehiclesModel> vehiclesList) {
        this.vehiclesList = vehiclesList;
        vehiclesListFull = new ArrayList<>(vehiclesList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_admin_vehicles, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final VehiclesModel model = vehiclesList.get(position);

        holder.u_user.setText(model.getUser());
        holder.u_id.setText(model.getId());

        holder.v_type.setText(model.getVehicle_Type());
        holder.v_model.setText(model.getVehicle_Model());
        holder.v_route.setText(model.getVehicle_Route());
        holder.v_operation.setText(model.getVehicle_Operation_Unit());
        holder.v_registration.setText(model.getVehicle_Registration_Number());
        holder.v_engine.setText(model.getVehicle_Engine_Number());
        holder.v_tracking.setText(model.getVehicle_Tracking_Number());

        holder.o_name.setText(model.getOwner_Name());
        holder.o_current.setText(model.getOwner_Current_Address());
        holder.o_nationality.setText(model.getOwner_Nationality());
        holder.o_origin.setText(model.getOwner_Origin());
        holder.o_govt.setText(model.getOwner_Government_Area());
        holder.o_home.setText(model.getOwner_Home_Town());
        holder.o_phone.setText(model.getOwner_Phone_Number());
        holder.o_email.setText(model.getOwner_Email());

        holder.d_name.setText(model.getDriver_Name());
        holder.d_residence.setText(model.getDriver_Residence_Address());
        holder.d_nationality.setText(model.getDriver_Nationality());
        holder.d_origin.setText(model.getDriver_Origin());
        holder.d_govt.setText(model.getDriver_Government_Area());
        holder.d_home.setText(model.getDriver_Home_Town());
        holder.d_phone.setText(model.getDriver_Phone_Number());
        holder.d_email.setText(model.getDriver_Email());

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Key", model.getId() + "");
                Intent intent = new Intent(v.getContext(), UpdateVehicleInformation.class);
                intent.putExtra("InfoKey", model.getId());
                intent.putExtra("status", AdminVehicles.Name);
                v.getContext().startActivity(intent);
            }
        });

        holder.btn_viewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String key = model.getId();
                Intent intent = new Intent(v.getContext(), AdminVehicles.class);
                intent.putExtra("status", AdminVehicles.Name);
                intent.putExtra("key", key);
                intent.putExtra("Position", holder.getAdapterPosition());
                v.getContext().startActivity(intent);
                ((Activity) v.getContext()).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehiclesList.size();
    }

    public void filterList(ArrayList<VehiclesModel> filteredList) {
        vehiclesList = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView u_user, u_id, v_type, v_model, v_route, v_operation, v_registration, v_engine, v_tracking,
                o_name, o_current, o_nationality, o_origin, o_govt, o_home, o_phone, o_email,
                d_name, d_residence, d_nationality, d_origin, d_govt, d_home, d_phone, d_email;

        public Button btn_viewImage;

        public LinearLayout parentLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.list_admin_parent_layout);

            //User
            u_user = itemView.findViewById(R.id.u_user);
            u_id = itemView.findViewById(R.id.u_id);

            //Vehicle
            v_type = itemView.findViewById(R.id.v_type);
            v_model = itemView.findViewById(R.id.v_model);
            v_route = itemView.findViewById(R.id.v_route);
            v_operation = itemView.findViewById(R.id.v_operation);
            v_registration = itemView.findViewById(R.id.v_registration);
            v_engine = itemView.findViewById(R.id.v_engine);
            v_engine = itemView.findViewById(R.id.v_engine);
            v_tracking = itemView.findViewById(R.id.v_tracking);

            //Owner
            o_name = itemView.findViewById(R.id.o_name);
            o_current = itemView.findViewById(R.id.o_current);
            o_nationality = itemView.findViewById(R.id.o_nationality);
            o_origin = itemView.findViewById(R.id.o_origin);
            o_govt = itemView.findViewById(R.id.o_govt);
            o_home = itemView.findViewById(R.id.o_home);
            o_phone = itemView.findViewById(R.id.o_phone);
            o_email = itemView.findViewById(R.id.o_email);

            //Driver
            d_name = itemView.findViewById(R.id.d_name);
            d_residence = itemView.findViewById(R.id.d_residence);
            d_nationality = itemView.findViewById(R.id.d_nationality);
            d_origin = itemView.findViewById(R.id.d_origin);
            d_govt = itemView.findViewById(R.id.d_govt);
            d_home = itemView.findViewById(R.id.d_home);
            d_phone = itemView.findViewById(R.id.d_phone);
            d_email = itemView.findViewById(R.id.d_email);
            btn_viewImage = itemView.findViewById(R.id.button_viewImage);
        }
    }

}
