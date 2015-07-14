package com.almondtools.xrayinterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassAccess is a Decorator for the static interface of a java class. Usage:
 * 
 * <p>
 * <code>InterfaceOfTheDecorator unlocked = ClassAccess.xray(ClassToUnlock.class).to(InterfaceOfTheDecorator.class);</code>
 * 
 * <p>
 * After that the variable unlocked contains an object of type InterfaceOfTheDecorator, where each method is mapped according to the xrayinterface conventions:
 * 
 * <table>
 * <col width="30%"/> <col width="30%"/> <col width="40%"/> <thead>
 * <tr>
 * <th>Method</th>
 * <th>Maps to</th>
 * <th></th>
 * </tr>
 * <thead>
 * <tr>
 * <td><code>ClassToUnlock create([signature])</code></td>
 * <td><code>ClassToUnlock([signature])</code></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td><code>void setProperty([sometype] t)</code></td>
 * <td><code>static void setProperty([sometype] t)</code></td>
 * <td>if exists</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td><code>property = t;</code></td>
 * <td>if there is a property <code>static [sometype] property;</td>
 * </tr>
 * <tr>
 * <td><code>[sometype] getProperty()</code></td>
 * <td><code>static [sometype] getProperty()</code></td>
 * <td>if exists</td>
 * <tr>
 * <tr>
 * <td></td>
 * <td><code>return property;</code></td>
 * <td>if there is a property <code>static [sometype] property;</td>
 * </tr>
 * <tr>
 * <td><code>[booleantype] isProperty()</code></td>
 * <td><code>static [booleantype] getProperty()</code></td>
 * <td>if exists</td>
 * <tr>
 * <tr>
 * <td></td>
 * <td><code>return property;</code></td>
 * <td>if there is a property <code>static [booleantype] property;</td>
 * </tr>
 * <tr>
 * <td><code>[return type] methodname([signature])</code></td>
 * <td><code>static [return type] methodname([signature])</code></td>
 * <td></td>
 * </tr>
 * </table>
 * 
 * @author Stefan Mandel
 */
public class ClassAccess extends StaticInvocationResolver implements InvocationHandler {

	private Map<Method, StaticMethodInvocationHandler> methods;

	public ClassAccess(Class<?> type) {
		super(type);
		this.methods = new HashMap<Method, StaticMethodInvocationHandler>();
	}

	/**
	 * wraps the given class. The result of this method will be decoratable with the new interface
	 * 
	 * @param type
	 *            the class to unlock/decorate
	 * @return the wrapped class
	 */
	public static ClassAccess xray(Class<?> type) {
		return new ClassAccess(type);
	}

	/**
	 * wraps the given class. The result of this method is a {@link ClassSnoop} object which enables the user to check if the wrapped class
	 * is compatible with a specific unlocked interface class. Note that in this case the "static" interface (all static methods including the constructor)
	 * would be checked on conflicts. The instance interface could be checked with {@link ObjectAccess.check}.
	 * 
	 * @param type
	 *            the target class to check on conflicts
	 * @return the wrapped class
	 */
	public static ClassSnoop check(Class<?> clazz) {
		return new ClassSnoop(clazz);
	}

	/**
	 * maps the given interface to the wrapped class
	 * 
	 * @param interfaceClass
	 *            the given interface class (defining the type of the result)
	 * @return an object of the type of interfaceClass (mapped to the members of the wrapped object)
	 * @throws NoSuchMethodException
	 *             if a method of the interface class could not be mapped according to the upper rules
	 */
	@SuppressWarnings("unchecked")
	public <T> T to(Class<T> interfaceClass) {
		try {
			for (Method method : interfaceClass.getDeclaredMethods()) {
				methods.put(method, findInvocationHandler(method));
			}
			return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, this);
		} catch (NoSuchMethodException e) {
			throw new InterfaceMismatchException("cannot resolve method/property " + e.getMessage() + " on " + getType());
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		StaticMethodInvocationHandler handler = methods.get(method);
		return handler.invoke(args);
	}

}
