package com.android.techcamp;

import com.example.camerapreviewdemo.R;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MainActivity extends Activity implements SensorEventListener {
	SensorManager mSensorManager;
	private Sensor accSensor;
	private Sensor magnetSensor;
	double pitch, pitchHeight;
	double azimut;
	double roll;
	float d ;  //Distance 
	float tempDistance;
	float personHeight = 1.6f;
		
	private Preview mPreview; // khung nhìn, hiển thị những gì máy ảnh thấy.
	private Camera mCamera;
	private int numberOfCameras;
	private int cameraCurrentlyLocked;
	private ZoomControls mZoomControl;
	private int currentZoomLevel;

	// The first rear facing camera
	int defaultCameraId; // Camera mac dinh - back
	private Button mBtnTakePicture;
	private Button mBtnSwitchCamera;
	private Button mono;

	TextView textView;
	TextView heightView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FrameLayout previewLayout = (FrameLayout) findViewById(R.id.preview);
		mPreview = new Preview(MainActivity.this); // Class "Preview" chi de
													// phuc vu su the hien len
													// man hinh ma thoi
		previewLayout.addView(mPreview);
		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}
		
		heightView = (TextView)findViewById(R.id.height);
		textView = (TextView) findViewById(R.id.textView);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(this, accSensor,SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, magnetSensor,SensorManager.SENSOR_DELAY_NORMAL);
		initButtons();
	}

	// ----------------------------------------------------------//
	private void initButtons() {
		mBtnTakePicture = (Button) findViewById(R.id.button_take_picture);
		mBtnSwitchCamera = (Button) findViewById(R.id.button_switch_camera);
		mBtnTakePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// mPreview.takePicture();
				tempDistance = d;								
			}
		});

		mBtnSwitchCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				float h2, height;				
				pitchHeight = pitch + 90;
				h2 = Math.abs((float) (tempDistance * Math.tan(pitchHeight * Math.PI / 180)));
				height = h2 + personHeight;
				heightView.setText("Height: " + height);
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
        mSensorManager.registerListener(this, accSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {		
		float[] gravity = new float[3];
		float[] geoMagnetic = new float[3];
		float[] R = new float[9];
		float[] I = new float[9];
		float[] orientation= new float[3];
		boolean ready = false;
		
		geoMagnetic[0] = 0;
		geoMagnetic[1] = 1;
		geoMagnetic[2] = 0;
		

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			geoMagnetic = event.values.clone();				

		
		if (gravity != null && geoMagnetic != null)
		{
			boolean success = SensorManager.getRotationMatrix(R, null, gravity, geoMagnetic);			
//			if (success)
			{
				/* Orientation has azimuth, pitch and roll */				
				SensorManager.getOrientation(R, orientation);
				azimut = Math.toDegrees(orientation[0]);
				pitch = Math.toDegrees(orientation[1]);
				roll = Math.toDegrees(orientation[2]);
				//textView.setText("Pitch: " + String.valueOf(pitch));
				 d = Math.abs((float) (personHeight * Math.tan(pitch * Math.PI / 180)));
				textView.setText("D: " + String.valueOf(d) + "\n Angle: " + pitch);
			}
		}

//		 textView.setText("X: " + event.values[0] +
//		 "\n Y: " + event.values[1]+
//		 "\n Z: " + event.values[2]);
		// int sensorHeight = 2;
		// double d = (Math.tan(Math.toRadians(Math.abs(pitch))) *
		// sensorHeight);
//		float d = Math.abs((float) (1.4f * Math.tan(pitch * Math.PI / 180)));
//		Toast.makeText(
//				getApplicationContext(),
//				"Distance = " + String.valueOf(d) + "m  Angle = "
//						+ String.valueOf(Math.toRadians(Math.abs(pitch))),
//				Toast.LENGTH_LONG).show();
	}

}
