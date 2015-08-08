package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.SignatureUtil.methodSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

public class XRayMatcher extends TypeSafeMatcher<Class<?>> {

	private Class<?> interfaceClazz;

	public XRayMatcher(Class<?> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

	public static XRayMatcher providesFeaturesOf(Class<?> interfaceClazz) {
		return new XRayMatcher(interfaceClazz);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("can unlock features of ").appendValue(interfaceClazz);
	}

	@Override
	protected void describeMismatchSafely(Class<?> item, Description mismatchDescription) {
		List<Method> conflicts = XRayInterface.xray(item).unMappable(interfaceClazz);
		if (!conflicts.isEmpty()) {
			mismatchDescription
				.appendText("cannot map following members in ")
				.appendValue(item)
				.appendText(": ")
				.appendList("\n", "\n", "", describe(conflicts));
		}
	}

	private List<SelfDescribing> describe(List<Method> conflicts) {
		List<SelfDescribing> descriptions = new ArrayList<SelfDescribing>(conflicts.size());
		for (Method conflict : conflicts) {
			descriptions.add(new Signature(methodSignature(conflict.getName(), conflict.getReturnType(), conflict.getParameterTypes(), conflict.getExceptionTypes())));
		}
		return descriptions;
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
