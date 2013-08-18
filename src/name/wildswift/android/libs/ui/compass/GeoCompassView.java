/*
 * Copyright (c) 2013.
 * This file is part of Wild Swift Solutions For Android library.
 *
 * Wild Swift Solutions For Android is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Wild Swift Solutions For Android is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Android Interface Toolkit.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.wildswift.android.libs.ui.compass;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Wild Swift
 */
public class GeoCompassView extends View implements SensorEventListener, LocationListener {

	private Logger log = Logger.getLogger(getClass().getName());

	public static interface StatusListener {
		public void onPrepared();
	}

	protected static final int AVERAGING_WINDOW = 20;

	private final ViewUpdater viewUpdater = new ViewUpdater();
	protected float[][] magneticAvg = new float[3][AVERAGING_WINDOW];
	protected float[][] accelerometerAvg = new float[3][AVERAGING_WINDOW];
	protected float[] magnetic = new float[3];
	protected float[] accelerometer = new float[3];

	private Camera camera = new Camera();
	private static final double EARTH_R = 6372.797;
	private GeoPoint currentLocation;
	private GeoPoint targetLocation;
	private boolean active = false;
	private Drawable pointImage;
	private Drawable compassImage;
	private StatusListener listener;

	public GeoCompassView(Context context) {
		super(context);
		setFocusable(false);
	}

	public GeoCompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(false);
	}

	public GeoCompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFocusable(false);
	}

	public void setTargetWayImage(Drawable image) {
		this.pointImage = image;
	}

	public void setCompassImage(Drawable image) {
		this.compassImage = image;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (pointImage != null) {
			Rect bounds = null;
			if (pointImage instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) pointImage;
				if (bitmapDrawable.getBitmap() != null) {
					bounds = new Rect(0, 0, bitmapDrawable.getBitmap().getWidth(), bitmapDrawable.getBitmap().getHeight());
				} else {
					bounds = bitmapDrawable.copyBounds();
				}
			} else {
				bounds = compassImage.copyBounds();
			}

			if (compassImage instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) compassImage;
				if (bitmapDrawable.getBitmap() != null) {
					bounds = maxBounds(bounds, new Rect(0, 0, bitmapDrawable.getBitmap().getWidth(), bitmapDrawable.getBitmap().getHeight()));
				} else {
					bounds = maxBounds(bounds, bitmapDrawable.copyBounds());
				}
			} else {
				bounds = maxBounds(bounds, compassImage.copyBounds());
			}

			int finalWidth = 0;
			int finalHeight = 0;
			switch (MeasureSpec.getMode(widthMeasureSpec)){
				case MeasureSpec.UNSPECIFIED:
					finalWidth = bounds.width();
					break;
				case MeasureSpec.AT_MOST:
					finalWidth = Math.min(bounds.width(), MeasureSpec.getSize(widthMeasureSpec));
					break;
				case MeasureSpec.EXACTLY:
					finalWidth = MeasureSpec.getSize(widthMeasureSpec);
					break;
			}
			switch (MeasureSpec.getMode(heightMeasureSpec)){
				case MeasureSpec.UNSPECIFIED:
					finalHeight = bounds.height();
					break;
				case MeasureSpec.AT_MOST:
					finalHeight = Math.min(bounds.height(), MeasureSpec.getSize(heightMeasureSpec));
					break;
				case MeasureSpec.EXACTLY:
					finalHeight = MeasureSpec.getSize(heightMeasureSpec);
					break;
			}
			setMeasuredDimension(finalWidth, finalHeight);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	private Rect maxBounds(Rect bounds1, Rect bounds2) {
		if (bounds1 == null && bounds2 == null) return null;
		if (bounds1 == null) return new Rect(bounds2);
		if (bounds2 == null) return new Rect(bounds1);
		return new Rect(0, 0, Math.max(bounds1.width(), bounds2.width()), Math.max(bounds1.height(), bounds2.height()));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		pointImage.setBounds(new Rect(0, 0, right - left, bottom - top));
		compassImage.setBounds(new Rect(0, 0, right - left, bottom - top));
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (compassImage != null) {
			canvas.save();
			canvas.translate(getWidth() / 2, getHeight() / 2);
			canvas.rotate((float) (getCompassAngle() * 180 / Math.PI));
			canvas.translate(-getWidth() / 2, -getHeight() / 2);
			compassImage.draw(canvas);
			canvas.restore();
		}
		if (pointImage != null) {
			canvas.save();
			canvas.translate(getWidth() / 2, getHeight() / 2);
			canvas.rotate((float) (getTargetAngle() * 180 / Math.PI));
			canvas.rotate((float) (getCompassAngle() * 180 / Math.PI));
			canvas.translate(-getWidth() / 2, -getHeight() / 2);
			pointImage.draw(canvas);
			canvas.restore();
		}
	}

	public void setTargetLocation(GeoPoint targetLocation) {
		this.targetLocation = targetLocation;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		unSetup(getContext());
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setup(getContext());
	}

	private void setup(Context context) {
		Log.i(getClass().getSimpleName(), "setup");
		this.active = true;
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

		if (sensors.size() == 0) {
			Log.e(getClass().getSimpleName(), "no magnetic field sensor, no way to work");
			return;
		}

		sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);

		sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors.size() == 0) {
			Log.e(getClass().getSimpleName(), "no accelerometer sensor, no way to work");
			return;
		}

		sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);

		post(viewUpdater);

		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
		Location knownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (knownLocation == null) {
			knownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (knownLocation == null) {
			return;
		}
		currentLocation = new GeoPoint(knownLocation.getLatitude(), knownLocation.getLongitude());
	}

	private void unSetup(Context context) {
		Log.i(getClass().getSimpleName(), "unSetup");
		this.active = false;
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

		if (sensors.size() == 0) {
			Log.e(getClass().getSimpleName(), "no magnetic field sensor, no way to work");
			return;
		}

		sensorManager.unregisterListener(this, sensors.get(0));

		sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors.size() == 0) {
			Log.e(getClass().getSimpleName(), "no accelerometer sensor, no way to work");
			return;
		}

		sensorManager.unregisterListener(this, sensors.get(0));

		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		locationManager.removeUpdates(this);
	}


	public double getTargetAngle() {
		if (currentLocation == null) {
			return 0;
		}

		log.config(String.format("current location = [%f,%f]", currentLocation.getLatitude(), currentLocation.getLongitude()));
		log.config(String.format("target location = [%f,%f]", targetLocation.getLatitude(), targetLocation.getLongitude()));

		camera.save();
		camera.translate(0, 0, (float) (EARTH_R * 1000));

		camera.rotateX((float) currentLocation.getLatitude());
		camera.rotateY((float) currentLocation.getLongitude());

		camera.rotateY((float) -targetLocation.getLongitude());
		camera.rotateX((float) -targetLocation.getLatitude());

		camera.translate(0, 0, (float) -(EARTH_R * 1000));

		Matrix matrix = new Matrix();
		camera.getMatrix(matrix);
		camera.restore();
		float[] matrixValues = new float[9];
		matrix.getValues(matrixValues);

		double baseAngle;
		if (matrixValues[5] != 0) {
			baseAngle = - Math.atan(matrixValues[2] / matrixValues[5]);
		} else {
			if (matrixValues[2] > 0) {
				baseAngle = Math.PI / 2;
			} else if (matrixValues[2] < 0) {
				baseAngle = - Math.PI / 2;
			} else {
				baseAngle = 0;
			}
		}

		if (matrixValues[5] <= 0) {
			baseAngle = Math.PI + baseAngle;
		}

		return baseAngle;

	}

	public double getCompassAngle() {
		if (accelerometer == null || magnetic == null) {
			return 0;
		}

		float[] iMatrix = new float[9];
		float[] rMatrix = new float[9];
		SensorManager.getRotationMatrix(rMatrix, iMatrix, accelerometer, magnetic);
		float[] aprAngles = new float[3];
		SensorManager.getOrientation(rMatrix, aprAngles);

		return - aprAngles[0];

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			double[] avg = new double[3];
			for (int i = 0; i < magneticAvg.length; i++) {
				float[] floats = magneticAvg[i];
				for (int j = 1; j < floats.length; j++) {
					float value = floats[j];
					avg[i] += value;
					floats[j - 1] = value;
				}
			}

			for (int i = 0; i < magneticAvg.length; i++) {
				magneticAvg[i][AVERAGING_WINDOW - 1] = sensorEvent.values[i];
				avg[i] += sensorEvent.values[i];
				magnetic[i] = (float) (avg[i]/ AVERAGING_WINDOW);
			}
		}
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			double[] avg = new double[3];
			for (int i = 0; i < accelerometerAvg.length; i++) {
				float[] floats = accelerometerAvg[i];
				for (int j = 1; j < floats.length; j++) {
					float value = floats[j];
					avg[i] += value;
					floats[j - 1] = value;
				}
			}

			for (int i = 0; i < accelerometerAvg.length; i++) {
				accelerometerAvg[i][AVERAGING_WINDOW - 1] = sensorEvent.values[i];
				avg[i] += sensorEvent.values[i];
				accelerometer[i] = (float) (avg[i]/ AVERAGING_WINDOW);
			}
		}
		if (accelerometer != null && magnetic != null && currentLocation != null && listener != null) {
			listener.onPrepared();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
		if (accelerometer != null && magnetic != null && listener != null) {
			listener.onPrepared();
		}
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
	}

	@Override
	public void onProviderEnabled(String s) {
	}

	@Override
	public void onProviderDisabled(String s) {
	}

	protected class ViewUpdater implements Runnable {

		@Override
		public void run() {
			if (active) {
				invalidate();
				post(this);
			}
		}
	}

	public void setListener(StatusListener listener) {
		this.listener = listener;
	}
}
