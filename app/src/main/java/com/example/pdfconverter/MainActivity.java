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
    FloatingActionButton Action_btn;
    RecyclerView rv;
    SqliteDatabase sq;
    ImageRecyclerAdapter imageRecyclerAdapter;
    int REQUEST_CODE = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getUtils();
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "Unable to load OpenCV!");
        else
            Log.d("OpenCV", "OpenCV loaded Successfully!");
         sq=new SqliteDatabase(this);
        imageRecyclerAdapter=new ImageRecyclerAdapter(sq.readAllData(),this);
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
            Mat imgGray = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
            Mat thresh = imgGray;
            Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap, mat);
            correctPerspective(mat);

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
    public static void correctPerspective(Mat imgSource)
    {

        // convert the image to black and white does (8 bit)
        Imgproc.Canny(imgSource.clone(), imgSource, 50, 50);

        // apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(imgSource, imgSource, new org.opencv.core.Size(5, 5), 5);

        // find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imgSource, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        MatOfPoint temp_contour = contours.get(0);
        // index 0 for starting
        // point
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            // compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                // check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.05, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    approxCurve = approxCurve_temp;
                }
            }
        }

        Imgproc.cvtColor(imgSource, imgSource, Imgproc.COLOR_BayerBG2RGB);

        double[] temp_double;
        temp_double = approxCurve.get(0, 0);
        Point p1 = new Point(temp_double[0], temp_double[1]);

        temp_double = approxCurve.get(1, 0);
        Point p2 = new Point(temp_double[0], temp_double[1]);

        temp_double = approxCurve.get(2, 0);
        Point p3 = new Point(temp_double[0], temp_double[1]);

        temp_double = approxCurve.get(3, 0);
        Point p4 = new Point(temp_double[0], temp_double[1]);

        List<Point> source = new ArrayList<Point>();
        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);
        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Mat result = warp(imgSource, startM);

        //Saving into bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(result.cols(),  result.rows(),Bitmap.Config.ARGB_8888);;
        Mat tmp = new Mat (result.cols(), result.rows(), CvType.CV_8U, new Scalar(4));
        Imgproc.cvtColor(result, tmp, Imgproc.COLOR_RGB2BGRA);
        Utils.matToBitmap(tmp, resultBitmap);

    }
    public static Mat warp(Mat inputMat, Mat startM)
    {

        int resultWidth = 1200;
        int resultHeight = 680;

        Point ocvPOut4 = new Point(0, 0);
        Point ocvPOut1 = new Point(0, resultHeight);
        Point ocvPOut2 = new Point(resultWidth, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, 0);

        if (inputMat.height() > inputMat.width())
        {
            ocvPOut3 = new Point(0, 0);
            ocvPOut4 = new Point(0, resultHeight);
            ocvPOut1 = new Point(resultWidth, resultHeight);
            ocvPOut2 = new Point(resultWidth, 0);
        }

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

        return outputMat;
    }


}