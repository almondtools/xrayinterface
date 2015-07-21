package com.almondtools.xrayinterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ObjectAccess is a Decorator for any object that should get a new public interface. Usage:
 * 
 * <p>
 * <code>InterfaceOfTheDecorator unlocked = ObjectAccess.xray(object).to(InterfaceOfTheDecorator.class);</code>
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
 * <td><code>void setProperty([sometype] t)</code></td>
 * <td><code>void setProperty([sometype] t)</code></td>
 * <td>if exists</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td><code>property = t;</code></td>
 * <td>if there is a property <code>[sometype] property;</td>
 * </tr>
 * <tr>
 * <td><code>[sometype] getProperty()</code></td>
 * <td><code>[sometype] getProperty()</code></td>
 * <td>if exists</td>
 * <tr>
 * <tr>
 * <td></td>
 * <td><code>return property;</code></td>
 * <td>if there is a property <code>[sometype] property;</td>
 * </tr>
 * <tr>
 * <td><code>[booleantype] isProperty()</code></td>
 * <td><code>[booleantype] getProperty()</code></td>
 * <td>if exists</td>
 * <tr>
 * <tr>
 * <td></td>
 * <td><code>return property;</code></td>
 * <td>if there is a property <code>[booleantype] property;</td>
 * </tr>
 * <tr>
 * <td><code>[return type] methodname([signature])</code></td>
 * <td><code>[return type] methodname([signature])</code></td>
 * <td></td>
 * </tr>
 * </table>
 * 
 * @author Stefan Mandel
 */
public class ObjectAccess extends InstanceInvocationResolver implements InvocationHandler {

	private Map<Method, MethodInvocationHandler> methods;
	private Object object;

	public ObjectAccess(Object object) {
		super(object.getClass());
		this.methods = new HashMap<Method, MethodInvocationHandler>();
		this.object = object;
	}
	
	public Map<Method, MethodInvocationHandler> getMethods() {
		return methods;
	}

	public Object getObject() {
		return object;
	}

	/**
	 * wraps the given object. The result of this method will be decoratable with the new interface
	 * 
	 * @param type the class to unlock/decorate
	 * @return the wrapped object
	 */
	public static ObjectAccess xray(Object object) {
		return new ObjectAccess(object);
	}

	/**
	 * wraps the given class. The result of this method is a {@link ObjectSnoop} object which enables the user to check if a wrapped object (of the given class)
	 * could be target of a mapping from a specific interface. Note that a class (not an object) is wrapped, but the result will check the instance interface of this class
	 * (all non-static methods without constructors) not the static interface. static interfaces could be checked with {@link com.almondtools.xrayinterface.ClassAccess#check(Class<?>)}.
	 * 
	 * @param type the target class to check
	 * @return the wrapped class
	 */
	public static ObjectSnoop check(Class<?> clazz) {
		return new ObjectSnoop(clazz);
	}

	/**
	 * maps the given interface to the wrapped object
	 * 
	 * @param interfaceClass the given interface class (defining the type of the result)
	 * @return an object of the type of interfaceClass (mapped to the members of the wrapped object)
	 * @throws NoSuchMethodException if a method of the interface class could not be mapped according to the upper rules
	 */
	@SuppressWarnings("unchecked")
	public <T> T to(Class<T> interfaceClass) {
		try {
			List<Class<?>> todo = new ArrayList<Class<?>>();
			Set<Class<?>> done = new HashSet<Class<?>>();
			todo.add(interfaceClass);
			while (!todo.isEmpty()) {
				Class<?> currentClass = todo.remove(0);
				done.add(currentClass);
				for (Method method : currentClass.getDeclaredMethods()) {
					if (!methods.containsKey(method)) {
						methods.put(method, findInvocationHandler(method));
					}
					for (Class<?> superInterfaceClazz : currentClass.getInterfaces()) {
						if (!done.contains(superInterfaceClazz)) {
							todo.add(superInterfaceClazz);
						}
					}
				}
			}
			return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, this);
		} catch (NoSuchFieldException e) {
			throw new InterfaceMismatchException("cannot resolve property " + e.getMessage() + " on " + object.getClass());
		} catch (NoSuchMethodException e) {
			throw new InterfaceMismatchException("cannot resolve method/property " + e.getMessage() + " on " + object.getClass());
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocationHandler handler = methods.get(method);
		return handler.invoke(object, args);
	}

}
