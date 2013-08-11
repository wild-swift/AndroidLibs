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
package name.wildswift.android.libs.server.json;

import name.wildswift.android.libs.json.parser.ContentHandler;
import name.wildswift.android.libs.json.parser.ParseException;

import java.io.IOException;

/**
 * Base handler to work with {@link  JsonSaxRequest}
 *
 * @author Wild Swift
 */
public class RequestHandler<T> implements ContentHandler {
	/**
	 * Result field
	 */
	private T result;

	/**
	 * Method to get parse result
	 *
	 * @return parse result
	 */
	public final T getResult() {
		return result;
	}

	/**
	 * Don't be public. Write access only protected. Protected to override.
	 *
	 * @param result define parse result.
	 */
	protected final void setResult(T result) {
		this.result = result;
	}

	/**
	 * Receive notification of the beginning of JSON processing.
	 * The parser will invoke this method only once.
	 *
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 */
	@Override
	public void startJSON() throws ParseException, IOException {
	}

	/**
	 * Receive notification of the end of JSON processing.
	 *
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 */
	@Override
	public void endJSON() throws ParseException, IOException {
	}

	/**
	 * Receive notification of the beginning of a JSON object.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 * @see #endJSON
	 */
	@Override
	public boolean startObject() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the end of a JSON object.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 * @see #startObject
	 */
	@Override
	public boolean endObject() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the beginning of a JSON object entry.
	 *
	 * @param key - Key of a JSON object entry.
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 * @see #endObjectEntry
	 */
	@Override
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the end of the value of previous object entry.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 * @see #startObjectEntry
	 */
	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the beginning of a JSON array.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 * @see #endArray
	 */
	@Override
	public boolean startArray() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the end of a JSON array.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 * @see #startArray
	 */
	@Override
	public boolean endArray() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the JSON primitive values:
	 * java.lang.String,
	 * java.lang.Number,
	 * java.lang.Boolean
	 * null
	 *
	 * @param value - Instance of the following:
	 *              java.lang.String,
	 *              java.lang.Number,
	 *              java.lang.Boolean
	 *              null
	 * @return false if the handler wants to stop parsing after return.
	 * @throws name.wildswift.android.libs.json.parser.ParseException
	 *
	 */
	@Override
	public boolean primitive(Object value) throws ParseException, IOException {
		return true;
	}
}
