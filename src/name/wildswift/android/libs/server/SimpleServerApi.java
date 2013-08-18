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
import name.wildswift.android.libs.exceptions.NetworkException;
import name.wildswift.android.libs.server.network.NetworkPool;
import name.wildswift.android.libs.exceptions.ServerApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic class to create Server API. Limits simultaneous connection to the connections available in network pool.
 * This implementation use URLConnection for requests
 *
 * @author Wild Swift
 */
public abstract class SimpleServerApi {

	
	/**
	 * NetworkImpl pool instance.
	 */
	protected NetworkPool networkPool = NetworkPool.getInstance();
	private final Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Constructor with cache initializer
	 *
	 * @param cache cache
	 */
	protected SimpleServerApi(LocalNetworkCache cache) {
		this.cache = cache;
	}

	private LocalNetworkCache cache;

	/**
	 * Method to get server binary data. Use cache if needed.
	 * Basic framework method.
	 *
	 * @param request			request implementation
	 * @param filter			to filter returned data
	 * @param useCache          is need to use rTag or LastModified flags. If server returns 304 status this method returns null
	 * @return result of request
	 * @throws NetworkException	  if error on transport layer
	 * @throws name.wildswift.android.libs.exceptions.ServerApiException    if error in server format
	 * @throws IllegalStateException if cache not initialized
	 */
	@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
	protected final <T> T processRequest(ServerRequest<T> request, InputFilter filter, boolean useCache) throws ServerApiException {
		final Network network = networkPool.getNetwork();
		InputStream inputToProcess = null;
		synchronized (network) {
			try {
                Date cacheDate = null;
                String dataIdentifier = null;
                if (useCache) {
                    // Load data and check for the changes
                    cacheDate = cache.getCacheDate(request.toString());
                    dataIdentifier = cache.getETag(request.toString());

                }
				HttpURLConnection serverConnection = network.openConnection(request.getUrl(), request.getParameters(), request.getMethod(), cacheDate, dataIdentifier, request.getContentType());

                if (serverConnection == null) {
                    return null;
                }

				// Save cache parameters
				Date lastModified = new Date(serverConnection.getLastModified());
				String eTag = serverConnection.getHeaderField("Etag");

                InputStream serverInput = serverConnection.getInputStream();
                inputToProcess = new BufferedInputStream(serverInput, 8192);
                cache.storeData(request.toString(), lastModified, eTag);

                if (log.isLoggable(Level.CONFIG)) {
					ByteArrayOutputStream dataCache = new ByteArrayOutputStream();

					// Fully read data
					int contentLength = serverConnection.getContentLength();
					if (contentLength < 0) {
						byte[] buffer = new byte[8192];
						int i;
						while ((i = inputToProcess.read(buffer)) >= 0) {
							dataCache.write(buffer, 0, i);
						}
					} else {
						byte[] buffer = new byte[8192];
						int i;
						while (contentLength > 0) {
							i = inputToProcess.read(buffer);
							dataCache.write(buffer, 0, i);
							contentLength -= i;
						}
					}

					// Close streams
					serverInput.close();
					dataCache.close();

					//store data
					byte[] data = dataCache.toByteArray();

					log.config("response = " + new String(data));


					inputToProcess = new ByteArrayInputStream(data);
				}

				// process request
				if (filter != null) {
					inputToProcess = new FilteredInputStream(inputToProcess, filter);
				}
				return request.processRequest(inputToProcess);
			} catch (IOException e) {
				// Box exception
				throw new NetworkException(e);
			} finally {
				// executed every time before exit
				if (inputToProcess != null) {
					try {
						inputToProcess.close();
					} catch (IOException ex) {/*pass*/}
				}
				network.close();
			}
		}
	}
}