package com.proyek.rahmanjai.eatitserver.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.proyek.rahmanjai.eatitserver.Model.Order;
import com.proyek.rahmanjai.eatitserver.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView name,quantity,price,discount;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name=itemView.findViewById(R.id.product_name);
        quantity=itemView.findViewById(R.id.product_quantity);
        price=itemView.findViewById(R.id.product_price);
        discount=itemView.findViewById(R.id.product_discount);

    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {
    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order order=myOrders.get(position);
        holder.name.setText(String.format("Name : %s",order.getProductName()));
        holder.quantity.setText(String.format("Quantity : %s",order.getQuantity()));
        holder.price.setText(String.format("Price : %s",order.getPrice()));
        holder.discount.setText(String.format("Discount : %s",order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
