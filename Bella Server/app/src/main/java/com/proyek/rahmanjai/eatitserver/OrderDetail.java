package com.proyek.rahmanjai.eatitserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.proyek.rahmanjai.eatitserver.Common.Common;
import com.proyek.rahmanjai.eatitserver.ViewHolder.OrderDetailAdapter;

public class OrderDetail extends AppCompatActivity {

    TextView order_id,order_phone,order_address,order_total,order_comment;
    String order_id_value="";
    RecyclerView lstfoods;
    RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_detail);

        order_id=findViewById(R.id.order_id);
        order_phone=findViewById(R.id.order_phone);
        order_address=findViewById(R.id.order_address);
        order_total=findViewById(R.id.order_total);
        order_comment=findViewById(R.id.order_comment);

        lstfoods=findViewById(R.id.lstFoods);
        lstfoods.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        lstfoods.setLayoutManager(layoutManager);

        if(getIntent() != null){
            order_id_value=getIntent().getStringExtra("OrderId");
        }

        //Set value
        order_id.setText(order_id_value);
        order_phone.setText(Common.currentRequest.getPhone());
        order_total.setText(Common.currentRequest.getTotal());
        order_address.setText(Common.currentRequest.getAddress());
        order_comment.setText(Common.currentRequest.getComment());

        OrderDetailAdapter adapter=new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        lstfoods.setAdapter(adapter);
    }
}
