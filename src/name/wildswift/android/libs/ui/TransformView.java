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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

/**
 * @author Wild Swift
 */
public class TransformView extends FrameLayout{
	protected Matrix transform = new Matrix();

	public TransformView(Context context) {
		super(context);
	}

	public TransformView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TransformView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Matrix getTransform() {
		return transform;
	}

	public void setTransform(Matrix transform) {
		this.transform = transform;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		float[] outPoints = new float[8];

		Matrix invertedTransform = new Matrix();
		transform.invert(invertedTransform);
		invertedTransform.mapPoints(outPoints, new float[]{0,0,0,MeasureSpec.getSize(heightMeasureSpec),MeasureSpec.getSize(widthMeasureSpec),0,MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec)});

		float minX = Integer.MAX_VALUE;
		float maxX = Integer.MIN_VALUE;

		float minY = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;

		for (int i = 0; i < 4; i++) {
			if (outPoints[i * 2] < minX) minX = outPoints[i * 2];
			if (outPoints[i * 2] > maxX) maxX = outPoints[i * 2];

			if (outPoints[i * 2 + 1] < minY) minY = outPoints[i * 2 + 1];
			if (outPoints[i * 2 + 1] > maxY) maxY = outPoints[i * 2 + 1];
		}

		super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getMode(widthMeasureSpec), Math.round(maxX - minX)), MeasureSpec.makeMeasureSpec(MeasureSpec.getMode(heightMeasureSpec), Math.round(maxY - minY)));

		invertedTransform.mapPoints(outPoints, new float[]{0, 0, 0, getMeasuredHeight(), getMeasuredWidth(), 0, getMeasuredWidth(), getMeasuredHeight()});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int count = getChildCount();

		Rect r = new Rect();
		getForeground().getPadding(r);
		final int parentLeft = getPaddingLeft() + r.left;
		final int parentRight = right - left - getPaddingRight() - r.right;

		final int parentTop = getPaddingTop() + r.top;
		final int parentBottom = bottom - top - getPaddingBottom() - r.bottom;

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				final int width = child.getMeasuredWidth();
				final int height = child.getMeasuredHeight();

				int childLeft = parentLeft;
				int childTop = parentTop;

				final int gravity = lp.gravity;

				if (gravity != -1) {
					final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
					final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

					switch (horizontalGravity) {
						case Gravity.LEFT:
							childLeft = parentLeft + lp.leftMargin;
							break;
						case Gravity.CENTER_HORIZONTAL:
							childLeft = parentLeft + (parentRight - parentLeft + lp.leftMargin +
									lp.rightMargin - width) / 2;
							break;
						case Gravity.RIGHT:
							childLeft = parentRight - width - lp.rightMargin;
							break;
						default:
							childLeft = parentLeft + lp.leftMargin;
					}

					switch (verticalGravity) {
						case Gravity.TOP:
							childTop = parentTop + lp.topMargin;
							break;
						case Gravity.CENTER_VERTICAL:
							childTop = parentTop + (parentBottom - parentTop + lp.topMargin +
									lp.bottomMargin - height) / 2;
							break;
						case Gravity.BOTTOM:
							childTop = parentBottom - height - lp.bottomMargin;
							break;
						default:
							childTop = parentTop + lp.topMargin;
					}
				}

				child.layout(childLeft, childTop, childLeft + width, childTop + height);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void draw(Canvas canvas) {
		canvas.setMatrix(transform);
		super.draw(canvas);
	}
}
