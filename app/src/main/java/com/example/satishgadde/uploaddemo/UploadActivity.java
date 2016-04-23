package com.example.satishgadde.uploaddemo;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class UploadActivity extends ActionBarActivity {
	// LogCat tag
	private static final String TAG = UploadActivity.class.getSimpleName();

	ProgressDialog dialog = null;
	int serverResponseCode = 0;
	private ProgressBar progressBar;
	private String filePath = null;
	private TextView txtPercentage;
	private ImageView imgPreview;
	private VideoView vidPreview;
	//private Button btnUpload;
	private TextView btnUpload;
	long totalSize = 0;

	private Toolbar toolbar;

	private EditText txtdescr;

	private Context context=this;



	private String filename="";
	private String vresval="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		
		

		setTitle("Send Prescription");
		

		
		
		
		txtPercentage = (TextView) findViewById(R.id.txtPercentage);
		//btnUpload = (Button) findViewById(R.id.btnUpload);
		btnUpload = (TextView) findViewById(R.id.btnUpload);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		vidPreview = (VideoView) findViewById(R.id.videoPreview);
		
		txtdescr=(EditText)findViewById(R.id.txtdescr);
		
		
		

		// Changing action bar background color
		try {
			//getActionBar().setBackgroundDrawable(
				//	new ColorDrawable(Color.parseColor(getResources().getString(
					//		R.color.action_bar))));
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Receiving the data from previous activity
		Intent i = getIntent();

		filename = i.getStringExtra("filename");
		// image or video path that is captured in previous activity
		filePath = i.getStringExtra("filePath");///external/images/media/60132   content://media/external/images/media/60132
///storage/emulated/0/Pictures/Android File Upload/IMG_20160111_180355.jpg
		if(filePath.contains("storage"))
		{
			filePath = i.getStringExtra("filePath");
		}
		else
		{
			try {
				Uri uri = Uri.parse("content://media"+filePath);//content://media/external/images/media/65044
				String[] projection = { MediaStore.Images.Media.DATA };
				 
				Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
				cursor.moveToFirst();
				 
				Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
				 
				int columnIndex = cursor.getColumnIndex(projection[0]);
				filePath = cursor.getString(columnIndex); // returns null  /storage/emulated/0/DCIM/Camera/P_20160106_095939.jpg
				cursor.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		// boolean flag to identify the media type, image or video
		boolean isImage = i.getBooleanExtra("isImage", true);

		if (filePath != null) {
			// Displaying the image or video on the screen
			previewMedia(isImage);
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry, file path is missing!", Toast.LENGTH_LONG).show();
		}

		try {
			btnUpload.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // uploading the file to server
                    try {
                //        new UploadFileToServer().execute();

						dialog = ProgressDialog.show(UploadActivity.this, "", "Uploading file...", true);
						new Thread(new Runnable() {
							public void run() {
								runOnUiThread(new Runnable() {
									public void run() {
										//tv.setText("uploading started.....");

										txtdescr.setText("Uploading Started");


									}
								});
								int response= uploadFile("/sdcard/android_1.png");
								System.out.println("RES : " + response);
							}
						}).start();
						//dialog.setCancelable(true);

					} catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Displaying captured image/video on the screen
	 * */
	private void previewMedia(boolean isImage) {
		// Checking whether captured media is image or video
		if (isImage) {
			imgPreview.setVisibility(View.VISIBLE);
			vidPreview.setVisibility(View.GONE);
			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// down sizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
			

			imgPreview.setImageBitmap(bitmap);
			
		} else {
			imgPreview.setVisibility(View.GONE);
			vidPreview.setVisibility(View.VISIBLE);
			vidPreview.setVideoPath(filePath);
			// start playing
			vidPreview.start();
		}
	}

	/**
	 * Uploading the file to server
	 * */
	private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
		private String vresval="";

		@Override
		protected void onPreExecute() {
			// setting progress bar to zero
			progressBar.setProgress(0);
			progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// Making progress bar visible
			progressBar.setVisibility(View.VISIBLE);

			// updating progress bar value
			progressBar.setProgress(progress[0]);

			// updating percentage value
			txtPercentage.setText(String.valueOf(progress[0]) + "%");
		}

		@Override
		protected String doInBackground(Void... params) {
			return uploadFile22();
		}


		private String uploadFile22() {
			String responseString = null;

			

/*
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

					AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                            new ProgressListener() {

                                @Override
                                public void transferred(long num) {
                                    publishProgress((int) ((num / (float) totalSize) * 100));
                                }
                            });


				File sourceFile = new File(filePath);

				// Adding file data to http body
				entity.addPart("image", new FileBody(sourceFile));

				// Extra parameters if you want to pass to server
				entity.addPart("website",
						new StringBody("www.yourcarepharmacy.in"));
				entity.addPart("email", new StringBody("satishgadde23@gmail.com"));

				totalSize = entity.getContentLength();
				httppost.setEntity(entity);

				// Making server call
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity r_entity = response.getEntity();

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					
					// Server response
					responseString = EntityUtils.toString(r_entity);
					ServiceHandler sh = new ServiceHandler();
					String txtdecr = txtdescr.getText().toString();
				
					txtdecr = txtdecr.replace(" ", "%20");
					txtdecr = txtdecr.replace(",", "%2c");
					txtdecr = txtdecr.replace(".", "%2E");
					
					Calendar calendar = Calendar.getInstance();
		        	
		        	long startTime = calendar.getTimeInMillis();
		        	
					String newfilename = userDetails.get(SessionManager.KEY_USERID)+startTime;
					
					///MedicalService.asmx/InsertPrescription?action=string&userid=string&desc=string&imgname=string&newfilename=string
					//String url = Allkeys.TAG_WEBSITE+"?userid="+ userDetails.get(SessionManager.KEY_USERID) +"&descr="+ txtdecr +"&imgname="+ filename +"&newfilename="+ newfilename +"";
					String url = Allkeys.TAG_WEBSITE+"InsertPrescription?action=insertpresc&custid="+ userDetails.get(SessionManager.KEY_USERID) +"&desc="+ txtdecr +"&imgname="+ filename +"&newfilename="+ newfilename +"&orderid="+ userDetails.get(SessionManager.KEY_ORDERID) +"&status=1&orderdt="+ getDateTime() +"";
					//http://yourcarepharmacy.com/MedicalService.asmx/InsertPrescription?action=insertpresc&custid=11&desc=7&imgname=12042772_765400193585968_4006769710146743259_n.jpg&newfilename=111453279851990&orderid=9&status=1&orderdt=2016-01-20
					vresval  = sh.makeServiceCall(url, ServiceHandler.GET);
					
					
					
				} else {
					responseString = "Error occurred! Http Status Code: "
							+ statusCode;
				}

			} catch (ClientProtocolException e) {
				responseString = e.toString();
			} catch (IOException e) {
				responseString = e.toString();
			}
*/

			return responseString;

		}

		@Override
		protected void onPostExecute(String result) {
			Log.e(TAG, "Response from server: " + result);

			// showing the server response in an alert dialog
			//showAlert(result);
			

			super.onPostExecute(result);
			
			
			
			
			if (vresval.equals("0")) 
			{
			Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show();
				
			} else {
				
				
				
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadActivity.this);
				 alertDialogBuilder.setTitle("Response From Server");
			      alertDialogBuilder.setMessage(result);
			      
			      alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			         @Override
			         public void onClick(DialogInterface arg0, int arg1) {
			            //Toast.makeText(SearchAndUploadActivity.this,"Sorry, Products are not available",Toast.LENGTH_LONG).show();
			            

							


			            
			         }
			      });
			    
			      
			      AlertDialog alertDialog = alertDialogBuilder.create();
			      alertDialog.show();
				alertDialog.setCancelable(false);
			      
				
				
				
				
				
				
									
			}
			
			
		}

	}

	/**
	 * Method to show alert dialog
	 * */
	private void showAlert(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setTitle("Response from Yourcare")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// do nothing
						

						Intent intent = new Intent(getApplicationContext(),CapturePrescriptionActivity.class);
						startActivity(intent);
						finish();

						
						
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_and_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_next_searchandupload) {
            
        	
        	 try {

                 //new UploadFileToServer().execute();

				 dialog = ProgressDialog.show(UploadActivity.this, "", "Uploading file...", true);
				 new Thread(new Runnable() {
					 public void run() {
						 runOnUiThread(new Runnable() {
							 public void run() {
								 //tv.setText("uploading started.....");

								 txtdescr.setText("Uploading Started");


							 }
						 });
						 int response= uploadFile("/sdcard/android_1.png");
						 System.out.println("RES : " + response);
					 }
				 }).start();


			 } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
        	
        }

        return super.onOptionsItemSelected(item);
    }
    
    private String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.getDefault());

		Date date = new Date();
		return dateFormat.format(date);
	}




	//File Uploading Demo

	@SuppressLint("LongLogTag")
	public int uploadFile(String sourceFileUri) {
		String upLoadServerUri = Config.FILE_UPLOAD_URL;//"http://10.0.2.2/upload_test/upload_media_test.php";
		final String fileName = filename;
		sourceFileUri =filePath;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);
		if (!sourceFile.isFile()) {
			Log.e("uploadFile", "Source File Does not exist");
			return 0;
		}
		try { // open a URL connection to the Servlet
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);
			conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
			conn.setDoInput(true); // <span id="IL_AD4" class="IL_AD">Allow</span> Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);
			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();


			Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
			if(serverResponseCode == 200){
				runOnUiThread(new Runnable() {
					public void run() {




							android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(UploadActivity.this);
							alertDialogBuilder.setTitle("Response From YourCare");
							alertDialogBuilder.setMessage("Prescription successfully uploaded...");
							alertDialogBuilder.setCancelable(false);
							alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									//Toast.makeText(SearchAndUploadActivity.this,"Sorry, Products are not available",Toast.LENGTH_LONG).show();


									Intent inten = new Intent(context, CapturePrescriptionActivity.class);
									startActivity(inten);
									finish();



								}
							});

							android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
							alertDialog.setCancelable(false);











						}


						//Toast.makeText(UploadActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();

				});
			}

			//close the streams //
			fileInputStream.close();
			dos.flush();
			dos.close();

		} catch (MalformedURLException ex) {
			dialog.dismiss();
			ex.printStackTrace();
			Toast.makeText(UploadActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
			Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
		} catch (Exception e) {
			dialog.dismiss();
			e.printStackTrace();
			Toast.makeText(UploadActivity.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
		}
		dialog.dismiss();
		return serverResponseCode;
	}
	//Complete File Uplaoding
    

}