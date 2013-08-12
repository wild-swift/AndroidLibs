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

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import name.wildswift.android.libs.exceptions.ServerApiException;
import name.wildswift.android.libs.ApplicationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Wild Swift
 */
public class ImageRequest extends ServerRequest<BitmapDrawable> {
	private final Logger log = Logger.getLogger(getClass().getName());
    private Context context;

    public ImageRequest(String url, Context context) {
		super(url);
        this.context = context;
    }

	@Override
	public BitmapDrawable processRequest(InputStream content) throws ServerApiException, IOException {
		BitmapDrawable bitmapDrawable;
		try {
			bitmapDrawable = new BitmapDrawable(context.getResources(), content);
		} catch (Throwable e) {
			log.severe(ApplicationUtils.getErrorReport(e.getMessage(), e));
            throw new ServerApiException(e);
		}
        if (log.isLoggable(Level.CONFIG)) {
            log.config( "BitmapDrawable create " + getUrl());
            if (bitmapDrawable.getBitmap() != null) {
                log.config( "BitmapDrawable " + bitmapDrawable.getBitmap().getWidth() + "x" + bitmapDrawable.getBitmap().getHeight());
                log.config( "BitmapDrawable " + bitmapDrawable.getBitmap().getConfig());
            }
        }
		return bitmapDrawable;
	}
}
