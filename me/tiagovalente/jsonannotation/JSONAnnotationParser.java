package me.tiagovalente.jsonannotation;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The actual parser class, containing static methods
 * for parsing an annotated class. Please note that 
 * this parser internally uses the {@link FailSafeParser} 
 *  
 * @author Tiago Valente
 * @see {@link FailSafeParser}
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class JSONAnnotationParser
{	
    public static <T extends JSONParsable> T parse(JSONObject obj, Class<T> objType)
            throws JSONParserException, InvalidMemberException
    {
        T result;
        boolean acessible;

        try
        {
            result = objType.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new JSONParserException(objType, JSONParserException.Reason.NON_INSTANCEABLE);
        }
        catch (IllegalAccessException e)
        {
            throw new JSONParserException(objType, JSONParserException.Reason.NON_ACCESSIBLE);
        }

        // Fields
        for( Field f : objType.getDeclaredFields() )
        {
            JSON ann = f.getAnnotation(JSON.class);

            // -- > if this field isn't annotated, move on
            if(ann == null) continue;
            
            // -- > infer or get the key
            String key = ann.key().equals("") ? f.getName() : ann.key() ;
            
            // -- > infer the type
            TypeNode type = TypeNode.build(f);
                        
            // --> do the hard work
            Object o = processKeyForJSONObj(obj, type, key);

            acessible = f.isAccessible();
            
            try
            {
            	// -- > allow me, it's just this one time
            	f.setAccessible(true);
                f.set(result, o);
            }
            catch (IllegalAccessException e)
            {
                throw new InvalidMemberException(objType, f);
            }
            finally
            {
                // -- > there, back to normal!
                f.setAccessible(acessible);
            }
        }

        // Transformations
//        for(Method m : objType.getDeclaredMethods())
//        {
//            JSON.TransformationMethod tm = m.getAnnotation(JSON.TransformationMethod.class);
//
//            JSON.ValueCollection collection_ann = m.getAnnotation(JSON.ValueCollection.class);
//            JSON.Value ann = m.getAnnotation(JSON.Value.class);
//            JSON.ParseAs parse_as = m.getAnnotation(JSON.ParseAs.class);
//
//            // -- > if this field isn't annotated, move on
//            if(tm == null) continue;
//
//            // -- > if this field hasn't more annotations, throw exception
//            if(ann == null && collection_ann == null)
//                throw new MissingAnnotationException(JSON.Value.class, objType, null, m);
//
//            // -- > if this field is annotated twice, throw Exception
//            if(ann != null && collection_ann != null)
//                throw new DuplicatedAnnotationException(objType, null, m);
//
//            Object o = process(obj, objType, ann, collection_ann, parse_as);
//
//            try
//            {
//            	acessible = m.isAccessible();
//            	// -- > allow me, it's just this one time
//            	m.setAccessible(true);
//                m.invoke(result, o);
//                m.setAccessible(acessible);
//            }
//            catch (IllegalAccessException e)
//            {
//                throw new UnparsableTypeException(objType, UnparsableTypeException.UnparsableReason.NOT_ACCESSIBLE);
//            }
//            catch (InvocationTargetException e)
//            {
//                throw new UnparsableTypeException(objType, UnparsableTypeException.UnparsableReason.NO_EMPTY_CONSTRUCTOR);
//            }
//        }

        return result;
    }

    /** JSONObject json is the containing JSON object with keys */
    private static Object processKeyForJSONObj(JSONObject json, TypeNode type, String key)
    		throws JSONParserException, InvalidMemberException
    {
        Object result = null;
        
        switch (type.getTypeNodeGroup())
        {
			case Primitive:
				result = getPrimitiveValue(json, type.actualType, key);
				break;
			
			case Collection:
			{
				JSONArray array = FailSafeParser.getJSONArray(json, key);
				result = process(array, type);
			} break;
			
//			case Map:
//			{				
//				JSONObject map = FailSafeParser.getJSONObject(json, key);
//				Map mapFromJSON = new HashMap();
//				
//				Iterator keyIterator = map.keys();
//				String k;
//				
//				while(keyIterator.hasNext())
//				{
//					k = (String) keyIterator.next();
//					result.
//					
//				}
//			} break;
			
			case Object:
			{
				JSONObject jobj = FailSafeParser.getJSONObject(json, key);
				result = parse(jobj, type.getNodeActualType());
				
			} break;
			
			case Unparsable: default:
				throw new JSONParserException(type.getNodeActualType(), JSONParserException.Reason.NON_PARSABLE);
			
		}
                
        return result;
    }
    
    private static Object process(JSONArray array, TypeNode type) 
    		throws JSONParserException, InvalidMemberException
    {
    	Object result;
    	TypeNode innerType = type.nextNodes.get(0);
    	int size = array.length();
    	
    	if(type.getNodeActualType().isArray())
    	{
    		result = Array.newInstance(innerType.getNodeActualType(), size);
    		
    		switch (innerType.getTypeNodeGroup())
    		{
    			case Primitive:
    			{
    				for(int i = 0; i < size; i++)
    					Array.set(result, i, getPrimitiveValue(array, innerType, i));
    			}; break;
    		
				case Collection:
				{
					JSONArray innerArray; 
					for(int i = 0; i < size; i++)
					{
						innerArray = FailSafeParser.getJSONArray(array, i);
    					Array.set(result, i, process(innerArray, innerType));
					}
				}; break;
				
				case Object:
				{
					JSONObject jobj;
					for(int i = 0; i < size; i++)
					{
						jobj = FailSafeParser.getJSONObject(array, i);
    					Array.set(result, i, parse(jobj, innerType.getNodeActualType()));
					}
				}; break;
								
				default: break;
			}
    	}
    	else
    	{
    		// Is a list
    		List temp = new LinkedList();
    		result = temp;
    		
    		switch (innerType.getTypeNodeGroup())
    		{
    			case Primitive:
    			{
    				for(int i = 0; i < size; i++)
    					temp.add(getPrimitiveValue(array, innerType, i));
    			}; break;
    		
				case Collection:
				{
					JSONArray innerArray; 
					for(int i = 0; i < size; i++)
					{
						innerArray = FailSafeParser.getJSONArray(array, i);
    					temp.add(process(innerArray, innerType));
					}
				}; break;
				
				case Object:
				{
					JSONObject jobj;
					for(int i = 0; i < size; i++)
					{
						jobj = FailSafeParser.getJSONObject(array, i);
    					temp.add(parse(jobj, innerType.getNodeActualType()));
					}
				}; break;
								
				default: break;
			}
    	}
    	
		return result;
	}

	/**
     * Obtains a primitive JSON type value, described in {@link RECOGNIZED_JSON_PRIMITIVES},
     * from the given key within the given JSONObject obj
     * @param obj The JSONObject that contains a mapping for the primitive value
     * @param type The primitive type Class
     * @param key The json key that maps to a primitive type
     * @return the primitive JSON value obtained, or null
     */
    private static Object getPrimitiveValue(JSONObject json, Class<?> type, String key)
    {
    	Object result = null;
    	
        if (type.equals(int.class) || type.equals(Integer.class))
        {
        	result = FailSafeParser.getInt(json, key);
        }
        else if (type.equals(double.class) || type.equals(Double.class))
        {
        	result = FailSafeParser.getDouble(json, key);
        }
        else if (type.equals(boolean.class) || type.equals(Boolean.class))
        {
        	result = FailSafeParser.getBool(json, key);
        }
        else if (type.equals(String.class))
        {
        	result = FailSafeParser.getString(json, key);
        }
        else if (type.equals(Date.class))
        {
        	result = FailSafeParser.getDate(json, key);	
        }
        else if (type.equals(long.class) || type.equals(Long.class))
        {
        	result = FailSafeParser.getLong(json, key);	
        }
        else if (type.equals(float.class) || type.equals(Float.class))
        {
        	Double temp = FailSafeParser.getDouble(json, key);
        	result = temp == null ? null : new Float(temp);
        }
        else if (type.equals(short.class) || type.equals(Short.class))
        {
        	Integer temp = FailSafeParser.getInt(json, key);
        	result = temp == null? null : new Short((short) temp.intValue());
        }
        else if (type.equals(char.class) || type.equals(Character.class))
        {
        	String temp = FailSafeParser.getString(json, key);
        	result = temp == null ? null : new Character(temp.charAt(0)); 
        }
        
        return result;
    }
    
   
    /**
     * Obtains a primitive JSON type value, described in {@link RECOGNIZED_JSON_PRIMITIVES},
     * in the given position in the given JSONArray jsonArr
     * @param jsonArr The JSONArray that contains a primitive value in the given position
     * @param type The primitive type Class
     * @param position the position in the array where the primitive value is stored
     * @return the primitive JSON value obtained, or null
     */
    private static Object getPrimitiveValue(JSONArray jsonArr, TypeNode type, int position)
    {
    	Object result = null;
    	
        if (type.getNodeActualType().equals(int.class) || type.getNodeActualType().equals(Integer.class))
        {
        	result = FailSafeParser.getInt(jsonArr, position);
        }
        else if (type.getNodeActualType().equals(double.class) || type.getNodeActualType().equals(Double.class))
        {
        	result = FailSafeParser.getDouble(jsonArr, position);
        }
        else if (type.getNodeActualType().equals(boolean.class) || type.getNodeActualType().equals(Boolean.class))
        {
        	result = FailSafeParser.getBool(jsonArr, position);
        }
        else if (type.getNodeActualType().equals(String.class))
        {
        	result = FailSafeParser.getString(jsonArr, position);
        }
        else if (type.getNodeActualType().equals(Date.class))
        {
        	result = FailSafeParser.getDate(jsonArr, position);	
        }
        else if (type.getNodeActualType().equals(long.class) || type.getNodeActualType().equals(Long.class))
        {
        	result = FailSafeParser.getLong(jsonArr, position);	
        }
        else if (type.getNodeActualType().equals(float.class) || type.getNodeActualType().equals(Float.class))
        {
        	Double temp = FailSafeParser.getDouble(jsonArr, position);
        	result = temp == null ? null : new Float(temp);
        }
        else if (type.getNodeActualType().equals(short.class) || type.getNodeActualType().equals(Short.class))
        {
        	Integer temp = FailSafeParser.getInt(jsonArr, position);
        	result = temp == null? null : new Short((short) temp.intValue());
        }
        else if (type.getNodeActualType().equals(char.class) || type.getNodeActualType().equals(Character.class))
        {
        	String temp = FailSafeParser.getString(jsonArr, position);
        	result = temp == null ? null : new Character(temp.charAt(0)); 
        }
        
        return result;
    }
        
		
    // --[ TYPE INFERENCE ]----------------------------------------------------------------------------
    
    private static class TypeNode
    {
    	public static TypeNode build(Field field)
    	{
    		return new TypeNode(field.getGenericType());
		}
    	
    	public static List<TypeNode> build(Method method)
    	{
    		List<TypeNode> argumentTypes = new LinkedList<JSONAnnotationParser.TypeNode>();
    		
    		for(Type argumentGnericType : method.getGenericParameterTypes())
    			argumentTypes.add(new TypeNode(argumentGnericType));
    		
    		return argumentTypes;
		}
    	
    	/**
    	 * Set of classes that are recognized as JSON primitives by the parser
    	 **/
    	private static final Set<Class> RECOGNIZED_JSON_PRIMITIVES = new HashSet<Class>(
    			Arrays.asList(
    					int.class, double.class, float.class, boolean.class, char.class, short.class, long.class,
    					Integer.class, Double.class, Float.class, Boolean.class, Character.class, Short.class, Long.class,
    					String.class, Date.class));
    	
    	private Class actualType;
    	private List<TypeNode> nextNodes;
    	private TypeGroup group;
    	
    	public enum TypeGroup
    	{
    		Primitive, Collection, Map, Object, Unparsable
    	}
    	
    	public TypeNode(Type type)
    	{
    		nextNodes = new LinkedList<JSONAnnotationParser.TypeNode>();
    		
    		if(type instanceof ParameterizedType)
    		{
    			ParameterizedType genericType = (ParameterizedType) type;
    			
    			actualType = (Class) genericType.getRawType();
    			    			
    			for (Type paramType: genericType.getActualTypeArguments()) 
    				nextNodes.add(new TypeNode(paramType));
    			
    			if(actualType.equals(Map.class))
    			{
    				if(nextNodes.size() > 0 && nextNodes.get(0).getNodeActualType().equals(String.class))
    					group = TypeGroup.Map;
    				else
    					group = TypeGroup.Unparsable;
    			}
    			else if(actualType.equals(List.class) || actualType.equals(Collection.class))
    				group = TypeGroup.Collection;
    			else if(JSONParsable.class.isAssignableFrom(actualType))
    				group = TypeGroup.Object;
    			else
    				group = TypeGroup.Unparsable;
    		}
    		else
    		{
    			actualType = (Class) type;
    			
    			if(RECOGNIZED_JSON_PRIMITIVES.contains(actualType)) group = TypeGroup.Primitive;
    			else if(JSONParsable.class.isAssignableFrom(actualType)) group = TypeGroup.Object;
    			else if(actualType.isArray())
    			{
    				group = TypeGroup.Collection;
    				nextNodes.add(new TypeNode(actualType.getComponentType()));
    			}
    			else
    				group = TypeGroup.Unparsable;
    		}
		}

    	public Class getNodeActualType()
    	{
    		return actualType;
    	}
    	
    	public TypeGroup getTypeNodeGroup()
    	{
    		return group;
    	}

    	public boolean hasNextNode()
    	{
    		return nextNodes != null && nextNodes.size() > 0;
    	}
    }


    // --[ EXCEPTIONS ]------------------------------------------------------------------------------

    public static class JSONParserException extends Exception
    {
		private static final long serialVersionUID = -4922894792026941777L;
		    	
    	public enum Reason
    	{
    		NON_INSTANCEABLE, NON_ACCESSIBLE, NON_PARSABLE;
    	}
    	
    	String msg;
    	
        public JSONParserException(Class<?> c, Reason r)
        {	
        	switch (r)
        	{
				case NON_INSTANCEABLE:
					msg = String.format("Type %s is not instanceable. Is it abstract, an interface or missing an empty constructor?", c.getName());
					break;
				case NON_ACCESSIBLE:
					msg = String.format("Type %s is not accessible.", c.getName());
					break;
				case NON_PARSABLE:
					msg = String.format("Type %s is a not parsable type. Please implement the Parsable interface or check the documentation for supported Java types.", c.getName());
					break;
			}
        }
        
        public String getHumanReadableReason()
        { return msg; }
    }
    
    public static class InvalidMemberException extends Exception
    {
		private static final long serialVersionUID = -7544254937366361304L;
		
		public InvalidMemberException(Class<?> type, Field f)
		{
			super(String.format("Field %s in type %s is inaccessible", f.getName()));
		}
		
		public InvalidMemberException(Class<?> type, Method m)
		{
			super(String.format("Method %s in type %s is inaccessible", m.getName()));
		}
		
		public String getHumanReadableReason()
        { return super.getMessage(); }
    }
}
