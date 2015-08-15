XRayInterface vs. Java Reflection
=================================

The typical way to access a private method in Java with Reflection is:
----------------------------------------------------------------------

1. lookup of the class of the object to access

    ```Java
    Class<?> clazz = object.getClass();
    ```
2. wrapping of param types into arrays

    ```Java
    Class<?>[] paramTypes = new Class<?>[]{char[].class};
    ```
3. lookup of the member to access
    
    ```Java
    Method m = clazz.getDeclaredMethod("callExample", args);
    ```
4. enabling access for private methods/fields
    
    ```Java
    m.setAccessible(true);
    ```
5. wrapping arguments into arrays
    
    ```Java
    Object[] args = new Object[]{"chars".toCharArray()};
    ```
6. indirectly setting, getting, invoking members
    
    ```Java
    Object result = m.invoke(object, args);
    ```
7. converting the result type 
    
    ```Java
    int intResult = ((Integer) result).intValue();
    ```

XRayInterface does not bother you with that boilerplate code:
-------------------------------------------------------------

1. Just define the interface

    ```Java
    interface Example {

      int callExample(char[] characters);
      
    }
    ```
2. Then bind it:

    ```Java
    Example example = XRayInterface.xray(object).to(Example.class);
    ```
3. And call it:

    ```Java
    int intResult = example.callExample("chars".toCharArray());
    ```

