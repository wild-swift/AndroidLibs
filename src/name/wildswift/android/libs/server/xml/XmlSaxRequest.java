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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Universal request to get server data in xml format. Use standard SAX parsing model and handle basic exceptions.
 * 
 * @author Wild Swift
 */
public abstract class XmlSaxRequest<T> extends ServerRequest<T> {
	/**
	 * Handler to parse server response. Must be constant. Defined in constructor.
	 */
	private RequestHandler<T> handler;

	/**
	 * Basic constructor initialize only URL. Method sets to default default (GET)
	 *
	 * @param url request URL
	 * @param handler Handler to parse response 
	 */
	protected XmlSaxRequest(String url, RequestHandler<T> handler) {
		super(url);
		this.handler = handler;
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	request URL
	 * @param method request method. Use constants {@link  name.wildswift.android.libs.server.ServerRequest#GET} or {@link  name.wildswift.android.libs.server.ServerRequest#POST}
	 * @param handler Handler to parse response
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlSaxRequest(String url, int method, RequestHandler<T> handler) {
		super(url, method);
		this.handler = handler;
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  name.wildswift.android.libs.server.ServerRequest#GET} or {@link  name.wildswift.android.libs.server.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @param handler Handler to parse response
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlSaxRequest(String url, int method, String contentType, RequestHandler<T> handler) {
		super(url, method, contentType);
		this.handler = handler;
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
			// Get parser
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			// Parse
			parser.parse(content, handler);
			// Get result
			return handler.getResult();
		} catch (ParserConfigurationException e) {
			// Box exception
			throw new ServerApiException(e);
		} catch (SAXException e) {
			// Box exception
			throw new ServerApiException(e);
		}
	}
}
