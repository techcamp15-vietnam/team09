/*
 * @author : Nguyen Vinh Phu 9-B
 * */
package com.android.techcamp;

import com.android.techcamp.R;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MainActivity extends Activity implements SensorEventListener {
	SensorManager mSensorManager;
	private Sensor accSensor;	  // sensor ACCELERATION
	private Sensor magnetSensor;  // sensor MAGNETIC
	double pitch, pitchHeight;	  // rotation around X axis
	double azimut;				  // rotation around the Z axis 
	double roll;				  //  rotation around Y axis
	float d; 					// Distance between object with camera
	float tempDistance;			// temporaty variables distance
	float personHeight = 1.6f;	// the height of camera
	boolean measuredDistance;
	double pitchDistance;

	private Preview mPreview; 	// Frame display in screen 
	private Camera mCamera;		// CAMERA
 
	private Button getDistance;	// get distance
	private Button getHeight;	// get Height a Object 
	TextView textView;			// 
	TextView heightResult;		// Show height result 		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FrameLayout previewLayout = (FrameLayout) findViewById(R.id.preview);
		mPreview = new Preview(MainActivity.this); 
		previewLayout.addView(mPreview);

		heightResult = (TextView) findViewById(R.id.height);
		textView = (TextView) findViewById(R.id.textView);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Use below method to get the default sensor for a given type
		accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//		mSensorManager.registerListener(this, accSensor,
//				SensorManager.SENSOR_DELAY_NORMAL);
//		mSensorManager.registerListener(this, magnetSensor,
//				SensorManager.SENSOR_DELAY_NORMAL);
		measuredDistance = false;
		initButtons();
	}

	// ----------------------------------------------------------//
	private void initButtons() {
		getDistance = (Button) findViewById(R.id.getDistance);
		getHeight = (Button) findViewById(R.id.getHeight);
		
		// When Click "getDistance" button
		getDistance.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// mPreview.takePicture();
				tempDistance = d;
				pitchDistance = pitch;
				measuredDistance = true;
			}
		});

		// When Click "getHeight" button
		getHeight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				float h2, height;
//				pitchHeight = pitch + 90;
//				h2 = Math.abs((float) (tempDistance * Math.tan(pitchHeight * Math.PI / 180)));
//				height = h2 + personHeight;
//				heightResult.setText("Height: " + height);
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
		mPreview.setCamera(mCamera);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
/* 
 * @Author: Nguyen Vinh Phu
 * pitch: angle of elevation
 * calculation by the formula : h = d * tan(pitch)
 * 
 * */
	
/*
 * @Editor: 9-A: Le Hoai Nam
 * 
 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] gravity = new float[3];
		float[] geoMagnetic = new float[3];
		float[] R = new float[9];
		float[] I = new float[9];
		float[] orientation = new float[3];
		boolean ready = false;
		float h2, height;

		geoMagnetic[0] = 0;
		geoMagnetic[1] = 1;
		geoMagnetic[2] = 0;

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			geoMagnetic = event.values.clone();

		if (gravity != null && geoMagnetic != null) {
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geoMagnetic);
			if (success)
				{
					/* Orientation has azimuth, pitch and roll */
					SensorManager.getOrientation(R, orientation);
					azimut = Math.toDegrees(orientation[0]);
					pitch = Math.toDegrees(orientation[1]);
					roll = Math.toDegrees(orientation[2]); //
					d = Math.abs((float) (personHeight * Math.tan(pitch * Math.PI/ 180)));
					textView.setText("D: " + String.valueOf(d) + "\n Angle: "
							+ pitch + "\n Azimut" + azimut);
					if (measuredDistance) {
						if  (gravity[2] == 0) 
							heightResult.setText("Height: " + personHeight);
							else
								if (gravity[2] < 0) {
									pitchHeight = pitch - 90;
									h2 = Math.abs((float) (tempDistance * Math.tan(pitchHeight * Math.PI / 180)));
									height = h2 + personHeight;
									heightResult.setText("Height: " + height);
								}
								else {
									height = personHeight*(1 - (float)(Math.tan(Math.abs(pitchDistance)* Math.PI / 180)/Math.tan(Math.abs(pitch)* Math.PI / 180)));
									heightResult.setText("Height: " + height);
								}	
							}
	
				}

		}
	}
}