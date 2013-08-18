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
package name.wildswift.android.libs.ui.ar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Wild Swift
 */
public class ArView extends AdapterView<ArAdapter> implements SensorEventListener, LocationListener, GestureDetector.OnGestureListener{
	private boolean gpsDetected = false;
	private final Logger log = Logger.getLogger(getClass().getName());
	private GestureDetector gestureDetector;
	private int orientation;
	private double vAngle = Math.PI / 4;
	private double hAngle = Math.PI / 4;
	private double minScale = 0.5;


	public static interface StatusListener {
		public void onPrepared();
	}

	protected static final int AVERAGING_WINDOW = 20;

	protected float[][] magneticAvg = new float[3][AVERAGING_WINDOW];
	protected float[][] accelerometerAvg = new float[3][AVERAGING_WINDOW];
	protected float[] magnetic = new float[3];
	protected float[] accelerometer = new float[3];
	private boolean active = false;
	private final ViewUpdater viewUpdater = new ViewUpdater();
	private Camera camera = new Camera();
	private ArAdapter adapter;
	private GeoPoint currentLocation = null;
	private double radius = 1;
	private List<Integer> visibleViews = new ArrayList<Integer>();
	private final Object visibleViewsMutex = new Object();

	private StatusListener listener;

	private int widthMeasureSpec;
	private int heightMeasureSpec;
	private static final double EARTH_R = 6372.797;

	public ArView(Context context) {
		super(context);
		setFocusable(false);
		setAlwaysDrawnWithCacheEnabled(true);
		gestureDetector = new GestureDetector(context, this);
	}

	public ArView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(false);
		setAlwaysDrawnWithCacheEnabled(true);
		gestureDetector = new GestureDetector(context, this);
	}

	public ArView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFocusable(false);
		setAlwaysDrawnWithCacheEnabled(true);
		gestureDetector = new GestureDetector(context, this);
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

	public void setListener(StatusListener listener) {
		this.listener = listener;
		if (isInitialized() && listener != null) {
			listener.onPrepared();
		}
	}

	private boolean isInitialized() {
		return accelerometer != null && magnetic != null && currentLocation != null;
	}

	private void setup(Context context) {
		log.info("setup");
		this.active = true;
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

		if (sensors.size() == 0) {
			log.severe("no magnetic field sensor, no way to work");
			return;
		}

		sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);

		sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors.size() == 0) {
			log.severe("no accelerometer sensor, no way to work");
			return;
		}

		sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);

		setStaticTransformationsEnabled(true);

		post(viewUpdater);

		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, this);
		Location knownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (knownLocation == null) {
			knownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} else {
			gpsDetected = true;
		}
		if (knownLocation == null) {
			return;
		}
		currentLocation = new GeoPoint(knownLocation.getLatitude(), knownLocation.getLongitude());
		if (adapter != null) {
			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(currentLocation.getLatitude());
			location.setLongitude(currentLocation.getLongitude());
			adapter.updateGpsPosition(location);
		}

		orientation = getContext().getResources().getConfiguration().orientation;
	}

	public void setRadius(double radius) {
		this.radius = radius;
		requestLayout();
	}

	private void unSetup(Context context) {
		log.info("unSetup");
		this.active = false;
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

		if (sensors.size() == 0) {
			log.severe("no magnetic field sensor, no way to work");
			return;
		}

		sensorManager.unregisterListener(this, sensors.get(0));

		sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors.size() == 0) {
			log.severe("no accelerometer sensor, no way to work");
			return;
		}

		sensorManager.unregisterListener(this, sensors.get(0));

		setStaticTransformationsEnabled(false);

		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		locationManager.removeUpdates(this);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = 0;
		this.widthMeasureSpec = widthMeasureSpec;
		this.heightMeasureSpec = heightMeasureSpec;
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY){
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		int height = 0;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY){
			height = MeasureSpec.getSize(heightMeasureSpec);
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		verifyVisibleRect();
	}

	private void verifyVisibleRect() {
		int count = adapter.getCount();
		synchronized (visibleViewsMutex) {
			List<Integer> newVisibleViews = new ArrayList<Integer>();
			for (int i = 0; i < count; i++) {
				GeoPoint point = adapter.getItem(i);
				if (getDistance(point.getLatitude(), point.getLongitude()) < radius) {
					log.config("add view = " + i);
					newVisibleViews.add(i);
				}
			}

			Collections.sort(newVisibleViews,new Comparator<Integer>() {
				@Override
				public int compare(Integer i1, Integer i2) {
					GeoPoint point1 = adapter.getItem(i1);
					GeoPoint point2 = adapter.getItem(i2);
					return (int) (getDistance(point2.getLatitude(), point2.getLongitude()) - getDistance(point1.getLatitude(), point1.getLongitude()));
				}
			});

			detachAllViewsFromParent();

			int oldViewsIterator = 0;
			int newViewsIterator = 0;
			while (oldViewsIterator < visibleViews.size() || newViewsIterator < newVisibleViews.size()) {
				if (oldViewsIterator < visibleViews.size() && newViewsIterator < newVisibleViews.size()) {
					if (visibleViews.get(oldViewsIterator).equals(newVisibleViews.get(newViewsIterator))) {
						layoutView(visibleViews.get(oldViewsIterator), oldViewsIterator);
						oldViewsIterator++;
						newViewsIterator++;
						continue;
					}
					
					removeViewInLayout(getChildAt(newViewsIterator));
					layoutView(newVisibleViews.get(newViewsIterator), newViewsIterator);
					oldViewsIterator++;
					newViewsIterator++;
					continue;

				}
				if (oldViewsIterator < visibleViews.size()) {
					removeViewInLayout(getChildAt(newViewsIterator));
					oldViewsIterator++;
					continue;
				}
				if (newViewsIterator < newVisibleViews.size()) {
					layoutView(newVisibleViews.get(newViewsIterator), newViewsIterator);
					newViewsIterator++;
					continue;
				}
			}
			visibleViews = newVisibleViews;
		}
	}

	private void layoutView(int index, int viewPosition) {
		View child = adapter.getView(index, null, this);
		LayoutParams lp;
		if (child.getLayoutParams() != null){
			lp = child.getLayoutParams();
		} else {
			lp = generateDefaultLayoutParams();
			child.setLayoutParams(lp);
		}

		child.setDrawingCacheEnabled(true);
		child.setWillNotCacheDrawing(false);

		addViewInLayout(child, viewPosition, lp);
		child.measure(ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getWidth(),MeasureSpec.UNSPECIFIED), 0, lp.width), ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getWidth(),MeasureSpec.UNSPECIFIED), 0, lp.height));
		child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
	}

	protected double getDistance(double dstLat, double dstLon) {
		if(currentLocation == null) return Double.POSITIVE_INFINITY;
		double rad = Math.PI / 180.0;

		double lat;
		double lon;
		lat = currentLocation.getLatitude() * rad;
		lon = currentLocation.getLongitude() * rad;
		dstLat *= rad;
		dstLon *= rad;

		double dlat = (dstLat - lat) / 2;
		double dlon = (dstLon - lon) / 2;

		double a = Math.sin(dlat) * Math.sin(dlat) + Math.cos(lat)
				* Math.cos(dstLat) * Math.sin(dlon) * Math.sin(dlon);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double dist = EARTH_R * c;

		dist = (int) (dist * 10);
		dist /= 10.0;
		return dist;
	}


	@Override
	public ArAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void setAdapter(ArAdapter arAdapter) {
		this.adapter = arAdapter;
		if (currentLocation != null){
			Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(currentLocation.getLatitude());
			location.setLongitude(currentLocation.getLongitude());
			adapter.updateGpsPosition(location);
		}
		removeAllViewsInLayout();
		visibleViews.clear();
		verifyVisibleRect();
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setSelection(int i) {
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		super.getChildStaticTransformation(child, t);
		int childIndex = -1;
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) == child) {
				childIndex = i;
			}
		}
		if (childIndex < 0 || visibleViews == null) {
			log.severe("Unable to find view");
			return false;
		}
		android.graphics.Matrix matrix = getChildMatrix(childIndex);
		if (matrix == null) return false;
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		t.getMatrix().set(matrix);
		return true;
	}

	private android.graphics.Matrix getChildMatrix(int childIndex) {
		synchronized (visibleViewsMutex) {
			if (isInitialized()) {
				View child = getChildAt(childIndex);
				GeoPoint point = adapter.getItem(visibleViews.get(childIndex));

				float[] result = new float[16];
				Matrix.setIdentityM(result, 0);
				Matrix.translateM(result, 0, 0, 0, (float) EARTH_R);

				Matrix.rotateM(result, 0, (float) -currentLocation.getLatitude(), 1, 0, 0);
				Matrix.rotateM(result, 0, (float) -currentLocation.getLongitude(), 0, 1, 0);

				Matrix.rotateM(result, 0, (float) point.getLongitude(), 0, 1, 0);
				Matrix.rotateM(result, 0, (float) point.getLatitude(), 1, 0, 0);

				Matrix.translateM(result, 0, 0, 0, - (float) EARTH_R);

				float[] coordinates = new float[4];

				System.arraycopy(result, 12, coordinates, 0, 4);

				float[] rMatrix = new float[9];
				float[] iMatrix = new float[9];
				SensorManager.getRotationMatrix(rMatrix, iMatrix, this.accelerometer, magnetic);
				float[] rAngles = new float[3];
				SensorManager.getOrientation(rMatrix, rAngles);

				Matrix.setIdentityM(result, 0);
				for (int i = 0; i < 3; i++) {
					System.arraycopy(rMatrix, i * 3, result, i * 4, 3);
				}

				float[] coordinatesScreen = new float[4];

				Matrix.multiplyMV(coordinatesScreen, 0, result, 0, coordinates, 0);

				float distance = Matrix.length(coordinatesScreen[0], coordinatesScreen[1], coordinatesScreen[2]);

				if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					float tmp = - coordinatesScreen[0];
					coordinatesScreen[0] = coordinatesScreen[1];
					coordinatesScreen[1] = tmp;
					coordinatesScreen[2] = - coordinatesScreen[2];
				}

				Matrix.frustumM(result, 0, -(float) (Math.tan(hAngle / 2)), (float) (Math.tan(hAngle / 2)), -(float) (Math.tan(vAngle / 2)), (float) (Math.tan(vAngle / 2)), 1, 2);

				float[] coordinatesProjection = new float[4];

				Matrix.multiplyMV(coordinatesProjection, 0, result, 0, coordinatesScreen, 0);

				// add this to avoid exceptions with small z coordinate
				if (Math.abs(coordinatesProjection[3]) > Float.MIN_VALUE * 1E+2) {
					for(int i =0; i < 4; i++){
						coordinatesProjection[i] /= coordinatesProjection[3];
					}
				}


				android.graphics.Matrix matrix = new android.graphics.Matrix();

				// if z > 0 and <= Float.MIN_VALUE * 1E+2 values may be not correct
				if (distance > radius || coordinatesScreen[2] <= Float.MIN_VALUE * 1E+2) {
					matrix.setValues(new float[9]);
				} else {
					camera.save();


					double currentScale =(1 + (distance / radius * (1 / minScale - 1)));
					camera.translate((float) (coordinatesProjection[0] * getWidth() / 2 * currentScale), (float) (coordinatesProjection[1] * getHeight() / 2 * currentScale), (float) (576 * (currentScale - 1)));

					double vRotate = Math.asin(coordinatesScreen[1] / distance) * 180 / Math.PI;
					camera.rotateX((float) vRotate);

					double hRotate = Math.asin(coordinatesScreen[0] / distance) * 180 / Math.PI;
					camera.rotateY((float) hRotate);

					camera.getMatrix(matrix);

					camera.restore();
					matrix.preTranslate(-child.getWidth() / 2, -child.getHeight() / 2);
					matrix.postTranslate(getWidth() / 2, getHeight() / 2);
				}

				return matrix;
			}
		}
		return null;
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
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
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
		if (isInitialized() && listener != null) {
			listener.onPrepared();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onLocationChanged(Location location) {
		if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider()) && gpsDetected) return;
		if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
			gpsDetected = true;
		}
		currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
		verifyVisibleRect();
		if (isInitialized() && listener != null) {
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent motionEvent) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent motionEvent) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) {
		synchronized (visibleViewsMutex) {
			for (int i = visibleViews.size() - 1; i >= 0; i--) {
				Integer viewNum = visibleViews.get(i);
				log.info("onSingleTapUp iterate = " + viewNum);
				android.graphics.Matrix forView = getChildMatrix(i);

				android.graphics.Matrix newMatrix = new android.graphics.Matrix();
				if(!forView.invert(newMatrix)) continue;
				float[] floats = {motionEvent.getX(), motionEvent.getY()};
				newMatrix.mapPoints(floats);
				if (floats[0] > 0 && floats[0] < getChildAt(i).getWidth() && floats[1] > 0 && floats[1] < getChildAt(i).getHeight()) {
					log.info("onSingleTapUp accept = " + viewNum);
					if (getOnItemClickListener() != null) {
						getOnItemClickListener().onItemClick(this, getChildAt(i), viewNum, adapter.getItemId(viewNum));
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent motionEvent) {
	}

	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		return false;
	}

	public void setAngles(double hAngle, double vAngle) {
		this.vAngle = vAngle;
		this.hAngle = hAngle;
	}

	public void setMinScale(double minScale) {
		this.minScale = minScale;
	}
}
