package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.SignatureUtil.fieldSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Determines whether a field can be written.
 *
 * <p>
 * Final <b>instance</b> fields are writable. It suffices to make the field accessible
 * ({@link Field#setAccessible(boolean)}) before unreflecting a setter for it.
 *
 * <p>
 * Final <b>static</b> fields are not writable. Up to Java 11 the final modifier could be stripped by reflecting on the
 * field <code>Field.modifiers</code>, yet Java 12 filters this field. Since then no supported mechanism grants write
 * access to a static final field - neither {@link Field#set(Object, Object)} nor
 * {@link java.lang.invoke.MethodHandles.Lookup#unreflectSetter(Field)} nor a
 * {@link java.lang.invoke.VarHandle}. Static final fields are therefore unsupported.
 */
public final class FinalUtil {

	private FinalUtil() {
	}

	public static boolean isFinal(Field field) {
		return (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
	}

	/**
	 * signals whether the given field can be written.
	 *
	 * @param field the field to check
	 * @return false if the field is static and final, true otherwise
	 */
	public static boolean isWritable(Field field) {
		return !isFinal(field) || !Modifier.isStatic(field.getModifiers());
	}

	/**
	 * ensures that the given field can be written.
	 *
	 * @param field the field to check
	 * @throws ReflectionFailedException if the field is static and final
	 */
	public static void ensureWritable(Field field) {
		if (!isWritable(field)) {
			throw new ReflectionFailedException("cannot write static final field "
				+ fieldSignature(field.getName(), field.getType())
				+ " of " + field.getDeclaringClass().getName()
				+ ", writing static final fields is not supported since java 12");
		}
	}

}
