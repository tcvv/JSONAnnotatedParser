package me.tiagovalente.jsonannotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates the field or parameter to a JSON member.
 * This associates it with the value contained in the JSON key 'key'.
 * 
 * If no key is provided, it is assumed that the method/field name 
 * should be used as the key.
 * 
 * @author Tiago Valente
 * @version 1.0.0
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JSON
{
	public String key() default "";
}
