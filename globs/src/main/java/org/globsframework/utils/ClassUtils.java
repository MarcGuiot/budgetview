package org.globsframework.utils;

import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Arrays;

public class ClassUtils {
  private ClassUtils() {
  }

  public static <T> T createFromProperty(String propertyName) throws InvalidParameter {
    String className = System.getProperty(propertyName);
    if ((className == null) || (className.length() == 0)) {
      throw new InvalidParameter("Property '" + propertyName + "' is not set");
    }
    try {
      return (T)Class.forName(className).newInstance();
    }
    catch (Exception e) {
      throw new InvalidParameter("Property '" + propertyName + "' must refer to a class " +
                                 "with a default constructor", e);
    }
  }
}
