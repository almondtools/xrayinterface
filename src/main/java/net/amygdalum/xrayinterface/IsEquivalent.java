package net.amygdalum.xrayinterface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
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
		if (item == null) {
			return false;
		}
		for (Map.Entry<String, Object> property : properties.entrySet()) {
			String name = property.getKey();
			Object value = property.getValue();
			try {
				Object itemValue = propertyValueFor(item, name);
				Matcher<?> matcher = matcherFor(value);
				if (!matcher.matches(itemValue)) {
					return false;
				}
			} catch (NoSuchFieldException e) {
				return false;
			}
		}
		return true;
	}

	private Object propertyValueFor(Object item, String name) throws NoSuchFieldException {
		Class<?> currentClass = item.getClass();
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
		throw new NoSuchFieldException(name);
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

	@Override
	public void describeMismatch(Object item, Description description) {
		Map<String, Object> mismatchedProperties = new LinkedHashMap<>();
		for (Map.Entry<String,Object> entry : properties.entrySet()) {
			String property = entry.getKey();
			Object expected = entry.getValue();
			try {
				Object value = propertyValueFor(item, property);
				if (expected instanceof Matcher<?>) {
					value = describe((Matcher<?>) expected, value);
				}
				mismatchedProperties.put(property, value);
			} catch (NoSuchFieldException e) {
				mismatchedProperties.put(property, "<missing>");
			}
		}
		description.appendText("with properties ").appendValueList("", ", ", "", mismatchedProperties.entrySet());
	}

	private String describe(Matcher<?> expected, Object value) {
		StringDescription description = new StringDescription();
		expected.describeMismatch(value, description);
		return description.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
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
