package net.amygdalum.xrayinterface;

import static java.util.Arrays.asList;
import static net.amygdalum.extensions.hamcrest.conventions.UtilityClassMatcher.isUtilityClass;
import static net.amygdalum.xrayinterface.SignatureUtil.computeFieldNames;
import static net.amygdalum.xrayinterface.SignatureUtil.fieldSignature;
import static net.amygdalum.xrayinterface.SignatureUtil.findTargetTypeName;
import static net.amygdalum.xrayinterface.SignatureUtil.isBooleanGetter;
import static net.amygdalum.xrayinterface.SignatureUtil.isConstructor;
import static net.amygdalum.xrayinterface.SignatureUtil.isGetter;
import static net.amygdalum.xrayinterface.SignatureUtil.isSetter;
import static net.amygdalum.xrayinterface.SignatureUtil.methodSignature;
import static net.amygdalum.xrayinterface.SignatureUtil.propertyAnnotationsOf;
import static net.amygdalum.xrayinterface.SignatureUtil.propertyOf;
import static net.amygdalum.xrayinterface.SignatureUtil.propertyTypeOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collections;

import org.junit.Test;

public class SignatureUtilTest {

	@Test
	public void testSignatureUtil() throws Exception {
		assertThat(SignatureUtil.class, isUtilityClass());
	}

	@Test
	public void testMethodSignatureOnlyName() throws Exception {
		assertThat(methodSignature("method", String.class, new Class<?>[0], new Class<?>[0]), equalTo("String method()"));
	}

	@Test
	public void testMethodSignatureNameAndException() throws Exception {
		Class<?>[] exceptions = { IOException.class };
		assertThat(methodSignature("method", String.class, new Class<?>[0], exceptions), equalTo("String method() throws IOException"));
	}

	@Test
	public void testMethodSignatureNameAndExceptions() throws Exception {
		Class<?>[] exceptions = { IOException.class, ParseException.class };
		assertThat(methodSignature("method", String.class, new Class<?>[0], exceptions), equalTo("String method() throws IOException, ParseException"));
	}

	@Test
	public void testMethodSignatureWithParameters() throws Exception {
		Class<?>[] classes = { String.class, int.class };
		assertThat(methodSignature("method", String.class, classes, new Class<?>[0]), equalTo("String method(String,int)"));
	}

	@Test
	public void testFieldSignature() throws Exception {
		assertThat(fieldSignature(asList("field"), String.class), equalTo("String field"));
	}

	@Test
	public void testFieldSignatureEmptyName() throws Exception {
		assertThat(fieldSignature(Collections.<String> emptyList(), String.class), equalTo("String "));
	}

	@Test
	public void testFieldSignatureMultipleAlternatives() throws Exception {
		assertThat(fieldSignature(asList("field", "Field"), String.class), equalTo("String field|Field"));
	}

	@Test
	public void testIsBooleanGetter() throws Exception {
		assertThat(isBooleanGetter(method("isBooleanGetter")), is(true));
		assertThat(isBooleanGetter(method("isBooleanGetterBoxed")), is(true));
		assertThat(isBooleanGetter(method("is")), is(false));
		assertThat(isBooleanGetter(method("notBooleanGetter")), is(false));
		assertThat(isBooleanGetter(method("isNotBooleanGetter")), is(false));
		assertThat(isBooleanGetter(method("isNotBooleanGetter", String.class)), is(false));
		assertThat(isBooleanGetter(method("isNotBooleanGetterExc")), is(false));
	}

	@Test
	public void testIsGetter() throws Exception {
		assertThat(isGetter(method("getGetter")), is(true));
		assertThat(isGetter(method("getNotGetter")), is(false));
		assertThat(isGetter(method("getNotGetter", String.class)), is(false));
		assertThat(isGetter(method("getNotGetterExc")), is(false));
		assertThat(isGetter(method("get")), is(false));
		assertThat(isGetter(method("notGetter")), is(false));
	}

	@Test
	public void testIsSetter() throws Exception {
		assertThat(isSetter(method("setSetter", String.class)), is(true));
		assertThat(isSetter(method("setNotSetter")), is(false));
		assertThat(isSetter(method("set", String.class)), is(false));
		assertThat(isSetter(method("notSetter", String.class)), is(false));
		assertThat(isSetter(method("setNotSetter", String.class)), is(false));
		assertThat(isSetter(method("setNotSetterExc", String.class)), is(false));
	}

	@Test
	public void testPropertyOf() throws Exception {
		assertThat(propertyOf(method("setSetter", String.class)), equalTo("setter"));
		assertThat(propertyOf(method("getGetter")), equalTo("getter"));
		assertThat(propertyOf(method("isBooleanGetter")), equalTo("booleanGetter"));
	}

	@Test
	public void testPropertyOfNonProperty() throws Exception {
		assertThat(propertyOf(method("isNotBooleanGetter")), equalTo("isNotBooleanGetter"));
	}

	@Test
	public void testIsConstructor() throws Exception {
		assertThat(isConstructor(method("newMethods")), is(true));
		assertThat(isConstructor(method("newMethods", String.class)), is(true));
		assertThat(isConstructor(method("notNewMethods")), is(false));
		assertThat(isConstructor(method("newMethods", int.class)), is(false));
		assertThat(isConstructor(method("newMethods", long.class)), is(false));
	}

	@Test
	public void testPropertyTypeOf() throws Exception {
		assertEquals(propertyTypeOf(method("setSetter", String.class)), String.class);
		assertEquals(propertyTypeOf(method("getGetter")), String.class);
		assertEquals(propertyTypeOf(method("isBooleanGetter")), boolean.class);
		assertNull(propertyTypeOf(method("newMethods")));
	}

	@Test
	public void testPropertyAnnotationsOf() throws Exception {
		assertThat(propertyAnnotationsOf(method("setSetter", String.class)), arrayWithSize(0));
		assertThat(propertyAnnotationsOf(method("getGetter")), arrayWithSize(0));
		assertThat(propertyAnnotationsOf(method("isBooleanGetter")), arrayWithSize(0));
		assertThat(propertyAnnotationsOf(method("setConverted", Object.class)), arrayWithSize(1));
		assertThat(propertyAnnotationsOf(method("getConverted")), arrayWithSize(1));
		assertThat(propertyAnnotationsOf(method("newMethods")), nullValue());
	}

	@Test
	public void testComputeFieldNames() throws Exception {
		assertThat(computeFieldNames("Field"), contains("field", "Field"));
		assertThat(computeFieldNames("FIELD"), contains("FIELD", "fIELD"));
	}

	@Test
	public void testFindTargetTypeNameWithoutAnnotationHint() throws Exception {
		assertThat(findTargetTypeName(new Annotation[0], String.class), nullValue());
	}

	@Test
	public void testFindTargetTypeNameWithAnnotationHint() throws Exception {
		assertThat(findTargetTypeName(method("getConverted").getAnnotations(), String.class), equalTo("String"));
	}

	@Test
	public void testFindTargetTypeNameWithExplicitAnnotationHint() throws Exception {
		assertThat(findTargetTypeName(method("convert").getAnnotations(), String.class), equalTo("Converted"));
	}

	public Method method(String name, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
		return Methods.class.getDeclaredMethod(name, parameterTypes);
	}

	interface Methods {

		Methods newMethods();

		Methods newMethods(String s);

		void newMethods(int i);

		long newMethods(long l);

		Methods notNewMethods();

		boolean isBooleanGetter();

		Boolean isBooleanGetterBoxed();

		boolean isNotBooleanGetter(String s);

		boolean isNotBooleanGetterExc() throws Exception;

		boolean notBooleanGetter();

		boolean is();

		int isNotBooleanGetter();

		String getGetter();

		void getNotGetter();

		String getNotGetter(String s);

		String getNotGetterExc() throws Exception;

		String get();

		String notGetter();

		void setSetter(String s);

		void setNotSetter();

		String setNotSetter(String s);

		void setNotSetterExc(String s) throws Exception;

		void set(String s);

		void notSetter(String s);

		void setConverted(@Convert Object o);

		@Convert
		Object getConverted();

		@Deprecated
		@Convert("Converted")
		Object convert();

	}

}
