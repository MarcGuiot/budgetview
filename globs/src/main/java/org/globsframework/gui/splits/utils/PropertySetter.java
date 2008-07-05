package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.exceptions.SplitsException;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

public class PropertySetter {
  public static void process(Object component,
                             SplitProperties properties,
                             SplitsContext context,
                             String... toExclude) {
    Set<String> excludeSet = new TreeSet<String>();
    for (String string : toExclude) {
      excludeSet.add(string.toLowerCase());
    }

    for (String property : properties.getPropertyNames()) {
      if (excludeSet.contains(property.toLowerCase())) {
        continue;
      }
      process(component, property, properties.get(property), context);
    }
  }

  private static void process(final Object object, final String property, String value, SplitsContext context) {
    final Class objectClass = object.getClass();
    Method[] methods = objectClass.getMethods();
    final Method setter = findMethod(methods, property);
    if (setter == null) {
      throw new SplitsException("No property '" + property +
                                "' found for class " + objectClass.getSimpleName());
    }
    Class<?> targetClass = setter.getParameterTypes()[0];
    if (targetClass == Color.class) {
      if (value.startsWith(Colors.HEXA_PREFIX)) {
        invokeSetter(object, setter, Colors.toColor(value.substring(1)), property, objectClass);
      }
      else if (value.length() == 0) {
        invokeSetter(object, setter, null, property, objectClass);
      }
      else {
        context.getService(ColorService.class).install(value, new ColorUpdater() {
          public void updateColor(Color color) {
            invokeSetter(object, setter, color, property, objectClass);
          }
        });
      }
    }
    else {
      Object targetValue = TypeConverter.getValue(targetClass, property, value, objectClass, context);
      invokeSetter(object, setter, targetValue, property, objectClass);
    }
  }

  private static void invokeSetter(Object object, Method setter, Object targetValue, String attribute, Class<? extends Component> componentClass) {
    try {
      setter.invoke(object, targetValue);
    }
    catch (IllegalAccessException e) {
      throw new SplitsException("Could not invoke setter found for property '" + attribute +
                                "' in class " + componentClass.getSimpleName(), e);
    }
    catch (InvocationTargetException e) {
      throw new SplitsException("Setter for property '" + attribute +
                                "' in class " + componentClass.getSimpleName() + "' threw exception:", e);
    }
  }

  private static Method findMethod(Method[] methods, String attribute) {
    String setter = "set" + attribute;
    for (Method method : methods) {
      if (method.getName().equalsIgnoreCase(setter) && method.getParameterTypes().length == 1) {
        return method;
      }
    }
    return null;
  }
}
