package net.amygdalum.xrayinterface;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.BoxingUtil.getUnboxed;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public final class SignatureUtil {

	public static final String CONSTRUCTOR = "new";
	public static final String IS = "is";
	public static final String GET = "get";
	public static final String SET = "set";

	private SignatureUtil() {
	}

	public static boolean isConstructor(Method method) {
		String name = method.getName();
		return name.startsWith(CONSTRUCTOR)
			&& name.endsWith(method.getReturnType().getSimpleName())
			&& method.getReturnType() != void.class
			&& !method.getReturnType().isPrimitive();
	}

	public static boolean isBooleanGetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = getUnboxed(method.getReturnType());
		return name.length() > 2
			&& name.startsWith(IS)
			&& parameterTypes.length == 0
			&& exceptionTypes.length == 0
			&& returnType == boolean.class;
	}

	public static boolean isGetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = getUnboxed(method.getReturnType());
		return name.length() > 3
			&& name.startsWith(GET)
			&& parameterTypes.length == 0
			&& exceptionTypes.length == 0
			&& returnType != void.class;
	}

	public static boolean isSetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = getUnboxed(method.getReturnType());
		return name.length() > 3
			&& name.startsWith(SET)
			&& parameterTypes.length == 1
			&& exceptionTypes.length == 0
			&& returnType == void.class;
	}

	public static String propertyOf(Method method) {
		String name = method.getName();
		if (isSetter(method) || isGetter(method)) {
			return toLowerCase(name.charAt(3)) + name.substring(4);
		} else if (isBooleanGetter(method)) {
			return toLowerCase(name.charAt(2)) + name.substring(3);
		} else {
			return name;
		}
	}

	public static Class<?> propertyTypeOf(Method method) {
		if (isSetter(method)) {
			return method.getParameterTypes()[0];
		} else if (isGetter(method) || isBooleanGetter(method)) {
			return method.getReturnType();
		} else {
			return null;
		}
	}

	public static Annotation[] propertyAnnotationsOf(Method method) {
		if (isSetter(method)) {
			return method.getParameterAnnotations()[0];
		} else if (isGetter(method) || isBooleanGetter(method)) {
			return method.getAnnotations();
		} else {
			return null;
		}
	}

	public static List<String> computeFieldNames(String fieldPattern) {
		char firstCharUC = toUpperCase(fieldPattern.charAt(0));
		char firstCharLC = toLowerCase(fieldPattern.charAt(0));
		String lastChars = fieldPattern.substring(1);

		if (fieldPattern.toUpperCase().equals(fieldPattern)) {
			return asList(firstCharUC + lastChars, firstCharLC + lastChars);
		} else {
			return asList(firstCharLC + lastChars, firstCharUC + lastChars);
		}
	}

	public static String fieldSignature(String fieldName, Class<?> type) {
		return fieldSignature(asList(fieldName), type);
	}

	public static String fieldSignature(List<String> fieldNames, Class<?> type) {
		StringBuilder buffer = new StringBuilder()
			.append(typeName(type))
			.append(' ');
		Iterator<String> iterator = fieldNames.iterator();
		if (iterator.hasNext()) {
			buffer.append(iterator.next());
		}
		while (iterator.hasNext()) {
			buffer.append('|');
			buffer.append(iterator.next());
		}
		return buffer.toString();
	}

	public static String methodSignature(String methodName, Class<?> resultType, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
		return typeName(resultType) + ' ' + methodName + parameters(parameterTypes) + throwsClause(exceptionTypes);
	}

	private static String throwsClause(Class<?>[] exceptionTypes) {
		if (exceptionTypes.length == 0) {
			return "";
		}
		return " throws " + exceptions(exceptionTypes);
	}

	private static String parameters(Class<?>[] parameterTypes) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (parameterTypes.length > 0) {
			buffer.append(typeName(parameterTypes[0]));
		}
		for (int i = 1; i < parameterTypes.length; i++) {
			buffer.append(',').append(typeName(parameterTypes[i]));
		}
		buffer.append(')');
		return buffer.toString();
	}

	private static String exceptions(Class<?>[] exceptionTypes) {
		StringBuilder buffer = new StringBuilder();
		if (exceptionTypes.length > 0) {
			buffer.append(typeName(exceptionTypes[0]));
		}
		for (int i = 1; i < exceptionTypes.length; i++) {
			buffer.append(", ").append(typeName(exceptionTypes[i]));
		}
		return buffer.toString();
	}

	private static String typeName(Class<?> clazz) {
		if (clazz == null) {
			return "<unknown>";
		}
		return clazz.getSimpleName();
	}

	public static String findTargetTypeName(Annotation[] annotations, Class<?> defaultType) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == Convert.class) {
				Convert convertible = (Convert) annotation;
				String name = convertible.value();
				if (name.isEmpty()) {
					return defaultType.getSimpleName();
				} else {
					return name;
				}
			}
		}
		return null;
	}

}
