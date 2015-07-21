package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.BindingType.AUTO;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Bind {

	BindingType type() default AUTO;
	
	String name() default "";
	
}
