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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Wild Swift
 */
public class FilteredInputStream extends InputStream {
	protected BufferedReader reader;
	protected InputFilter filter;
	protected InputStream is;
	protected byte[] buffer = new byte[0];
	protected int bufferPosition = 0;

	public FilteredInputStream(InputStream input, InputFilter filter) {
		is = input;
		this.reader = new BufferedReader(new InputStreamReader(is));
		this.filter = filter;
	}

	@Override
	public int read() throws IOException {
		String line;
		while (buffer.length <= bufferPosition && (line = reader.readLine()) != null) {
			buffer = (filter.filter(line) + System.getProperty("line.separator")).getBytes();
			bufferPosition = 0;
		}
		if (buffer.length > bufferPosition) {
			int result = buffer[bufferPosition] > 0 ? buffer[bufferPosition] : 256 + buffer[bufferPosition];
			bufferPosition ++;
			return result;
		}
		return -1;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}
}
