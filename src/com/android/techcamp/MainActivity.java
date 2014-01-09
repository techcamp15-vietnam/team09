/*
 * @author : Nguyen Vinh Phu 9-B
 * */
package com.android.techcamp;

import com.android.techcamp.R;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MainActivity extends Activity implements SensorEventListener {
	SensorManager mSensorManager;
	private Sensor accSensor;	  						// sensor ACCELERATION
	private Sensor magnetSensor;  						// sensor MAGNETIC
	double pitch, pitchHeight;	  						// rotation around X axis
	float d; 											// Distance between object with camera
	float tempDistance;									// temporaty variables distance
	float personHeight;									// the height of camera
	float inputH, inputB;
	double pitchDistance;
	boolean pressedGetDistanceButton;
	boolean pressedGetHeightButton;
	boolean maxHeightValue;
	boolean minHeightValue;
	boolean invalidGetDistanceButton;
	boolean invalidGetHeightButton;
	boolean inputed;

	private Preview mPreview; 							// Frame display in screen 
	private Camera mCamera;								// CAMERA
 
	private Button getDistance;							// get distance
	private Button getHeight;							// get Height a Object
	private Button inputHeight;
	TextView textView;			
	TextView heightResult;								// Show height result 		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FrameLayout previewLayout = (FrameLayout) findViewById(R.id.preview);
		mPreview = new Preview(MainActivity.this); 
		previewLayout.addView(mPreview);
		
		Resources res = getResources();
		Drawable background = res.getDrawable(R.drawable.background1);		
		background.setAlpha(80);
		mPreview.setBackgroundDrawable(background);
		
		heightResult = (TextView) findViewById(R.id.height);
		textView = (TextView) findViewById(R.id.textView);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Use below method to get the default sensor for a given type
		accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		inputed = false;
		initButtons();
		PromptDialog();
	}
/*
 * @author: 9-A: Le Hoai Nam
 * initial variable values and set button
 */
	private void initValues() {
		pressedGetDistanceButton = false;
		pressedGetHeightButton = false;
		maxHeightValue = false;
		minHeightValue = false;
		invalidGetDistanceButton = false;
		invalidGetHeightButton = false;
		heightResult.setText("Height result");
	}
	
	private void initButtons() {
		getDistance = (Button) findViewById(R.id.getDistance);
		getHeight = (Button) findViewById(R.id.getHeight);
		inputHeight = (Button) findViewById(R.id.inputHeight);
		getHeight.setVisibility(View.INVISIBLE);
		
		// When Click "getDistance" button
		getDistance.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!invalidGetDistanceButton) {
					tempDistance = d;
					pitchDistance = pitch;
					if (pressedGetDistanceButton) {
						initValues();
						getHeight.setVisibility(View.INVISIBLE);
					}
					else
						pressedGetDistanceButton = true;
				}
			}
		});

		// When Click "getHeight" button
		getHeight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!invalidGetHeightButton) {
						pressedGetHeightButton = true;
					getHeight.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		// When Click "InputHeight" button
		inputHeight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PromptDialog();
				personHeight = inputB + inputH;
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
 * write check function distance and height
 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!inputed) {
			getDistance.setVisibility(View.INVISIBLE);
			return;
		}
		else getDistance.setVisibility(View.VISIBLE);
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
			if (success) {
					/* Orientation has azimuth, pitch and roll */
					SensorManager.getOrientation(R, orientation);
//					azimut = Math.toDegrees(orientation[0]);
					pitch = Math.toDegrees(orientation[1]);
//					roll = Math.toDegrees(orientation[2]);
					
					/*caculate distance  from camera to object*/
					d = Math.abs((float) (personHeight * Math.tan(pitch * Math.PI/ 180)));
					if (!pressedGetDistanceButton) {
						if (gravity[2] > 0) {
							textView.setText("D: " + d + "\n Angle: "+ pitch);
							invalidGetDistanceButton = false;
						}
						else {
							textView.setText("Please aim at the ground");
							invalidGetDistanceButton = true;
						}
					}
					/*caculate object's height*/
					if (pressedGetDistanceButton && !pressedGetHeightButton) {
						getHeight.setVisibility(View.VISIBLE);
						if  (gravity[2] == 0) 
							height = personHeight;
						else
							if (gravity[2] < 0) {
								if (pitch == 0) {
									maxHeightValue = true;
									invalidGetHeightButton = true;
								}
								else {
									maxHeightValue = false;
									invalidGetHeightButton = false;
								}
									pitchHeight = pitch - 90;
									h2 = Math.abs((float) (tempDistance * Math.tan(pitchHeight * Math.PI / 180)));
									height = h2 + personHeight;
								}
								else {
									if (Math.abs(pitch) <= Math.abs(pitchDistance)) {
										minHeightValue = true;
										invalidGetHeightButton = true;
									}
									else {
										minHeightValue = false;
										invalidGetHeightButton = false;
									}
									height = personHeight*(1 - (float)(Math.tan(Math.abs(pitchDistance)* Math.PI / 180)/Math.tan(Math.abs(pitch)* Math.PI / 180)));
								}
						if (!pressedGetHeightButton) {
							if (minHeightValue)
								heightResult.setText("MIN");
							else
								if (maxHeightValue)
									heightResult.setText("MAX");
								else
									heightResult.setText("Height: " + height+ "\n Angle: "+ pitch);
						}
					}
				}

		}
	}
	/*
	 * @author: Nguyen Vinh Phu 9-B
	 * show dialog input height camre and height buiding
	 * */
	private void PromptDialog() {
		// TODO Auto-generated method stub
		Context context = this;
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.input_height, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);
		alertDialogBuilder.setTitle("Input Height");

		final EditText userInputHeight = (EditText) promptsView.findViewById(R.id.inputHeight);
		final EditText userInputBuiding = (EditText) promptsView.findViewById(R.id.inputBuiding);

		userInputHeight.setFilters(new InputFilter[] {
			// Maximum 2 characters.
			new InputFilter.LengthFilter(4),
			// Digits only.
			DigitsKeyListener.getInstance(false, true), // Not strictly
																// needed, IMHO.
		});

		// Digits only & use numeric soft-keyboard.
		userInputBuiding.setKeyListener(DigitsKeyListener.getInstance(false,true));

		userInputBuiding.setFilters(new InputFilter[] {
			// Maximum 2 characters.
			new InputFilter.LengthFilter(4),
			// Digits only.
			DigitsKeyListener.getInstance(false, true), // Not strictly
														// needed, IMHO.
		});

		// Digits only & use numeric soft-keyboard.
		userInputHeight.setKeyListener(DigitsKeyListener.getInstance(false,true));

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// get user input and set it to result
				// edit text
				// result.setText(userInput.getText());
				if (userInputHeight.getText().toString().matches(""))
					inputH = 0;
				else inputH = Float.parseFloat(userInputHeight.getText().toString());
				if (userInputBuiding.getText().toString().matches(""))
					inputB = 0;
				else inputB = Float.parseFloat(userInputBuiding.getText().toString());
				personHeight = inputB + inputH;
				inputed = true;
				if(inputH < 0.5) {
					inputed = false;
					Toast.makeText(getApplicationContext(),"height Camera must langer 0.5m ",Toast.LENGTH_LONG).show();
					PromptDialog();
					return;
				}
				if(inputB > 500) {
					inputed = false;
					Toast.makeText(getApplicationContext(),"range: 0m ~ 500m",Toast.LENGTH_LONG).show();
					PromptDialog();
					return;
				}
				
				initValues();
			}
			}).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
		}
}