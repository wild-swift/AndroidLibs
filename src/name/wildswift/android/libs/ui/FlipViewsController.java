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
package name.wildswift.android.libs.ui;

import android.app.Activity;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;

/**
 * @author Wild Swift
 */
public class FlipViewsController implements Animation.AnimationListener {

	protected Activity callback;
	protected View fromView;
	protected View toView;
	protected ViewGroup container;
	protected int duration;
	protected Mode mode;
	protected final Object startedAnimationsMutex = new Object();
	protected int startedAnimations = 2;
	protected int stoppedAnimations = 2;
	protected Listener animationListener;

	public FlipViewsController(Activity callback, ViewGroup container, View fromView, View toView, Mode mode, int duration) {
		this.callback = callback;
		this.container = container;
		this.duration = duration;
		this.fromView = fromView;
		this.mode = mode;
		this.toView = toView;
	}

	public void setAnimationListener(Listener animationListener) {
		this.animationListener = animationListener;
	}


	public void start(){
		if (startedAnimations != stoppedAnimations || startedAnimations != 2){
			return;
		}
		startedAnimations = 0;
		stoppedAnimations = 0;
		FlipAnimation outAnimation = null;
		FlipAnimation inAnimation = null;

		switch (mode) {
			case bottomTop:
				outAnimation = new FlipAnimation(0, -90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, true);
				inAnimation = new FlipAnimation(90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, true);
				break;
			case leftRight:
				outAnimation = new FlipAnimation(0, 90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, false);
				inAnimation = new FlipAnimation(-90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, false);
				break;
			case rightLeft:
				outAnimation = new FlipAnimation(0, -90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, false);
				inAnimation = new FlipAnimation(90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, false);
				break;
			case topBottom:
				outAnimation = new FlipAnimation(0, 90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, true);
				inAnimation = new FlipAnimation(-90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, true);
				break;
		}

		outAnimation.setDuration(duration / 2);
		outAnimation.setInterpolator(new AccelerateInterpolator());
		outAnimation.setFillAfter(true);
		outAnimation.setAnimationListener(this);
		fromView.setAnimation(outAnimation);

		inAnimation.setDuration(duration / 2);
		inAnimation.setInterpolator(new DecelerateInterpolator());
		inAnimation.setFillAfter(true);
		inAnimation.setAnimationListener(this);
		inAnimation.setStartOffset(duration / 2);

		AnimationSet inAnimationSet = new AnimationSet(false);
		inAnimationSet.addAnimation(inAnimation);

		fromView.setAnimation(outAnimation);
		toView.setAnimation(inAnimationSet);

		callback.runOnUiThread(new Runnable() {
			public void run() {
				int index = container.indexOfChild(fromView);
				if (index > 0) {
					container.removeViewAt(index);
				}
				container.addView(toView, index);
			}
		});
	}


	public void onAnimationStart(Animation animation) {
		synchronized (startedAnimationsMutex){
			startedAnimations ++;
		}
	}

	public void onAnimationEnd(Animation animation) {
		boolean isEnd;
		synchronized (startedAnimationsMutex){
			stoppedAnimations ++;
			isEnd = startedAnimations == stoppedAnimations && startedAnimations == 2;
		}
		if (isEnd && animationListener != null) {
			animationListener.flipEnd();
		}
	}

	public void onAnimationRepeat(Animation animation) {
	}


	class FlipAnimation extends Animation {
		private final double fromDegrees;
		private final double toDegrees;
		private final double centerX;
		private final double centerY;
		private final double depthZ;
		private final boolean reverse;
		private boolean horizontal;
		private Camera camera;

		public FlipAnimation(float fromDegrees, float toDegrees,
								 float centerX, float centerY, float depthZ, boolean reverse, boolean horizontal) {
			this.fromDegrees = fromDegrees;
			this.toDegrees = toDegrees;
			this.centerX = centerX;
			this.centerY = centerY;
			this.depthZ = depthZ;
			this.reverse = reverse;
			this.horizontal = horizontal;
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			camera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			double degrees = fromDegrees + ((toDegrees - fromDegrees) * interpolatedTime);

			final Matrix matrix = t.getMatrix();

			camera.save();
			if (reverse) {
				camera.translate(0.0f, 0.0f, (float) (depthZ * interpolatedTime));
			} else {
				camera.translate(0.0f, 0.0f, (float) (depthZ * (1.0d - interpolatedTime)));
			}
			if (horizontal) {
				camera.rotateX((float) degrees);
			} else {
				camera.rotateY((float) degrees);
			}
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate((float)-centerX, (float)-centerY);
			matrix.postTranslate((float)centerX, (float)centerY);
		}
	}

	public static enum Mode {
		leftRight, rightLeft, topBottom, bottomTop
	}

	public static interface Listener {
		public void flipEnd();
	}

}
