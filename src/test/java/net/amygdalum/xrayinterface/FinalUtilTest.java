package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.UtilityClassMatcher.isUtilityClass;
import static net.amygdalum.xrayinterface.FinalUtil.ensureNonFinal;
import static net.amygdalum.xrayinterface.FinalUtil.isFinal;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

import net.amygdalum.xrayinterface.FinalUtil;


public class FinalUtilTest {

	@Test
	public void testFinalUtil() throws Exception {
		assertThat(FinalUtil.class, isUtilityClass());
	}

	@Test
	public void testEnsureNonFinal() throws Exception {
		Field field = WithFinal.class.getDeclaredField("field");
		ensureNonFinal(field);
		assertThat(isFinal(field), is(false));
	}

	private static class WithFinal {
		@SuppressWarnings("unused")
		private final String field = "";
	}

}
