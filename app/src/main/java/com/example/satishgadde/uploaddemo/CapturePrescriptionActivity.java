package com.example.satishgadde.uploaddemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class CapturePrescriptionActivity extends ActionBarActivity {
    Toolbar toolbar;
    private LinearLayout capturePrescription;
    private LinearLayout selectFromGallery;
    private Context context = this;


    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private int PICK_IMAGE_REQUEST = 1;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private Uri fileUri; // file url to store image/video
    private ImageView imgPreview;
    private LinearLayout specifyPrescription;
    private TextView txtnext;




    private static final String TAG = CapturePrescriptionActivity.class.getSimpleName();
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_prescription);


        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);







        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        capturePrescription = (LinearLayout) findViewById(R.id.llcaptureprescription);
        selectFromGallery = (LinearLayout) findViewById(R.id.llselectfromgallery);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        txtnext = (TextView) findViewById(R.id.txtnext);

        specifyPrescription = (LinearLayout) findViewById(R.id.specifyprescription);

        specifyPrescription.setVisibility(View.GONE);


        txtnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(context , "Please wait",Toast.LENGTH_SHORT).show();
                Snackbar.make(coordinatorLayout, "Please wait...", Snackbar.LENGTH_SHORT).show();

            }
        });


        capturePrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(context , "Capture Prescription using camera",Toast.LENGTH_SHORT).show();
                Snackbar.make(coordinatorLayout, "Capture Prescription using camera", Snackbar.LENGTH_SHORT).show();
                captureImage();

            }
        });

        selectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);


            }
        });


        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            //Toast.makeText(getApplicationContext(),"Sorry! Your device doesn't support camera", Snackbar.LENGTH_LONG).show();
            Snackbar.make(coordinatorLayout, "Sorry! Your device doesn't support camera", Snackbar.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }


    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Launching camera app to record video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // successfully captured the image
                // launching upload activity
                launchUploadActivity(true);


            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                //Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
                Snackbar.make(coordinatorLayout, "User cancelled image capture", Snackbar.LENGTH_SHORT).show();


            } else {
                // failed to capture image
                //Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
                Snackbar.make(coordinatorLayout, "Sorry! Failed to capture image", Snackbar.LENGTH_SHORT).show();


            }

        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                // launching upload activity
                launchUploadActivity(false);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            fileUri = data.getData();//content://media/external/images/media/60132

            try {

                Uri selectedImageUri = data.getData();
                String[] projection = {MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);///storage/emulated/0/Download/12042772_765400193585968_4006769710146743259_n.jpg
                /*Bitmap bm;
            	BitmapFactory.Options options = new BitmapFactory.Options();
            	options.inJustDecodeBounds = true;
            	BitmapFactory.decodeFile(selectedImagePath, options);
            	final int REQUIRED_SIZE = 200;
            	int scale = 1;
            	while (options.outWidth / scale / 2 >= REQUIRED_SIZE
            	&& options.outHeight / scale / 2 >= REQUIRED_SIZE)
            	scale *= 2;
            	options.inSampleSize = scale;
            	options.inJustDecodeBounds = false;
            	bm = BitmapFactory.decodeFile(selectedImagePath, options);
            	ivImage.setImageBitmap(bm)*/
                ;


                launchUploadActivity(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void launchUploadActivity(boolean isImage) {

        try {


            //get file
            File photo = new File(fileUri.getPath());

            //file name
            String fileName = photo.getName();
            try {


                Integer.parseInt(fileName);

                //Uri selectedImageUri = data.getData();
                String[] projection = {MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, fileUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);///storage/emulated/0/Download/12042772_765400193585968_4006769710146743259_n.jpg

                Uri filePath = Uri.parse(selectedImagePath);

                photo = new File(filePath.getPath());
                fileName = photo.getName();


            } catch (Exception e) {
                Log.d("Convertion Error : ", e.getMessage());
            }


            Calendar calendar = Calendar.getInstance();

            long startTime = calendar.getTimeInMillis();


            //resave file with new name
            File newFile = new File(""+startTime);
            photo.renameTo(newFile);


            Intent i = new Intent(CapturePrescriptionActivity.this, UploadActivity.class);


            i.putExtra("filePath", fileUri.getPath());
            i.putExtra("isImage", isImage);
            i.putExtra("filename", fileName);
            startActivity(i);
            finish();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }



    //Get Max ORder Id From Server


    //Complete Getting Max Order Id From Server
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault());

        Date date = new Date();
        return dateFormat.format(date);
    }



}
