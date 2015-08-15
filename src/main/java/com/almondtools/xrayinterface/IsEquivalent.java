package com.almondtools.xrayinterface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;

public class IsEquivalent<S, T extends Matcher<S>> extends BaseMatcher<S> {

	private static final String WITH = "with";

	private Class<T> interfaceClazz;
	private Map<String, Object> properties;

	public IsEquivalent(Class<T> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
		this.properties = new LinkedHashMap<String, Object>();
	}

	public static <S, T extends Matcher<S>> T equivalentTo(Class<T> interfaceClazz) {
		return new XRayInterfaceWith<S, T>(new IsEquivalent<S, T>(interfaceClazz)).to(interfaceClazz);
	}

	protected MethodInvocationHandler handle(final String name) {
		return new MethodInvocationHandler() {

			@SuppressWarnings("unchecked")
			@Override
			public Object invoke(Object object, Object... args) throws Throwable {
				((IsEquivalent<S, T>) object).properties.put(name, args[0]);
				return new XRayInterfaceWith<S, T>(IsEquivalent.this).to(interfaceClazz);
			}
		};
	}

	@Override
	public boolean matches(Object item) {
		Class<?> itemClass = item.getClass();
		for (Map.Entry<String, Object> property : properties.entrySet()) {
			String name = property.getKey();
			Object value = property.getValue();
			Matcher<?> matcher = matcherFor(value);
			Object itemValue = propertyValueFor(itemClass, item, name);
			if (!matcher.matches(itemValue)) {
				return false;
			}
		}
		return true;
	}

	private Object propertyValueFor(Class<?> itemClass, Object item, String name) {
		Class<?> currentClass = itemClass;
		while (currentClass != null) {
			for (String fieldName : SignatureUtil.computeFieldNames(name)) {
				try {
					Field field = currentClass.getDeclaredField(fieldName);
					field.setAccessible(true);
					return field.get(item);
				} catch (Exception e) {
					continue;
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return null;
	}

	private Matcher<?> matcherFor(Object value) {
		if (value instanceof Matcher<?>) {
			return (Matcher<?>) value;
		} else if (value == null) {
			return IsNull.nullValue();
		} else {
			return IsEqual.equalTo(value);
		}
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("with properties ").appendValueList("", ", ", "", properties.entrySet());
	}

	private static final class XRayInterfaceWith<S, T extends Matcher<S>> extends XRayInterface {
		private XRayInterfaceWith(Object object) {
			super(object);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected MethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException, NoSuchFieldException {
			IsEquivalent<S, T> satisfiesMatcher = (IsEquivalent<S, T>) getObject();
			if (method.getName().startsWith(WITH) && method.getParameterTypes().length == 1 && method.getReturnType() == satisfiesMatcher.interfaceClazz) {
				return satisfiesMatcher.handle(method.getName().substring(4));
			}
			return super.findInvocationHandler(method);
		}
	}

}
