package com.almondtools.xrayinterface;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * ObjectSnoop is a Decorator for the interface of a java object. It helps finding conflicts of the wrapped class with a given interface. Usage:
 * 
 * <p><code>List<Method> conflictingMethods = ObjectAccess.check(ClassOfObjectToCheck.class).onConflicts(InterfaceToMapFrom.class);</code>
 * 
 * <p>The conflicting methods are all methods that could not be mapped according to the rules in {@link ObjectAccess}
 * 
 * @author Stefan Mandel
 */
public class ObjectSnoop extends InvocationResolver {

	public ObjectSnoop(Class<?> clazz) {
		super(clazz);
	}

	/**
	 * collects all methods of the given interface conflicting with the wrapped object
	 * 
	 * @param interfaceClazz
	 *            the interface to check on conflicts
	 * @return a list of methods conflicting
	 */
	public List<Method> onConflicts(Class<?> interfaceClazz) {
		List<Method> conflicts = new LinkedList<Method>();
		for (Method method : interfaceClazz.getDeclaredMethods()) {
			try {
				findInvocationHandler(method);
			} catch (NoSuchFieldException | NoSuchMethodException e) {
				conflicts.add(method);
			}
		}
		return conflicts;
	}

}
