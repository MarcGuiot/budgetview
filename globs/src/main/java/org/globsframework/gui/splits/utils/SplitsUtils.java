package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.GridPos;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitsUtils {
  private static Pattern SHORT_FORMAT = Pattern.compile("\\(" +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*" +
                                                        "\\)");
  private static Pattern LONG_FORMAT = Pattern.compile("\\(" +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([0-9]+)[ ]*" +
                                                       "\\)");
  private static Pattern INT_FORMAT = Pattern.compile("[0-9]+");

  static final String GRIDPOS_ERROR_MESSAGE =
    "GridPos should be defined as '(x,y)' or '(x,y,w,z)' - for instance '(0,2)' or '(0,2,1,1)'";
  static final String DIMENSION_ERROR_MESSAGE =
    "Dimension should be defined as '(x,y)' - for instance '(0,2)'";

  public static GridPos parseGridPos(String desc) throws SplitsException {
    String trimmed = desc.trim();
    Matcher shortMatcher = SHORT_FORMAT.matcher(trimmed);
    if (shortMatcher.matches()) {
      return new GridPos(Integer.parseInt(shortMatcher.group(1)),
                         Integer.parseInt(shortMatcher.group(2)),
                         1, 1);
    }
    Matcher longMatcher = LONG_FORMAT.matcher(trimmed);
    if (longMatcher.matches()) {
      return new GridPos(Integer.parseInt(longMatcher.group(1)),
                         Integer.parseInt(longMatcher.group(2)),
                         Integer.parseInt(longMatcher.group(3)),
                         Integer.parseInt(longMatcher.group(4)));
    }
    throw new SplitsException(GRIDPOS_ERROR_MESSAGE);
  }

  public static Dimension parseDimension(String desc) {
    String trimmed = desc.trim();
    Matcher shortMatcher = SHORT_FORMAT.matcher(trimmed);
    if (shortMatcher.matches()) {
      return new Dimension(Integer.parseInt(shortMatcher.group(1)),
                           Integer.parseInt(shortMatcher.group(2)));
    }
    throw new SplitsException(DIMENSION_ERROR_MESSAGE);
  }

  public static String toNiceUpperCase(String value) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if ((i > 0) && Character.isUpperCase(c) && Character.isLowerCase(value.charAt(i - 1))) {
        builder.append("_");
      }
      builder.append(Character.toUpperCase(c));
    }
    return builder.toString();
  }

  public static String capitalize(String value) {
    if ((value == null) || "".equals(value)) {
      return value;
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1, value.length());
  }

  public static Integer parseInt(String value) throws NumberFormatException {
    if (INT_FORMAT.matcher(value).matches()) {
      return Integer.valueOf(value);
    }

    Class targetClass;
    Field field;
    if (value.indexOf(".") < 0) {
      targetClass = SwingConstants.class;
      field = getField(value.toUpperCase(), targetClass);
    }
    else {
      targetClass = getClassInConstant(value);
      field = getField(value, targetClass);
    }
    if (!Modifier.isStatic(field.getModifiers())) {
      throw new SplitsException("Field '" + field.getName() + "' in class '" +
                                targetClass.getName() + "' is not static");
    }
    try {
      Object fieldValue = field.get(null);
      if (fieldValue == null) {
        throw new SplitsException("Field '" + field.getName() + "' in class '" +
                                  targetClass.getName() + "' is not set");
      }
      if (!Integer.TYPE.isInstance(fieldValue) && !Integer.class.isInstance(fieldValue)) {
        throw new SplitsException("Field '" + field.getName() + "' in class '" +
                                  targetClass.getName() + "' is not an integer");
      }
      return (Integer)fieldValue;
    }
    catch (IllegalAccessException e) {
      throw new SplitsException("Could not retrieve value for field '" +
                                field.getName() + "' in class '" + targetClass.getName() + "'");
    }
  }

  private static Class getClassInConstant(String value) {
    String className = value.substring(0, value.lastIndexOf("."));
    try {
      return Class.forName(className);
    }
    catch (ClassNotFoundException e) {
      try {
        return Class.forName("javax.swing." + className);
      }
      catch (ClassNotFoundException e1) {
        throw new SplitsException("Unable to locate class for constant '" + value + "'");
      }
    }
  }

  private static Field getField(String value, Class targetClass) {
    String name = value.substring(value.lastIndexOf(".") + 1);
    try {
      return targetClass.getField(name);
    }
    catch (NoSuchFieldException e) {
      if (targetClass == SwingConstants.class) {
        throw new SplitsException("Field '" + name + "' not found in class: " + targetClass.getName() + " - " +
                                  "value should be either an integer, one of the constants of the SwingConstants class " +
                                  "or a reference to a class constant such as 'JLabel.RIGHT'");
      }
      else {
        throw new SplitsException("Field '" + name + "' not found in class: " + targetClass.getName());
      }
    }
  }

  public static String convertString(String value, TextLocator textLocator) {
    if (value.startsWith("$")) {
      return textLocator.get(value.substring(1));
    }
    if (value.startsWith("\\$")) {
      return value.substring(1);
    }
    return value;
  }

  public static <T> T instantiate(String className, Class<T> targetClass) {
    Class<?> actualClass;
    try {
      actualClass = Class.forName(className);
    }
    catch (ClassNotFoundException e) {
      throw new ItemNotFound("Cannot find class '" + className + "'");
    }

    if (!targetClass.isAssignableFrom(actualClass)) {
      throw new SplitsException("Class '" + className + "' should be a subclass of '" + targetClass.getSimpleName() + "'");
    }

    Constructor<?> constructor;
    try {
      constructor = actualClass.getConstructor();
    }
    catch (NoSuchMethodException e) {
      throw new SplitsException("Class " + actualClass.getName() + " must have a public default constructor");
    }

    try {
      return (T)constructor.newInstance();
    }
    catch (Exception e) {
      throw new RuntimeException("Could not instantiate '" + className + "'", e);
    }
  }
}
