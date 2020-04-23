package com.example.androideatitshipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.androideatitshipper.Common.Common;
import com.example.androideatitshipper.Model.Shipper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {

    Button btn_sign_in;
    MaterialEditText edt_phone,edt_password;

    FirebaseDatabase database;
    DatabaseReference shippers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btn_sign_in=findViewById(R.id.btnSignIn);
        edt_phone=findViewById(R.id.edtPhone);
        edt_password=findViewById(R.id.edtPassword);

        database=FirebaseDatabase.getInstance();
        shippers=database.getReference(Common.SHIPPER_TABLE);

        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(edt_phone.getText().toString(), edt_password.getText().toString());
            }
        });
    }

    private void login(String phone, final String password) {
        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            Shipper shipper=dataSnapshot.getValue(Shipper.class);
                            if(shipper.getPassword().equals(password)){

                                //Login succeed
                                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                Common.currentShipper=shipper;
                                finish();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Password incorrect !", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else{
                            Toast.makeText(MainActivity.this, "Shippers phone does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
