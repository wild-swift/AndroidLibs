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

/**
 * @author Wild Swift
 */
public class GeoPoint {
	private double latitude;
	private double longitude;

	public GeoPoint() {
	}

	public GeoPoint(double latitude, double longitude) {
		if (latitude > 90 || latitude < -90) throw new IllegalArgumentException("Invalid Latitude");
		if (longitude <= -180 || longitude > 180) throw new IllegalArgumentException("Invalid Longitude");
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		if (latitude > 90 || latitude < -90) throw new IllegalArgumentException("Invalid Latitude");
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		if (longitude <= -180 || longitude > 180) throw new IllegalArgumentException("Invalid Longitude");
		this.longitude = longitude;
	}
}
