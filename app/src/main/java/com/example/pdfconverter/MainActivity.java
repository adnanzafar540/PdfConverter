package com.example.pdfconverter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton Action_btn;
    RecyclerView rv;
    SqliteDatabase sq;
    ImageRecyclerAdapter imageRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getUtils();
         sq=new SqliteDatabase(this);
        imageRecyclerAdapter=new ImageRecyclerAdapter(checkForDeleteFiles(sq.readAllData()),this);
        rv.setLayoutManager(new GridLayoutManager(this,2));
        imageRecyclerAdapter.notifyDataSetChanged();
        rv.setAdapter(imageRecyclerAdapter);

        Action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }

        });


    }

    public void getUtils() {
        Action_btn = findViewById(R.id.floating_act_btn);
        rv=findViewById(R.id.main_rv);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Images model=new Images();
       SqliteDatabase sq2=new SqliteDatabase(this);

        Uri uri = data.getData();
       // img.setImageURI(uri);
        Bitmap bitmap = null;
        model.setUrl(String.valueOf(uri));
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageinfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageinfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        pdfDocument.finishPage(page);
        String imagename="Converted pdf"+RandomGenerator()+".pdf";
        ///pdfDocument.close();

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                , imagename);
        model.setPdfurl(file.getAbsolutePath());
        model.setImage_txt(imagename);
        FileOutputStream outputStream1 = null;
        try {
            outputStream1 = new FileOutputStream(file);
            pdfDocument.writeTo(outputStream1);
            outputStream1.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();
        sq.insertData(model);
      ImageRecyclerAdapter imageRecyclerAdapter2=new ImageRecyclerAdapter(checkForDeleteFiles(sq.readAllData()),this);
                imageRecyclerAdapter2.notifyDataSetChanged();
                rv.setAdapter(imageRecyclerAdapter2);


    }

    public int RandomGenerator(){

        Random generator = new Random();
        int n = 1000;
        n = generator.nextInt(n);
        return n;
    }
    public ArrayList<Images> checkForDeleteFiles(ArrayList<Images> Filter) {
        ArrayList<Images> FiterData_Deletion;
        FiterData_Deletion = new ArrayList<>();
        try {
            for (Images img1 : Filter)
                if (img1.getPdfurl() != null) {
                    File Delfile = new File(img1.getPdfurl());
                    if (Delfile.exists()) {
                        FiterData_Deletion.add(img1);
                    }
                }

        } catch (Exception e) {
        }

        return FiterData_Deletion;
    }

}