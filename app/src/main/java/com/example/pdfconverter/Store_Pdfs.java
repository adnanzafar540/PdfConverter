package com.example.pdfconverter;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Store_Pdfs extends AppCompatActivity {
    RecyclerView rv;
    SqliteDatabase sq;
    ImageRecyclerAdapter imageRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sq=new SqliteDatabase(this);
        imageRecyclerAdapter=new ImageRecyclerAdapter(sq.readAllData(),this);
        rv.setLayoutManager(new GridLayoutManager(this,2));
        imageRecyclerAdapter.notifyDataSetChanged();
        rv.setAdapter(imageRecyclerAdapter);
    }
}
