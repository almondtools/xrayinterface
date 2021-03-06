package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.BindingQualifier.AUTO;
import static net.amygdalum.xrayinterface.BindingQualifier.CONSTRUCTOR;
import static net.amygdalum.xrayinterface.BindingQualifier.GET;
import static net.amygdalum.xrayinterface.BindingQualifier.METHOD;
import static net.amygdalum.xrayinterface.BindingQualifier.SET;
import static net.amygdalum.xrayinterface.FixedType.VOID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class BindingSignatureTest {

	@Test
	public void testBindingSignature() throws Exception {
		assertThat(new BindingSignature().name, equalTo(""));
		assertThat(new BindingSignature().qualifier, equalTo(AUTO));
		assertThat(new BindingSignature().result, equalTo(VOID));
		assertThat(new BindingSignature().params.length, equalTo(0));
		assertThat(new BindingSignature().exceptions.length, equalTo(0));
	}

	@Test
	public void testBindingSignatureString() throws Exception {
		assertThat(new BindingSignature("name").name, equalTo("name"));
		assertThat(new BindingSignature("name").qualifier, equalTo(AUTO));
		assertThat(new BindingSignature("name").result, equalTo(VOID));
		assertThat(new BindingSignature("name").params.length, equalTo(0));
		assertThat(new BindingSignature("name").exceptions.length, equalTo(0));
	}

	@Test
	public void testBindingSignatureStringQualifier() throws Exception {

		assertThat(new BindingSignature("name", GET).name, equalTo("name"));
		assertThat(new BindingSignature("name", GET).qualifier, equalTo(GET));
		assertThat(new BindingSignature("name", GET).result, equalTo(VOID));
		assertThat(new BindingSignature("name", GET).params.length, equalTo(0));
		assertThat(new BindingSignature("name", GET).exceptions.length, equalTo(0));
	}

	@Test
	public void testHasName() throws Exception {
		assertThat(new BindingSignature().hasName(), is(false));
		assertThat(new BindingSignature("name").hasName(), is(true));
	}

	@Test
	public void testTypes() throws Exception {
		assertThat(new BindingSignature().types(), contains(METHOD, GET, SET, CONSTRUCTOR));
		assertThat(new BindingSignature("name", METHOD).types(), contains(METHOD));
		assertThat(new BindingSignature("name", GET).types(), contains(GET));
		assertThat(new BindingSignature("name", SET).types(), contains(SET));
		assertThat(new BindingSignature("name", CONSTRUCTOR).types(), contains(CONSTRUCTOR));
	}

}
