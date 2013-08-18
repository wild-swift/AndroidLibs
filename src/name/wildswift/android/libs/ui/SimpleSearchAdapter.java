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
package name.wildswift.android.libs.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.*;

/**
 * @author Wild Swift
 */
public class SimpleSearchAdapter extends ArrayAdapter<String> implements SectionIndexer {
	protected Map<String, Integer> lettersMap;
	protected String[] letters;
	private List<String> strings;

	public SimpleSearchAdapter(Context context, int layout, int textViewResourceId, List<String> objects) {
		super(context, layout, textViewResourceId, objects);
		this.strings = objects;
		Collections.sort(objects);
		lettersMap = new HashMap<String, Integer>();
		String currentLetter = "";
		for (int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
			String str = objects.get(i);
			if (str.length() == 0) continue;
			String newLetter = str.substring(0,1);
			if (!newLetter.equals(currentLetter)){
				currentLetter = newLetter;
				lettersMap.put(currentLetter, i);
			}
		}
		List<String> lettersList = new ArrayList<String>();
		lettersList.addAll(lettersMap.keySet());
		Collections.sort(lettersList);
		letters = new String[lettersList.size()];
		letters = lettersList.toArray(letters);
	}

	public Object[] getSections() {
		return letters;
	}

	public int getPositionForSection(int i) {
		return lettersMap.get(letters[i]);
	}

	public int getSectionForPosition(int i) {
		return Arrays.binarySearch(letters, strings.get(i).substring(0,1));
	}
}
