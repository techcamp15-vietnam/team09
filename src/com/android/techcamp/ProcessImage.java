package com.android.techcamp;

import java.io.File;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class ProcessImage {
	
	public void Exe(ImageView pic){

		try {
			Paint paint = new Paint();
			paint.setFilterBitmap(true);
			String imgPath = Environment.getExternalStorageDirectory()
					+ File.separator + "tmp.jpg"; // tra ve /sdcard + tenfile
			Bitmap bitmapOrg = BitmapFactory.decodeFile(imgPath); // Lay bitmap
			// Bitmap bitmapOrg =
			// BitmapFactory.decodeResource(getResources(),R.drawable.);
			int x, y, x1, y1;
			x = bitmapOrg.getWidth();
			y = bitmapOrg.getHeight();
			x1 = x / 3;
			y1 = y / 3;

			int targetWidth = 500;
			int targetHeight = 500;
			
			// tra ve 1 mang bitmap tu anh doc duoc
			Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
					targetHeight, Bitmap.Config.ARGB_8888);

			RectF rectf = new RectF(0, 0, 500, 500);

			Canvas canvas = new Canvas(targetBitmap);
			Path path = new Path();

			path.addRect(rectf, Path.Direction.CW);
			canvas.clipPath(path);

			canvas.drawBitmap(bitmapOrg, new Rect(0, 0, bitmapOrg.getWidth(),
					bitmapOrg.getHeight()), new Rect(0, 0,
					bitmapOrg.getWidth(), bitmapOrg.getHeight()), paint);

			Matrix matrix = new Matrix();
			matrix.postScale(2f, 2f);
			Bitmap resizedBitmap = Bitmap.createBitmap(targetBitmap, x1, y1,
					x1, y1, matrix, true);

			/* convert Bitmap to resource */
			BitmapDrawable bd = new BitmapDrawable(resizedBitmap); 
			pic.setImageDrawable(bd);

		} catch (Exception e) {
			System.out.println("Error1 : " + e.getMessage() + e.toString());
		}
		final RotateAnimation rotateAnim = new RotateAnimation(0.0f, 90,
	            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
	            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
	    rotateAnim.setDuration(0);
	    rotateAnim.setFillAfter(true);
	    pic.startAnimation(rotateAnim);		
		
	}
	
}
