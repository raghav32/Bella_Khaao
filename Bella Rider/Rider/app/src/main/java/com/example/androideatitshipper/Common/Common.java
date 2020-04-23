package com.example.androideatitshipper.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;

import com.example.androideatitshipper.Model.Request;
import com.example.androideatitshipper.Model.Shipper;
import com.example.androideatitshipper.Model.ShippingInformation;
import com.example.androideatitshipper.Remote.IGeoCoordinates;
import com.example.androideatitshipper.Remote.RetrofitClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;

public class Common {
    public static final String SHIPPER_TABLE="Shippers";

    public static Shipper currentShipper;
    public static final int REQUEST_CODE=1000;

    public static final String ORDER_NEED_SHIP_TABLE="OrdersNeedShip";
    public static final String SHIPPER_INFO_TABLE="ShippingOrders";
    public static final String baseUrl = "https://maps.googleapis.com";


    public static Request currentRequest;
    public static String currentKey;

    public static String convertCodeToStatus(String code){
        if (code==null){
            code="empty string";}
        if (code.equals("0"))
            return "Placed";
        else if (code.equals("1"))
            return "On My Way";
        else if(code.equals("2"))
            return "Shipping";
        else
            return "Shipped";

    }

    public static String getDate(long time){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date=new StringBuilder(DateFormat.format("dd-MM-yyyy HH:mm",calendar).toString());
        return date.toString();

    }


    public static void createShippingOrder(String key, String phone, Location mLastLocation) {
        ShippingInformation shippingInformation=new ShippingInformation();
        shippingInformation.setOrderId(key);
        shippingInformation.setShipperPhone(phone);
        shippingInformation.setLat(mLastLocation.getLatitude());
        shippingInformation.setLng(mLastLocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SHIPPER_INFO_TABLE)
                .child(key)
                .setValue(shippingInformation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error",e.getMessage());
                    }
                });

    }

    public static void updateShippingInformation(String currentKey, Location mLastLocation) {
        Map<String,Object> update_Location=new HashMap<>();
        update_Location.put("lat",mLastLocation.getLatitude());
        update_Location.put("lng",mLastLocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SHIPPER_INFO_TABLE)
                .child(currentKey)
                .updateChildren(update_Location)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR",e.getMessage());
                    }
                });

    }

    public static IGeoCoordinates getGeoCodeService() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap (Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaleY = newHeight/(float)bitmap.getHeight();
        float pivotX=0, pivotY=0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

}
