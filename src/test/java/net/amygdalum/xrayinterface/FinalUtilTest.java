package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.UtilityClassMatcher.isUtilityClass;
import static net.amygdalum.xrayinterface.FinalUtil.ensureWritable;
import static net.amygdalum.xrayinterface.FinalUtil.isFinal;
import static net.amygdalum.xrayinterface.FinalUtil.isWritable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;


public class FinalUtilTest {

	@Test
	public void testFinalUtil() throws Exception {
		assertThat(FinalUtil.class, isUtilityClass());
	}

	@Test
	public void testIsFinal() throws Exception {
		assertThat(isFinal(fieldOf("finalField")), is(true));
		assertThat(isFinal(fieldOf("staticFinalField")), is(true));
		assertThat(isFinal(fieldOf("field")), is(false));
	}

	@Test
	public void testIsWritable() throws Exception {
		assertThat(isWritable(fieldOf("field")), is(true));
		assertThat(isWritable(fieldOf("staticField")), is(true));
		assertThat(isWritable(fieldOf("finalField")), is(true));
	}

	@Test
	public void testIsNotWritableForStaticFinal() throws Exception {
		assertThat(isWritable(fieldOf("staticFinalField")), is(false));
	}

	@Test
	public void testEnsureWritable() throws Exception {
		ensureWritable(fieldOf("field"));
		ensureWritable(fieldOf("staticField"));
		ensureWritable(fieldOf("finalField"));
	}

	@Test
	public void testEnsureWritableFailsForStaticFinal() throws Exception {
		ReflectionFailedException exception = assertThrows(ReflectionFailedException.class, () -> {
			ensureWritable(fieldOf("staticFinalField"));
		});

		assertThat(exception.getMessage(), is("cannot write static final field String staticFinalField of "
			+ WithFinal.class.getName()
			+ ", writing static final fields is not supported since java 12"));
	}

	private static Field fieldOf(String name) throws NoSuchFieldException {
		return WithFinal.class.getDeclaredField(name);
	}

	@SuppressWarnings("unused")
	private static class WithFinal {
		private static final String staticFinalField = "";
		private static String staticField = "";
		private final String finalField = "";
		private String field = "";
	}

}
