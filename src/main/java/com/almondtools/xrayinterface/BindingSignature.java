package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.BindingQualifier.AUTO;
import static com.almondtools.xrayinterface.BindingQualifier.CONSTRUCTOR;
import static com.almondtools.xrayinterface.BindingQualifier.GET;
import static com.almondtools.xrayinterface.BindingQualifier.METHOD;
import static com.almondtools.xrayinterface.BindingQualifier.SET;
import static com.almondtools.xrayinterface.FixedType.VOID;
import static java.util.Arrays.asList;

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
