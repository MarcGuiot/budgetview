package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.exceptions.SplitsException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class TypeConverter {
  static Object getValue(Class<?> targetClass, String property, String value, Class<? extends Component> componentClass, SplitsContext context) {
    if (targetClass == String.class) {
      return SplitsUtils.convertString(value, context.getTextLocator());
    }
    if (value.length() == 0) {
      if (targetClass.isPrimitive()) {
        throw new SplitsException("Empty value not allowed for property '" + property +
                                  "' of type " + targetClass.getSimpleName() +
                                  " on class " + componentClass.getSimpleName());
      }
      return null;
    }
    if ((targetClass == Boolean.class) || (targetClass == Boolean.TYPE)) {
      return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }
    if ((targetClass == Integer.class) || (targetClass == Integer.TYPE)) {
      return SplitsUtils.parseInt(value);
    }
    if ((targetClass == Long.class) || (targetClass == Long.TYPE)) {
      return Long.valueOf(value);
    }
    if ((targetClass == Float.class) || (targetClass == Float.TYPE)) {
      return Float.valueOf(value);
    }
    if ((targetClass == Double.class) || (targetClass == Double.TYPE)) {
      return Double.valueOf(value);
    }
    if (targetClass == Icon.class) {
      IconLocator locator = context.getIconLocator();
      return locator.get(value);
    }
    if (targetClass == Dimension.class) {
      return SplitsUtils.parseDimension(value);
    }
    if (targetClass == Font.class) {
      return SplitsUtils.parseFont(value);
    }
    if (targetClass == Action.class) {
      return context.getAction(value);
    }
    if (targetClass == Border.class) {
      return BorderUtils.parse(value, context.getColorService());
    }
    throw new SplitsException("Cannot use string value for property '" + property +
                              "' of type '" + targetClass.getSimpleName() +
                              "' in class " + componentClass.getSimpleName());
  }
}
