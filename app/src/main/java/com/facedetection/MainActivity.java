package com.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView tvInfo;
    ImageView img1;
    Button btnChooseFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = findViewById(R.id.tvInfo);
        img1 = findViewById(R.id.img);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        final FragmentImage fragmentImage = new FragmentImage();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,fragmentImage).commit();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bb);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        detectImage(image);

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentImage.viewGallery();
            }
        });


    }

    public void onImageSelectResult(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            Glide.with(this).load(uri).into(img1);
            image = FirebaseVisionImage.fromFilePath(this,uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,e.toString());
        }
        detectImage(image);
    }
   /* public void onImageSelectResult(Bitmap bitmap) {
                img1.setImageURI(Uri.fromFile(file));
                FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(this, Uri.fromFile(file));
                detectImage(image);
    }*/


    private void detectImage(FirebaseVisionImage image)
    {
        tvInfo.setText("Detecting Faces...");
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();


        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result= detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                Log.e(TAG,"onSuccess : "+firebaseVisionFaces);
                StringBuilder faces = new StringBuilder();
                int cnt = 1 ;
                for (FirebaseVisionFace face:firebaseVisionFaces)
                {
                    // If classification was enabled:
                    if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        float smileProb = face.getSmilingProbability();
                        Log.e(TAG,"smileProb : "+smileProb);

                        if(smileProb>0.7)
                        {
                            faces.append("Face[").append(cnt).append("] is : Smile \n");
                        }
                        else
                        {
                            faces.append("Face[").append(cnt).append("] is : Not Smile \n");
                        }
                    }
                    if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        float rightEyeOpenProb = face.getRightEyeOpenProbability();
                        Log.e(TAG,"rightEyeOpenProb : "+rightEyeOpenProb);
                    }
                    cnt++;
                }

                tvInfo.setText(faces.toString());


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"onFailure : "+e.toString());
                tvInfo.setText("Fail to DetectFaces.!");
            }
        });
    }
}
