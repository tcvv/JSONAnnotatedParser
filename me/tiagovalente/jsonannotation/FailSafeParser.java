package me.tiagovalente.jsonannotation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A lower level, opinionated JSON parser.
 * In order to avoid exceptions when parsing, it uses nullable, 
 * object versions of primitive types.
 *  
 * @author Tiago Valente
 * @version 1.0.0
 * @since 1.0.0
 */
public class FailSafeParser
{
    private static final String NULL_STR = "null";

    /**
     * Attempts to get a Boolean value for the given key String
     * @return
     *  the Boolean value if key exists and it's boolean;
     *  null otherwise
     */
    public static Boolean getBool(JSONObject obj, String str)
    {
        try
        {
            return obj.getBoolean(str);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * Attempts to get a String value for the given Key String
     * @return
     *  the String value if key exists and it's non-null string;
     *  null otherwise
     */
    public static String getString(JSONObject obj, String str)
    {
        try
        {
            String res = obj.getString(str);

            if(res.equalsIgnoreCase(NULL_STR))
                return null;
            else
                return res;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Attempts to get an integer value for the given Key String
     * @return
     *  the int value as an Integer if key exists and it's an int;
     *  null otherwise
     */
    public static Integer getInt(JSONObject obj, String str)
    {
        try
        {
            return obj.getInt(str);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Attempts to get a double value for the given Key String
     * @return
     *  the double value as a Double if key exists and its a double;
     *  null otherwise
     */
    public static Double getDouble(JSONObject obj, String str)
    {
        try
        {
            return obj.getDouble(str);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Attempts to get a long value for the given Key String
     * @return
     *  the long value as a Long if key exists and it's a long;
     *  null otherwise
     */
    public static Long getLong(JSONObject obj, String str)
    {
        try
        {
            return obj.getLong(str);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Attempts to get a date value for the given Key String
     * @return
     *  the date value if key exists and it's a date;
     *  null if the key value is null, the date is invalid (or others)
     */
    public static Date getDate(JSONObject obj, String key)
    {
        try
        {
            String dateStr = getString(obj, key);
            
            if(dateStr == null || dateStr.length() == 0) return null;
            else return javax.xml.bind.DatatypeConverter.parseDateTime(dateStr).getTime();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * @return 
     *  a JSONArray for the given key; 
     *  an empty JSONArray if none is obtainable
     */
    public static JSONArray getJSONArray(JSONObject obj, String key)
    {
        try
        {
            return obj.getJSONArray(key);
        }
        catch (JSONException e)
        {
            return new JSONArray();
        }
    }

    /**
     * @return
     *  the JSONObject for the given key;
     *  null if the key has no associated object
     */
    public static JSONObject getJSONObject(JSONObject obj, String key)
    {
        try
        {
            return obj.getJSONObject(key);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * @return
     *  the JSONObject in the given position;
     *  null if either the position is invalid or no object is present
     */
    public static JSONObject getJSONObject(JSONArray arr, int position)
    {
        try
        {
            return arr.getJSONObject(position);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * @return
     * 	the integer in the given position;
     *  null if either the position is invalid or no integer is present
     */
    public static Integer getInt(JSONArray arr, int position)
    {
        try
        {
            return arr.getInt(position);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * @return
     * 	the long in the given position;
     *  null if either the position is invalid or no long is present
     */
    public static Long getLong(JSONArray arr, int position)
    {
        try
        {
            return arr.getLong(position);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * @return
     * 	the boolean in the given position;
     *  null if either the position is invalid or no boolean is present
     */
    public static Boolean getBool(JSONArray arr, int position)
    {
        try
        {
            return arr.getBoolean(position);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * @return
     * 	the String in the given position;
     *  null if either the position is invalid or no String is present
     */
    public static String getString(JSONArray arr, int position)
    {
        try
        {
        	String result = arr.getString(position); 
            return result.equals(NULL_STR) ? null : result;
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * @return
     * 	the double in the given position;
     *  null if either the position is invalid or no double is present
     */
    public static Double getDouble(JSONArray arr, int position)
    {
        try
        {
            return arr.getDouble(position);
        }
        catch (JSONException e)
        {
            return 0.0;
        }
    }

    /**
     * @return
     * 	the Date in the given position;
     *  null if either the position is invalid or no date is present/unparsable
     */
    public static Date getDate(JSONArray arr, int position)
    {
        try
        {
            String dateStr = arr.getString(position);

            if(dateStr == null || dateStr.length() == 0)
                return null;
            else
                return javax.xml.bind.DatatypeConverter.parseDateTime(dateStr).getTime();
        }
        catch (JSONException e)
        {
            return null;
        }
    }
}
