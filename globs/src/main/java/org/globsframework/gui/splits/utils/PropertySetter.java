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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

public class PropertySetter {

  private static PropertySetterCache setterCache = new PropertySetterCache();

  public static void process(Object component,
                             SplitProperties properties,
                             SplitsContext context,
                             String... toExclude) {
    Set<String> excludeSet = new TreeSet<String>();
    for (String string : toExclude) {
      excludeSet.add(string.toLowerCase());
    }

    String actionProperty = properties.get("action");
    if (actionProperty != null) {
      invokeSetter(component, "action", actionProperty, context);
      excludeSet.add("action");
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
    final Method setter = setterCache.findSetter(objectClass, property);
    if (setter == null) {
      throw new SplitsException("No property '" + property +
                                "' found for class " + objectClass.getSimpleName() +
                                " (target value: '" + value + "')");
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
        ComponentColorUpdater colorUpdater =
          new ComponentColorUpdater(object, setter, property, value, objectClass);
        colorUpdater.install(context.getService(ColorService.class));
        context.addDisposable(colorUpdater);
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
                                "' in class " + componentClass.getName(), e);
    }
    catch (InvocationTargetException e) {
      throw new SplitsException("Setter for property '" + attribute +
                                "' in class " + componentClass.getName() + "' threw exception:", e);
    }
    catch (IllegalArgumentException e) {
      throw new SplitsException("Setter for property '" + attribute +
                                "' in class " + componentClass.getName() + "' threw exception:", e);
    }

    if (targetValue instanceof Action) {
      processAction(object, attributeValue);
    }
  }

  private static void processAction(Object object, String attributeValue) {
    if (object instanceof JComponent) {
      JComponent component = (JComponent)object;
      if (component.getName() == null) {
        component.setName(attributeValue);
      }
    }
  }

  private static class ComponentColorUpdater extends ColorUpdater {
    private final Method setter;
    private final String property;
    private final String value;
    private final Class objectClass;
    private Object object;

    public ComponentColorUpdater(Object object, Method setter, String property, String value, Class objectClass) {
      super(value);
      this.object = object;
      this.setter = setter;
      this.property = property;
      this.value = value;
      this.objectClass = objectClass;
    }

    public void updateColor(Color color) {
      invokeSetter(object, setter, color, property, value, objectClass);
    }

    public String toString() {
      return "ComponentColorUpdater for " + objectClass + "." + setter.getName() + "(...)";
    }
  }
}
