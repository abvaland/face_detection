package com.facedetection;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ajay on 11-09-2017.
 */
public class FragmentImage extends Fragment {

    private static final int PERMISSION_ALL = 1;
    private static final String TAG = "FragmentImage";
    private static final String IMAGE_DIRECTORY_NAME = "Ajay";
    //TODO :: Change MainActivity with your activity
    MainActivity activity;
    private String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    Dialog imgChoiceDialog;
    public static final int CAMERA_INTENT=1;
    public static final int GALLERY_INTENT=2;

    ArrayList<String> realPath;
    private Uri imageCaptureUri;
    private File imageCaptureFile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_image_selection,container,false);

        initDialogView();
        return view;

    }

    private void initDialogView() {

        imgChoiceDialog = new Dialog(getActivity());
        imgChoiceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imgChoiceDialog.setContentView(R.layout.imgchoice_dialog);
        View viewDialogCamera = imgChoiceDialog.findViewById(R.id.btnCamera);
        View viewDialogGallery = imgChoiceDialog.findViewById(R.id.btnGallery);
        imgChoiceDialog.setTitle("Select Image From ?");

        viewDialogCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
                imgChoiceDialog.dismiss();
            }
        });

        viewDialogGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewGallery();
                imgChoiceDialog.dismiss();
            }
        });


    }

    public void viewGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,GALLERY_INTENT);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M)
        {
            imageCaptureUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile());
        }
        else {
            imageCaptureUri = Uri.fromFile(getOutputMediaFile());
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
        startActivityForResult(intent, CAMERA_INTENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //TODO :: Change MainActivity with your activity
        activity= (MainActivity) context;
    }

    public void showChoiceDialog() {
        if(!hasPermission(activity,permissions))
        {
            ActivityCompat.requestPermissions(activity,permissions,PERMISSION_ALL);
        }
        else
        {
            imgChoiceDialog.show();
        }
    }

    private boolean hasPermission(Context context, String[] permissions) {

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && context!=null && permissions!=null)
        {
            for(String permission:permissions)
            {
                if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode==GALLERY_INTENT && resultCode== Activity.RESULT_OK && data != null) {
            realPath=new ArrayList<>();
           /* ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();

                    Log.e(TAG,"URI :: "+uri.toString());
                    //In case you need image's absolute path
                    String path= getRealPathFromURI(activity, uri);
                    realPath.add(path);
                }
                copyImagesToDesireFolder(realPath);
            }
            else {*/
               Uri uri=data.getData();
            activity.onImageSelectResult(uri);

            Log.e(TAG,"1 Image "+uri.toString());
                String path= getRealPathFromURI(activity, uri);
                realPath.add(path);
                copyImagesToDesireFolder(realPath);
            //}

        }
        else if(requestCode==CAMERA_INTENT && resultCode== Activity.RESULT_OK)
        {
            realPath=new ArrayList<>();
            Log.e(TAG,"Camera Image :: "+imageCaptureUri.toString());
            Log.e(TAG,"Camera Image Path :: "+imageCaptureFile.getAbsolutePath());

            realPath.add(imageCaptureFile.getAbsolutePath());
            copyImagesToDesireFolder(realPath);

        }
        else
        {
            Log.e(TAG,"Image Can't Get From Camera or Gallery");
           // Log.e(TAG,"Camera Image :: "+imageCaptureUri.toString());
        }

    }


    public String getRealPathFromURI(Context context, Uri contentUri) {

        if(contentUri.toString().startsWith("file://"))
        {
            return contentUri.getPath();
        }
        Cursor cursor = null;
        try {
             cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            if (cursor == null) {
                return contentUri.getPath();
            } else {
                cursor.moveToFirst();
                //int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                int index=cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                return cursor.getString(index);


            }
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }



    /**
     * returning image / video
     */
    public  File getOutputMediaFile() {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Merchant", "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile = null;


        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        imageCaptureFile=mediaFile;
        return mediaFile;
    }

    private void copyImagesToDesireFolder(ArrayList<String> realPath) {
        ArrayList<String> storePath=new ArrayList<>();

        //TODO :: // change this location with your location
        File saveFolder=new File(activity.getFilesDir(),"images");//this location list out app data folder
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        Date d=new Date();

        for (int i=0;i<realPath.size();i++) {
            Log.e(TAG, "Real Path :: "+realPath.get(i));

            String fileName= dateFormat.format(d) +"_"+i+".jpeg";
            File src=new File(realPath.get(i));
            File dest=new File(saveFolder.getAbsolutePath(),fileName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                copy(src,dest);
            }

            storePath.add(dest.getAbsolutePath());
        }

        passResultToActivity(storePath);

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void copy(File src, File dst) {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
        catch (IOException e)
        {
            Log.e(TAG,"Error in copy =>"+e.toString());
        }
    }


    private void passResultToActivity(ArrayList<String> imagePath)
    {
       // activity.onImageSelectResult(imagePath);
    }

}
