package me.tiagovalente.jsonannotation;

import me.tiagovalente.jsonannotation.JSONAnnotationParser.InvalidMemberException;
import me.tiagovalente.jsonannotation.JSONAnnotationParser.JSONParserException;

import org.json.JSONObject;

/**
 * Marks the type as parsable by the annotation parser.
 * No methods are required to implement.
 * 
 * @author Tiago Valente
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JSONParsable
{
	/**
	 * This inner abstract class implements a static method for parsing.
	 * This method should fit most of your needs.
	 * 
	 * @author Tiago Valente
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public static class FromJSON
	{
		/**
		 * Creates a new object from the given JSONObject object
		 * by parsing it
		 * @param jsonObject - The JSON to parse
		 * @param target - The class that describes what parsed object should be
		 * @return the object that results from the parsing 
		 */
		public static <T extends JSONParsable> T create(JSONObject jsonObject, Class<T> target)
		{
			T result = null;
			
			try
			{
				result = JSONAnnotationParser.parse(jsonObject, target);
			}
			catch (JSONParserException e)
			{
				System.err.println(e.getHumanReadableReason());
			}
			catch (InvalidMemberException e)
			{
				System.err.println(e.getHumanReadableReason());
			}
			
			return result;
		}
	}
}
