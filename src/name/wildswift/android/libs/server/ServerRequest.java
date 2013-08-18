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

import name.wildswift.android.libs.server.network.Network;
import name.wildswift.android.libs.exceptions.ServerApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Basic class of sever request.
 *
 * @author Wild Swift
 */
public abstract class ServerRequest<T> {
	/**
	 * Constant of representation GET http request method
	 */
	public static int GET = Network.GET;

	/**
	 * Constant of representation POST http request method
	 */
	public static int POST = Network.POST;

	/**
	 * HTTP request method. Must be constant in child instances.
	 */
	private int method;

	/**
	 * Request parameters. Must be constant in child instances.
	 */
	private Map<String,String> parameters = new HashMap<String, String>();
	
	/**
	 * Request url
	 */
	private String url;
	private String contentType = null;

	/**
	 * Basic constructor initialize only URL. Method sets to default default (GET)
	 * @param url request URL
	 */
	protected ServerRequest(String url) {
		this.method = Network.GET;
		this.url = url;
	}

	/**
	 * Constructor with initialize constant fields.
	 * @param url request URL
	 * @param method request method. Use constants {@link  ServerRequest#GET} or {@link  ServerRequest#POST}
	 * @throws IllegalArgumentException if method not
	 */
	protected ServerRequest(String url, int method) {
		if (method == GET || method == POST) {
			this.method = method;
		} else {
			throw new IllegalArgumentException("Unknown constant of method. " + method);
		}
		this.url = url;
	}

	/**
	 * Constructor with initialize constant fields.
	 * @param url request URL
	 * @param method request method. Use constants {@link  ServerRequest#GET} or {@link  ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @throws IllegalArgumentException if method not
	 */
	protected ServerRequest(String url, int method, String contentType) {
		if (method == GET || method == POST) {
			this.method = method;
		} else {
			throw new IllegalArgumentException("Unknown constant of method. " + method);
		}
		this.contentType = contentType;
		this.url = url;
	}

	/**
	 * Gets http method of current request.<br/>
	 * Return one of constant {@link  ServerRequest#GET} or {@link  ServerRequest#POST}<br/>
	 *
	 * @return current url
	 */
	public final int getMethod() {
		return method;
	}

	/**
	 * Gets URL of current request<br/>
	 *
	 * @return current url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Remove all parameters.<br/>
	 */
	public final void clearParameters(){
		parameters.clear();
	}

	/**
	 * Add new parameter in to request. Override if already exists.
	 * Append parameter with empty string is used to send data without key and url encoding
	 *
	 * @param name new parameter name
	 * @param value new parameter value 
	 */
	public final void appendParameter(String name, String value) {
		parameters.put(name, value);
	}

	/**
	 * Get copy of current parameters map.
	 *
	 * @return copy of current parameters map 
	 */
	public final Map<String, String> getParameters() {
		Map<String, String> result = new HashMap<String, String>();
		result.putAll(parameters);
		return result;
	}

	/**
	 * Callback to work with response. Called when received input from server and return result of the request
	 * @param content input from the server side
	 * @return result of processing request (data object or other information)
	 * @throws name.wildswift.android.libs.exceptions.ServerApiException if error in server format of data
	 * @throws IOException if IOErrors occurred
	 */
	public abstract T processRequest(InputStream content) throws ServerApiException, IOException;

	/**
	 * Create representation of the current request. Unique for all different request  
	 * @return representation string
	 */
	@Override
	public String toString() {
		String reqParameters = "";
		Set<String> keys = parameters.keySet();
		for (String key : keys) {
			reqParameters += key + "=" + parameters.get(key) + "&";
		}
		if (method == GET) {
			return "GET: " + url + "?" + reqParameters;
		} else if (method == POST){
			return "POST: " + url + "?" + reqParameters;
		}
		return "";
	}

	public String getContentType() {
		return contentType;
	}

    public String getId() {
        return toString();
    }
}