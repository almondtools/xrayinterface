package com.almondtools.xrayinterface;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XRay is a Decorator for any object (or class) that should get a new public interface. Usage:
 * 
 * <p>
 * <code>InterfaceOfTheDecorator unlocked = XRayInterface.xray(object).to(InterfaceOfTheDecorator.class);</code>
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
public class XRayInterface extends InvocationResolver implements InvocationHandler {

	private Map<Method, MethodInvocationHandler> methods;
	private Object object;

	public XRayInterface(Object object) {
		super(object.getClass());
		this.methods = new HashMap<Method, MethodInvocationHandler>();
		this.object = object;
	}
	
	public XRayInterface(Class<?> clazz) {
		super(clazz);
		this.methods = new HashMap<Method, MethodInvocationHandler>();
	}
	
	public Map<Method, MethodInvocationHandler> getInterfaceMethods() {
		return methods;
	}
	
	public List<ConstructorInvoker> getConstructors() {
		return methods.values().stream()
			.filter(value -> value instanceof ConstructorInvoker)
			.map(value -> (ConstructorInvoker) value)
			.collect(toList());
	}

	public List<FieldProperty> getFieldProperties() {
		Map<String, FieldProperty> properties = new LinkedHashMap<>();
		for (FieldSetter setter : getFieldSetters()) {
			String field = setter.getFieldName();
			FieldProperty property = properties.computeIfAbsent(field, key -> new FieldProperty());
			property.setSetter(setter);
		}
		for (FieldGetter getter : getFieldGetters()) {
			String field = getter.getFieldName();
			FieldProperty property = properties.computeIfAbsent(field, key -> new FieldProperty());
			property.setGetter(getter);
		}
		return new ArrayList<>(properties.values());
	}

	public List<FieldSetter> getFieldSetters() {
		return filteredMethods(FieldSetter.class);
	}

	public List<FieldGetter> getFieldGetters() {
		return filteredMethods(FieldGetter.class);
	}

	public List<StaticProperty> getStaticProperties() {
		return null;
	}

	public List<StaticSetter> getStaticSetters() {
		return filteredMethods(StaticSetter.class);
	}

	public List<StaticGetter> getStaticGetters() {
		return filteredMethods(StaticGetter.class);
	}

	public List<MethodInvoker> getMethods() {
		return filteredMethods(MethodInvoker.class);
	}

	public List<StaticMethodInvoker> getStaticMethods() {
		return filteredMethods(StaticMethodInvoker.class);
	}

	private <T> List<T> filteredMethods(Class<T> clazz) {
		return methods.values().stream()
			.filter(value -> clazz.isInstance(value))
			.map(value -> clazz.cast(value))
			.collect(toList());
	}

	public String methodName(MethodInvocationHandler m) {
		return methods.entrySet().stream()
			.filter(entry -> entry.getValue() == m)
			.findFirst()
			.map(method -> method.getKey().getName())
			.orElse(null);
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
	public static XRayInterface xray(Object object) {
		if (object instanceof Class<?>) {
			return new XRayInterface((Class<?>) object);
		} else {
			return new XRayInterface(object);
		}
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
			throw new InterfaceMismatchException("cannot resolve property " + e.getMessage() + " on " + getType());
		} catch (NoSuchMethodException e) {
			throw new InterfaceMismatchException("cannot resolve method/property " + e.getMessage() + " on " + getType());
		}
	}

	/**
	 * collects all methods of the given interface conflicting with the wrapped object
	 * 
	 * @param interfaceClazz the interface to check on conflicts
	 * @return a list of methods conflicting
	 */
	public List<Method> unMappable(Class<?> interfaceClazz) {
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

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocationHandler handler = methods.get(method);
		return handler.invoke(object, args);
	}

}
