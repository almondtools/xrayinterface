package net.amygdalum.xrayinterface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

public class ClassUnlockableMatcher extends TypeSafeMatcher<Class<?>> {

	private Class<?> interfaceClazz;

	public ClassUnlockableMatcher(Class<?> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

	public static ClassUnlockableMatcher canBeTreatedAs(Class<?> interfaceClazz) {
		return new ClassUnlockableMatcher(interfaceClazz);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(" can unlock features of class ").appendValue(interfaceClazz.getSimpleName());
	}

	@Override
	protected void describeMismatchSafely(Class<?> item, Description mismatchDescription) {
		List<Method> conflicts = XRayInterface.xray(item).unMappable(interfaceClazz);
		if (!conflicts.isEmpty()) {
			mismatchDescription
			.appendText("cannot find following members in ")
			.appendValue(item.getSimpleName())
			.appendText(": ")
			.appendList("\n", "\n", "", describe(conflicts));
		}
	}

	private List<SelfDescribing> describe(List<Method> conflicts) {
		List<SelfDescribing> descriptions = new ArrayList<SelfDescribing>(conflicts.size());
		for (Method conflict : conflicts) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(conflict.getReturnType().getSimpleName());
			buffer.append(' ');
			buffer.append(conflict.getName());
			buffer.append('(');
			Class<?>[] parameterTypes = conflict.getParameterTypes();
			if (parameterTypes.length > 0) {
				buffer.append(parameterTypes[0].getSimpleName());
			}
			for (int i = 1; i < parameterTypes.length; i++) {
				buffer.append(", ");
				buffer.append(parameterTypes[i].getSimpleName());
			}
			buffer.append(')');
			Class<?>[] exceptionTypes = conflict.getExceptionTypes();
			if (exceptionTypes.length > 0) {
				buffer.append(" throws ");
				buffer.append(exceptionTypes[0].getSimpleName());
				for (int i = 1; i < exceptionTypes.length; i++) {
					buffer.append(", ");
					buffer.append(exceptionTypes[i].getSimpleName());
				}
			}
			descriptions.add(new Signature(buffer.toString()));
		}
		return descriptions ;
	}

	@Override
	protected boolean matchesSafely(Class<?> item) {
		return XRayInterface.xray(item).unMappable(interfaceClazz).isEmpty();
	}

	private final class Signature implements SelfDescribing {
		private final String signature;

		private Signature(String signature) {
			this.signature = signature;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(signature);					
		}
	}

}
