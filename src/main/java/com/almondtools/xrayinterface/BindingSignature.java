package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.BindingType.AUTO;
import static com.almondtools.xrayinterface.BindingType.CONSTRUCTOR;
import static com.almondtools.xrayinterface.BindingType.GET;
import static com.almondtools.xrayinterface.BindingType.METHOD;
import static com.almondtools.xrayinterface.BindingType.SET;
import static com.almondtools.xrayinterface.FixedType.VOID;
import static java.util.Arrays.asList;

import java.util.List;

public class BindingSignature {

	public BindingType type;
	public String name;
	public Type result;
	public Type[] params;
	public Type[] exceptions;
	
	public BindingSignature() {
		this(AUTO,"");
	}
	
	public BindingSignature(String name) {
		this(AUTO, name);
	}

	public BindingSignature(BindingType type, String name) {
		this.type = type;
		this.name = name;
		this.result = VOID;
		this.params = new Type[0];
		this.exceptions = new Type[0];
	}
	
	public boolean hasName() {
		return name != null && !"".equals(name);
	}

	public List<BindingType> types() {
		if (type == AUTO) {
			return asList(METHOD, CONSTRUCTOR,GET,SET);
		} else {
			return asList(type);
		}
	}

}
