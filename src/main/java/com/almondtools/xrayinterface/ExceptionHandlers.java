package com.almondtools.xrayinterface;

import java.util.function.Function;

public final class ExceptionHandlers {
	
	public static final Function<Throwable, Object> RETURN_NULL = t -> null;

	private ExceptionHandlers() {
	}

}
