package com.example.androideatitshipper.ViewHolder;


import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.androideatitshipper.R;


public class OrderViewHolder extends RecyclerView.ViewHolder  {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress, txtOrderDate;

    public Button btnShipping;


    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderDate = itemView.findViewById(R.id.order_date);


        btnShipping = itemView.findViewById(R.id.btnShipping);


    }
}
