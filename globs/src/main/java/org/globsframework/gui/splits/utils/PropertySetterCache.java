package org.globsframework.gui.splits.utils;

import org.globsframework.utils.Pair;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PropertySetterCache {
  private Map<Pair<Class, String>, Method> setters = new HashMap<Pair<Class, String>, Method>();

  public Method findSetter(Class objectClass, String property) {
    Pair<Class, String> key = new Pair<Class, String>(objectClass, property);
    if (setters.containsKey(key)) {
      return setters.get(key);
    }

    Method method = findMethod(objectClass, property);
    setters.put(key, method);
    return method;
  }

  private static Method findMethod(Class objectClass, String attribute) {
    String setter = "set" + attribute;
    for (Method method : objectClass.getMethods()) {
      if (method.getName().equalsIgnoreCase(setter) && method.getParameterTypes().length == 1) {
        return method;
      }
    }
    return null;
  }

}
