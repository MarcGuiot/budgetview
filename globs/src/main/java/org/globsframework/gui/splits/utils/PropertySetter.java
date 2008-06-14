package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.exceptions.SplitsException;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

public class PropertySetter {
  public static void process(Component component,
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

  private static void process(final Component component, final String property, String value, SplitsContext context) {
    final Class<? extends Component> componentClass = component.getClass();
    Method[] methods = componentClass.getMethods();
    final Method setter = findMethod(methods, property);
    if (setter == null) {
      throw new SplitsException("No property '" + property +
                                "' found for class " + componentClass.getSimpleName());
    }
    Class<?> targetClass = setter.getParameterTypes()[0];
    if (targetClass == Color.class) {
      if (value.startsWith(Colors.HEXA_PREFIX)) {
        invokeSetter(component, setter, Colors.toColor(value.substring(1)), property, componentClass);
      }
      else if (value.length() == 0) {
        invokeSetter(component, setter, null, property, componentClass);
      }
      else {
        context.getColorService().install(value, new ColorUpdater() {
          public void updateColor(Color color) {
            invokeSetter(component, setter, color, property, componentClass);
          }
        });
      }
    }
    else {
      Object targetValue = TypeConverter.getValue(targetClass, property, value, componentClass, context);
      invokeSetter(component, setter, targetValue, property, componentClass);
    }
  }

  private static void invokeSetter(Component component, Method setter, Object targetValue, String attribute, Class<? extends Component> componentClass) {
    try {
      setter.invoke(component, targetValue);
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
