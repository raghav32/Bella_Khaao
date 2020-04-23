package com.example.androideatitshipper;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.androideatitshipper.Common.Common;
import com.example.androideatitshipper.Helper.DirectionJSONParser;
import com.example.androideatitshipper.Model.Request;
import com.example.androideatitshipper.Remote.IGeoCoordinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Location mLastLocation;
    Marker mCurrentMarker;

    IGeoCoordinates mService;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Polyline polyline;

    Button btn_call,btn_shipped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_call=findViewById(R.id.btn_call);
        btn_shipped=findViewById(R.id.btn_shipped);

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+Common.currentRequest.getPhone()));
               if(ActivityCompat.checkSelfPermission(TrackingOrder.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                return;

               startActivity(intent);
            }
        });

        btn_shipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will delete order in table
                //orderneedShip
                //shippingorder
                //and update status of order to shipped
                shippedOrder();
            }
        });


        mService=Common.getGeoCodeService();
        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    private void shippedOrder() {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_NEED_SHIP_TABLE)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update status on request table
                        Map<String,Object> update_status=new HashMap<>();
                        update_status.put("status","03");

                        FirebaseDatabase.getInstance()
                                .getReference("Requests")
                                .child(Common.currentKey)
                                .updateChildren(update_status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Delete from shipping order
                                        FirebaseDatabase.getInstance()
                                                .getReference(Common.SHIPPER_INFO_TABLE)
                                                .child(Common.currentKey)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(TrackingOrder.this, "Shipped..!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void buildLocationCallBack() {
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation=locationResult.getLastLocation();

                if(mCurrentMarker != null)
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                Common.updateShippingInformation(Common.currentKey,mLastLocation);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

                drawRoute(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),Common.currentRequest);
            }
        };

    }

    private void drawRoute(final LatLng yourLocation, Request request) {
        if(polyline != null)
            polyline.remove();

        if(request.getAddress() != null && !request.getAddress().isEmpty())
        {
            mService.getGeoCode(request.getAddress(),"AIzaSyCT0CeTVYlxfG-6YWS2bS5JJ3b1xAG1H8M").enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    try{
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        for(int i=0;i<jsonObject.length();i++) {
                            String lat = ((JSONArray) jsonObject.get("results"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lat").toString();


                            String lng = ((JSONArray) jsonObject.get("results"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lng").toString();

                            LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
                            bitmap = Common.scaleBitmap(bitmap, 70, 70);

                            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .title("Order of " + Common.currentRequest.getPhone())
                                    .position(orderLocation);
                            mMap.addMarker(markerOptions);

                            //draw route
                            mService.getDirections(yourLocation.latitude + ", " + yourLocation.longitude,
                                    orderLocation.latitude + ", " + orderLocation.longitude,"AIzaSyCT0CeTVYlxfG-6YWS2bS5JJ3b1xAG1H8M")
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            new ParserTask().execute(response.body().toString());
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }else {
            if(request.getLatLng() != null && !request.getLatLng().isEmpty()){
                String[] latLng=request.getLatLng().split(",");
                LatLng orderLocation=new LatLng(Double.parseDouble(latLng[0]),Double.parseDouble(latLng[1]));


                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
                bitmap = Common.scaleBitmap(bitmap, 70, 70);

                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Order of " + Common.currentRequest.getPhone())
                        .position(orderLocation);
                mMap.addMarker(markerOptions);

                mService.getDirections(mLastLocation.getLatitude()+","+mLastLocation.getLongitude(),
                        orderLocation.latitude+","+orderLocation.longitude,"AIzaSyCT0CeTVYlxfG-6YWS2bS5JJ3b1xAG1H8M")
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                new ParserTask().execute(response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });
            }
        }
    }



    private void buildLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

    }

    @Override
    protected void onStop() {
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation=location;
                LatLng yourLocation = new LatLng(location.getLatitude(),location.getLongitude());
                mCurrentMarker=mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

            }
        });

    }

    private class ParserTask extends AsyncTask<String, Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog = new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting..");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes=null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes = parser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions=null;

            for (int i = 0; i <lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for (int j=0; j<path.size(); j++){
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));

                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
            mMap.addPolyline(polylineOptions);
        }
    }


}

