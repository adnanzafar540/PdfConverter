package com.example.pdfconverter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button Store_Pdfs;
    Button Import_Images;
    Button Smart_Scan;

    int REQUEST_CODE = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_layout);
        getUtils();
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "Unable to load OpenCV!");
        else
            Log.d("OpenCV", "OpenCV loaded Successfully!");


        Import_Images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
                        .galleryOnly()	//User can only select image from Gallery
                        .start();
            }

        }); Smart_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
                        .cameraOnly()	//User can only capture image using Camera
                        .start();
            }

        }); Store_Pdfs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, com.example.pdfconverter.Store_Pdfs.class);
                startActivity(intent);
            }

        });


    }

    public void getUtils() {
        Store_Pdfs = findViewById(R.id.btn_show_pdf);
        Import_Images = findViewById(R.id.btn_import);
        Smart_Scan = findViewById(R.id.btn_smartscan);

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