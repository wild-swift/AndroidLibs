package name.wildswift.android.libs.server.network;

import name.wildswift.android.libs.exceptions.NetworkException;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Map;

/**
 * Interface to transport layer of application API
 * @author Wild Swift
 */
public interface Network {

	/**
	 * Constant represented POST http request
	 */
	public final int GET = 0;

	/**
	 * Constant represented POST http request
	 */
	public final int POST = 1;

	/**
	 * Open InputStream to server content specified by url, request parameters,and request method. If localCacheDate specified verify if content changed. 
	 *
	 * @param url request url
	 * @param parameters request parameters map
	 * @param method specified request method. Accept one of constant {@link  Network#GET} or {@link  Network#POST}
	 * @param localCacheDate date of cache previous content. If null no cache supported.
	 * @param eTag data id
	 * @param contentType request content type
	 * @return null if server return NOT_MODIFIED status, and HTTPConnection other way
	 * @throws name.wildswift.android.libs.exceptions.NetworkException if server return status different from OK and NOT_MODIFIED or IO error occurred
	 */
	public InputStream getInputForRequest(String url, Map<String,String> parameters, int method, Date localCacheDate, String eTag, String contentType) throws NetworkException;

	/**
	 * Open connection to server specified by url, request parameters,and request method. If localCacheDate specified verify if content changed.
	 *
	 * @param url request url
	 * @param parameters request parameters map
	 * @param method specified request method. Accept one of constant {@link  Network#GET} or {@link  Network#POST}
	 * @param localCacheDate date of cache previous content. If null no cache supported.
	 * @param eTag data id
	 * @param contentType request content type
	 * @return null if server return NOT_MODIFIED status, and HTTPConnection other way
	 * @throws NetworkException if server return status different from OK and NOT_MODIFIED or IO error occurred
	 */
	public HttpURLConnection openConnection(String url, Map<String,String> parameters, int method, Date localCacheDate, String eTag, String contentType) throws NetworkException;

	/**
	 * Close previous connection and streams
	 */
	public void close();

}
