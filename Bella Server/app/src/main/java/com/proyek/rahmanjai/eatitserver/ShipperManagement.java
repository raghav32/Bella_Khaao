package com.proyek.rahmanjai.eatitserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyek.rahmanjai.eatitserver.Common.Common;
import com.proyek.rahmanjai.eatitserver.Model.Shipper;
import com.proyek.rahmanjai.eatitserver.ViewHolder.ShipperViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

public class ShipperManagement extends AppCompatActivity {

    FloatingActionButton fabAdd;

    FirebaseDatabase database;
    DatabaseReference shippers;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Shipper, ShipperViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shipper_management);

        fabAdd=findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateShipperLayout();
            }
        });

        recyclerView=findViewById(R.id.recycler_shippers);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        database=FirebaseDatabase.getInstance();
        shippers=database.getReference("Shippers");

        loadAllShippers();
    }

    private void loadAllShippers() {
            adapter= new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(
                    Shipper.class,
                    R.layout.shipper_layout,
                    ShipperViewHolder.class,
                    shippers) {
                @Override
                protected void populateViewHolder(ShipperViewHolder viewHolder, final Shipper model, final int position) {
                        viewHolder.shipper_phone.setText(model.getPhone());
                        viewHolder.shipper_name.setText(model.getName());

                        viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showEditDialog(adapter.getRef(position).getKey(),model);
                            }
                        });
                        viewHolder.btn_remove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeShipper(adapter.getRef(position).getKey());
                            }
                        });
                }
            };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void removeShipper(String key) {
        shippers.child(key)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShipperManagement.this, "Remove succeed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void showEditDialog(String key,Shipper model) {

        android.app.AlertDialog.Builder create_shipper_dialog= new AlertDialog.Builder(ShipperManagement.this);
        create_shipper_dialog.setTitle("Update Shipper");

        LayoutInflater inflater=this.getLayoutInflater();
        View view= inflater.inflate(R.layout.create_shipper_layout,null);

        final MaterialEditText edtName=view.findViewById(R.id.edtName);
        final MaterialEditText edtPhone=view.findViewById(R.id.edtPhone);
        final MaterialEditText edtPassword=view.findViewById(R.id.edtPassword);

        create_shipper_dialog.setView(view);
        create_shipper_dialog.setIcon(R.drawable.ic_local_shipping_black_24dp);

        edtName.setText(model.getName());
        edtPassword.setText(model.getPassword());
        edtPhone.setText(model.getPhone());

        create_shipper_dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Map<String,Object> update=new HashMap<>();
                update.put("name",edtName.getText().toString());
                update.put("phone",edtPhone.getText().toString());
                update.put("password",edtPassword.getText().toString());

                shippers.child(edtPhone.getText().toString())
                        .setValue(update)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ShipperManagement.this, "Shipper Updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        create_shipper_dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        create_shipper_dialog.show();

    }

    private void showCreateShipperLayout() {
        android.app.AlertDialog.Builder create_shipper_dialog= new android.app.AlertDialog.Builder(ShipperManagement.this);
        create_shipper_dialog.setTitle("Create Shipper");

        LayoutInflater inflater=this.getLayoutInflater();
        View view= inflater.inflate(R.layout.create_shipper_layout,null);

         final MaterialEditText edtName=(MaterialEditText) view.findViewById(R.id.edtName);
         final MaterialEditText edtPhone=(MaterialEditText)view.findViewById(R.id.edtPhone);
         final MaterialEditText edtPassword=(MaterialEditText)view.findViewById(R.id.edtPassword);

         create_shipper_dialog.setView(view);
         create_shipper_dialog.setIcon(R.drawable.ic_local_shipping_black_24dp);

        create_shipper_dialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Shipper shipper=new Shipper();
                shipper.setName(edtName.getText().toString());
                shipper.setPassword(edtPassword.getText().toString());
                shipper.setPhone(edtPhone.getText().toString());

                shippers.child(edtPhone.getText().toString())
                        .setValue(shipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ShipperManagement.this, "Shipper Created", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        create_shipper_dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        create_shipper_dialog.show();

    }

}
