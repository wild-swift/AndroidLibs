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

import name.wildswift.android.libs.exceptions.ServerApiException;
import name.wildswift.android.libs.server.ServerRequest;
import name.wildswift.android.libs.json.parser.JSONParser;
import name.wildswift.android.libs.json.parser.ParseException;
import name.wildswift.android.libs.system.log.ApplicationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Universal request to get server data in JSON format. Use SAX style parsing model and handle basic exceptions.
 *
 * @author Wild Swift
 */
public abstract class JsonSaxRequest<T> extends ServerRequest<T> {
	/**
	 * internal logger
	 */
	private Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Basic constructor initialize only URL. Method sets to default default (GET)
	 *
	 * @param url	 request URL
	 */
	protected JsonSaxRequest(String url) {
		super(url, POST, "application/json; charset=utf-8");
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	 request URL
	 * @param method  request method. Use constants {@link  name.wildswift.android.libs.server.ServerRequest#GET} or {@link  name.wildswift.android.libs.server.ServerRequest#POST}
	 * @throws IllegalArgumentException if method not
	 */
	protected JsonSaxRequest(String url, int method) {
		super(url, method, "application/json; charset=utf-8");
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  name.wildswift.android.libs.server.ServerRequest#GET} or {@link  name.wildswift.android.libs.server.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @throws IllegalArgumentException if method not
	 */
	protected JsonSaxRequest(String url, int method, String contentType) {
		super(url, method, contentType);
	}

	/**
	 * Callback to work with response. Called when input from server gets, and return result of request.<br/>
	 * Parse JSON with defined handler.
	 * Don't override. If need, use {@link  name.wildswift.android.libs.server.ServerRequest} instead
	 *
	 * @param content input from server side
	 * @return result of processing request (data object or other information)
	 * @throws name.wildswift.android.libs.exceptions.ServerApiException
	 *                             if error in server format of data
	 * @throws java.io.IOException if IOErrors occurred
	 */
	@Override
	public final T processRequest(InputStream content) throws ServerApiException, IOException {
        RequestHandler<T> handler = getHandler();
        try {
            new JSONParser().parse(new InputStreamReader(content), handler);
		} catch (ParseException e) {
			log.severe(ApplicationUtils.getErrorReport(e.getMessage(), e));
			throw new ServerApiException(e);
		}
		return handler.getResult();
	}

    protected abstract RequestHandler<T> getHandler();
}