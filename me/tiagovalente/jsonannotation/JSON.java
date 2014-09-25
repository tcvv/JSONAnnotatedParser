package me.tiagovalente.jsonannotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class encapsulates annotations other requirements for the
 * JSONAnnotationParser
 * 
 * @author Tiago Valente
 * @version 1.0.0
 * @since 1.0.0
 */
public class JSON
{
    /**
     * Associates the field or parameter to a JSON member.
     * This associates it with the value contained in the JSON key 'key',
     * which is expected to be of the type 'type'.
     *
     * An OBJ type requires one additional annotation (ParseAs)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface Value
    {
        public String key();
        public Type type();
    }

    /**
     * Associates the field or parameter to a JSON member.
     * This associates it with the value contained in the JSON key 'key',
     * which is a collection of members of the type 'type'.
     *
     * If the members are of type OBJ, they require one additional annotation (ParseAs)
     *
     * (CAVEAT: in order to deal with Collection of Collection, an intermediary object must be used)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface ValueCollection
    {
        public String key();
        public Type of();
    }

    /**
     * Use to describe how to parse an OBJ JSON type
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface ParseAs
    {
        public Class value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    /**
     * Marks this method as a transformation method.
     * Transformation methods should accept only one parameter.
     * This parameter is described using any of the other available annotations.
     */
    public @interface TransformationMethod
    {}

    /** Recognizable JSON Types **/
    public enum Type
    { INT, LONG, STRING, BOOL, DOUBLE, DATE, OBJ }
}
