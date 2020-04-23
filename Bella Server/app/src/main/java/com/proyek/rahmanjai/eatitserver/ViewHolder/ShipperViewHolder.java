package com.proyek.rahmanjai.eatitserver.ViewHolder;

import android.view.View;
import android.widget.TextView;

import com.proyek.rahmanjai.eatitserver.Interface.ItemClickListener;
import com.proyek.rahmanjai.eatitserver.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import info.hoang8f.widget.FButton;

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView shipper_name,shipper_phone;
    public FButton btn_edit,btn_remove;
    private ItemClickListener itemClickListener;

    public ShipperViewHolder(@NonNull View itemView) {
        super(itemView);

        shipper_name=itemView.findViewById(R.id.shipper_name);
        shipper_phone=itemView.findViewById(R.id.shipper_phone);
        btn_edit=itemView.findViewById(R.id.btnEdit);
        btn_remove=itemView.findViewById(R.id.btnRemove);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
