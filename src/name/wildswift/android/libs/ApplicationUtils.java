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
package name.wildswift.android.libs;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import name.wildswift.android.libs.system.log.LogsUncaughtExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Thread.*;

/**
 * Wild Swift
 * Android Libraries
 *
 * @author Wild Swift
 */
public class ApplicationUtils {

	public static final String ERROR_REPORT = "%s \n %s";

	public static void setupLogging(Context context, String applicationName, int logFileId){
		try {
			ClassLoader loader = currentThread().getContextClassLoader();
			currentThread().setContextClassLoader(ApplicationUtils.class.getClassLoader());
			InputStream inputStream = context.getResources().openRawResource(logFileId);
			LogManager.getLogManager().readConfiguration(inputStream);
			inputStream.close();

			// initialize all headers
			inputStream = context.getResources().openRawResource(logFileId);
			Properties properties = new Properties();
			properties.load(inputStream);
			inputStream.close();
			Set<Object> keys = properties.keySet();
			for (Object keyO : keys) {
				String key = (String) keyO;
				if (key!= null && key.endsWith(".handlers")) {
					Logger.getLogger(key.substring(0, key.length() - ".handlers".length())).getHandlers();
				}
			}

			//Log application info
			String version = null;
			String packageName = null;
			try {
				PackageInfo packageInfo = context.getPackageManager().getPackageInfo(new ComponentName(context, context.getClass()).getPackageName(), 0);
				version = packageInfo.versionName;
				packageName = packageInfo.packageName;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
            if (Logger.getLogger("AndroidAppDeveloper").isLoggable(Level.INFO)) {
                Logger.getLogger("AndroidAppDeveloper").info("Starting Application " + applicationName + " v. " + version + " package: " + packageName);
            }

			currentThread().setContextClassLoader(loader);
		} catch (Exception e) {
			Log.e("ApplicationUtils", e.getMessage(), e);
		}

//        currentThread().setUncaughtExceptionHandler(new LogsUncaughtExceptionHandler());
	}

	public static String getErrorReport(String message, Throwable e) {
		if (message != null && message.length() > 0) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(byteArrayOutputStream));
			return String.format(ERROR_REPORT, message, byteArrayOutputStream.toString());
		} else {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(byteArrayOutputStream));
			return byteArrayOutputStream.toString();
		}


	}

}
