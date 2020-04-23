package com.proyek.rahmanjai.eatitserver;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.proyek.rahmanjai.eatitserver.Common.Common;
import com.proyek.rahmanjai.eatitserver.Interface.ItemClickListener;
import com.proyek.rahmanjai.eatitserver.Model.MyResponse;
import com.proyek.rahmanjai.eatitserver.Model.Notification;
import com.proyek.rahmanjai.eatitserver.Model.Request;
import com.proyek.rahmanjai.eatitserver.Model.Sender;
import com.proyek.rahmanjai.eatitserver.Model.Token;
import com.proyek.rahmanjai.eatitserver.Remote.APIService;
import com.proyek.rahmanjai.eatitserver.ViewHolder.OrderViewHolder;

import java.util.ArrayList;
import java.util.List;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    MaterialSpinner spinner,shipperSpinner;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_status);

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");

        mService=Common.getFCMClient();

        //Init
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders(); // load all orders
    }

    private void loadOrders() {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(), adapter.getItem(position));

                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(position).getKey());

                    }
                });

                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder = new Intent(OrderStatus.this, TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status..");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On My Way", "Shipping");

        shipperSpinner = view.findViewById(R.id.shipperSpinner);
        //Load all shippers
        final List<String> shipperList=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Shippers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot shipperSnapshot:dataSnapshot.getChildren())
                            shipperList.add(shipperSnapshot.getKey());
                        shipperSpinner.setItems(shipperList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                if(item.getStatus().equals("2")){

                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);

                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();
                    sendOrderStatusToUser(localKey, item);
                    sendOrderShipRequestToShipper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(), item);

                }else
                    {
                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();
                    sendOrderStatusToUser(localKey, item);
                }
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void sendOrderShipRequestToShipper(String shipperPhone, Request item) {
        DatabaseReference tokens=db.getReference("Tokens");
        tokens.child(shipperPhone)
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                      if(dataSnapshot.exists()){
                        Token token=dataSnapshot.getValue(Token.class);

                        //make raw payload
                        Notification notification=new Notification("OopMini","Your have new order need ship");
                        Sender content=new Sender(token.getToken(),notification);
                        mService.sendNotification(content)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                        if(response.body().success==1)
                                        {
                                            Toast.makeText(OrderStatus.this, "Sent to shipper", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(OrderStatus.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                        Log.e("Error",t.getMessage());
                                    }
                                });
                    }}
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens=db.getReference("Tokens");
        tokens.child(item.getPhone())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Token token = dataSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification("OopMini", "Your order " + key + " was updated");
                            Sender content = new Sender(token.getToken(), notification);
                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(OrderStatus.this, "Order was updated !!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(OrderStatus.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("Error", t.getMessage());
                                        }
                                    });


                        } }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}
