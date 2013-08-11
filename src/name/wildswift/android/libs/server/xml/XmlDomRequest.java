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
package name.wildswift.android.libs.server.xml;

import name.wildswift.android.libs.exceptions.ServerApiException;
import name.wildswift.android.libs.server.ServerRequest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Universal request to get server data in xml format. Use standard DOM parsing model and handle basic exceptions.
 *
 * @author Wild Swift
 */
public abstract class XmlDomRequest<T> extends ServerRequest<T> {
	/**
	 * Basic constructor initialize only URL. Method sets to default default (GET)
	 *
	 * @param url request URL
	 */
	protected XmlDomRequest(String url) {
		super(url);
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	request URL
	 * @param method request method. Use constants {@link  name.wildswift.android.libs.server.ServerRequest#GET} or {@link  name.wildswift.android.libs.server.ServerRequest#POST}
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlDomRequest(String url, int method) {
		super(url, method);
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  name.wildswift.android.libs.server.ServerRequest#GET} or {@link  name.wildswift.android.libs.server.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlDomRequest(String url, int method, String contentType) {
		super(url, method, contentType);
	}

	/**
	 * Callback to work with response. Called when input from server gets, and return result of request.<br/>
	 * Used in other classes. Protect to override.
	 * If need to override, use {@link  name.wildswift.android.libs.server.ServerRequest} instead
	 *
	 * @param content input from server side
	 * @return result of processing request (data object or other information)
	 * @throws name.wildswift.android.libs.exceptions.ServerApiException
	 *                             if error in server format of data
	 * @throws java.io.IOException if IOErrors occurred
	 */
	@Override
	public final T processRequest(InputStream content) throws ServerApiException, IOException {
		try {
			// create builder
			DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// parse, convert and return result
			return convertDom(domBuilder.parse(content));
		} catch (ParserConfigurationException e) {
			// Box exception
			throw new ServerApiException(e);
		} catch (SAXException e) {
			// Box exception
			throw new ServerApiException(e);
		}
	}

	/**
	 * Method to convert <b>document object model</b>
	 * 
	 * @param document input dom
	 * @return request result
	 * @throws name.wildswift.android.libs.exceptions.ServerApiException if XML dom not valid
	 */
	protected abstract T convertDom(Document document) throws ServerApiException;
}
