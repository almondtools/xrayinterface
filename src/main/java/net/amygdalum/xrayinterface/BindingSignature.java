package net.amygdalum.xrayinterface;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.BindingQualifier.AUTO;
import static net.amygdalum.xrayinterface.BindingQualifier.CONSTRUCTOR;
import static net.amygdalum.xrayinterface.BindingQualifier.GET;
import static net.amygdalum.xrayinterface.BindingQualifier.METHOD;
import static net.amygdalum.xrayinterface.BindingQualifier.SET;
import static net.amygdalum.xrayinterface.FixedType.VOID;

import java.util.List;

public class BindingSignature {

	public BindingQualifier qualifier;
	public String name;
	public Type result;
	public Type[] params;
	public Type[] exceptions;

	public BindingSignature() {
		this("", AUTO);
	}

	public BindingSignature(String name) {
		this(name, AUTO);
	}

	public BindingSignature(String name, BindingQualifier qualifier) {
		this.qualifier = qualifier;
		this.name = name;
		this.result = VOID;
		this.params = new Type[0];
		this.exceptions = new Type[0];
	}

	public boolean hasName() {
		return name != null && !"".equals(name);
	}

	public List<BindingQualifier> types() {
		if (qualifier == AUTO) {
			return asList(METHOD, GET, SET, CONSTRUCTOR);
		} else {
			return asList(qualifier);
		}
	}

}
