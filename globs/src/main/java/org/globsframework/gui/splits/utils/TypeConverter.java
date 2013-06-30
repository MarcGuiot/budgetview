package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.layout.LayoutService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.Fonts;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.exceptions.SplitsException;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.border.Border;
import java.awt.*;

public class TypeConverter {
  static Object getValue(Class<?> targetClass, String property, String value, Class componentClass, SplitsContext context) {
    if (targetClass == String.class) {
      return SplitsUtils.convertString(value, context.getService(TextLocator.class));
    }
    if ((value == null) || (value.length() == 0)) {
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
      return IconParser.parse(value,
                              context.getService(ColorService.class),
                              context.getService(ImageLocator.class),
                              context);
    }
    if (targetClass == Dimension.class) {
      return SplitsUtils.parseDimension(value);
    }
    if (targetClass == Font.class) {
      return Fonts.parseFont(value, context.getService(FontLocator.class));
    }
    if (targetClass == Action.class) {
      return context.getAction(value);
    }
    if (targetClass == Border.class) {
      return BorderParser.parse(value, context.getService(ColorService.class), context);
    }
    if (targetClass == Cursor.class) {
      return Cursors.parse(value);
    }
    if (ComponentUI.class.isAssignableFrom(targetClass)) {
      return context.getService(UIService.class).getUI(value, context);
    }
    if (LayoutManager.class.isAssignableFrom(targetClass)) {
      return context.getService(LayoutService.class).getLayout(value, context);
    }
    throw new SplitsException("Cannot use string value for property '" + property +
                              "' of type '" + targetClass.getSimpleName() +
                              "' in class " + componentClass.getSimpleName());
  }
}
