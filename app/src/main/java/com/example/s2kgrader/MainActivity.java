package com.example.s2kgrader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Long.parseLong;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener{

    ImageView image;
    TextView textView;
    TextView textView2;

    Student student;

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID+".fileprovider";

    static final int REQUEST_TAKE_PHOTO = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String TAG = "MainActivity";

    private  String imagePath = null;

    DatabaseReference reff;

    long maxid=0;

    String name;
    int rollno;
    long mob;
    String resulttext;
    String subject;
    String marks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println(AUTHORITY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);

        Intent intent = getIntent();
        subject = intent.getStringExtra("subject_string");
        marks = intent.getStringExtra("marks_string");

        Log.d("DATA PASSED",subject);
        Log.d("DATA PASSED",marks);

        image = findViewById(R.id.image_view);
        textView2 = findViewById(R.id.textView2);

        reff = FirebaseDatabase.getInstance().getReference().child("Marks");

        reff.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    maxid = (dataSnapshot.child(subject).getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }




    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(MainActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public void onClick(View view) {
                try {
                    // Do something
                    getpicture();

                } catch (Exception e) {
                    Log.e("Photo Err",e.getMessage());
                }
    }


    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }



    private void getpicture() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Photo Error",ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            image.setImageBitmap(bitmap);
            extractText(bitmap);
        }


    }

    private void extractText(Bitmap bitmap) {

        student = new Student();

        //Bitmap resizedBitmap = getResizedBitmap(bitmap,480,360);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        //Id used instead of 'hi'
        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("en", "hi","us"))
                .build();
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudTextRecognizer();

        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                resulttext = firebaseVisionText.getText();
                //textView2.setText(resulttext);

                Log.d("Result",resulttext);

                //FireBase Reference TO BE EDITED LATER!
                //reff.push().setValue("DEMO TEST");

            }
        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error Occured",e.getMessage());

                            }
                        });
        //EXPERIMENTAL CODE

        FirebaseVisionImage barimage = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionBarcodeDetector barcodeDetector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();

        Task<List<FirebaseVisionBarcode>> result = barcodeDetector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {

                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully

                        for (FirebaseVisionBarcode barcode: barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            Log.d("QR RESULT",rawValue);

                            final JSONObject obj;
                            try {
                                obj = new JSONObject(rawValue);

                                name = obj.getString("name");
                                rollno = Integer.parseInt(obj.getString("rollno"));
                                mob = parseLong(obj.getString("mob"));

                                Log.d("Result",name);
                                Log.d("Result", String.valueOf(rollno));
                                Log.d("Result", String.valueOf(mob));

                                student.setName(name);
                                student.setRollno(rollno);
                                student.setPhoneno(mob);
                                student.setAnswer(resulttext);


                                reff.child(subject).child(String.valueOf(maxid)).setValue(student);
                                //reff.push().child("English").child(String.valueOf(maxid)).setValue(student);
                                Toast.makeText(MainActivity.this,"Data Added", Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                Toast.makeText(MainActivity.this,"Error Scanning ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Log.e("Error Occured",e.getMessage());
                        Toast.makeText(MainActivity.this,"Error Scanning ", Toast.LENGTH_LONG).show();
                    }
                });


//            student.setName(name);
//            student.setRollno(rollno);
//            student.setPhoneno(mob);
//            student.setAnswer("DEMO ANSWER");

//            reff.child(String.valueOf(maxid+1)).setValue(student);
        //reff.child("English").child(String.valueOf(maxid+1)).setValue(student);






        }


}






