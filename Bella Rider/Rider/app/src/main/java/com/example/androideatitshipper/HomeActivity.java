package com.example.androideatitshipper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.androideatitshipper.Common.Common;
import com.example.androideatitshipper.Model.Request;
import com.example.androideatitshipper.Model.Token;
import com.example.androideatitshipper.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class HomeActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    Location mLastLocation;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference shipperOrders;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE
            },Common.REQUEST_CODE);
        }
        else{
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }

        database=FirebaseDatabase.getInstance();
        shipperOrders=database.getReference(Common.ORDER_NEED_SHIP_TABLE);

        recyclerView=findViewById(R.id.recycler_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        updateTokenShipper(FirebaseInstanceId.getInstance().getToken());

        loadAllOrderNeedShip(Common.currentShipper.getPhone());
    }

    private void loadAllOrderNeedShip(String phone) {

        DatabaseReference orderInChildOfShipper=shipperOrders.child(phone);

        FirebaseRecyclerOptions<Request> listOrders = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderInChildOfShipper,Request.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(listOrders) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, final int position, @NonNull final Request model) {
                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.txtOrderAddress.setText(model.getAddress());
                holder.txtOrderPhone.setText(model.getPhone());
                holder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                holder.btnShipping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Common.createShippingOrder(adapter.getRef(position).getKey(),
                                Common.currentShipper.getPhone(),
                                mLastLocation);
                        Common.currentRequest=model;
                        Common.currentKey=adapter.getRef(position).getKey();
                        startActivity(new Intent(HomeActivity.this,TrackingOrder.class));

                    }
                });

            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_view_layout,parent,false);

                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void updateTokenShipper(String token) {
        DatabaseReference tokens= database.getReference("Tokens");
        Token data=new Token(token,false);
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrderNeedShip(Common.currentShipper.getPhone());
    }

    @Override
    protected void onStop() {
        if(adapter != null)
            adapter.stopListening();
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case Common.REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        buildLocationRequest();
                        buildLocationCallBack();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            return;
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    }else{
                        Toast.makeText(this, "You should assign permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    private void buildLocationCallBack() {
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation=locationResult.getLastLocation();

            }
        };

    }

    private void buildLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

    }
}
