package com.proyek.rahmanjai.eatitserver.Remote;


import com.proyek.rahmanjai.eatitserver.Model.MyResponse;
import com.proyek.rahmanjai.eatitserver.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAARl049o4:APA91bGfJGpYGkct5NeeJHVWB1g_nxzlQqUGGqN0mwdL6ymtOWB6smU7YytCXGtUMGesLhuAwJEGs-XOT2Xb2toTiS2KGO_ITiX5JkepoHEAd_o9_LzdP4up3cw3bPBnY4zjB-TsyQn3"
            }

    )



    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body Sender body);
}
