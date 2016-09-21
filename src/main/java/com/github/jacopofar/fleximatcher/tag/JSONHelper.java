package com.github.jacopofar.fleximatcher.tag;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.NoSuchElementException;

public class JSONHelper {
    /**
     * Extracts a field specified by a string from a JSON structure
     *
     * Example: "friend.name" with the JSON {height: 34, friend: {name: "john", weight: 45}}
     * produces "john"
     * */
    public static String extract(JSONObject element, String field) throws NoSuchElementException {
        return _extract(element, field, field);
    }

    private static String _extract(JSONObject element, String field, String originalField) throws NoSuchElementException {
        if (!field.contains(".")) {
            try {
                return element.getString(field);
            } catch (JSONException e) {
                if (field.equals(originalField))
                    throw new NoSuchElementException("cannot find element " + field + " in the JSON");
                else
                    throw new NoSuchElementException("cannot find element " + field + " of " + originalField + " in the JSON");
            }
        }
        String root = field.substring(0, field.indexOf('.'));
        try {
            JSONObject subtree = element.getJSONObject(root);
            return _extract(subtree, field.substring(field.indexOf('.') + 1), originalField);
        } catch (JSONException e) {
            throw new NoSuchElementException("cannot find element " + root + " of " + originalField + " in the JSON");
        }
    }
}
