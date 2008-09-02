package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.exceptions.SplitsException;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
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
      if (property.equalsIgnoreCase("autoHideIfDisabled")) {
        installAutoHideListener(component, properties.get(property));
      }
      else {
        invokeSetter(component, property, properties.get(property), context);
      }
    }
  }

  private static void installAutoHideListener(Object component, String propertyValue) {
    if (!"true".equalsIgnoreCase(propertyValue)) {
      return;
    }

    if (!JComponent.class.isAssignableFrom(component.getClass())) {
      throw new SplitsException("autoHideIfDisabled can only be used with JComponent objects");
    }
    final JComponent jComponent = (JComponent)component;
    jComponent.setVisible(jComponent.isEnabled());

    jComponent.addPropertyChangeListener("enabled", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        jComponent.setVisible(jComponent.isEnabled());
      }
    });
  }

  private static void invokeSetter(final Object object, final String property, final String value, SplitsContext context) {
    final Class objectClass = object.getClass();
    Method[] methods = objectClass.getMethods();
    final Method setter = findMethod(methods, property);
    if (setter == null) {
      throw new SplitsException("No property '" + property +
                                "' found for class " + objectClass.getSimpleName());
    }
    Class<?> targetClass = setter.getParameterTypes()[0];
    if (targetClass == Color.class) {
      if (Colors.isHexaString(value)) {
        invokeSetter(object, setter, Colors.toColor(value), property, value, objectClass);
      }
      else if (value.length() == 0) {
        invokeSetter(object, setter, null, property, value, objectClass);
      }
      else {
        final WeakReference ref = new WeakReference<Object>(object);
        final ColorService colorService = context.getService(ColorService.class);
        colorService.install(value, new ColorUpdater() {
          public void updateColor(Color color) {
            Object target = ref.get();
            if (target == null) {
              colorService.uninstall(this);
              return;
            }
            invokeSetter(target, setter, color, property, value, objectClass);
          }
        });
      }
    }
    else {
      Object targetValue = TypeConverter.getValue(targetClass, property, value, objectClass, context);
      invokeSetter(object, setter, targetValue, property, value, objectClass);
    }
  }

  private static void invokeSetter(Object object, Method setter, Object targetValue,
                                   String attribute, String attributeValue,
                                   Class componentClass) {
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

    if (targetValue instanceof Action) {
      processAction(object, attributeValue);
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

  private static void processAction(Object object, String attributeValue) {
    if (object instanceof JComponent) {
      JComponent component = (JComponent)object;
      if (component.getName() == null) {
        component.setName(attributeValue);
      }
    }
  }
}
