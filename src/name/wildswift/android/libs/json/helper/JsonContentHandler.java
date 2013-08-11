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
package name.wildswift.android.libs.json.helper;

import name.wildswift.android.libs.json.parser.ContentHandler;
import name.wildswift.android.libs.json.parser.ParseException;

import java.io.IOException;
import java.util.Stack;

/**
 *
 * @author wild Swift
 */
public class JsonContentHandler implements ContentHandler {
    protected Stack<JsonNode> currentNodes;
    protected String currentKey;

    public JsonContentHandler(JsonRootNode jsonRootNode) {
        currentNodes = new Stack<JsonNode>();
        currentNodes.add(jsonRootNode);
    }

    public void startJSON() throws ParseException, IOException {
    }

    public void endJSON() throws ParseException, IOException {
    }

    public boolean startObject() throws ParseException, IOException {
        if (currentKey != null) {
            currentNodes.push(currentNodes.peek().getNode(currentKey));
        }
        currentNodes.peek().getJsonObjectListener().onStart();
        return true;
    }

    public boolean endObject() throws ParseException, IOException {
        currentNodes.peek().getJsonObjectListener().onEnd();
        if (currentNodes.size() > 1) {
            currentNodes.pop();
        }
        return true;
    }

    public boolean startObjectEntry(String s) throws ParseException, IOException {
        currentKey = s;
        return true;
    }

    public boolean endObjectEntry() throws ParseException, IOException {
        currentKey = null;
        return true;
    }

    public boolean startArray() throws ParseException, IOException {
        return true;
    }

    public boolean endArray() throws ParseException, IOException {
        return true;
    }

    public boolean primitive(Object value) throws ParseException, IOException {
        currentNodes.peek().getJsonObjectListener().onField(currentKey, value);
        return true;
    }
}
