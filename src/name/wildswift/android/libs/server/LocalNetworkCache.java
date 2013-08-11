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
package name.wildswift.android.libs.server;

import java.util.Date;

/**
 * Basic interface for http transport cache
 * @author Wild Swift
 */
public interface LocalNetworkCache {
	/**
	 * Stores data in cache 
	 * @param id request id
	 * @param storeDate last update date
	 * @param eTag content verifier
	 */
	public void storeData(String id, Date storeDate, String eTag);

	/**
	 * Get last update date for the data
	 * @param id request id
	 * @return last updated date
	 */
	public Date getCacheDate(String id);

	/**
	 * Get data identifier 
	 * @param id request id
	 * @return Data identifier 
	 */
	public String getETag(String id);
}
