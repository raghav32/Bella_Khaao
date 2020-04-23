package com.proyek.rahmanjai.eatitserver.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.format.DateFormat;

import com.proyek.rahmanjai.eatitserver.Model.Request;
import com.proyek.rahmanjai.eatitserver.Model.User;
import com.proyek.rahmanjai.eatitserver.Remote.APIService;
import com.proyek.rahmanjai.eatitserver.Remote.FCMRetrofitClient;
import com.proyek.rahmanjai.eatitserver.Remote.IGeoCoordinates;
import com.proyek.rahmanjai.eatitserver.Remote.RetrofitClient;

import java.util.Calendar;
import java.util.Locale;

public class Common {
    public static User currentUser;
    public static Request currentRequest;


    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final int PICK_IMAGE_REQUEST = 71;

    public static final String baseUrl = "https://maps.googleapis.com";

    public static final String fcmUrl = "https://fcm.googleapis.com/";

     public static String PHONE_TEXT="userPhone";
    public static final String SHIPPERS_TABLE="Shippers";
    public static final String ORDER_NEED_SHIP_TABLE="OrdersNeedShip";

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

    public static IGeoCoordinates getGeoCodeService() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMClient() {
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
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

    public static String getDate(long time){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date=new StringBuilder(DateFormat.format("dd-MM-yyyy HH:mm",calendar).toString());
        return date.toString();
    }
}
