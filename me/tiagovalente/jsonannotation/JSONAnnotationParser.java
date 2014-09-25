package me.tiagovalente.jsonannotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

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
            throws InvalidAnnotationException,
                   UnparsableTypeException,
                   MissingAnnotationException,
                   DuplicatedAnnotationException,
                   InvalidMemberException
    {
        T result;

        try
        {
            result = objType.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new UnparsableTypeException(objType, UnparsableTypeException.UnparsableReason.NO_EMPTY_CONSTRUCTOR);
        }
        catch (IllegalAccessException e)
        {
            throw new UnparsableTypeException(objType, UnparsableTypeException.UnparsableReason.NOT_ACCESSIBLE);
        }

        // Fields
        for( Field f : objType.getDeclaredFields() )
        {
            JSON.Value ann = f.getAnnotation(JSON.Value.class);
            JSON.ValueCollection collection_ann = f.getAnnotation(JSON.ValueCollection.class);
            JSON.ParseAs parse_as = f.getAnnotation(JSON.ParseAs.class);

            // -- > if this field isn't annotated, move on
            if(ann == null && collection_ann == null) continue;

            // -- > if this field is annotated twice, throw Exception
            if(ann != null && collection_ann != null)
                throw new DuplicatedAnnotationException(objType, f, null);

            Object o = process(obj, objType, ann, collection_ann, parse_as);

            try
            {
            	// -- > allow me, it's just this one time
            	f.setAccessible(true);
                f.set(result, o);
                f.setAccessible(false);
            }
            catch (IllegalAccessException e)
            {
                throw new InvalidMemberException(objType, f, null);
            }
        }

        // Transformations
        for(Method m : objType.getDeclaredMethods())
        {
            JSON.TransformationMethod tm = m.getAnnotation(JSON.TransformationMethod.class);

            JSON.ValueCollection collection_ann = m.getAnnotation(JSON.ValueCollection.class);
            JSON.Value ann = m.getAnnotation(JSON.Value.class);
            JSON.ParseAs parse_as = m.getAnnotation(JSON.ParseAs.class);

            // -- > if this field isn't annotated, move on
            if(tm == null) continue;

            // -- > if this field hasn't more annotations, throw exception
            if(ann == null && collection_ann == null)
                throw new MissingAnnotationException(JSON.Value.class, objType, null, m);

            // -- > if this field is annotated twice, throw Exception
            if(ann != null && collection_ann != null)
                throw new DuplicatedAnnotationException(objType, null, m);

            Object o = process(obj, objType, ann, collection_ann, parse_as);

            try
            {
            	// -- > allow me, it's just this one time
            	m.setAccessible(true);
                m.invoke(result, o);
                m.setAccessible(false);
            }
            catch (IllegalAccessException e)
            {
                throw new UnparsableTypeException(objType, UnparsableTypeException.UnparsableReason.NOT_ACCESSIBLE);
            }
            catch (InvocationTargetException e)
            {
                throw new UnparsableTypeException(objType, UnparsableTypeException.UnparsableReason.NO_EMPTY_CONSTRUCTOR);
            }
        }

        return result;
    }

    private static Object process(JSONObject obj, Class objType, JSON.Value ann, JSON.ValueCollection c_ann, JSON.ParseAs parse_as)
            throws
            MissingAnnotationException,
            InvalidAnnotationException,
            UnparsableTypeException,
            InvalidMemberException,
            DuplicatedAnnotationException

    {
        Object o;
        
        String key;
        JSON.Type type;

        if(ann != null) // Value
        {
            key = ann.key();
            type = ann.type();

            switch(type)
            {
                case OBJ:
                    if (parse_as == null)
                        throw new MissingAnnotationException(JSON.ParseAs.class, objType, null, null);

                    JSONObject temp = FailSafeParser.getJSONObject(obj,key);

                    o = temp != null ? parse(temp, parse_as.value()) : null; 

                    break;

                default:
                    o = getPrimalValue(obj, key, type);
            }
        }
        else // ValueCollection
        {
            key = c_ann.key();
            type = c_ann.of();

            JSONArray jarr = FailSafeParser.getJSONArray(obj, key);

            if(jarr == null) o = new LinkedList();
            else
            {
                switch(type)
                {
                    case OBJ:
                        if (parse_as == null)
                            throw new MissingAnnotationException(JSON.ParseAs.class, objType, null, null);

                        o = getCollection(jarr, type, parse_as.value());
                        break;

                    default:
                        o = getCollection(jarr, type, null);
                }
            }
        }

        return o;
    }

    private static List getCollection(JSONArray arr, JSON.Type type, Class cls)
            throws
            UnparsableTypeException,
            MissingAnnotationException,
            InvalidAnnotationException,
            InvalidMemberException,
            DuplicatedAnnotationException

    {
        List result = new LinkedList();
        Object value = null;

        for(int i = 0; i < arr.length(); i++)
        {
            switch(type)
            {
                case STRING:
                    value = FailSafeParser.getString(arr, i);
                    break;

                case INT:
                    value = FailSafeParser.getInt(arr, i);
                    break;

                case LONG:
                    value = FailSafeParser.getLong(arr, i);
                    break;

                case BOOL:
                    value = FailSafeParser.getBool(arr, i);
                    break;

                case DOUBLE:
                    value = FailSafeParser.getDouble(arr, i);
                    break;

                case DATE:
                    value = FailSafeParser.getDate(arr, i);
                    break;

                case OBJ:
                    JSONObject obj = FailSafeParser.getJSONObject(arr, i);
                    if(obj != null) value = parse(obj, cls);
                    break;
            }

            if(value!=null) result.add(value);
        }

        return result;
    }

    private static Object getPrimalValue(JSONObject json, String key, JSON.Type type)
    {
        Object value = null;

        switch (type)
        {
            case STRING:
                value = FailSafeParser.getString(json, key);
                break;

            case INT:
                value = FailSafeParser.getInt(json, key);
                break;

            case LONG:
                value = FailSafeParser.getLong(json, key);
                break;

            case BOOL:
                value = FailSafeParser.getBool(json, key);
                break;

            case DOUBLE:
                value = FailSafeParser.getDouble(json, key);
                break;

            case DATE:
                value = FailSafeParser.getDate(json, key);
                break;
        }

        return value;
    }

    // --[ EXCEPTIONS ]-----------------------------------------------------------------------------

    public static abstract class JSONParserException extends Exception
    {
		private static final long serialVersionUID = -4922894792026941777L;
		
		public Class cls;

        public JSONParserException(String msg, Class c)
        {
            super(msg);
            this.cls = c;
        }

        public JSONParserException(String msg, Throwable t, Class c)
        {
            super(msg, t);
            this.cls = c;
        }

        public abstract String getHumanReadableReason();
    }

    public static abstract class AnnotationGenericException extends JSONParserException
    {
		private static final long serialVersionUID = 8619016078969492540L;
		
		public Field field;
        public Method method;

        public AnnotationGenericException(String msg, Class c, Field f)
        {
            super(msg, c);
            field = f;
            method = null;
        }

        public AnnotationGenericException(String msg, Class c, Method m)
        {
            super(msg, c);
            this.method = m;
            this.field = null;
        }

        public AnnotationGenericException(String msg, Class c, Method m, Field f)
        {
            super(msg, c);
            this.method = m;
            this.field = f;
        }
    }

    public static class InvalidMemberException extends AnnotationGenericException
    {
		private static final long serialVersionUID = -4538929064566221037L;

		public InvalidMemberException(Class c, Field f, Method m)
        {
            super(c.getName(), c, m, f);
        }

        @Override
        public String getHumanReadableReason()
        {
            if(method != null)
                return String.format("Invalid method %s", method.getName());
            if(field != null)
                return String.format("Invalid field %s", field.getName());

            return "";
        }
    }

    public static class MissingAnnotationException extends AnnotationGenericException
    {
		private static final long serialVersionUID = 3522044727658136834L;
		
		public Class<? extends Annotation> annotation_t;

        public MissingAnnotationException(Class<? extends Annotation> a, Class c, Field f, Method m)
        {
            super("Missing annotation", c, m, f);
            this.annotation_t = a;
        }

        @Override
        public String getHumanReadableReason()
        {
            String start = String.format("Missing annotation %s in class %s",
                                 annotation_t.getName(), cls.getName());

            if(method != null)
                return start + String.format(", method %s", method.getName());
            if(field != null)
                return start + String.format(", field %s", field.getName());

            return start;
        }
    }

    /** Thrown for an invalid annotation while parsing **/
    public static class InvalidAnnotationException extends AnnotationGenericException
    {
		private static final long serialVersionUID = -5499912785882304884L;
		
		public Annotation annotation;

        public InvalidAnnotationException(Annotation a, Class c, Field f, Method m)
        {
            super("Invalid annotation", c, m, f);
            this.annotation = a;
        }

        @Override
        public String getHumanReadableReason()
        {
            String start =  String.format("Invalid annotation %s in class %s",
                                 annotation.annotationType().getName(), cls.getName());

            if(method != null)
                return start + String.format(", method %s", method.getName());
            if(field != null)
                return start + String.format(", field %s", field.getName());

            return start;
        }
    }

    public static class DuplicatedAnnotationException extends AnnotationGenericException
    {
		private static final long serialVersionUID = -4937157683494656980L;

		public DuplicatedAnnotationException(Class cls, Field f, Method m)
        {
            super("Duplicated JSON Annotation", cls, m, f);
        }

        @Override
        public String getHumanReadableReason()
        {
            String start = String.format("Duplicated annotations in class %s", cls);

            if(method != null)
                return start + String.format(", method %s", method.getName());
            if(field != null)
                return start + String.format(", field %s", field.getName());

            return start;
        }
    }

    /** Thrown when attempting to parse a type */
    public static class UnparsableTypeException extends JSONParserException
    {
		private static final long serialVersionUID = 1885490338971282647L;
		public UnparsableReason reason;

        public <T> UnparsableTypeException(Class<T> objType, UnparsableReason reason)
        {
            super(String.format("Unparsable class: %s", objType.getName()), objType);
            this.reason = reason;
        }

        @Override
        public String getHumanReadableReason()
        {
            switch (reason)
            {
                case NO_EMPTY_CONSTRUCTOR:
                    return String.format("No empty constructor in class %s", cls.getName());
                case NOT_ACCESSIBLE:
                    return String.format("Class %s is not accessible", cls.getName());
                default:
                    return cls.getName();
            }
        }

        public enum UnparsableReason
        {
            NO_EMPTY_CONSTRUCTOR, NOT_ACCESSIBLE
        }
    }
}
